package eu.trade.repo.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.chemistry.opencmis.commons.enums.Action;

/**
 * Annotation used to denote a method as a CMIS secured action based on permission mappings as defined in the specification.
 * 
 * @see ApplyTo
 * 
 * @author porrjai
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface Secured {

	Action value();
}
