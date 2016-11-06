// File Name GreetingServer.java

import java.net.*;
import java.io.*;
import java.util.*;

public class GreetingServer{
   private static ServerSocket serverSocket;
   private static ArrayList<Socket> clients = new ArrayList<Socket>();

   public GreetingServer(int port) throws IOException{
      serverSocket = new ServerSocket(port);
      serverSocket.setSoTimeout(60000);
      System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
   }

  
   public static void main(String [] args){
      try{
         int port = Integer.parseInt(args[0]);
         GreetingServer g = new GreetingServer(port);
         Thread l = new Thread(new Thread(){
            public void run(){
               while(true){
                  try{
                     Socket server = serverSocket.accept();
                     System.out.println("Just connected to " + server.getRemoteSocketAddress());
                     clients.add(server);
                     Thread msgHandler = new Thread(new Thread(){
                        public void run(){
                           while(true){
                              try{
                                 DataInputStream in = new DataInputStream(server.getInputStream());
                                 String msg = in.readUTF();
                                 for(int j = 0 ; j < clients.size() ; j++){
                                    if(server == clients.get(j)) continue;
                                    DataOutputStream out = new DataOutputStream(clients.get(j).getOutputStream());
                                    /* Send data to the ClientSocket */
                                    out.writeUTF(msg);
                                    out.flush();
                                }
                              }catch(SocketTimeoutException s){
                                 System.out.println("Socket timed out!");
                                 break;
                              }catch(IOException er){
                                 clients.remove(server);
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
         });   
         l.start();
      }catch(ArrayIndexOutOfBoundsException e){
         System.out.println("Usage: java GreetingServer <port no.> ");
      }catch(IOException e){
         e.printStackTrace();
      }
   }
}