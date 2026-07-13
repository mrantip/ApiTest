package api.versioning;


/**
 * Хранит версию API для текущего потока (теста)
 */
public class ApiVersionContext {

    private static final ThreadLocal<String> currentVersion = new ThreadLocal<>();

    public static void setVersion(String version) {
        System.out.println("🔵 ApiVersionContext.setVersion: " + version);
        currentVersion.set(version);
    }

    public static String getVersion() {
        String version = currentVersion.get();
        System.out.println("🔵 ApiVersionContext.getVersion: " + version);
        return version != null ? version : ApiVersionManager.getCurrentApiVersion();
    }

    public static void clear() {
        System.out.println("🔵 ApiVersionContext.clear");
        currentVersion.remove();
    }
}