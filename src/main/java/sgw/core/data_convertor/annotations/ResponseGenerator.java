package sgw.core.data_convertor.annotations;

import java.lang.annotation.*;

/**
 * Responsible of generating response
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseGenerator {
}
