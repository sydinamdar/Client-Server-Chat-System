package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerSide {
	// ArrayList to maintain all the clients objects for which each thread is been
	// allocated
	static ArrayList<ServerClientHandler> clientObjects = new ArrayList<ServerClientHandler>();
	static int serverPort;
	static ServerGUI guiObject;
	private static ServerSocket server;
	// Keep track of the count of the currently active users
	static int noOfOnlineUsers;
	BufferedReader input;
	PrintStream output;
	Socket socket;

	// ServerSide constructors takes in the GUI object
	public ServerSide(ServerGUI object, int serverPort) {
		guiObject = object;
		this.serverPort = serverPort;
	}

	// returns the server port
	public static int getServerPort() {
		return serverPort;
	}

	// starts the server and starts accepting clients, creates a new thread for
	// every new user that logs in
	public void startServer() throws IOException {

		try {
			// ServerSocket object is created here
			server = new ServerSocket(serverPort);
			System.out.println("Server is active on port : " + serverPort);

			// while loop to continously accept client connections
			while (true) {
				// client connection is accepted here and their input and output streams are
				// fetched
				socket = server.accept();
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output = new PrintStream(socket.getOutputStream());
				ServerClientHandler clientHandlerObject = new ServerClientHandler(socket, input, output, clientObjects,
						guiObject);
				// Each new client connecting to the server is provided with a thread to
				// communicate with it
				Thread thread = new Thread(clientHandlerObject);
				clientObjects.add(clientHandlerObject);
				thread.start();
				noOfOnlineUsers++;
				guiObject.setActiveUserCount(noOfOnlineUsers);
			}

		} catch (Exception e) {
			System.out.println("");
		}

	}

	// disconnects the server and informs all the active clients about the server
	// termination
	public void stopServer() throws IOException {
		try {
			System.out.println("Server stopped");
			// The Server Ui is updated and the client object is deleted from the arraylist
			guiObject.setSereverLogs("Server Disconnected!!");
			ArrayList<ServerClientHandler> handler = clientObjects;
			for (ServerClientHandler client : handler) {
				client.output.println("ServerDisconnected");
			}

			guiObject.lblUserCount.setText("0");
			guiObject.listActiveUsers = null;
			server.close();
		} catch (Exception e) {
			System.out.println("");
		}

	}

}