package eu.trade.repo.mbean.stats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.SimpleType;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import eu.trade.repo.stats.RepoStatsCollector;

@Component
@ManagedResource(objectName = StatsStarter.MBEAN_NAME, description = "Allows to start/stop the register of service layer statistics")
public class StatsStarter extends NoAttributeHolder implements DynamicMBean, ApplicationListener<ApplicationContextEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(StatsStarter.class);


	private static final String PROPNAME_JMX_DEFAULTS_DIR = "repo.jmx.stats.defaults.dir";
	private static final String PROPNAME_JMX_DEFAULTS_FILENAME = "repo.jmx.stats.defaults.fileName";
	private static final String PROPNAME_COLLECT_STATS_ON_START = "repo.jmx.stats.collectStatsOnStart";

	private static final String DEFAULT_DIR = "C:/Temp/";
	private static final String DEFAULT_FILENAME = "stats";
	private static final String LOG_EXTENSION = ".log";

	private static final String LOG_SUFFIX_STATS_STARTER_STOPPED = "StarterStopped";
	private static final String LOG_SUFFIX_CONTEXT_CLOSED = "ContextClosed";
	private static final String LOG_SUFFIX_CONTEXT_STOPPED = "ContextStopped";
	private static final String LOG_SUFFIX_CONTEXT_REFRESHED = "ContextRefreshed";


	public static final String MBEAN_NAME = "trade.repo:type=reports,name=StatsStarter";

	@Autowired
	private RepoStatsCollector methodStatsRecorder;
	@Autowired
	private MethodAvgTime methodAvgTime;
	@Autowired
	private MethodCallCount methodCallCount;
	@Autowired
	private MethodMaxTime methodMaxTime;
	@Autowired
	private MethodTotalTime methodTotalTime;
	@Autowired
	private MethodAvgTimeInLast5Minutes methodAvgTimeInLast5Minutes;
	@Autowired
	private MethodAvgTimeInLast10Minutes methodAvgTimeInLast10Minutes;
	@Autowired
	private MethodAvgTimeInLast15Minutes methodAvgTimeInLast15Minutes;
	@Autowired
	private MethodLastExecutionTime methodLastExecutionTime;
	@Autowired @Qualifier("jmxConfig")
	private Configuration jmxConfig;

	private final OpenMBeanInfoSupport statsMBeanInfo;

	public StatsStarter() throws OpenDataException {
		OpenMBeanParameterInfo[] printParams = new OpenMBeanParameterInfoSupport[]{
				new OpenMBeanParameterInfoSupport("ResultFile", "Statistics result file", SimpleType.STRING, "C:/Dev/confs/repo/stats.log")
		};
		statsMBeanInfo = new OpenMBeanInfoSupport(
				this.getClass().getName(),
				"Method Statistics Collector MBean",
				new OpenMBeanAttributeInfoSupport[0] ,
				new OpenMBeanConstructorInfoSupport[0],
				new OpenMBeanOperationInfoSupport[]{
					new OpenMBeanOperationInfoSupport("start", "Start recording statistics", null, SimpleType.VOID, OpenMBeanOperationInfoSupport.ACTION),
					new OpenMBeanOperationInfoSupport("stop", "Stop recording", null, SimpleType.VOID, OpenMBeanOperationInfoSupport.ACTION),
					new OpenMBeanOperationInfoSupport("print", "Print statistics to a file", printParams, SimpleType.VOID, OpenMBeanOperationInfoSupport.ACTION)
				},
				new MBeanNotificationInfo[0]);

	}


	private void start(){
		methodStatsRecorder.setEnabled(true);
	}
	private void stop(){
		methodStatsRecorder.setEnabled(false);
		try{
			print(obtainAutomaticLogName(LOG_SUFFIX_STATS_STARTER_STOPPED));
		}catch (InvalidAttributeValueException | RuntimeOperationsException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
	}

	private void print(String fileName) throws RuntimeOperationsException, InvalidAttributeValueException {
		File f = createFile(fileName);
		// Autocloseable stream
		try (PrintWriter writer = new PrintWriter(new OutputStreamWriter( new FileOutputStream( f ), StandardCharsets.UTF_8))) {
			writeStatsPart(methodTotalTime, writer);
			writeStatsPart(methodCallCount, writer);
			writeStatsPart(methodMaxTime, writer);
			writeStatsPart(methodAvgTime, writer);
			writeStatsPart(methodAvgTimeInLast5Minutes, writer);
			writeStatsPart(methodAvgTimeInLast10Minutes, writer);
			writeStatsPart(methodAvgTimeInLast15Minutes, writer);
			writeStatsPart(methodLastExecutionTime, writer);
		} catch (IOException e) {
			LOG.error(e.getLocalizedMessage(), e);
			throw new RuntimeOperationsException(new RuntimeException(e), "Error creating stats file");
		}
	}

	private File createFile(String fileName) throws InvalidAttributeValueException {
		File f = new File(fileName);
		if( f.exists() ){
			StringBuilder stb = new StringBuilder(fileName);
			stb.append("_").append(System.currentTimeMillis());
			if ( f.renameTo(new File(stb.toString())) ) {
				f = new File(fileName);
			}
			else {
				throw new InvalidAttributeValueException("The file already exists and it is not possible to backup it");
			}
		}
		try {
			String parentPath = f.getParent();
			if ( null != parentPath ) {
				File dir = new File(f.getParent());
				if (!dir.exists() && !dir.mkdirs()) {
					throw new InvalidAttributeValueException("Error creating stats file's parent directory");
				}
			}
			// We are sure the file is new (previously renamed if exists).
			if( !f.createNewFile()){
				throw new InvalidAttributeValueException("Error creating stats file");
			}
		} catch (IOException | SecurityException e) {
			LOG.error(e.getLocalizedMessage(), e);
			throw new InvalidAttributeValueException("Error creating stats file");
		}
		return f;
	}
	private <T extends Comparable<T>> void writeStatsPart(AbstractMethodStat<T> statBean, PrintWriter writer){
		writer.println(statBean.getMBeanDescription());
		writer.println();

		List<ResultItem<T>> resultList = obtainOrderedList(statBean.getAttributeValueMap());

		for (ResultItem<T> item : resultList) {
			writer.print(item.getValue().toString());
			writer.print("\t\t\t\t\t");
			writer.println(item.getMethodName());
		}
		writer.println();
		writer.println();
		writer.flush();
	}


	private <T extends Comparable<T>> List<ResultItem<T>> obtainOrderedList(Map<String, T> map){
		List<ResultItem<T>> resultList = new ArrayList<>();
		Iterator<Entry<String, T>> it = map.entrySet().iterator();
		Entry<String,T> entry = null;
		while (it.hasNext()) {
			entry = it.next();
			resultList.add(new ResultItem<T>(entry.getKey(), entry.getValue()));
		}
		Collections.sort( resultList );

		if( LOG.isDebugEnabled() ){
			LOG.debug("............ MAP SIZE: {}", map.size());
			LOG.debug("............ RESULT LIST SIZE: {}", resultList.size());
		}
		return resultList;
	}

	private String obtainAutomaticLogName(String suffix){
		StringBuilder stb = new StringBuilder();
		stb.append(jmxConfig.getString(PROPNAME_JMX_DEFAULTS_DIR, DEFAULT_DIR)).append(File.separator);
		stb.append(jmxConfig.getString(PROPNAME_JMX_DEFAULTS_FILENAME, DEFAULT_FILENAME)).append("_").append(suffix).append(LOG_EXTENSION);
		return stb.toString();
	}

	/* DynamicMBean interface implementation */

	@Override
	public Object getAttribute(String attribute)
			throws AttributeNotFoundException, MBeanException,
			ReflectionException {


		throw new RuntimeOperationsException(new IllegalArgumentException("This MBean does not have attributes"),
				"This MBean does not have attributes");

	}

	@Override
	public AttributeList getAttributes(String[] attributes) {
		validateAttributes(attributes);

		AttributeList resultList = new AttributeList();
		for (String attribute : attributes) {
			try {
				Object value = getAttribute(attribute);
				resultList.add(new Attribute(attribute, value));
			} catch (Exception e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
		}
		return resultList;
	}

	@Override
	public AttributeList setAttributes(AttributeList attributes) {
		return new AttributeList();
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {

		if ( null == actionName) {
			throw new RuntimeOperationsException(new IllegalArgumentException("Operation name cannot be null"),
					"Cannot call invoke with null operation name");
		}

		try {
			switch (actionName) {
			case "start":
				start();
				break;
			case "stop":
				stop();
				break;
			case "print":
				if (null == params || params.length == 0) {
					throw new RuntimeOperationsException(new IllegalArgumentException("File name cannot be null"),
							"File name cannot be null");
				}
				print((String) params[0]);
				break;
			default:
				throw new RuntimeOperationsException(new IllegalArgumentException("action " + actionName + " not supported"));
			}
		} catch (Exception e) {
			String message = new StringBuilder().append("invoking ").append(actionName).append(e.getClass().getName()).append("caught [").append(e.getMessage()).append("]").toString();
			throw new MBeanException(e, message);
		}
		return null;
	}

	@Override
	public MBeanInfo getMBeanInfo() {
		return statsMBeanInfo;
	}


	/* ApplicationListener interface implementation */

	@Override
	public void onApplicationEvent(ApplicationContextEvent event) {
		String logName = null;
		if( event instanceof ContextClosedEvent ){
			logName = obtainAutomaticLogName(LOG_SUFFIX_CONTEXT_CLOSED);
		}else if( event instanceof ContextStoppedEvent ){
			logName = obtainAutomaticLogName(LOG_SUFFIX_CONTEXT_STOPPED);
		}else if( event instanceof ContextRefreshedEvent ){
			logName = obtainAutomaticLogName(LOG_SUFFIX_CONTEXT_REFRESHED);

		}else if( jmxConfig.getBoolean(PROPNAME_COLLECT_STATS_ON_START, false) && event instanceof ContextStartedEvent ){
			start();
		}
		if( null != logName){
			try{
				print(logName);
			}catch (InvalidAttributeValueException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
		}
	}


}

class ResultItem<T extends Comparable<T>> implements Comparable<ResultItem<T>>{
	private final String methodName;
	private final T value;

	ResultItem(String methodName, T value){
		this.methodName = methodName;
		this.value = value;
	}

	public String getMethodName(){
		return methodName;
	}
	public T getValue(){
		return value;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResultItem other = (ResultItem) obj;
		if (value == null) {
			return other.value == null;
		} else return value.equals(other.value);
	}

	@Override
	public int compareTo(ResultItem<T> o) {
		if( null == o ){
			return -1;
		}else if( null == o.getValue() && null == value){
			return 0;
		}else if( null == o.getValue() ){
			return -1;
		}else if( null == value ){
			return 1;
		}
		return -1 * value.compareTo(o.getValue());
	}
}