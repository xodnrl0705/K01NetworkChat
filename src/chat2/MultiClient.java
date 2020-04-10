package chat2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MultiClient {

	public static void main(String[] args) {
		System.out.print("이름을 입력하세요:");
		Scanner scanner = new Scanner(System.in);
		String s_name = scanner.nextLine();
		
		PrintWriter out = null;
		BufferedReader in = null;
		
		try {
			String ServerIP = "localhost";
			//localhost대신 127.0.0.1로 접속해도 무방하다.
			if(args.length > 0) {
				ServerIP = args[0];
			}
			//IP주소와 포트를 기반으로 소켓객체를 생성하여 서버에 접속함
			Socket socket = new Socket(ServerIP,9999);
			
			System.out.println("서버와 연결되었습니다...");
			
			
			out = new PrintWriter(socket.getOutputStream(),true);
			in = new BufferedReader(new 
					InputStreamReader(socket.getInputStream()));
			//접속자의 "대화명"을 서버측으로 최초 전송한다.
			out.println(s_name);
			
			/*
			소켓이 close되기전이라면 클라이언트는 지속적으로 서버측으로 메세지를
			보낼수 있다.
			*/
			
			while(out != null) {
				try {
					//서버가 echo해준 내용을 라인단위로 읽어와서 콘솔출력
					if(in!=null) {
						System.out.println("Receive : "+ in.readLine());
					}
					
					//클라이언트는 내용을 입력후 서버로 전송한다.
					String s2 = scanner.nextLine();
					if(s2.equals("q") || s2.equals("Q")) {
						//입력값이 Q(q)이면 while 루프 탈출
						break;
					}
					else {
						//만약 q가 아니면 서버로 입력내용 전송
						out.println(s2);
					}
				}
				catch (Exception e) {
					System.out.println("예외:"+e);
				}
			
			}
			//클라이언트가 q를 입력하면 소켓과 스트림이 모두 종료됨.
			in.close();
			out.close();
			socket.close();
		}
		catch (Exception e) {
			System.out.println("예외발생[MultiClient]"+e);
		}
	}	
}
