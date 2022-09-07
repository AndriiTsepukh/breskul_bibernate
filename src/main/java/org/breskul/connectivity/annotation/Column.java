package org.breskul.connectivity.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    String name() default "";

    boolean ignoreDirtyCheck() default false;
}
