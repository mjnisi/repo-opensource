package eu.trade.repo.index.triggers.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import eu.trade.repo.index.triggers.StreamChangeType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RegisterStreamChange {

	StreamChangeType value() default StreamChangeType.INSERT;

}
