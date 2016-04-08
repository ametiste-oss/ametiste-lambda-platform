package org.ametiste.laplatform.dsl;

import java.lang.annotation.*;

/**
 * <p>
 *     Bse annotation to define protocol elements meta information.
 * </p>
 *
 * @since 0.2.2
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ProtocolMeta {

    String shortName() default "";

    String group() default "";

}
