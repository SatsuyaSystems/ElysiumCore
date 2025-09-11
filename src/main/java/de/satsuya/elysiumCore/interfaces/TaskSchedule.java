package de.satsuya.elysiumCore.interfaces;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface TaskSchedule {
    long delay() default 0L;   // Startverzögerung in Ticks
    long period() default 20L; // Wiederholrate in Ticks
    boolean async() default true; // true = asynchron, false = synchron
}
