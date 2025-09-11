package de.satsuya.elysiumCore.interfaces;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface ManagerInterface {
    String name();
    String[] params() default {};
    int weight() default 0;
}
