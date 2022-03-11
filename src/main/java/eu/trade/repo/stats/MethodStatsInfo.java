package eu.trade.repo.stats;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import eu.trade.repo.util.Constants;

public class MethodStatsInfo {

	public static final int ZERO = 0;
	private String methodName;
	private long count;
	private long totalTime;
	private long lastTotalTime;
	private long maxTime;
	//time of invocation(when it occurred), elapsed time of invocation
	private final Map<Long, Long> invocationTimes = Collections.synchronizedMap(new LinkedHashMap<Long, Long>());
	private long lastExecutionTime;

	public MethodStatsInfo(String methodName) {
		this.methodName = methodName;
	}
	//there is no interest in a part of ms
	public long getAvgTime(){
		return (ZERO < count)? totalTime / count : ZERO;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	public long getLastTotalTime() {
		return lastTotalTime;
	}

	public void setLastTotalTime(long lastTotalTime) {
		this.lastTotalTime = lastTotalTime;
	}

	public long getMaxTime() {
		return maxTime;
	}

	public void setMaxTime(long maxTime) {
		this.maxTime = maxTime;
	}

	public long getAvgInvocationTimeInSpecifiedPeriod(long timePeriodToCheck, long currentTime) {
		long invocationElapsedTime = ZERO;
		int numberOfInvocations = ZERO;

		synchronized (invocationTimes){
			for (Long invocationMoment : invocationTimes.keySet()) {
				if ((currentTime - invocationMoment) <= timePeriodToCheck) {
					invocationElapsedTime += invocationTimes.get(invocationMoment);
					numberOfInvocations++;
				}
			}
		}
		return numberOfInvocations > ZERO ? invocationElapsedTime / numberOfInvocations : ZERO;
	}

	public void addElapsedTime(long elapsedTime, long currentTime) {
		invocationTimes.put(currentTime, elapsedTime);
		removeEntriesOlderThan(Constants.FIFTEEN_MINUTES_IN_MILLIS, currentTime);
	}

	void removeEntriesOlderThan(long elementExpirationTime, long currentTime) {
		synchronized (invocationTimes){
			Iterator<Map.Entry<Long,Long>> iterator = invocationTimes.entrySet().iterator();
			while(iterator.hasNext()){
				Map.Entry<Long, Long> entry = iterator.next();
				Long invocationMoment = entry.getKey();
				if((currentTime - invocationMoment) > elementExpirationTime){
					iterator.remove();
				} else {
					break;
				}
			}
		}
	}

	public Map<Long, Long> getInvocationTimes() {
		return Collections.unmodifiableMap(invocationTimes);
	}

	public long getLastExecutionTime() {
		return lastExecutionTime;
	}

	public void setLastExecutionTime(long lastExecutionTime) {
		this.lastExecutionTime = lastExecutionTime;
	}
}
