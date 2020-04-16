package chat7;

import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class MultiClient{
	
	public static void main(String[] args) {
		try {
			Connection con;
			Statement stmt;
			PreparedStatement psmt;
			ResultSet rs;
			
			
			String s_name;
			String control;

			Class.forName("oracle.jdbc.OracleDriver");
			con = DriverManager.getConnection
					("jdbc:oracle:thin://@localhost:1521:orcl", 
							"kosmo","1234"
							);
			System.out.println("오라클 DB 연결성공");

			while(true) {
				boolean check=true; //중복을 체크하는 변수
				
				System.out.print("이름을 입력하세요:");
				Scanner scanner = new Scanner(System.in);
				s_name = scanner.nextLine();

				String query = "SELECT chatname from user_tb ";

				stmt = con.createStatement();
				rs = stmt.executeQuery(query);

				while(rs.next()) {
					String chatname = rs.getString("chatname");

					if(s_name.equals(chatname)) {
						System.out.println("중복된 이름이 있습니다.");
						System.out.println("이름을 다시 설정해주세요.");
						System.out.println();
						check=false;
						break;
					}	
				}

				if(check==true) {
					while(true) {
						
						System.out.println("권한:(1)방장/(2)일반사용자");
						control = scanner.nextLine();
						scanner.nextLine();
						if(control.equals("1")) {
							System.out.print("비밀번호를 입력하시오:");
							String divide = scanner.nextLine();
							if(divide.equals("1234")) {
								System.out.println("방장이 되셨습니다.");
								break;
							}
							else {
								System.out.println("비밀번호가 틀리셨습니다.");
							}
							
						}
						else if(control.equals("2")){
							System.out.println("일반사용자로 시작합니다.");
							break;
						}
						else {
							System.out.println("잘못입력하셨습니다.");
						}
					}
					
					query = "INSERT INTO user_tb VALUES (?,?)";

		    		psmt = con.prepareStatement(query);

		    		psmt.setString(1, s_name);
		    		psmt.setString(2, control);

		    		psmt.executeUpdate();
					
					break;
				}
			}
		

			String ServerIP = "localhost";
			if(args.length>0) {
				ServerIP = args[0];
			}
			Socket socket = new Socket(ServerIP, 9999);
			System.out.println("서버와 연결되었습니다...");
			
			//서버에서 보내는 Echo메세지를 클라이언트에 출력하기 위한 쓰레드 생성
			Thread receiver = new Receiver(socket);
			receiver.start();
			
			//클라이언트의 메세지를 서버로 전송해주는 쓰레드 생성
			Thread sender = new Sender(socket,s_name);
			sender.start();

		}
		
		catch (ClassNotFoundException e) {
			//ojdbc6.jar파일이 없거나 연동되지 않았을때 예외발생
			System.out.println("오라클 드라이버 로딩 실패");
			e.printStackTrace();
		}
		catch (SQLException e) {
			//커넥션 URL이나 계정명이 잘못되었을때 발생되는 예외
			System.out.println("DB 연결 실패");
			e.printStackTrace();
		}
	

		catch (Exception e) {
			System.out.println("예외발생[MultiClient]"+e);
		}
	}

}

