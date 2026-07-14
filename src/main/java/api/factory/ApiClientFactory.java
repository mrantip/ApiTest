package api.factory;

import api.configs.Config;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.specs.ResponseSpecs;
import api.versioning.ApiVersionManager;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ApiClientFactory {
    private static final String API_VERSION = Config.getProperty("api.version", "/api/v1/");

    public static CrudRequester createClient(Endpoint endpoint) {
        return createClient(endpoint, ApiVersionManager.getCurrentApiVersion());
    }

    public static CrudRequester createClient(Endpoint endpoint, String apiVersion) {
        // Получаем URL для указанной версии
        String baseUri = ApiVersionManager.getBackendUrlForVersion(apiVersion);

        // Логируем для отладки
        log.info("🚀 Creating API client:");
        log.info("  - Endpoint: {}", endpoint.getUrl());
        log.info("  - Version: {}", apiVersion);
        log.info("  - Base URI: {}", baseUri);
        log.info("  - Base Path: {}", API_VERSION);
        log.info("  - Full URL: {}{}{}", baseUri, API_VERSION, endpoint.getUrl());

        // Проверяем, что baseUri не null и не пустой
        if (baseUri == null || baseUri.isEmpty()) {
            throw new IllegalStateException("Base URI is null or empty for version: " + apiVersion);
        }

        // Строим RequestSpecification
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setBasePath(API_VERSION)
                .setContentType(ContentType.JSON)
//                .accept(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();

        ResponseSpecification responseSpec = ResponseSpecs.getDefaultResponseSpec();

        return new CrudRequester(requestSpec, endpoint, responseSpec);
    }

    public static CrudRequester createLegacyClient(Endpoint endpoint) {
        log.info("Creating legacy API client for version: with_validation_fix");
        return createClient(endpoint, "with_validation_fix");
    }

    public static CrudRequester createCurrentClient(Endpoint endpoint) {
        log.info("Creating current API client for version: with_database_with_fix");
        return createClient(endpoint, "with_database_with_fix");
    }
}