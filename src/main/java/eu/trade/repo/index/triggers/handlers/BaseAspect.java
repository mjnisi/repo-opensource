package eu.trade.repo.index.triggers.handlers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import eu.trade.repo.index.triggers.IndexParamName;

public class BaseAspect {

	public Annotation[][] getActualParameterAnnotations(JoinPoint joinPoint) {
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

	public Method getActualMethod(JoinPoint joinPoint, MethodSignature methodSignature) {
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

	public <T> T getParam(IndexParamName paramName, Map<IndexParamName, Object> paramMap){
		Object obj = paramMap.get(paramName);
		return (null == obj)? null : (T)obj;
	}

}
