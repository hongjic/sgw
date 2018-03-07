package sgw.core.data_convertor.annotations;

import org.apache.thrift.TBase;

import java.lang.annotation.*;

/**
 * A sub annotation of {@link Router}. Defines the routing detail of thrift services.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Router
public @interface ThriftRouter {
    String[] http();
    String service();
    String method();
    Class<? extends TBase> args();
    Class<? extends TBase> result();
}
