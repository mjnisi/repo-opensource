package eu.trade.client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DBLimitTest {

	public static void main(String[] args) throws Exception {
		Connection[] conn = new Connection[64];
		Class.forName("oracle.jdbc.OracleDriver");
		
		
		ExecutorService executorService = Executors.newFixedThreadPool(conn.length);
		CountDownLatch countDownLatch = new CountDownLatch(conn.length);

		//initialise the connections
		
		for(int i=0;i<conn.length;i++) {
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					try {
					Connection con = DriverManager
							.getConnection(
									"jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=serverdb.trade.cec.eu.int)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=dev.ec.europa.eu)))",
									"username", "******");
					System.out.println(con.getMetaData().getMaxConnections());
					PreparedStatement stm = con
							.prepareStatement("select count(*) from property");
					for(int j=0;j<100;j++) {
						stm.execute();
						System.out.print(".");
					}
					
					stm.close();
					con.close();
					System.out.println("finished!");
					
					} catch (Exception e) {
						e.printStackTrace();
					}
				}});
		}

		countDownLatch.await();
		executorService.shutdown();
	}

}
