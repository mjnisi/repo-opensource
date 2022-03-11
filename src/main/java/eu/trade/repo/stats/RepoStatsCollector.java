package eu.trade.repo.stats;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


@Aspect
public class RepoStatsCollector extends AbstractLogger{

	private static final Logger LOG = LoggerFactory.getLogger(RepoStatsCollector.class.getName());
	private static final MethodStatsMap<String, MethodStatsInfo> methodStats = new MethodStatsMap<String, MethodStatsInfo>();

	private static class StatsCollector implements Runnable {

		private final String methodSignature;
		private final long start;
		private final long end;

		/**
		 * @param methodSignature
		 * @param elapsedTime
		 */
		public StatsCollector(String methodSignature, long start) {
			this.methodSignature = methodSignature;
			this.start = start;
			this.end = System.currentTimeMillis();
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public synchronized void run() {
			long elapsedTime = end - start;
			MethodStatsInfo stats = new MethodStatsInfo(methodSignature);
			MethodStatsInfo oldStats = methodStats.putIfAbsent(methodSignature, stats);
			stats = null == oldStats? stats : oldStats;
			stats.setCount(stats.getCount() + 1);
			stats.setTotalTime(stats.getTotalTime() + elapsedTime);
			stats.addElapsedTime(elapsedTime, end);
			stats.setLastExecutionTime(elapsedTime);
			if(elapsedTime > stats.getMaxTime()) {
				stats.setMaxTime(elapsedTime);
			}
			methodStats.put(methodSignature, stats);
		}
	}

	@Autowired
	private Executor statsExecutor;

	private boolean enabled = true;


	public MethodStatsMap<String, MethodStatsInfo> getMethodStats(){
		return methodStats;
	}

	public synchronized void setEnabled(boolean enabled) {
		LOG.info("Audit {}",  enabled ? "enabled":"disabled");
		this.enabled = enabled;
	}

	public synchronized boolean getEnabled() {
		return this.enabled;
	}

	public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {
		long start = System.currentTimeMillis();
		try {
			return joinPoint.proceed();
		}
		finally {
			if (getEnabled()) {
				try {
					statsExecutor.execute(new StatsCollector(getSignature(joinPoint, false), start));
				} catch (RejectedExecutionException e) {
					LOG.warn("Stats collection rejected: " + e.getLocalizedMessage(), e);
				}
			}
		}
	}
}