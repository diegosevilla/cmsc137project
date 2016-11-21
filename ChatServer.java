import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer implements Runnable{
		private static ServerSocket serverSocket;
	  private static ArrayList<Socket> clients = new ArrayList<Socket>();
		public static int port = 8080;
		
		public ChatServer() throws IOException{
			serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(60000);
			System.out.println("[ChatServer]: Waiting for client on port " + serverSocket.getLocalPort() + "...");
			new Thread(this).start();
		}
		
		public void run(){
		   while(true){
		      try{
		       Socket server = serverSocket.accept();
		       System.out.println("[ChatServer]: Just connected to " + server.getRemoteSocketAddress());
		       clients.add(server);
		       Thread msgHandler = new Thread(new Thread(){
		        public void run(){
		           while(true){
		              try{
		                 DataInputStream in = new DataInputStream(server.getInputStream());
		                 String msg = in.readUTF();
		                 for(int j = 0 ; j < clients.size() ; j++){
		                    DataOutputStream out = new DataOutputStream(clients.get(j).getOutputStream());
		                    /* Send data to the ClientSocket */
		                    out.writeUTF(msg);
		                    out.flush();
		                }
		              }catch(SocketTimeoutException s){
		                 System.out.println("[ChatServer]: Socket timed out!");
		                 break;
		              }catch(IOException er){
		                 clients.remove(server);
		                 break;
		              }
		           }
		        }
		       });
		       msgHandler.start();
		    }catch(IOException e){
		       break;
		    }
		   }
		}
}
