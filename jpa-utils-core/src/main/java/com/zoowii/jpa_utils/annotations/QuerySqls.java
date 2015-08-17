package com.zoowii.jpa_utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zoowii on 2015/8/17.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface QuerySqls {
    QuerySql[] rawQueries() default {};

    QuerySql[] queries() default {};
}
