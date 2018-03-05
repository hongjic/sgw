package sgw.core.data_convertor.annotations;

import java.lang.annotation.*;

/**
 * This annotation only works when it is put on one of the variables in a method annotated
 * as {@link RequestParser}
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PathVar {
    /**
     * name of the parsed variable in reques path
     * @return parameter name
     */
    String value();
}
