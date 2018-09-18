package client;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ClientSide extends Thread {
	String chatString = "";
	private static JFrame frame;
	private static JTextField usernameTextField;
	private JTextField chatInputField;
	private JButton btnConnect;
	private JButton btnDisconnect;
	private JButton btnBroadcast;
	private DefaultListModel users;

	private JScrollPane scrollPane;
	private JLabel lblActiveUsers;
	private static JList listActiveUsers;
	private JScrollPane scrollPane_1;
	static ClientSide clientLoginObject;
	static int clientConnectId;
	static String msgToSend;
	static Socket socket = null;
	static BufferedReader input;
	static PrintStream output;
	static Thread receiveMessage;
	private static DefaultListModel userModel;
	private JTextField userNameChat;
	JButton btnStartChat;
	static ArrayList<String> activeUsers = new ArrayList<>(); // Arraylist to maintain all the current active users
	private JLabel lblBroadcastChat;
	private JTextArea textAreaBroadcast;
	static String chattingUserName;
	static JButton btnBroadcastChat;
	static String myUserName;
	static BufferedReader inp = new BufferedReader(new InputStreamReader(System.in));
	private JButton btnClearChat;

	// sets the user name
	public static void setMyUserName() {
		myUserName = usernameTextField.getText();
	}

	// returns the user name
	public String getMyUserName() {
		return myUserName;
	}

	// setter method to set the user name of the client that the user wants to chat
	// with
	public void setChattingUserName() {
		chattingUserName = userNameChat.getText();
	}

	// getter method to get the user name of the client that the user wants to chat
	// with
	public String getChattingUserName() {
		return chattingUserName;
	}

	// setter method to set the currently active user list
	public static void setUserListPanel(String ma) {
		try {
			userModel = new DefaultListModel<>();
			String[] lis = ma.split(",");

			for (int i = 0; i < lis.length; i++) {
				activeUsers.add(lis[i]);
				userModel.addElement(lis[i]);
			}
			listActiveUsers.setModel(userModel);
		} catch (Exception e) {
			System.out.println("system terminated");
		}
	}

	// server output stream is maintained in the method any method that is to be
	// passed to the client is sent here
	public void sendMessage(String msg) {
		try {
			String messageToTransmit = msg;
			if (messageToTransmit == null) {
				messageToTransmit = "";
			}
			this.output.println(messageToTransmit);
			this.output.flush();

		} catch (Exception e) {
			System.out.println("Output Stream is closed");
		}
	}

	/*
	 * This method opens a thread for the continuous input stream at the server end
	 * All the input activities of the server are taken care in this method
	 */
	public void inputStreamThread() {

		try {
			// Starts a thread for the client to continously receive messages
			receiveMessage = new Thread(new Runnable() {
				public void run() {
					while (true) {
						try {
							// Reads the input message and checks for the keyword to take appropriate action
							// on it
							String messageReceived = input.readLine();
							System.out.println(messageReceived);

							// this is received from the server to update the client about the user list
							if (messageReceived.contains("ConnectedUserLists")) {
								String[] userList = messageReceived.split("~~");
								setUserListPanel(userList[1]);
							}
							// client informed about duplicity of username
							if (messageReceived.contains("UserExists")) {
								System.out.println("USER EXISTS");
								JOptionPane.showMessageDialog(frame,
										"Username Already Exists!\nPlease choose another username.");
								disabledConnection();
							}

							// server disconnected message action
							if (messageReceived.contains("ServerDisconnected")) {
								disabledConnection();
								System.out.println("server terminated");
								JOptionPane.showMessageDialog(frame, "Server Disconnected !!");
								listActiveUsers = null;

							}

							// personal message received handled in this block
							if (messageReceived.contains("Personal")) {
								String[] personalMessage = messageReceived.split("~~");
								chatString = chatString + "\n ***private message *****\n" + personalMessage[1];
								textAreaBroadcast.setText(chatString);
							}

							// broadcast messages are handled in this block
							if (messageReceived.contains("Broadcast")) {
								String[] broadcastMessage = messageReceived.split("~~");
								chatString = chatString + "\n" + broadcastMessage[2] + " : " + broadcastMessage[1];
								textAreaBroadcast.setText(chatString);
							}

							//User joining notification
							if (messageReceived.contains("joining")) {
								String[] broadcastMessage = messageReceived.split("~~");
								chatString = chatString + "\n" + broadcastMessage[1];
								textAreaBroadcast.setText(chatString);
							}
							
							//user disconnecting notification
							if (messageReceived.contains("left")) {
								String[] broadcastMessage = messageReceived.split("~~");
								chatString = chatString + "\n" + broadcastMessage[1];
								textAreaBroadcast.setText(chatString);
							}

						} catch (Exception e) {
							// TODO Auto-generated catch block
							System.out.println("Input Stream Close!!");
							break;
						}
					}
				}
			});
			//starts the input stream thread
			receiveMessage.start();

		} catch (Exception e) {
			System.out.println("Host not connected ");
			if (receiveMessage != null) {
				receiveMessage.stop();
			}

		}

	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientSide window = new ClientSide();
					window.frame.setVisible(true);
				} catch (Exception e) {
					System.out.println("main ended");
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ClientSide() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setBackground(SystemColor.activeCaption);
		frame.setResizable(false);
		frame.setTitle("Client Chat");
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				String messageToSend = "Disconnect~~" + getMyUserName();
				setMessageToSent(messageToSend);
				try {
					if (!socket.isClosed()) {
						disabledConnection();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("");
				}
			}
		});
		frame.setBounds(100, 100, 817, 657);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lblEnterUsername = new JLabel("Username :   ");
		lblEnterUsername.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblEnterUsername.setBounds(52, 13, 149, 40);
		frame.getContentPane().add(lblEnterUsername);

		usernameTextField = new JTextField();
		usernameTextField.setBackground(SystemColor.inactiveCaption);
		usernameTextField.setFont(new Font("Tahoma", Font.PLAIN, 18));
		usernameTextField.setBounds(156, 14, 184, 38);
		frame.getContentPane().add(usernameTextField);
		usernameTextField.setColumns(10);

		chatInputField = new JTextField();
		chatInputField.setBackground(SystemColor.inactiveCaption);
		chatInputField.setBounds(26, 554, 514, 40);
		chatInputField.setEnabled(false);
		frame.getContentPane().add(chatInputField);
		chatInputField.setColumns(10);

		btnBroadcast = new JButton("Broadcast Message");
		btnBroadcast.setEnabled(false);
		btnBroadcast.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String stringToSendToServer = "";

				if (btnBroadcast.getText().equals("Broadcast Message")) {
					stringToSendToServer = "Broadcast~~" + chatInputField.getText();
					setMessageToSent(stringToSendToServer);
					chatInputField.setText("");
				} else {
					stringToSendToServer = "Personal~~" + getChattingUserName() + "~~" + chatInputField.getText();
					chatString = chatString + "\n **** private text to " + getChattingUserName() + "*** \n"
							+ getMyUserName() + " : " + chatInputField.getText();
					textAreaBroadcast.setText(chatString);
					setMessageToSent(stringToSendToServer);
					chatInputField.setText("");
				}
			}
		});
		btnBroadcast.setBounds(551, 555, 212, 38);
		frame.getContentPane().add(btnBroadcast);

		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean flag = true;

				String name = usernameTextField.getText();
				if (name.equals("")) {
					JOptionPane.showMessageDialog(frame, "Please enter a username!!");
					flag = false;
				} else {
					try {
						socket = new Socket("localhost", 1222);
						input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						output = new PrintStream(socket.getOutputStream());
					} catch (UnknownHostException e1) {
						// TODO Auto-generated catch block
						System.out.println("Server not connected");
						JOptionPane.showMessageDialog(frame, "Server not connected");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						System.out.println("Server not connected");
						JOptionPane.showMessageDialog(frame, "Server not connected");
					}
					if (socket != null) {
						textAreaBroadcast.setEditable(true);
						String messageToSend = "Connect~~" + usernameTextField.getText();
						frame.setTitle(usernameTextField.getText() + "'s Client Chat");
						setMyUserName();
						activeConnection();
						inputStreamThread();
						setMessageToSent(messageToSend);
					}

				}

			}
		});
		btnConnect.setBounds(368, 15, 176, 40);
		frame.getContentPane().add(btnConnect);

		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.setEnabled(false);
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String messageToSend = "Disconnect~~" + getMyUserName();
				setMessageToSent(messageToSend);
				try {
					disabledConnection();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnDisconnect.setBounds(603, 13, 164, 39);
		frame.getContentPane().add(btnDisconnect);

		scrollPane = new JScrollPane();
		scrollPane.setBounds(26, 201, 514, 335);
		frame.getContentPane().add(scrollPane);

		textAreaBroadcast = new JTextArea();
		textAreaBroadcast.setFont(new Font("Monospaced", Font.PLAIN, 16));
		textAreaBroadcast.setBackground(SystemColor.inactiveCaption);
		scrollPane.setViewportView(textAreaBroadcast);
		textAreaBroadcast.setEditable(false);
		textAreaBroadcast.setEnabled(false);

		lblActiveUsers = new JLabel("Active Users ");
		lblActiveUsers.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblActiveUsers.setBounds(603, 154, 164, 46);
		frame.getContentPane().add(lblActiveUsers);

		scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(571, 201, 202, 326);
		frame.getContentPane().add(scrollPane_1);

		listActiveUsers = new JList();
		listActiveUsers.setFont(new Font("Tahoma", Font.PLAIN, 16));
		listActiveUsers.setBackground(SystemColor.inactiveCaption);
		scrollPane_1.setViewportView(listActiveUsers);

		JLabel lblEnterUsernameFor = new JLabel("Private Chat :");
		lblEnterUsernameFor.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblEnterUsernameFor.setBounds(26, 62, 142, 50);
		frame.getContentPane().add(lblEnterUsernameFor);

		userNameChat = new JTextField();
		userNameChat.setFont(new Font("Tahoma", Font.PLAIN, 16));
		userNameChat.setBackground(SystemColor.inactiveCaption);
		userNameChat.setEnabled(false);
		userNameChat.setBounds(156, 70, 184, 38);
		frame.getContentPane().add(userNameChat);
		userNameChat.setColumns(10);

		btnStartChat = new JButton("Start Private Chat");
		btnStartChat.setEnabled(false);
		btnStartChat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String name = userNameChat.getText();

				if (name.equals("")) {
					JOptionPane.showMessageDialog(frame, "Please enter a username to chat with!!");
				} else {
					boolean flag = false;
					for (int i = 0; i < activeUsers.size(); i++) {
						if (name.equals(activeUsers.get(i))) {
							flag = true;
						}
					}
					if (flag) {
						btnBroadcast.setText("Send to " + name);
						btnBroadcastChat.setEnabled(true);
						setChattingUserName();

					} else {
						JOptionPane.showMessageDialog(frame, "User is not online!");
					}

				}
			}

		});
		btnStartChat.setBounds(368, 68, 176, 42);
		frame.getContentPane().add(btnStartChat);

		lblBroadcastChat = new JLabel("Chat Window");
		lblBroadcastChat.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblBroadcastChat.setBounds(139, 142, 329, 50);
		frame.getContentPane().add(lblBroadcastChat);

		btnBroadcastChat = new JButton("Broadcast Chat");
		btnBroadcastChat.setEnabled(false);
		btnBroadcastChat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnBroadcast.setText("Broadcast Message");

			}
		});
		btnBroadcastChat.setBounds(603, 68, 164, 43);
		frame.getContentPane().add(btnBroadcastChat);

		btnClearChat = new JButton("Clear Chat");
		btnClearChat.setEnabled(false);
		btnClearChat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textAreaBroadcast.setText("");
				chatString = "";
			}
		});
		btnClearChat.setBounds(368, 167, 164, 25);
		frame.getContentPane().add(btnClearChat);
	}

	/*
	 * Setter method to configure the UI for the client once the client is connected
	 * to the server
	 */
	public void activeConnection() {
		listActiveUsers.setEnabled(true);
		btnClearChat.setEnabled(true);
		btnConnect.setEnabled(false);
		btnBroadcast.setEnabled(true);
		btnDisconnect.setEnabled(true);
		chatInputField.setEnabled(true);
		textAreaBroadcast.setEnabled(true);
		userNameChat.setEnabled(true);
		btnStartChat.setEnabled(true);
		textAreaBroadcast.setText("");
		usernameTextField.setEditable(false);
		chatString = "";
	}

	/*
	 * setter method to set the UI of the client once the connection is terminated
	 */
	public void disabledConnection() throws IOException {
		btnBroadcastChat.setEnabled(false);
		listActiveUsers.setEnabled(false);
		btnClearChat.setEnabled(false);
		btnConnect.setEnabled(true);
		btnBroadcast.setEnabled(false);
		btnDisconnect.setEnabled(false);
		chatInputField.setEnabled(false);
		textAreaBroadcast.setText(usernameTextField.getText() + " Disconected");
		userNameChat.setEnabled(false);
		usernameTextField.setEditable(true);
		btnStartChat.setEnabled(false);
		try {
			socket.close();
			output.close();
			input.close();
		} catch (Exception e) {
			System.out.println("unable to disconnect client");
		}
	}

	/* private method to handle all the message transactions with the server */
	private void setMessageToSent(String msg) {
		msgToSend = "";
		msgToSend = msg;
		System.out.println("mesage to send in the Message block" + msg);
		sendMessage(msgToSend);
	}

	// method to return the client string that is to be passed to the server.
	public static String getMessage() {
		return msgToSend;
	}
}
