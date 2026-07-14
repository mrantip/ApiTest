package api.configs;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class Config {
    private static final Config INSTANCE = new Config();
    private final Properties properties = new Properties();

    private Config() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in resources");
            }
            properties.load(input);
            log.info("Config loaded successfully");

            // Логируем все свойства для отладки
            properties.forEach((key, value) ->
                    log.debug("Loaded config: {} = {}", key, value));
        } catch (IOException e) {
            throw new RuntimeException("Fail to load config.properties", e);
        }
    }

    public static String getProperty(String key) {
        // ПРИОРИТЕТ 1 - системное свойство
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isEmpty()) {
            log.debug("Using system property: {} = {}", key, systemValue);
            return systemValue;
        }

        // ПРИОРИТЕТ 2 - переменная окружения
        String envKey = key.toUpperCase().replace('.', '_');
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isEmpty()) {
            log.debug("Using env variable: {} = {}", envKey, envValue);
            return envValue;
        }

        // ПРИОРИТЕТ 3 - config.properties
        String value = INSTANCE.properties.getProperty(key);
        log.debug("Using config.properties: {} = {}", key, value);
        return value;
    }

    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * Получить URL бэкенда для указанной версии
     * ВАЖНО: сначала ищем URL для конкретной версии,
     * и ТОЛЬКО если не нашли - используем дефолтный
     */
    public static String getBackendUrl(String version) {
        // Формируем ключ: backend.with_validation_fix.url
        String urlKey = "backend." + version + ".url";
        System.out.println("🔍 Looking for URL with key: " + urlKey);

        String url = getProperty(urlKey);

        if (url == null || url.isEmpty()) {
            // Если не нашли - используем дефолтный
            url = "http://localhost:4111";
            System.out.println("⚠️ No URL found for version '" + version + "', using DEFAULT: " + url);
        } else {
            System.out.println("✅ Found URL for version '" + version + "': " + url);
        }

        return url;
    }

    /**
     * Получить активную версию бэкенда
     */
    public static String getActiveBackendVersion() {
        return getProperty("backend.active.version", "with_database_with_fix");
    }

    public static String getUsername() {
        return INSTANCE.properties.getProperty("admin.username");
    }

    public static String getPassword() {
        return INSTANCE.properties.getProperty("admin.password");
    }
}