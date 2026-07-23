package common.extensions;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import common.annotations.FraudCheckMock;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Locale;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.post;

public class FraudCheckWireMockExtension implements BeforeEachCallback, AfterEachCallback {

    private WireMockServer wireMockServer;
    private int currentPort;
    private static final int FIXED_PORT = 8090;  // ← фиксированный порт

    @Override
    public void beforeEach(ExtensionContext context) {
        // Find the FraudCheckMock annotation on the test method or class
        FraudCheckMock mockConfig = context.getTestMethod()
                .map(method -> method.getAnnotation(FraudCheckMock.class))
                .orElseGet(() -> context.getTestClass()
                        .map(clazz -> clazz.getAnnotation(FraudCheckMock.class))
                        .orElse(null));

        if (mockConfig != null) {
            setupWireMock(mockConfig);
        }
    }

    private void setupWireMock(FraudCheckMock config) {
        // Используем фиксированный порт
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(FIXED_PORT));

        try {
            wireMockServer.start();
            currentPort = FIXED_PORT;
            System.out.println("✅ WireMock started on port: " + currentPort);
        } catch (Exception e) {
            System.err.println("❌ Port " + FIXED_PORT + " is busy! Trying dynamic port...");
            // Если порт занят, используем динамический
            wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
            wireMockServer.start();
            currentPort = wireMockServer.port();
            System.out.println("✅ WireMock started on dynamic port: " + currentPort);
        }

        // Проверяем, что WireMock действительно работает
        try {
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection)
                    new java.net.URL("http://localhost:" + currentPort + "/__admin/health").openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            System.out.println("✅ WireMock health check (localhost): " + responseCode);
        } catch (Exception e) {
            System.err.println("❌ WireMock health check failed: " + e.getMessage());
        }

        // Настраиваем WireMock клиент
        WireMock.configureFor("localhost", currentPort);

        // ===== 1. Мок для POST /fraud-check =====
        setupPostFraudCheckMock(config);

        // ===== 2. Мок для GET /fraud-check/status/{transactionId} =====
        setupGetFraudCheckStatusMock(config);

        // Добавляем слушатель для логирования запросов
        wireMockServer.addMockServiceRequestListener((request, response) -> {
            System.out.println("=== WIREMOCK REQUEST RECEIVED ===");
            System.out.println("URL: " + request.getUrl());
            System.out.println("Method: " + request.getMethod().getName());
            System.out.println("Headers: " + request.getHeaders());
            System.out.println("Body: " + request.getBodyAsString());
            System.out.println("Response Status: " + response.getStatus());
            System.out.println("Response Body: " + response.getBodyAsString());
            System.out.println("==================================");
        });

//        // Mock the fraud detection service endpoint
//        stubFor(post(urlPathMatching(config.endpoint()))
//                .willReturn(aResponse()
//                        .withStatus(200)
//                        .withHeader("Content-Type", "application/json")
//                        .withBody(responseBody)));


        System.out.println("✅ WireMock stub configured for endpoint: " + config.endpoint());

        // ВАЖНО: Устанавливаем URL для Docker
        String fraudServiceUrl = "http://host.docker.internal:" + currentPort;
        System.setProperty("FRAUD_DETECTION_SERVICE_URL", fraudServiceUrl);
        System.setProperty("fraud.detection.service.url", fraudServiceUrl);
        System.setProperty("fraud.service.url", fraudServiceUrl);

        System.out.println("✅ FRAUD_DETECTION_SERVICE_URL set to: " + fraudServiceUrl);
        System.out.println("✅ Docker should use port: " + currentPort);
    }

    private void setupPostFraudCheckMock(FraudCheckMock config) {
        String responseBody = String.format(Locale.US,
                "{\n" +
                        "  \"status\": \"%s\",\n" +
                        "  \"decision\": \"%s\",\n" +
                        "  \"riskScore\": %.1f,\n" +
                        "  \"reason\": \"%s\",\n" +
                        "  \"requiresManualReview\": %s,\n" +
                        "  \"additionalVerificationRequired\": %s\n" +
                        "}",
                config.status(),
                config.decision(),
                config.riskScore(),
                config.reason(),
                config.requiresManualReview(),
                config.additionalVerificationRequired()
        );

        stubFor(post(urlPathMatching(config.endpoint()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        System.out.println("✅ POST mock configured for: " + config.endpoint());
        System.out.println("   Response: " + responseBody);
    }

    private void setupGetFraudCheckStatusMock(FraudCheckMock config) {
        // Формируем ответ для GET запроса
        String responseBody = String.format(
                "{\n" +
                        "  \"transactionId\": \"%s\",\n" +
                        "  \"status\": \"%s\",\n" +
                        "  \"note\": \"%s\"\n" +
                        "}",
                "{{request.pathSegments.[1]}}",  // Извлекаем transactionId из пути
                config.checkStatus(),
                config.note()
        );

        // Мокаем GET запрос с path параметром
        stubFor(get(urlPathMatching(config.endpoint() + "/[0-9]+"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        System.out.println("✅ GET mock configured for: " + config.endpoint() + "/{transactionId}");
        System.out.println("   Response: " + responseBody);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            wireMockServer = null;
            System.out.println("✅ WireMock stopped");
        }
    }
}