package client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class ChatClientApp {
	private static final String SERVER_IP   = "192.168.1.22";
	private static final int    SERVER_PORT = 8100;
	private static char FS = ':';
	private static Scanner scanner = new Scanner(System.in);
	
	public static void main(String[] args) {
		String name = null;
		Socket socket = null;
		
		// -->JOIN 둘리\r\n
		try {
			consoleLog("Connect try -> IP : "+SERVER_IP+", PORT : "+SERVER_PORT);
			socket = new Socket();
			// 2. Server Connect			
			socket.connect( new InetSocketAddress(SERVER_IP, SERVER_PORT) );
			
			// 3. IO Receive
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			BufferedReader br = new BufferedReader( new InputStreamReader( is, "utf-8") );
			PrintWriter pw = new PrintWriter( new OutputStreamWriter( os, "utf-8" ), true);
			
			// 4. Write/Read
			// <--JOIN:OK\r\n
			while (true) {
				name = makeNickName();

				consoleLog("Ask to Join : "+requestJoin(name));
				pw.println( requestJoin(name) );

				String echoMsg = br.readLine();
				consoleLog("echoMsg : "+ echoMsg);

				//프로토콜 분석
				String[] tokens = echoMsg.split( String.valueOf(FS) );
				String protocol = tokens[0];
				String type 	= tokens[1];
				String content  = tokens[2];
				
				if ("SUCCESS".equals(type)) {
					new ChatWindow(name, socket, br, pw).show();
					break;
				} else if ("FAIL".equals(type)) {
					continue;
				}
			}	
		} catch( IOException e ) {
			e.printStackTrace();
		} finally {
			scanner.close();
		}
		
	}
	
	
	private static String makeNickName() {
		String nickName = null;

		while( true ) {
			consoleLog("대화명을 입력하세요.");
			System.out.print(">>> ");
			nickName = scanner.nextLine();
			
			if (nickName.isEmpty() == false ) {
				break;
			}
			
			consoleLog("대화명은 한글자 이상 입력해야 합니다.\n");
		}
		
		return nickName;
	}
	
	
	private static void consoleLog( String msg) {
		System.out.println("[Client] "+  msg  );
	}
	
	
	private static String requestJoin(String name) {
		return  "JOIN"+FS+"ASK"+FS+name; 
	}

}
