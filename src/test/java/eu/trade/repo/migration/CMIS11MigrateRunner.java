package eu.trade.repo.migration;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CMIS11MigrateRunner {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		ApplicationContext context = new ClassPathXmlApplicationContext(new String[] {
						"migrationApplicationContext.xml"});
		
		CMIS11Migrate cmis11Migrate = (CMIS11Migrate)context.getBean("cmis11Migrate");
		
		cmis11Migrate.migration();
		
		System.exit(0);
	}

}
