package client;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatWindow {
	final static char FS = ':';
	
	private Socket socket = null;
	private String nickName = null;
	private BufferedReader br;
	private PrintWriter pw;
	private Scanner scanner = new Scanner( System.in );
	
	//UI
	private Frame frame;
	private Panel pannel;
	private Panel mainPannel;
	private Button buttonSend;
	private TextField textField;
	private TextArea textArea;
	private TextArea loginList;
	
	private static void consoleLog( String msg) {
		System.out.println("[Client] "+  msg  );
	}
	
	public ChatWindow(String name, Socket socket, BufferedReader br, PrintWriter pw) {
		frame   	= new Frame(name);
		pannel	 	= new Panel();
		mainPannel	= new Panel();
		buttonSend 	= new Button("Send");
		textField 	= new TextField();
		textArea 	= new TextArea(30, 80);
		loginList   = new TextArea(30, 80);
		
		this.socket   = socket;
		this.br 	  = br;
		this.nickName = name;
		this.pw 	  = pw;
	}

	public void show() {
		// Button
		buttonSend.setBackground(Color.GRAY);
		buttonSend.setForeground(Color.WHITE);
		buttonSend.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent actionEvent ) {
				sendMessage();
			}
		});

		// Textfield
		textField.setColumns(80);
		textField.addKeyListener( new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				char keyCode = e.getKeyChar();
				if (keyCode == KeyEvent.VK_ENTER) {
					sendMessage();
				}
			}
		});

		// TextArea
		textArea.setEditable(false);
		textArea.setLocation(0, 0);
		//textArea.setSize(400, 400);
		//frame.add(BorderLayout.CENTER, textArea);
		
		loginList.setEditable(false);
		loginList.setLocation(400, 0);
		loginList.setSize(100, 100);
		loginList.setBackground(Color.LIGHT_GRAY);
		//frame.add(BorderLayout.CENTER, loginList);
		
		
		// Pannel
		pannel.setBackground(Color.LIGHT_GRAY);
		pannel.add(textField);
		pannel.add(buttonSend);
		frame.add(BorderLayout.SOUTH, pannel);
		
		mainPannel.setBackground(Color.LIGHT_GRAY);
		mainPannel.add(textArea);
		mainPannel.add(loginList);
		frame.add(BorderLayout.CENTER, mainPannel);

		

		// Frame
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				//서버에 연결끊기 요청 필요 
				System.exit(0);
			}
		});
		
		frame.setVisible(true);
		frame.pack();
		
		//스레드 생성 
		consoleLog("- Tread Start -");
		new ChatClientReceiveThread().start();
		
		
		//인터페이스의 경우 
//		new Thread( new Runnable() {
//			@Override
//			public void run() {
//				String line = null;
//				try {
//					br.readLine();
//				} catch (IOException e )  {
//					e.printStackTrace();
//				}
//				textArea.append( "둘리:" + message );
//				textArea.append("\n");
//			}
//		}).start();
	}
	
	private void sendMessage() {
		consoleLog("====== sendMessage ======");
		String message = textField.getText();
		if (message == null) {
			consoleLog("message is empty.");
			return;
		}

		//종료시 
		if( "exit".equals(message)) {
			//서버와 연결을 끊어야해
			consoleLog("Ask to exit : "+"QUIT"+FS+"ASK"+FS+nickName);
			pw.println( "QUIT"+FS+"ASK"+FS+nickName );
			return;
		}
		
		//메세지 보내기 
		pw.println( "MESSAGE"+FS+"SEND"+FS+message );
		
		textField.setText("");
		textField.requestFocus();		
	}
	
	private void closeApp() {
		consoleLog("====== closeApp ======");
		System.exit(0);
		try {
			if (socket != null && socket.isClosed() == false ) {
				socket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void refreshLoginList(String data) {
		String[] users = data.split("@");
		//지워줘야함 
		
		for ( int i=0; i<users.length; i++) {
			loginList.append(users[i]+"\n");
			
		}
		
	}
	
	
	//내부 클래스 
	public class ChatClientReceiveThread extends Thread {
		@Override
		public void run() {
			try {
				while( true ) {
					String echoMsg = br.readLine();
					consoleLog("echoMsg : "+ echoMsg);
					
					//프로토콜 분석
					String[] tokens = echoMsg.split( String.valueOf(FS) );
					String protocol = tokens[0];
					String type 	= tokens[1];
					String content  = tokens[2];
					
					if( "MESSAGE".equals( protocol )  ) {
						textArea.append( content );
						textArea.append("\n");
						//return "INFO"+FS+"LOGINLIST"+FS+data;
					} else if ("INFO".equals( protocol )) {
						if ("LOGINLIST".equals( type ) ) {
							refreshLoginList( content );
						}
					}else if ("QUIT".equals( protocol )) {
						closeApp();
						break;
					} else {
					   consoleLog( "에러 : 알수 없는 응답(" + protocol + ")" );
					}
				}
			} catch( IOException e ) {
				e.printStackTrace();
			} finally {
				try {
					if (socket != null && socket.isClosed() == false ) {
						socket.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				scanner.close();
			}
		}
	}
}
