package eu.trade.repo.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import eu.trade.repo.model.ActionParameter;

/**
 * Annotation used in conjunction with {@link Secured} to specify at parameter level the security constraint target.
 * <p>
 * When no parameter value is defined, then the default action parameter will be used for the specified action in the {@link Secured} annotation.
 * <p>
 * Examples:
 * <br/>
 * <ul>
 * <li>@ApplyTo(ActionParameter.FOLDER)</li>
 * <li>@ApplyTo(ActionParameter.OBJECT)</li>
 * <li>@ApplyTo</li>
 * </ul>
 * <p>
 * In the last case, it will mean that the Secured annotation value is a single parameter action. If not, it will fail at execution time.
 * <p>
 * Note: This annotation must be only applied to {@link String} parameters.
 *  
 * @author porrjai
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Inherited
public @interface ApplyTo {

	ActionParameter value() default ActionParameter.DEFAULT;
	boolean mandatory() default true;
}
