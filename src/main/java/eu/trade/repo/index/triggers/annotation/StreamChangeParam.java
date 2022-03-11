package eu.trade.repo.index.triggers.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import eu.trade.repo.index.triggers.IndexParamName;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StreamChangeParam {

	IndexParamName value() default IndexParamName.OBJECT_ID;
	
}
