package api.specs;

import api.configs.Config;
import api.versioning.ApiVersionContext;
import api.versioning.ApiVersionManager;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import api.models.LoginUserRequest;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestSpecs {

    private static Map<String, String> authHeaders = new HashMap<>();
    private static final String API_VERSION = Config.getProperty("apiVersion", "/api/v1/");

    private static String encodeBasicAuth(String username, String password) {
        String auth = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

    static {
        // Инициализация admin из конфига
        String adminUsername = Config.getProperty("admin.username");
        String adminPassword = Config.getProperty("admin.password");
        authHeaders.put(adminUsername, encodeBasicAuth(adminUsername, adminPassword));
    }

    private RequestSpecs() {
    }

    /**
     * Получить версию для текущего запроса (из контекста теста)
     */
    private static String getVersionForRequest() {
        String version = ApiVersionContext.getVersion();
        System.out.println("🔍 RequestSpecs using version: " + version);

        if (version == null) {
            version = ApiVersionManager.getCurrentApiVersion();
            System.out.println("🔍 RequestSpecs using fallback version: " + version);
        }

        return version;
    }

    /**
     * Создать RequestSpecBuilder с версией из контекста
     */
    private static RequestSpecBuilder defaultRequestBuilder() {
        String version = getVersionForRequest();
        String baseUri = ApiVersionManager.getBackendUrlForVersion(version);

        System.out.println("=== RequestSpecs DEBUG ===");
        System.out.println("Version from context: " + version);
        System.out.println("Base URI: " + baseUri);
        System.out.println("API Version: " + API_VERSION);
        System.out.println("Full URL: " + baseUri + API_VERSION);
        System.out.println("===========================");

        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()))
                .setBaseUri(baseUri)
                .setBasePath(API_VERSION);
    }



    /**
     * Получить версию для текущего запроса (из контекста теста)
     */
    private static String getBaseUriForCurrentVersion() {
        return ApiVersionManager.getBackendUrlForCurrentVersion();
    }

    /**
     * Получить baseUri для указанной версии
     */
    private static String getBaseUriForVersion(String version) {
        return ApiVersionManager.getBackendUrlForVersion(version);
    }

    public static RequestSpecification unauthSpec() {
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification adminSpec() {
        return defaultRequestBuilder()
                .addHeader("Authorization", authHeaders.get("admin"))
                .build();
    }

    public static RequestSpecification authAsUser(String username, String password) {
        return defaultRequestBuilder()
                .addHeader("Authorization", getUserAuthHeader(username, password))
                .build();
    }

    public static String getUserAuthHeader(String username, String password) {
        String version = getVersionForRequest();
        String key = username + "@" + version;
        String userAuthHeader;

        if (!authHeaders.containsKey(key)) {
            String baseUri = ApiVersionManager.getBackendUrlForVersion(version);

            RequestSpecification loginSpec = new RequestSpecBuilder()
                    .setContentType(ContentType.JSON)
                    .setAccept(ContentType.JSON)
                    .addFilters(List.of(new RequestLoggingFilter(),
                            new ResponseLoggingFilter()))
                    .setBaseUri(baseUri)
                    .setBasePath(API_VERSION)
                    .build();

            userAuthHeader = new CrudRequester(
                    loginSpec,
                    Endpoint.LOGIN,
                    ResponseSpecs.requestReturnsOK())
                    .post(LoginUserRequest.builder().username(username).password(password).build())
                    .extract()
                    .header("Authorization");

            authHeaders.put(key, userAuthHeader);
        } else {
            userAuthHeader = authHeaders.get(key);
        }

        return userAuthHeader;
    }
}