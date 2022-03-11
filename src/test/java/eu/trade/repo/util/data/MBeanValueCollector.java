package eu.trade.repo.util.data;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trade.repo.util.Utilities;

public final class MBeanValueCollector implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(MBeanValueCollector.class);

	private static final int PARAM_HOST  	= 0;
	private static final int PARAM_PORT  	= 1;
	private static final int PARAM_UNAME 	= 2;
	private static final int PARAM_PWD 	 	= 3;
	private static final int PARAM_PERIOD	= 4;
	private static final int PARAM_FILE		= 5;
	private static final int PARAM_MODE		= 6;
	private static final int PARAM_GUI		= 7;
	private static final int PARAM_TIMES	= 8;
	private static final int PARAM_LENGTH	= 8;

	private static final String HOST  	= "host";
	private static final String PORT  	= "port";
	private static final String UNAME 	= "username";
	private static final String PWD 	= "password";
	private static final String PERIOD	= "period";
	private static final String FILE	= "file";
	private static final String MODE	= "mode";
	private static final String TIMES	= "times";

	private static final String MODE_XML  = "XML";
	private static final String MODE_CSV  = "CSV";
	private static final String MODE_GUI  = "GUI";

	private static final String MBEAN_PREFIX ="trade.repo";
	private static final String LABEL_TXT ="Lines written : ";

	private static final String BUTT_START_TEXT = "start";
	private static final String BUTT_STOP_TEXT = "stop";

	private JFrame frame = null;
	private MBeanServerConnection serverConn = null;
	private final Set<ObjectName> mbeans = new HashSet<ObjectName>();
	private boolean headerRow = true;
	private Thread thread;

	private String mode = "", host="", port="", uname="", pwd="";
	private int period 	= -1, writtenCount = 0;

	private int times = -1;
	private File out 	= null;
	private boolean loop = true, forever, gui;
	//SWING
	private final JTabbedPane tp = new JTabbedPane(JTabbedPane.SCROLL_TAB_LAYOUT);
	private final JButton stateButt = new JButton(BUTT_START_TEXT);
	private final JButton saveButt = new JButton("Save");
	private final JButton reconnButt = new JButton("Reconnect");

	private final JLabel argsLabel 	= new JLabel();
	private final JLabel credsLabel 	= new JLabel("Connected to : ");
	private final JLabel countLabel = new JLabel(LABEL_TXT+writtenCount);
	private final JLabel startedLabel = new JLabel("Started :");
	private final JLabel stopedLabel 	= new JLabel("Stopped :");
	private final JTextField periodFld = new JTextField();
	private final JTextField outputFld = new JTextField();
	private final JTextField hostFld 	 = new JTextField();
	private final JTextField portFld   = new JTextField();
	private final JTextField unameFld 	 = new JTextField();
	private final JTextField pwdFld   = new JTextField();

	private PrintWriter os;

	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss");
	private MBeanValueCollector instance = null;

	//CONSTRUCTORs
	public MBeanValueCollector(String propertiesPath) {
		Properties properties = Utilities.loadProperties(propertiesPath);
		String host = properties.getProperty(HOST);
		String port = properties.getProperty(PORT);
		String username = properties.getProperty(UNAME);
		String password = properties.getProperty(PWD);
		int period = getInt(properties.getProperty(PERIOD));
		String file = properties.getProperty(FILE);
		String mode = properties.getProperty(MODE).toUpperCase();
		int times = getInt(properties.getProperty(TIMES));
		init(host, port, username, password, mode, file, period, false, false, times);
	}

	public MBeanValueCollector(String host, String port, String uname, String pwd, String mode, String outFilename, int period, boolean gui, boolean forever, int times) {
		init(host, port, uname, pwd, mode, outFilename, period, gui, forever, times);
	}

	private void init(String host, String port, String uname, String pwd, String mode, String outFilename, int period, boolean gui, boolean forever, int times) {
		if (validateCredentials(host, port, uname, pwd)) {
			this.mode = mode;
			this.period = period*1000;
			this.out = new File(outFilename);
			this.host = host;
			this.port = port;
			this.uname = uname;
			this.pwd = pwd;
			this.gui = gui;
			this.forever = forever;
			this.times = times;
			try {
				doConnect();
			} catch (Exception e) {
				LOG.error("Maybe server is down ..."+e.getMessage());
				return;
			}
			initialize();
			if (this.gui) {
				initUI();
				frame.setVisible(true);
			} else {
				thread = new Thread(this);
			}
		}
		instance = this;
	}

	public void start() {
		this.thread.start();
		try {
			this.thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	//INTERFACE
	@Override
	public void run() {
		try {
			os = new PrintWriter(out);
		} catch (FileNotFoundException e) {
			if (gui) {
				JOptionPane.showConfirmDialog( null, "Cannot write to file "+e.getMessage(), "Failed", JOptionPane.PLAIN_MESSAGE);
				toggleButtonText();
				enableSetupComps(true);
			}
			LOG.error(e.getMessage());
			loop = false;
			thread = null;
			writtenCount = 0;
			return;
		}

		while (loop && (forever || times > 0)) {
			if (mode.equals(MODE_CSV)) {
				if (headerRow) {
					//csv header. only once per run
					os.println(getCSVrow(mbeans, true));
					headerRow = false;
				}
				os.println(getCSVrow(mbeans, false));
			} else {
				os.println(getXMLrow(mbeans));
			}
			os.flush();
			writtenCount++;
			if (!gui) {
				LOG.info(LABEL_TXT+writtenCount);
			}
			try {
				// these is serial so its not exactly 'period'
				Thread.sleep(period);
			} catch (InterruptedException ignored) {
				//When stop button is clicked in the middle of the sleep ..expected
			}
			if (!forever) {
				times--;
			}
		}
	}

	//PRIVATE
	private void cleanUp () {
		if (os != null) {
			os.close();
		}
	}

	private void initialize()  {
		try {
			mbeans.addAll(serverConn.queryNames(null, null));
		} catch (IOException e) {
			LOG.error(e.getMessage());
			return;
		}
	}

	private void enableSetupComps(boolean b) {
		hostFld.setEnabled(b);
		portFld.setEnabled(b);
		unameFld.setEnabled(b);
		pwdFld.setEnabled(b);
		periodFld.setEnabled(b);
		outputFld.setEnabled(b);
		saveButt.setEnabled(b);
		reconnButt.setEnabled(b);
	}

	private void initUI() {
		if (frame == null) {
			frame = new JFrame();
		}

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setSize(500, 328);
		frame.setLocation(400,400);
		frame.setResizable(false);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cleanUp();
				System.exit(0);
			}
		});

		Container c = frame.getContentPane();
		c.setLayout(new BorderLayout());
		c.add(stateButt, BorderLayout.SOUTH);


		JLabel periodLabel 	= new JLabel("Period ");
		JLabel outputLabel 	= new JLabel("Output ");
		JLabel hostLabel 	= new JLabel("Host ");
		JLabel portLabel 	= new JLabel("Port ");
		JLabel unameLabel 	= new JLabel("User ");
		JLabel pwdLabel 	= new JLabel("Password ");

		{
			JPanel setuppanel = new JPanel();
			GridBagLayout layout = new GridBagLayout();
			layout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
			layout.rowHeights = new int[] {7, 7, 7, 7, 7, 7, 7, 7};
			layout.columnWeights = new double[] {0.0, 1.0};
			layout.columnWidths = new int[] {7, 7};
			setuppanel.setLayout(layout);

			setuppanel.add(periodLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 0), 0, 0));
			setuppanel.add(outputLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
			setuppanel.add(hostLabel, 	new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
			setuppanel.add(portLabel, 	new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
			setuppanel.add(unameLabel, 	new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
			setuppanel.add(pwdLabel,	new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));

			setuppanel.add(periodFld, 	new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 0), 0, 0));
			setuppanel.add(outputFld, 	new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
			setuppanel.add(saveButt, 	new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
			setuppanel.add(hostFld, 	new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
			setuppanel.add(portFld, 	new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
			setuppanel.add(unameFld, 	new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
			setuppanel.add(pwdFld, 		new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
			setuppanel.add(reconnButt,	new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
			tp.add("Setup", new JScrollPane(setuppanel));
		}

		{
			JPanel mainpanel = new JPanel();
			GridBagLayout layout = new GridBagLayout();
			layout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0};
			layout.rowHeights = new int[] {7, 7, 7, 7, 7, 7, 7};
			layout.columnWeights = new double[] {0.1};
			layout.columnWidths = new int[] {7};
			mainpanel.setLayout(layout);
			mainpanel.add(startedLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
			mainpanel.add(stopedLabel, 	new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
			mainpanel.add(credsLabel, 	new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
			mainpanel.add(argsLabel, 	new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));
			mainpanel.add(countLabel, 	new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 0), 0, 0));

			tp.add("Info", mainpanel);
		}

		c.add(tp, BorderLayout.CENTER);
		setupComponents();
	}

	private void setupComponents() {
		credsLabel.setText(String.format("Connected to -> %s:%s [%s,%s]", host, port, uname, pwd));
		argsLabel.setText(String.format("OUTPUT FILENAME: %s | PERIOD: %s (msec)| MODE: %s", out, period, mode));
		periodFld.setText(""+period);
		outputFld.setText(out.getPath());
		hostFld.setText(host);
		portFld.setText(port);
		unameFld.setText(uname);
		pwdFld.setText(pwd);


		stateButt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (stateButt.getText().equals(BUTT_START_TEXT)) {
					tp.setSelectedIndex(1);
					enableSetupComps(false);
					writtenCount = 0;
					loop = true;
					headerRow = true;
					startedLabel.setText("Started at :"+sdf.format(new Date()));
					stopedLabel.setText("Stopped at :");
					thread = new Thread(instance);
					thread.start();
				} else
					if (stateButt.getText().equals(BUTT_STOP_TEXT)) {
						enableSetupComps(true);
						loop = false;
						thread.interrupt();
						thread = null;
						os.flush();
						os.close();
						stopedLabel.setText("Stopped at :"+sdf.format(new Date()));
						try {
							LOG.info("Output flushed to file: "+out.getCanonicalPath());
						} catch (IOException ex) {
							LOG.error(ex.getMessage());
						}
					}
				toggleButtonText();
			}
		});
		reconnButt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (validateCredentials(hostFld.getText(), portFld.getText(), unameFld.getText(), pwdFld.getText())) {
					host = hostFld.getText();
					port = portFld.getText();
					uname = unameFld.getText();
					pwd = pwdFld.getText();
					try {
						doConnect();
					} catch (Exception x) {
						JOptionPane.showConfirmDialog(null, "Cannot connect, maybe server is down or the credentials are invalid!", "Failed", JOptionPane.PLAIN_MESSAGE);
						LOG.error("Maybe server is down ..."+x.getMessage());
						return;
					}
					JOptionPane.showConfirmDialog( null, "Connection established!", "Connected", JOptionPane.PLAIN_MESSAGE);
					credsLabel.setText(String.format("Connected to -> %s:%s [%s,%s]", host, port, uname, pwd));
					initialize();
				}
			}
		});

		saveButt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					period = Integer.parseInt(periodFld.getText());
				} catch (NumberFormatException x) {
					JOptionPane.showConfirmDialog( null, "The supplied period is not a NUMBER, cannot save", "Failed", JOptionPane.PLAIN_MESSAGE);
					LOG.error("The supplied PERIOD is not a number, cannot save");
					return;
				}
				out = new File(outputFld.getText());
				if (out.exists()) {
					JOptionPane.showConfirmDialog( null, "The supplied file already exists, it will be OVERWRITTEN", "Confirm", JOptionPane.PLAIN_MESSAGE);
				}
				argsLabel.setText(String.format("OUTPUT FILENAME: %s | PERIOD: %s (msec)| MODE: %s", out, period, mode));
			}
		});
	}

	private void toggleButtonText() {
		if (stateButt.getText().equals(BUTT_START_TEXT)) {
			stateButt.setText(BUTT_STOP_TEXT);
		} else
			if (stateButt.getText().equals(BUTT_STOP_TEXT)) {
				stateButt.setText(BUTT_START_TEXT);
			}
	}

	private String getXMLrow(Set<ObjectName> mbeans) {
		return "UNIMPLEMENTED";
	}

	private String getCSVrow(Set<ObjectName> mbeans, boolean getHeader) {
		StringBuffer header = new StringBuffer("#,TIMESTAMP,");
		StringBuffer row = new StringBuffer(writtenCount+1+","+sdf.format(new Date())+",");
		for (final ObjectName mbean : mbeans) {
			if (mbean.getCanonicalName().startsWith(MBEAN_PREFIX)) {
				MBeanAttributeInfo[] attributes;
				try {
					attributes = serverConn.getMBeanInfo(mbean).getAttributes();
				} catch (Exception e) {
					LOG.error(e.getMessage());
					return null;
				}

				for (final MBeanAttributeInfo attribute : attributes) {
					try {
						if (getHeader) {
							String s = mbean.getCanonicalName();
							int start = s.indexOf("trade.repo:name=")+16;
							int end = s.indexOf(",type=");
							header.append(s.substring(start, end)+"."+attribute.getName()+",");
						} else {
							row.append(serverConn.getAttribute(mbean,attribute.getName())+",");
						}
						//"\t"+attribute.getName()+" -> "+value;
					} catch (Exception ignored) {
					}
				}
			}
		}
		return getHeader ? header.toString() : row.toString();
	}

	private void doConnect() throws IOException {
		Hashtable<String, String[]> env = new Hashtable<String, String[]>();
		String[] credentials = new String[] {uname, pwd};
		env.put(JMXConnector.CREDENTIALS, credentials);

		try {
			JMXServiceURL target = new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://"+host+":"+port+"/repoJmxConnector");
			JMXConnector connector = JMXConnectorFactory.connect(target, env);
			serverConn = connector.getMBeanServerConnection();
		} catch (IOException e) {
			throw e;
		}
		LOG.info("Connection established to host -> " + host+":"+port);
		LOG.info("Writting to -> " + out.getPath());
		LOG.info("Period -> " + period);
	}

	private boolean validateCredentials(String host, String port, String uname, String pwd) {
		if (uname.length() <= 0) {
			LOG.error("Cannot output , please supply a non-empty USERNAME");
			return false;
		}

		if (host.length() <= 0 || port.length() <= 0) {
			LOG.error("Cannot output , please supply a non-empty server HOST and PORT");
			return false;
		}

		try {
			Integer.parseInt(port);
		} catch (NumberFormatException e) {
			LOG.error("The supplied PORT value is not a number");
			return false;
		}
		return true;
	}


	/**
	 * The arguments are:
	 * <ul>
	 * <li>host: Host for JMX connection</li>
	 * <li>port: Port for JMX connection</li>
	 * <li>username: User name for JMX connection</li>
	 * <li>password: Password for JMX connection</li>
	 * <li>period: Period time in seconds to collect the statistics.</li>
	 * <li>file: Path for the output file.</li>
	 * <li>mode: [CSV,XML]</li>
	 * <li>gui: [GUI,noGUI]</li>
	 * <li>times: Number of times to collect the statistics. Optional, If not specified, then it will be forever.</li>
	 * </ul>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < PARAM_LENGTH) {
			LOG.error("MBeanValueCollector: arguments expected are host, port, username, password, period, file, mode, gui, times (optional)");
			return;
		}

		String host = args[PARAM_HOST];
		String port = args[PARAM_PORT];
		String username = args[PARAM_UNAME];
		String password = args[PARAM_PWD];
		int period = getInt(args[PARAM_PERIOD]);

		String file = args[PARAM_FILE];
		if (file.length() <= 0) {
			LOG.error("MBeanValueCollector: please supply a non-empty output filename");
			throw new IllegalArgumentException("MBeanValueCollector: please supply a non-empty output filename");
		}

		String mode = args[PARAM_MODE].toUpperCase();
		if (!mode.equals(MODE_CSV) && !mode.equals(MODE_XML)) {
			LOG.error("MBeanValueCollector: mode not recognised, expected CSV or XML");
			throw new IllegalArgumentException("MBeanValueCollector: mode not recognised, expected CSV or XML");
		}

		boolean gui = args[PARAM_GUI].equalsIgnoreCase(MODE_GUI);
		if (gui) {
			try {
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if ("Nimbus".equals(info.getName())) {
						UIManager.setLookAndFeel(info.getClassName());
						break;
					}
				}
			} catch (Exception e) {
				LOG.error(e.getMessage());
			}
		}

		int times = 0;
		boolean forever = true;
		boolean optional = args.length >= PARAM_LENGTH;
		if (optional) {
			times = getInt(args[PARAM_TIMES]);
			forever = false;
		}

		new MBeanValueCollector(host, port, username, password, mode, file, period, gui, forever, times).start();
	}

	private static int getInt(String arg) {
		int number;
		try {
			number = Integer.parseInt(arg);
		} catch (NumberFormatException e) {
			LOG.error("The supplied param is not a number: " + arg);
			throw new IllegalArgumentException("The supplied param is not a number: " + arg, e);
		}
		return number;
	}
}