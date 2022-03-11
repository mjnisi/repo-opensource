package eu.trade.repo.index.triggers.handlers;

import java.lang.annotation.Annotation;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.index.impl.IndexConfigHolder;
import eu.trade.repo.index.triggers.IndexParamName;
import eu.trade.repo.index.triggers.IndexTriggersDelegate;
import eu.trade.repo.index.triggers.annotation.RegisterStreamChange;
import eu.trade.repo.index.triggers.annotation.StreamChangeParam;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.IndexingState;

@Aspect
public class StreamChangeListener extends BaseAspect{
	
	@Autowired
	private IndexConfigHolder indexConfigHolder;
	@Autowired
	private IndexTriggersDelegate indexTriggersDelegate;
	@PersistenceContext
	private EntityManager entityManager;
	
	@After("@annotation(registerStreamChange)")
	public void updateTriggerIndexContentInfo(JoinPoint  joinPoint, RegisterStreamChange registerStreamChange) {
		if( indexConfigHolder.getAtomicIndexEnabled() ){
			Map<IndexParamName, Object> paramMap = obtainTriggerIndexParams(joinPoint);
			Integer objectId = this.<Integer>getParam(IndexParamName.OBJECT_ID, paramMap);
			Integer fileSize = this.<Integer>getParam(IndexParamName.STREAM_SIZE, paramMap);

			CMISObject object = entityManager.find(CMISObject.class, objectId);
			indexTriggersDelegate.registerStreamChange(objectId, registerStreamChange.value(), (null == fileSize)? null : BigInteger.valueOf(fileSize.longValue()));
			
			object.setIndexStateContent(IndexingState.NONE.getState());
			object.setIndexTriesContent(Integer.valueOf(0));
		}
	}

	private Map<IndexParamName, Object> obtainTriggerIndexParams(JoinPoint joinPoint) {
		Map<IndexParamName, Object> streamChangeParameters = new HashMap<>();
		Annotation[][] parameterAnnotations = getActualParameterAnnotations(joinPoint);
		Object[] params = joinPoint.getArgs();
		for (int i = 0; i < params.length; i++) {
			StreamChangeParam paramAnnotation = getParamAnnotation(parameterAnnotations[i]);
			if (paramAnnotation != null) {
				IndexParamName paramName = paramAnnotation.value();
				if (streamChangeParameters.containsKey(paramName)) {
					throw new IllegalArgumentException("Duplicated stream change parameter: "  + paramName);
				}
				streamChangeParameters.put(paramName, params[i]);
			}
		}
		if( null == streamChangeParameters.get(IndexParamName.OBJECT_ID) ){
			throw new IllegalArgumentException("Object ID is a required to be annotated with @StreamChangeParam(OBJECT_ID)");
		}
		return streamChangeParameters;
	}

	private StreamChangeParam getParamAnnotation(Annotation[] parameterAnnotations) {
		if (parameterAnnotations != null) {
			for (Annotation annotation : parameterAnnotations) {
				if (annotation instanceof StreamChangeParam) {
					return (StreamChangeParam) annotation;
				}
			}
		}
		return null;
	}
}
