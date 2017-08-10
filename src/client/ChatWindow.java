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

	private BufferedReader br;
	private PrintWriter pw;
	
	Socket socket = null;
	
	
	
	private Frame frame;
	private Panel pannel;
	private Button buttonSend;
	private TextField textField;
	private TextArea textArea;
	
	Scanner scanner = new Scanner( System.in );

	public ChatWindow(String name, Socket socket, BufferedReader br) {
		frame = new Frame(name);
		pannel = new Panel();
		buttonSend = new Button("Send");
		textField = new TextField();
		textArea = new TextArea(30, 80);
		
		

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

		// Pannel
		pannel.setBackground(Color.LIGHT_GRAY);
		pannel.add(textField);
		pannel.add(buttonSend);
		frame.add(BorderLayout.SOUTH, pannel);

		// TextArea
		textArea.setEditable(false);
		frame.add(BorderLayout.CENTER, textArea);

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
		String message = textField.getText();

		//test code
		//리시브 스레드에서 받아서 넣어야 되는 코드 
		//textArea.append( "둘리:" + message );
		//textArea.append("\n");
		
		
		//여기서 서버로 데이터를 전송해야되!
		String msg = scanner.nextLine();
		
		if( "exit".equals(msg)) {
			//서버와 연결을 끊어야해 
		}
		
		//메세지 보내기 
		pw.println( msg );
		
		//에코 메세지받기
		String echoMsg = br.readLine();
		if (echoMsg == null) {
			System.out.println("[Client] Disconnection by Server");
			break;
		}

		textField.setText("");
		textField.requestFocus();		
	}
	
	
	
	
	//내부 클래스 
	public class ChatClientReceiveThread extends Thread {
		
		@Override
		public void run() {
			while( true ) {
				String line;
				try {
					line = br.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				textArea.append( "둘리:" + line );
				textArea.append("\n");
			}
		}
	}
}
