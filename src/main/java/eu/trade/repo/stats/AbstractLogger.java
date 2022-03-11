package eu.trade.repo.stats;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.core.Ordered;

public class AbstractLogger implements Ordered {

	private int order = 1;


	// allows us to control the ordering of advice
	public int getOrder() {
		return this.order;
	}

	public final void setOrder(int order) {
		this.order = order;
	}


	public String getSignature(ProceedingJoinPoint joinPoint, boolean includeArgs){
		StringBuilder stb =  new StringBuilder().append(joinPoint.getTarget().getClass().getName()).append(".").append(joinPoint.getSignature().getName());
		if( includeArgs ){
			stb.append(" (");
			Object[] args = joinPoint.getArgs();
			for (int i = 0; i < args.length; i++) {
				stb.append(args[i]).append(",");
			}
			if (args.length > 0) {
				stb.deleteCharAt(stb.length() - 1);
			}

			stb.append(")");
		}
		return stb.toString();
	}
	

	
	
	
}
