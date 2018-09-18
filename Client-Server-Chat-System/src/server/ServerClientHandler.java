package server;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServerClientHandler implements Runnable {

	Socket socket;
	BufferedReader input;
	PrintStream output;
	String userName;
	// hashmap to maintain all the active users
	static HashMap<String, Socket> userNameObjectMap = new HashMap<String, Socket>();
	// Arraylist that contains all the currently active users client objects
	static ArrayList<ServerClientHandler> clientObjects = new ArrayList<ServerClientHandler>();
	static boolean online;
	ServerGUI guiObject;
	static String broadcastString = "";

	/*
	 * constructor takes in the client socket, the input and output stream of the
	 * clients and also it takes in object list of all the currently active users
	 */
	public ServerClientHandler(Socket socket, BufferedReader inp, PrintStream out, ArrayList list,
			ServerGUI guiObject) {
		this.socket = socket;
		this.input = inp;
		this.output = out;
		this.clientObjects = list;
		this.guiObject = guiObject;
		this.userName = "Annoymous";
		this.online = true;

	}

	// Adds the user name and the socket to a hashmap for ease of tracking active
	// users and broadacsting messages
	public boolean addUserList(String name, Socket object) {
		boolean flag = true;
		// updating the username in list
		ArrayList<ServerClientHandler> handler = clientObjects;
		for (ServerClientHandler client : handler) {
			if (name.equals(client.userName)) {
				System.out.println("user name exists!");
				output.println("UserExists");
				flag = false;
				break;
			}

			if (client.socket == object) {
				client.userName = name;
			}

		}
		if (flag) {
			userNameObjectMap.put(name, object);
			guiObject.setUserListPanel(userNameObjectMap);
		}

		return flag;
	}

	// returns the list of all the active users
	public HashMap<String, Socket> getUserList() {
		return userNameObjectMap;
	}

	// client thread on the server
	public void run() {
		String inputMessage;

		// While loop to continuously keep reading message from the client
		while (true) {
			try {
				inputMessage = input.readLine();
				System.out.println("server read " + inputMessage);
				System.out.println("socket inside is " + socket);

				// when client disconnects the active user count is decreased by one and the
				// clientobject is delted from the arraylist
				// and the socket is closed for the client
				if (inputMessage.contains("Disconnect")) {
					System.out.println("server disconnected");
					String[] user = inputMessage.split("~~");
					guiObject.setSereverLogs(user[1] + " Disconnected");
					clientObjects.remove(this);

					ServerSide.noOfOnlineUsers = ServerSide.noOfOnlineUsers - 1;
					int count = Integer.parseInt(guiObject.lblUserCount.getText());
					guiObject.setActiveUserCount(count - 1);

					userNameObjectMap.remove(user[1]);
					guiObject.setUserListPanel(userNameObjectMap);

					HashMap username = getUserList();
					List<String> keys = new ArrayList<>(username.keySet());
					String usersListComma = String.join(",", keys);

					for (ServerClientHandler client : ServerSide.clientObjects) {
						client.output.println("left~~" + user[1] + " disconnected!");
						client.output.println("ConnectedUserLists~~" + usersListComma);

					}
					online = false;
					break;
				}

				//Checks for user connection and performs the user name validation and updates the active client list
				if (inputMessage.contains("Connect")) {
					String[] user = inputMessage.split("~~");
					boolean flag = addUserList(user[1], socket);

					if (flag) {
						HashMap username = getUserList();
						List<String> keys = new ArrayList<>(username.keySet());
						String usersListComma = String.join(",", keys);

						for (ServerClientHandler client : ServerSide.clientObjects) {

							client.output.println("ConnectedUserLists~~" + usersListComma);
							client.output.println("joining~~" + user[1] + " is now connected!");

						}

						guiObject.setSereverLogs(user[1] + " connected");
						System.out.println(user[1] + " connected");
						// break;
						output.println(
								"Welcome " + userName + ".\nYou are now connected to port : " + guiObject.serverPort);
					} else {
						ServerSide.noOfOnlineUsers = ServerSide.noOfOnlineUsers - 1;
						int count = Integer.parseInt(guiObject.lblUserCount.getText());
						guiObject.setActiveUserCount(count - 1);
					}
				}

				//broadcast the message to all the clients
				if (inputMessage.contains("Broadcast")) {
					for (ServerClientHandler client : ServerSide.clientObjects) {
						String broadcastMessage = inputMessage + "~~" + userName;
						client.output.println(broadcastMessage);
						client.output.flush();
					}
					guiObject.setSereverLogs(userName + " broadcasted a message ");
				}
				
				//This block helps to send private messages between clients
				if (inputMessage.contains("Personal")) {
					String[] personalMessage = inputMessage.split("~~");
					String user = personalMessage[1];
					for (ServerClientHandler client : clientObjects) {
						if (client.userName.equals(user)) {
							client.output.println("Personal~~" + this.userName + " : " + personalMessage[2]);
							client.output.flush();
						}
					}
				}

			} catch (Exception e) {
				System.out.println("");
				break;
			}

		}
	}

}
