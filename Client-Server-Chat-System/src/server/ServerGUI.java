package server;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class ServerGUI implements Runnable {

	private JFrame frame;
	private static JTextPane textPaneLogs;
	private JLabel lblUserLogs;
	private JLabel lblServerPort;
	private JLabel lblServerPortNumber;
	public static JLabel lblActiveUser;
	public static JLabel lblUserCount;
	public static JList listActiveUsers;
	private JButton btnStartTheServer;
	private JButton btnStopTheServer;
	private int threadVariable;
	static ServerGUI myObject;
	// HashMap to maintain list of all the active user count
	protected static HashMap<Socket, Integer> connections = new HashMap<Socket, Integer>();
	static int serverPort = 1222;
	private static ServerSide server;
	static String serverLogs = "";
	public JLabel lblActiveUsers;
	private static DefaultListModel users;
	private JScrollPane scrollPane;
	private JScrollPane scrollPane_1;

	// Sets the server logs
	public static void setSereverLogs(String log) {
		// Sets the servers logs along with the timestamp of the ocurrence of the event
		// Calendara anda DateFormat classes are used to capture the current time
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String time = dateFormat.format(cal.getTime());

		// server logs are appended to the server text panel
		serverLogs = serverLogs + time + " : " + log + "\n";
		textPaneLogs.setText(serverLogs);
	}

	// Sets the active user list
	public static void setUserListPanel(HashMap userList) {
		// Default List model and a Jlist is used to keep a track of active users in the
		// network
		users = new DefaultListModel<>();
		List<String> lis = new ArrayList<String>(userList.keySet());
		for (int i = 0; i < lis.size(); i++) {
			users.addElement(lis.get(i));
		}
		listActiveUsers.setModel(users);
	}

	// provides the count of the active users
	public static void setActiveUserCount(int count) {
		lblUserCount.setText(Integer.toString(count));
	}

	public ServerGUI(int a) {
		this.threadVariable = a;
	}

	// launches the SERVER GUI and starts the server side
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerGUI window = new ServerGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ServerGUI() {
		initialize();
	}

	public void run() {
		server = new ServerSide(myObject, serverPort);
		try {
			server.startServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// initializes the server frame
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setBackground(SystemColor.activeCaption);
		frame.setTitle("Server Management");
		frame.setBounds(100, 100, 736, 622);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		lblUserLogs = new JLabel("User Logs");
		lblUserLogs.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblUserLogs.setBounds(86, 181, 141, 36);
		frame.getContentPane().add(lblUserLogs);

		lblServerPort = new JLabel("Server Port Number :");
		lblServerPort.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblServerPort.setBounds(49, 106, 193, 41);
		frame.getContentPane().add(lblServerPort);

		lblServerPortNumber = new JLabel("");
		lblServerPortNumber.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblServerPortNumber.setBounds(238, 114, 122, 27);
		frame.getContentPane().add(lblServerPortNumber);

		lblActiveUser = new JLabel("Number of Active Users : ");
		lblActiveUser.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblActiveUser.setBounds(23, 46, 204, 41);
		frame.getContentPane().add(lblActiveUser);

		lblUserCount = new JLabel("0");
		lblUserCount.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblUserCount.setBounds(238, 53, 77, 27);
		frame.getContentPane().add(lblUserCount);

		btnStopTheServer = new JButton("Stop Server");
		btnStopTheServer.setEnabled(false);
		btnStopTheServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					btnStartTheServer.setEnabled(true);
					btnStopTheServer.setEnabled(false);
					lblServerPortNumber.setText("");
					server.stopServer();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
				e1.printStackTrace();
				}
			}
		});
		btnStopTheServer.setBounds(454, 98, 216, 49);
		frame.getContentPane().add(btnStopTheServer);

		btnStartTheServer = new JButton("Start Server");
		btnStartTheServer.addActionListener(new ActionListener() {

			// Server start action is performed here
			public void actionPerformed(ActionEvent arg0) {
				btnStopTheServer.setEnabled(true);
				btnStartTheServer.setEnabled(false);
				setSereverLogs("Server started!!");
				lblServerPortNumber.setText(Integer.toString(serverPort));
				myObject = new ServerGUI(10);
				Thread t = new Thread(myObject);
				t.start();
			}
		});
		btnStartTheServer.setBounds(454, 25, 216, 49);
		frame.getContentPane().add(btnStartTheServer);

		lblActiveUsers = new JLabel("Active Users");
		lblActiveUsers.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblActiveUsers.setBounds(496, 186, 129, 27);
		frame.getContentPane().add(lblActiveUsers);

		scrollPane = new JScrollPane();
		scrollPane.setBounds(23, 230, 415, 316);
		frame.getContentPane().add(scrollPane);

		textPaneLogs = new JTextPane();
		textPaneLogs.setFont(new Font("Tahoma", Font.PLAIN, 16));
		textPaneLogs.setBackground(SystemColor.inactiveCaption);
		scrollPane.setViewportView(textPaneLogs);

		scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(466, 230, 204, 316);
		frame.getContentPane().add(scrollPane_1);

		listActiveUsers = new JList();
		listActiveUsers.setFont(new Font("Tahoma", Font.PLAIN, 16));
		listActiveUsers.setBackground(SystemColor.inactiveCaption);
		scrollPane_1.setViewportView(listActiveUsers);
	}

	public void initializingFrame() {
		// blUserCount.setText();
	}
}
