import java.net.*;
import java.io.*;
import java.util.Scanner;

public class GreetingClient{
   private static Socket client;

   public static void main(String [] args){
      try{
         String serverName = args[0]; //get IP address of server from first param
         int port = Integer.parseInt(args[1]); //get port from second param
         /* Open a ClientSocket and connect to ServerSocket */

         System.out.println("Connecting to " + serverName + " on port " + port);
         //insert missing line here for creating a new socket for client and binding it to a port
         Socket client = new Socket(serverName, port);
         System.out.println("Just connected to " + client.getRemoteSocketAddress());
         /* Send data to the ServerSocket */
         Thread sendMsg = new Thread(new Thread(){
            public void run(){
               System.out.print("Enter username: ");
               String username = new Scanner(System.in).nextLine();
               while(true){
                  try{
                     String message = new Scanner(System.in).nextLine(); //get message from the third param
                     OutputStream outToServer = client.getOutputStream();
                     DataOutputStream out = new DataOutputStream(outToServer);
                     out.writeUTF(username+": "+message);
                     out.flush();
                  }catch(Exception e){
                     try{
                        client.close();
                     }catch(Exception err){}
                     System.exit(1);
                  }
               }
            }
         });

         Thread receiveMsg = new Thread(new Thread(){
            public void run(){
               try{
                  InputStream inFromServer = client.getInputStream();
                  DataInputStream in = new DataInputStream(inFromServer);
                  String msg = "";
                  while(true){
                        msg = in.readUTF(); 
                        System.out.println(msg);
                  }
               }catch(Exception e){
                  try{
                     client.close();
                  }catch(Exception err){}
                  System.exit(1);
               }
            }
         });
         /* Receive data from the ServerSocket */
         
         sendMsg.start();
         receiveMsg.start();
         
      }catch(IOException e){
         //e.printStackTrace();
         System.out.println("Cannot find Server");
      }catch(ArrayIndexOutOfBoundsException e){
         System.out.println("Usage: java GreetingClient <server ip> <port no.>");
      }
   }
}