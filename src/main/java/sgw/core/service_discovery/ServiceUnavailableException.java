package sgw.core.service_discovery;

public class ServiceUnavailableException extends Exception {

    public ServiceUnavailableException(String serviceName) {
        super(serviceName + " not available.");
    }
}
