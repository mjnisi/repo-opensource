package eu.trade.repo.index.impl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import eu.trade.repo.index.IndexTask;
import eu.trade.repo.stats.AbstractLogger;

@Aspect
public class ReverseIndexLogger extends AbstractLogger{

	private static final Logger LOG = LoggerFactory.getLogger(ReverseIndexLogger.class);


	private final ThreadLocal<Integer> deep = new ThreadLocal<Integer>();

	public ReverseIndexLogger(){
		super();
		setOrder(2);
	}

	public Object logTimeMethod(ProceedingJoinPoint joinPoint) throws Throwable{

		String methodName = getSignature(joinPoint, true);
		
		int deepInt = (null != deep.get())? deep.get() : 0;
		deep.set(deepInt + 1);
		LOG.info("BEGIN. {}", methodName);
		Object retVal = null;
		StopWatch stopWatch = new StopWatch();
		try {
			stopWatch.start(joinPoint.toShortString());
			retVal = joinPoint.proceed();
		} finally {
			stopWatch.stop();
			StringBuilder indentStb = new StringBuilder();
			deepInt = deep.get();
			while(deepInt > 0){
				indentStb.append(">>");
				deepInt--;
			}
			LOG.info("{} END. ExecutionTime: {} s. {}", indentStb.toString(), stopWatch.getTotalTimeSeconds(), methodName);
			deep.set(deep.get() - 1);
		}
		return retVal;
	}
	
	@Override
	public String getSignature(ProceedingJoinPoint joinPoint, boolean includeArgs){
		
		Object[] args = joinPoint.getArgs();
		if( null != args && args[0] instanceof IndexTask){
			StringBuilder stb =  new StringBuilder().append(joinPoint.getTarget().getClass().getName()).append(".").append(joinPoint.getSignature().getName());
			stb.append(" (");
			IndexTask task = (IndexTask)args[0];
			stb.append("owner: ").append(task.getOwner());
			stb.append("; ").append("operation: ").append(task.getOperation());
			stb.append("; ").append("repoId: ").append(task.getRepositoryId());
			stb.append("; ").append("objId: ").append(task.getObjectId());
			stb.append("; ").append("fileName: ").append(task.getFileName());
			stb.append("; ").append("fileSize: ").append(task.getFileSize());
			stb.append(")");
			return stb.toString();
		}else{
			return super.getSignature(joinPoint, includeArgs);
		}
		
	}

}