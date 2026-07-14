package ui;

import api.base.BaseTest;
import api.models.CreateUserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import common.extensions.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Map;


import static com.codeborne.selenide.Selenide.executeJavaScript;

@ExtendWith(AdminSessionExtension.class)
@ExtendWith(UserSessionExtension.class)
@ExtendWith(BrowserMatchExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // ОДИН экземпляр для всех тестов
public class BaseUiTest extends BaseTest {

    private static boolean browserInitialized = false;

    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = api.configs.Config.getProperty("uiRemote");
        Configuration.baseUrl = api.configs.Config.getProperty("uiBaseUrl");
        Configuration.browser = api.configs.Config.getProperty("browser");
        Configuration.browserSize = api.configs.Config.getProperty("browserSize");
        Configuration.headless = false;
        Configuration.timeout = 10000;
        Configuration.pageLoadTimeout = 60000;
        Configuration.reportsFolder = "build/reports/tests";
        Configuration.holdBrowserOpen = false;

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("selenoid:options",
                Map.of(
                        "enableVNC", true,
                        "enableLog", true,
                        "sessionTimeout", "10m"
                )
        );
        Configuration.browserCapabilities = capabilities;

        System.out.println("=== Selenoid Configuration ===");
        System.out.println("Remote: " + Configuration.remote);
        System.out.println("Base URL: " + Configuration.baseUrl);
    }

    @BeforeEach
    public void setupBrowser() {
        // Открываем браузер ТОЛЬКО 1 раз для всех тестов
        if (!browserInitialized) {
            try {
                Selenide.open("/");
                browserInitialized = true;
                System.out.println("✅ Browser initialized ONCE for all UI tests");
            } catch (Exception e) {
                System.err.println("❌ Failed to initialize browser: " + e.getMessage());
                throw e;
            }
        }

        // НЕ ОЧИЩАЕМ localStorage и cookies здесь!
        // Это делается в тестах, если нужно
        // Просто убеждаемся, что мы на главной странице
        try {
            // Если текущая страница не главная - переходим на главную
            String currentUrl = WebDriverRunner.getWebDriver().getCurrentUrl();
            if (!currentUrl.equals(Configuration.baseUrl + "/") &&
                    !currentUrl.equals(Configuration.baseUrl)) {
                Selenide.open("/");
            }
        } catch (Exception e) {
            // Если что-то пошло не так - переоткрываем
            Selenide.open("/");
        }
    }

    @AfterEach
    public void tearDownBrowser() {
        // НЕ проверяем алерт - просто закрываем браузер
        // Если есть алерт - он закроется вместе с браузером

        if (WebDriverRunner.hasWebDriverStarted()) {
            try {
                WebDriverRunner.getWebDriver().quit();
            } catch (Exception e) {
                // Игнорируем
            }
            WebDriverRunner.closeWebDriver();
        }
    }

    @AfterAll
    public void tearDownAll() {
        // Закрываем браузер ПОСЛЕ ВСЕХ тестов
        if (WebDriverRunner.hasWebDriverStarted()) {
            try {
                WebDriverRunner.getWebDriver().quit();
            } catch (Exception e) {
                // Игнорируем
            }
            WebDriverRunner.closeWebDriver();
        }
        browserInitialized = false;
        System.out.println("✅ Browser closed after all UI tests");
    }

    public void authAsUser(String username, String password) {
        Selenide.open("/");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(username, password);
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }

    public void authAsUser(CreateUserRequest createUserRequest) {
        authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword());
    }
}