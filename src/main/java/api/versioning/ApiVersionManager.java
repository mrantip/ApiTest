package api.versioning;

import api.configs.Config;
import common.annotations.APIVersion;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;


@Slf4j
public class ApiVersionManager {

    private static String currentApiVersion = Config.getActiveBackendVersion();
    private static final List<String> SUPPORTED_VERSIONS = Arrays.asList(
            "with_validation_fix",
            "with_database_with_fix",
            "with_fraud_check"
    );

    public static String getApiVersionForTest(Class<?> testClass, Method testMethod) {
        // 1. Проверяем аннотацию на методе
        if (testMethod.isAnnotationPresent(APIVersion.class)) {
            String version = testMethod.getAnnotation(APIVersion.class).value();
            log.info("📍 Found API version on METHOD {}: {}", testMethod.getName(), version);
            return version;
        }

        // 2. Проверяем аннотацию на классе
        if (testClass.isAnnotationPresent(APIVersion.class)) {
            String version = testClass.getAnnotation(APIVersion.class).value();
            log.info("📍 Found API version on CLASS {}: {}", testClass.getSimpleName(), version);
            return version;
        }

        // 3. Используем версию из конфига
        String configVersion = Config.getProperty("api.default.version");
        if (configVersion != null && !configVersion.isEmpty()) {
            log.info("📍 Using API version from config: {}", configVersion);
            return configVersion;
        }

        // 4. Версия по умолчанию
        log.info("📍 No API version specified, using default: {}", currentApiVersion);
        return currentApiVersion;
    }

    /**
     * Проверяет, поддерживается ли указанная версия
     * ВСЕГДА возвращает true, если версия есть в списке поддерживаемых
     */
    public static boolean isVersionSupported(String version) {
        if (version == null || version.isEmpty()) {
            return true;
        }

        // Проверяем, есть ли версия в списке поддерживаемых
        boolean supported = SUPPORTED_VERSIONS.contains(version);

        log.info("🔍 Version '{}' is {}supported (supported versions: {})",
                version, supported ? "" : "not ", SUPPORTED_VERSIONS);

        // ВАЖНО: НЕ сравниваем с backend.active.version!
        // Просто проверяем, что версия есть в списке
        return supported;
    }

    /**
     * Получить URL бэкенда для указанной версии
     */
    public static String getBackendUrlForVersion(String version) {
        String url = Config.getBackendUrl(version);
        log.info("🔗 Backend URL for version '{}': {}", version, url);
        return url;
    }

    public static String getBackendUrlForCurrentVersion() {
        return getBackendUrlForVersion(getCurrentApiVersion());
    }

    public static void setCurrentApiVersion(String version) {
        if (isVersionSupported(version)) {
            currentApiVersion = version;
            log.info("✅ Current API version set to: {}", version);
        } else {
            log.warn("⚠️ Version {} is not in supported list", version);
        }
    }

    public static String getCurrentApiVersion() {
        return currentApiVersion;
    }

    public static List<String> getSupportedVersions() {
        return SUPPORTED_VERSIONS;
    }
}
