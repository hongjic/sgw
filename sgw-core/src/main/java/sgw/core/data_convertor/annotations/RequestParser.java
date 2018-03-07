package sgw.core.data_convertor.annotations;

import java.lang.annotation.*;

/**
 * Responsible for parseing http request
 * This annotation only works when it is put on a method in a class annotated as
 * {@link Router}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParser {
}
