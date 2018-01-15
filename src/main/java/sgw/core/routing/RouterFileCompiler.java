package sgw.core.routing;

import java.io.File;

public class RouterFileCompiler implements RouterGenerator{

    private File routerFile;

    private RouterFileCompiler(File file) {
        routerFile = file;
    }

    @Override
    public Router generate() {
        return null;
    }

    public static class Factory implements  RouterGeneratorFactory {

        private File routerFile;
        private static final String defaultFilePath = "routing.properties";

        public Factory() {
            this(defaultFilePath);
        }

        public Factory(String filePath) {
            routerFile = new File(filePath);
        }

        @Override
        public RouterFileCompiler create(){
            return new RouterFileCompiler(routerFile);
        }
    }
}
