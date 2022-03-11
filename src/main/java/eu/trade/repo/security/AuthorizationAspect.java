package eu.trade.repo.security;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.model.ActionParameter;
import eu.trade.repo.security.CustomSecured.CustomAction;

/**
 * Using aspects this class defines the method interception in order to implement the repository authorization at cmis action level.
 * <p>
 * All public methods annotated with {@link Secured} will be intercepted. The authorization will be based in the context user and the repository permission mappings.
 * 
 * @author porrjai
 */
@Aspect
public class AuthorizationAspect {

	@Autowired
	private Authorization security;

	@Autowired
	private CallContextHolder callContextHolder;

	@Pointcut("execution(public * *(..))")
	private void anyPublicMethod() {
	}

	@Before("anyPublicMethod() && @annotation(secured)")
	private void secured(JoinPoint joinPoint, Secured secured) {
		callContextHolder.login();
		Action action = secured.value();
		security.checkPermission(action, getActionParameters(joinPoint));
	}

	@Before("anyPublicMethod() && @annotation(customSecured)")
	private void customSecured(JoinPoint joinPoint, CustomSecured customSecured) {
		callContextHolder.login();
		CustomAction action = customSecured.value();
		security.checkPermission(action, getActionParameters(joinPoint));
	}

	private ApplyTo getApplyTo(Annotation[] parameterAnnotations) {
		if (parameterAnnotations != null) {
			for (Annotation annotation : parameterAnnotations) {
				if (annotation instanceof ApplyTo) {
					return (ApplyTo) annotation;
				}
			}
		}
		return null;
	}

	private Map<ActionParameter, String> getActionParameters(JoinPoint joinPoint) {
		Map<ActionParameter, String> actionParameters = new HashMap<>();
		Annotation[][] parameterAnnotations = getActualParameterAnnotations(joinPoint);
		Object[] params = joinPoint.getArgs();
		for (int i = 0; i < params.length; i++) {
			ApplyTo applyTo = getApplyTo(parameterAnnotations[i]);
			if (applyTo != null) {
				ActionParameter actionParameter = applyTo.value();
				if (applyTo.mandatory() && params[i] == null) {
					throw new IllegalArgumentException("This parameter is mandatory: "  + actionParameter);
				}
				if (actionParameters.containsKey(actionParameter)) {
					throw new IllegalArgumentException("Duplicated action parameter: "  + actionParameter);
				}
				actionParameters.put(actionParameter, getStringParameter(params[i]));
			}
		}
		return actionParameters;
	}

	private String getStringParameter(Object param) {
		if (param == null) {
			return null;
		}
		if (param instanceof String) {
			return (String) param;
		}
		if (param instanceof Holder<?>) {
			return getStringParameter(((Holder<?>) param).getValue());
		}
		throw new IllegalArgumentException("Parameter type not supported (only String or Hoder<String>)");
	}

	private Annotation[][] getActualParameterAnnotations(JoinPoint joinPoint) {
		Annotation[][] parameterAnnotations = null;
		Signature signature = joinPoint.getSignature();
		if (signature instanceof MethodSignature) {
			MethodSignature methodSignature = (MethodSignature) signature;
			Method actualMethod = getActualMethod(joinPoint, methodSignature);
			parameterAnnotations = actualMethod.getParameterAnnotations();
		}
		else {
			throw new IllegalArgumentException("Is not a MethodSignature: " + signature);
		}

		return parameterAnnotations;
	}

	private Method getActualMethod(JoinPoint joinPoint, MethodSignature methodSignature) {
		Method actualMethod;
		Method currentMethod = methodSignature.getMethod();
		try {
			Class<?> actualClass = joinPoint.getTarget().getClass();
			// This call will look up for public methods with the same signature declared in the actual class or in a superclass.
			actualMethod = actualClass.getMethod(currentMethod.getName(), currentMethod.getParameterTypes());
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Impossible state. No matching method found: " + currentMethod);
		}
		return actualMethod;
	}

}
