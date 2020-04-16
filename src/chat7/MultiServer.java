package chat7;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class MultiServer extends ConnectDB{
   
   static ServerSocket serverSocket = null;
   static Socket socket = null;
   //클라이언트 정보 저장을 위한 Map컬렉션 정의
   Map<String, PrintWriter> clientMap;
   
   //생성자
   public MultiServer() {
      super();
      
      //클라이언트의 이름과 출력스트림을 저장할 HashMap생성
      clientMap = new HashMap<String, PrintWriter>();
      //HashMap동기화 설정. 쓰레드가 사용자정보에 동시에 접근하는것을 차단한다.
      Collections.synchronizedMap(clientMap);
   }
   
   
   public void init() {
      
      try {
         serverSocket = new ServerSocket(9999);
         System.out.println("서버가 시작되었습니다.");
         
         while(true) {
            socket = serverSocket.accept();
            System.out.println(socket.getInetAddress()+":"+socket.getPort());
            /*
		            클라이언트의 메세지를 모든 클라이언트에게 전달하기 위한
		            쓰레드 생성 및 start.
             */
            Thread mst = new MultiServerT(socket);
            mst.start();
         }
      }
      catch (Exception e) {
         e.printStackTrace();
      }
      finally {
         try {
            serverSocket.close();
         }
         catch (Exception e) {
            e.printStackTrace();
         }
      }
   }
   
   //메인메소드 : Server객체를 생성한후 초기화한다.
   public static void main(String[] args) {
      MultiServer ms = new MultiServer();
      ms.init();
   }

   
   //접속된 모든 클라이언트에게 메세지를 전달하는 역항의 메소드
   public void sendAllMsg(String name, String msg) {
      //Map에 저장된 객체의 키값(이름)을 먼저 얻어온다.
      Iterator<String> it = clientMap.keySet().iterator();
      
      //저장된 객체(클라이언트)의 갯수만큼 반복한다.
      while(it.hasNext()) {
         try {
            //각 클라이언트의 PrintWriter객체를 얻어온다.
            PrintWriter it_out = (PrintWriter) clientMap.get(it.next());
            
            //클라이언트에게 메세지를 전달한다.
            /*
		            매개변수 name이 있는 경우에는 이름+메세지
		            없는경우에는 메세지만 클라이언트로 전달한다.
             */
            if(name.equals("")) {
               it_out.println(URLEncoder.encode(msg,"UTF-8"));
            }
            else {
               it_out.println("["+name+"]:"+msg);
            }
            
            
         }
         catch (Exception e) {
            System.out.println("예외:"+e);
            
         }
      }
   }
   
   //메세지를 전달하는 역항의 메소드(한번귓속말)
   public void sendMsg(String[] arr,String name) {
      
      try {
         //각 클라이언트의 PrintWriter객체를 얻어온다.
         PrintWriter it_out = (PrintWriter) clientMap.get(arr[1]);

         if(arr[1].equals("")) {
            for(int i=2; i<arr.length;i++) {
               it_out.println(URLEncoder.encode(arr[i],"UTF-8"));
            }
         }
         else {
            it_out.print("["+name+"]:");
            for(int i=2; i<arr.length;i++) {
               it_out.print(arr[i]+" ");
            }
            it_out.println();
         }


      }
      catch (Exception e) {
         System.out.println("예외:"+e);

      }
   }
   
   
   //내부클래스
   class MultiServerT extends Thread{
      
      //멤버변수
      Socket socket;
      PrintWriter out = null;
      BufferedReader in = null;
      
      //생성자 : Socket을 기반으로 입출력 스트림을 생성한다.
      public MultiServerT(Socket socket) {
         this.socket = socket;
         try {
            out = new PrintWriter(this.socket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(),"UTF-8"));
                  
         }
         catch (Exception e) {
            System.out.println("예외:"+e);
         }
      }
      public void dbSave(String name, String s) {
    	  try {
    		  String query = "INSERT INTO chatting_tb VALUES (seq_chat.nextval,?,?,TO_CHAR(sysdate,'YYYY-MM-DD/HH:MI:SS'))";

    		  psmt = con.prepareStatement(query);

    		  psmt.setString(1, name);
    		  psmt.setString(2, s);

    		  psmt.executeUpdate();

    	  }
    	  catch (Exception e) {
    		  // e.printStackTrace();
    	  }
      }
      
      
      @Override
      public void run() {
         //클라이언트로부터 전송된 "대화명"을 저장할 변수
         String name = "";
         //메세지 저장용 변수
         String s = "";
         //귓속말상대 저장용 변수
         String m = "";
         try {
            //클라이언트의 이름을 읽어와서 저장
            name = in.readLine();
            name = URLDecoder.decode(name,"UTF-8");
            //접속한 클라이언트에게 새로운 사용자의 입장을 알림.
            //접속자를 제외한 나머지 클라이언트만 입장메세지를 받는다.
            sendAllMsg("",name+" 님이 입장하셨습니다.");
            
            //현재 접속한 클라이언트를 HashMap에 저장한다.
            clientMap.put(name, out);
            
            //HashMap에 저장된 객체의 수로 접속자수를 파악할수 있다.
            System.out.println(name+" 접속");
            System.out.println("현재 접속자 수는"+clientMap.size()+"명 입니다.");
            //입력한 메세지는 모든 클라이언트에게 Echo된다.
            while(in!=null) {
               
               s = in.readLine();
               s = URLDecoder.decode(s,"UTF-8");
               //읽어온 메세지를 콘솔에 출력하고...
               System.out.println(name+ " >> " + s);
               dbSave(name, s);
               
               if(s==null) {
                  break;
               }
               else if(s.charAt(0)=='/') {
                  
                  StringTokenizer st = new StringTokenizer(s);
                  String [] arr = new String[st.countTokens()];
                  int i = 0;
                  while(st.hasMoreElements()){
                     arr[i++] = st.nextToken();
                  }
                  switch (arr[0]) {
                  
                  case "/list":
                     
                     PrintWriter listOut = (PrintWriter) clientMap.get(name);
                     Set<String> keys = clientMap.keySet();
                     listOut.println("채팅방안에 사용자 목록");
                     for(String key : keys) {
                        listOut.println(key);
                     }
                     break;
                     
                  case "/to":
                     if(i>=3) {
                     sendMsg(arr, name);   
                     }
                     else {
                        
                        PrintWriter static_out = (PrintWriter) clientMap.get(arr[1]);
                        static_out.println("["+name+"]"+ "님께서 귓속말을 보냅니다");
                        while(true) {
                           
                           s = in.readLine();
                           s = URLDecoder.decode(s,"UTF-8");
                           
                           System.out.println(name+ " >> " + s);
                           dbSave(name, s);
							
                           if(s.equals("/q")) {
                              static_out.println("["+name+"]:"+ "께서 귓속말을 그만두셨습니다.");
                              static_out = (PrintWriter) clientMap.get(name);
                              static_out.println("귓속말을 종료합니다.");
                              break;
                           }
                                       
                           
                           if(arr[1].equals("")) {
                              static_out.println(URLEncoder.encode(s,"UTF-8"));
                           }
                           else {
                              static_out.println("["+name+"]:"+s);
                           }
                        }
                     }
                     break;
                  default:
                     break;
                  }
               }
               else{
                  //클라이언트에게 Echo해준다.
                  sendAllMsg(name,s);
               }
            }
         }
         catch (Exception e) {
            //e.printStackTrace();
         }
         finally {
            /*
            클라이언트가 접속을 종료하면 예외가 발생하게 되어 finally로
            넘어오게된다. 이때 "대화명"을 통해 remove()시켜준다.
             */
            clientMap.remove(name);
            sendAllMsg("",name+"님이 퇴장하셨습니다.");
            //퇴장하는 클라이언트의 쓰레드명을 보여준다.
            System.out.println(name + " ["+
            Thread.currentThread().getName()+ "] 퇴장");
            System.out.println("현재 접속자 수는"+clientMap.size()+"명 입니다.");
            
            try {
               in.close();
               out.close();
               socket.close();
            }
            catch (Exception e) {
               e.printStackTrace();
            }
         }
      }
      
      
   }

}
