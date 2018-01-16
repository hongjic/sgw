package sgw.core.routing;

import java.util.HashMap;

/**
 * Stores information of the router configuration datasource but not the
 * data source itself. Use {@link RouterGenerator} to implement more
 * data sources.
 */
public class RouterDataSource extends HashMap<String, Object>{

    private static final String TYPE = "sourceType";
    private static final String PROPERTIES_FILEPATH = "propertiesFilePath";

    public enum Type {
        PROPERTIES_FILE
    }

    public RouterDataSource(Type type) {
        super();
        put(TYPE, type);
    }

    public Type getType() {
        return (Type) get(TYPE);
    }

    public String getPropertiesFilePath() {
        return (String) get(PROPERTIES_FILEPATH);
    }

    public void setPropertiesFilePath(String filePath) {
        put(PROPERTIES_FILEPATH, filePath);
    }
}
