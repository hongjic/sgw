package sgw.core.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class RouterGeneratorFactory {

    private final Logger logger = LoggerFactory.getLogger(RouterGeneratorFactory.class);

    private RouterDataSource source;

    public RouterGeneratorFactory(RouterDataSource source) {
        this.source = source;
    }

    /**
     *
     * @return a created RouterGenerator Implementation
     */
    public RouterGenerator create() {
        if (source.getType() == RouterDataSource.Type.PROPERTIES_FILE) {
            String filePath = source.getPropertiesFilePath();
            return new RouterPropertiesFileCompiler(new File(filePath));
        }
        else {
            // other possible RouterGenerator Implementations
            return null;
        }
    }
}
