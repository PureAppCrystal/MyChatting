package server;

import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;



public class ServerApp {

	public static void main(String[] args) {
		final int SERVER_PORT = 8100;
		//1. 서버 소켓 생성
		ServerSocket serverSocket = null;
		Map<String, Writer> listWriters = new HashMap<String, Writer>();
		
		
		
		
		try {
			serverSocket = new ServerSocket();
			
			//2. 바인딩 
			InetAddress inetAddress = InetAddress.getLocalHost();
			String localhostAddress = inetAddress.getHostAddress();
			
			serverSocket.bind( new InetSocketAddress(localhostAddress, SERVER_PORT) );
			consoleLog("Binding " + localhostAddress+":"+SERVER_PORT);
			
			while ( true ) {
				//3. 연결 요청 기다림 (accept) - blocking
				Socket socket = serverSocket.accept();
				new MyServerThread( socket, listWriters ).start();
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (serverSocket != null && serverSocket.isClosed() == false ) { 
					serverSocket.close(); 
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void consoleLog( String msg) {
		System.out.println("[Server] ["+Thread.currentThread().getId()+"] "+msg  );
	}

}
