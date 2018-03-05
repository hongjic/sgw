package sgw.core.data_convertor.annotations;

import java.lang.annotation.*;

/**
 * Mark a class as a router definition. This annotation is only for scanning.
 *
 * Upon initialization, SGW scans the package to find all classes annotated as {@link Router}
 * and load them into {@link sgw.core.http_channel.routing.Router} instance.
 *
 * Considering downstream services may use different protocols, each protocol should
 * has their own routing class annotation to contain detail settings.
 * For example, we have {@link ThriftRouter} for thrift services.
 *
 * Class annotated as {@link Router} should have two method annotated as {@link RequestParser}
 * and {@link ResponseGenerator} respectively.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Router {
}
