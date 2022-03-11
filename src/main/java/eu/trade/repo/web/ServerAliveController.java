package eu.trade.repo.web;

import com.sun.management.OperatingSystemMXBean;

import eu.trade.repo.selectors.CMISObjectSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.List;

@Controller
public class ServerAliveController {

	public static final int PERCENTAGE_CONVERTER = 100;
	public static final int MEMORY_DIVIDER = 1024;
	private static final Logger logger = LoggerFactory.getLogger(ServerAliveController.class);
	private static final String SERVER_ALIVE_PAGE = "serverAlive";
	@Autowired
	private DataSource dataSource;

	@Autowired
	private CMISObjectSelector cmisObjectSelector;

	@RequestMapping(value = "/serverAlive.jsp")
	public String displayDocuments(Model model, HttpServletRequest request, HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_OK);
		Date now = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
		String date = sdf.format(now);

		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();

		OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

		model.addAttribute("processCpuLoad", Math.round(PERCENTAGE_CONVERTER * (osBean.getProcessCpuLoad())) + "%");
		model.addAttribute("systemCPULoad", Math.round(PERCENTAGE_CONVERTER * (osBean.getSystemCpuLoad())) + "%");

		model.addAttribute("date", date);
		model.addAttribute("maxMemory", ((maxMemory / MEMORY_DIVIDER) / MEMORY_DIVIDER) + "MB");
		model.addAttribute("allocatedMemory", ((allocatedMemory / MEMORY_DIVIDER) / MEMORY_DIVIDER) + "MB");
		model.addAttribute("freeMemory", ((freeMemory / MEMORY_DIVIDER) / MEMORY_DIVIDER) + "MB");

		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			model.addAttribute("dbURL", connection.getMetaData().getURL());
			model.addAttribute("dbUserName", connection.getMetaData().getUserName());
			model.addAttribute("dbConnectionValid", connection.isValid(0));
			model.addAttribute("dbDriverName", connection.getMetaData().getDriverName());
			model.addAttribute("dbDriverVersion", connection.getMetaData().getDriverVersion());
			model.addAttribute("dbTransactionIsolationLevel", getTransactionIsolationLevel(connection));
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			model.addAttribute("dbExceptionMessage", e.getMessage());
			model.addAttribute("dbExceptionStackTrace", e.getStackTrace());
			return SERVER_ALIVE_PAGE;
		} finally {
		   if(connection != null){
			   try {
				   connection.close();
			   } catch (SQLException e) {
				   logger.error(e.getMessage(), e);
			   }
		   }
		}
		List allObjects = cmisObjectSelector.getAllObjectsCount();
		model.addAttribute("repoObjects", allObjects);
		return SERVER_ALIVE_PAGE;
	}

	private String getTransactionIsolationLevel(Connection connection) throws SQLException {
		int transactionIsolation = connection.getTransactionIsolation();
		switch (transactionIsolation) {
			case Connection.TRANSACTION_NONE:
				return "TRANSACTION_NONE";
			case Connection.TRANSACTION_READ_COMMITTED:
				return "TRANSACTION_READ_COMMITTED";
			case Connection.TRANSACTION_READ_UNCOMMITTED:
				return "TRANSACTION_READ_UNCOMMITTED";
			case Connection.TRANSACTION_REPEATABLE_READ:
				return "TRANSACTION_REPEATABLE_READ";
			case Connection.TRANSACTION_SERIALIZABLE:
				return "TRANSACTION_SERIALIZABLE";
			default:
				return "";
		}
	}
}
