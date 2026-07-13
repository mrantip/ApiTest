package common.extensions;

import api.versioning.ApiVersionContext;
import api.versioning.ApiVersionManager;
import common.annotations.APIVersion;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.Method;
import java.util.Optional;

@Slf4j
public class ApiVersionExtension implements BeforeEachCallback, TestWatcher, TestExecutionExceptionHandler {

    @Override
    public void beforeEach(ExtensionContext context) {
        System.out.println("=== API VERSION EXTENSION STARTED ===");

        Class<?> testClass = context.getRequiredTestClass();
        Method testMethod = context.getRequiredTestMethod();

        System.out.println("Test class: " + testClass.getName());
        System.out.println("Test method: " + testMethod.getName());

        String requiredVersion = null;

        // 1. Проверяем аннотацию на МЕТОДЕ
        if (testMethod.isAnnotationPresent(APIVersion.class)) {
            requiredVersion = testMethod.getAnnotation(APIVersion.class).value();
            System.out.println("✅ Found version on METHOD: " + requiredVersion);
        }
        // 2. Если нет на методе - проверяем на КЛАССЕ
        else if (testClass.isAnnotationPresent(APIVersion.class)) {
            requiredVersion = testClass.getAnnotation(APIVersion.class).value();
            System.out.println("✅ Found version on CLASS: " + requiredVersion);
        }

        // 3. Если нет аннотации - используем версию по умолчанию
        if (requiredVersion == null) {
            requiredVersion = ApiVersionManager.getCurrentApiVersion();
            System.out.println("⚠️ No annotation, using default: " + requiredVersion);
        }

        System.out.println("Required version: " + requiredVersion);

        // Проверяем, поддерживается ли версия
        if (!ApiVersionManager.isVersionSupported(requiredVersion)) {
            String message = String.format(
                    "Test '%s' requires API version '%s' but it's not in supported list: %s",
                    testMethod.getName(),
                    requiredVersion,
                    ApiVersionManager.getSupportedVersions()
            );
            System.out.println("❌ " + message);
            throw new TestAbortedException(message);
        }

        // Устанавливаем версию в контекст
        ApiVersionContext.setVersion(requiredVersion);
        ApiVersionManager.setCurrentApiVersion(requiredVersion);

        System.out.println("✅ Version set in context: " + ApiVersionContext.getVersion());
        System.out.println("=== API VERSION EXTENSION FINISHED ===");
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        System.out.println("Test disabled: " + reason.orElse("No reason"));
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        System.out.println("Test successful, clearing context");
        ApiVersionContext.clear();
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        System.out.println("Test aborted: " + cause.getMessage());
        ApiVersionContext.clear();
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        System.out.println("Test failed: " + cause.getMessage());
        ApiVersionContext.clear();
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        System.out.println("Test execution exception: " + throwable.getMessage());
        throw throwable;
    }
}