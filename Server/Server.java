import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLServerSocketFactory;

public class Server {

	private ServerSocket serverSocket;
	private SSLServerSocketFactory factory;

	public Server(int port) {

		factory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
		try {
			
			System.out.println("Server running..");
			
			System.out.print("Running at : \n" + InetAddress.getLocalHost().getHostAddress());
			Socket socketGoogle = new Socket();
			socketGoogle.connect(new InetSocketAddress("google.com", 80));
			System.out.print("		OR \n" + socketGoogle.getLocalAddress().toString().replace("/", "") + "\n");
			socketGoogle.close();
			
			this.serverSocket = factory.createServerSocket(port);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not listen on port " + port);
			System.exit(-1);
		}

	}

	public void acceptSocket() {
		Socket socket = null;
		try {
			socket = this.serverSocket.accept();
			new ServerHandler(59090,socket).run();
		} catch (IOException ie) {
			System.out.println("Accept failed: 59090");
		}		
	}

	public static void main(String[] args) {
		System.setProperty("javax.net.ssl.keyStore", "sslserverkeys");
		System.setProperty("javax.net.ssl.keyStorePassword", "password");

		System.setProperty("javax.net.ssl.trustStore", "sslservertrust");
		System.setProperty("javax.net.ssl.trustStorePassword", "password");
		
		Server obj = new Server(59090);
		while (true) {
			obj.acceptSocket();
		}
	}
}
