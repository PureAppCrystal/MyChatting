package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MyServerThread extends Thread{
	static char FS = ':';
	
	private String nickname;
	private Socket socket;
	
	Map<String, Writer> listWriters;
	
	public MyServerThread( Socket socket, Map<String, Writer> listWriters) {
		this.socket = socket;
		this.listWriters = listWriters;
	}
	
	
	private void consoleLog( String msg) {
		System.out.printf("[Server] [%05d] %s \n",getId(), msg  );
	}
	
	
	@Override
	public void run() {

		//4. 연결 성공
		InetSocketAddress remoteSocketAddress = (InetSocketAddress)socket.getRemoteSocketAddress();
		int remoteHostPort = remoteSocketAddress.getPort();
		String remoteHostAddress = remoteSocketAddress.getAddress().getHostAddress();
		consoleLog("Connected From " + remoteHostAddress+":"+remoteHostPort);
		
		//데이터
		InputStream  is = null;
		OutputStream os = null;
		
		try {
			//5. I/O Stream 받아오기 
			is = socket.getInputStream();
			os = socket.getOutputStream();
			BufferedReader br = new BufferedReader( new InputStreamReader( is , "utf-8"));
			PrintWriter pw = new PrintWriter( os, true );
			
			while( true ) {
				//6. 데이터 읽기 (read)
				String msg = br.readLine();
				consoleLog("Received msg : "+msg);
				
				//클라이언트로부터 연결 끊김
				if( msg == null ) {
					consoleLog("DisConnection By Client");
					doQuit( pw );
					break;
				}
				

				//프로토콜 분석

				String[] tokens = msg.split( String.valueOf(FS) );
				String protocol = tokens[0];
				String type 	 = tokens[1];
				String content  = tokens[2];
				
				
				
				
				
				
				
				if( "JOIN".equals( protocol ) ) {
				   doJoin( content, pw );

				} else if( "MESSAGE".equals( protocol ) ) {
				   doMessage( content );

				} else if( "QUIT".equals( protocol ) ) {
				   doQuit( pw );

				} else {
				   consoleLog( "에러:알수 없는 요청(" + tokens[0] + ")" );
				}

				
				
				
				// 7. 데이터 쓰기 (write) 
				//consoleLog("Received : " + msg );
				//pw.println( msg );
			}
			
		} catch( SocketException e ) {
			//상대편이 소켓을 정상적으로 닫지 않고 종료한 경우 
			consoleLog("Sudden Closed By Client ");
		} catch (IOException e ) {
			e.printStackTrace();
		} finally {
			try {
				if (socket != null && socket.isClosed() == false ) { 
					socket.close(); 
				}
			} catch (IOException e ) {
				e.printStackTrace();
			}
		}
	}
	
	
	private void doJoin( String nickName, PrintWriter writer ) {
		consoleLog("====== doJoin ======");
		this.nickname = nickName;
		addWriter( nickName, writer );
		
		//접속 성공 응답 보내야됨
		consoleLog("====== send response join ======");
		//doMessage(writer, responeJoin());
		writer.println(responeJoin());
		writer.flush();
		
		
		//그다음 이 메세지를 전체에게 보내야됨 
		String data = "## "+nickName + "님이 참여하였습니다 ##";
		consoleLog("====== send broadcast join ======");
		broadcast( makeBrodcast(data) );
		
		
	}
	
	private void addWriter( String nickName, Writer writer ) {
	   synchronized( listWriters ) {
		   listWriters.put( nickName, writer );
	   }
	}
	
	
	
//	private void doMessage(Writer writer, String message  ) {
//
//		String data =  "["+nickname+"] "+message;  
//		
//		
//		synchronized( listWriters ) {
//			Set<String> set = listWriters.keySet();
//			   
//			   Iterator<String> it = set.iterator();
//			   while(it.hasNext()) {
//				   if (it.next().equals(nickname) ) {
//					   PrintWriter printWriter = (PrintWriter)listWriters.get(it);
//					   printWriter.println(  "MESSAGE"+FS+"RECEIVE"+FS+data );
//			    	   printWriter.flush();
//				   }
//			   }
//		}
//
//	}
	
	private void doMessage( String message ) {

		String data =  "["+nickname+"] "+message;  
		broadcast( "MESSAGE"+FS+"RECEIVE"+FS+data );

	}

	
	private void broadcast( String data ) {

	   synchronized( listWriters ) {

		   Set<String> set = listWriters.keySet();
		   
		   Iterator<String> it = set.iterator();
		   while(it.hasNext()) {
			   PrintWriter printWriter = (PrintWriter)listWriters.get(it.next());
			   printWriter.println( data );
	    	   printWriter.flush();
		   }
		   
	   }

	}
	
	

	private void doQuit(  Writer writer ) {
		consoleLog("====== doQuit ======");
		//consoleLog(nickname+" sended QUIT");
		removeWriter( writer );
			
		String data = "## "+nickname + "님이 퇴장 하였습니다 ##";
	   
	    broadcast( data );
	   
	}

	
	private void removeWriter( Writer writer ) {

		synchronized( listWriters ) {
			Set<String> set = listWriters.keySet();
			   
			   Iterator<String> it = set.iterator();
			   while(it.hasNext()) {
				   consoleLog("it.next() : "+it.next()+", nickname : "+nickname);
				   if (it.equals(nickname) ) {
					   listWriters.remove(it);
					   break;
				   }
			   }
		}
	}
	
	
	private String responeJoin() {
		//
		return "JOIN"+FS+"SUCCESS"+FS+"-";
	}

	private String makeBrodcast(String data) {
		return "MESSAGE"+FS+"RECEIVE"+FS+data;
	}


	
	
	
}
