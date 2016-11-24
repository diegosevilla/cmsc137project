import java.awt.Canvas;
import java.awt.Graphics;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;
import java.net.*;
import java.io.*;
import java.util.*;

public class GameServer implements Runnable{
	/**
	 *
	 */
	private final int WAITING = 1;
	private final int GAME_START = 2;
	private final int IN_PROGRESS = 3;

	private static final long serialVersionUID = 1L;

	int numOfPlayers;
	String data;
	static int port = 4444;
	Map<String, RaceCar> gameState = null;
	DatagramSocket serverSocket = null;
	int gameStage;
	int startX, startY;

	public GameServer(int numOfPlayers){
		this.numOfPlayers = numOfPlayers;
		try {
			serverSocket = new DatagramSocket(port);
			serverSocket.setSoTimeout(100);
		} catch (IOException e) {
			System.err.println("Could not listen on port: "+port);
			System.exit(-1);
		}catch(Exception e){}
		gameState = new HashMap<String, RaceCar>();
		gameStage = WAITING;
		new Thread(this).start();
	}

	public void setPort(int port){
		GameServer.port = port;
	}

	public void broadcast(String msg){
		for(String key : gameState.keySet()){
			RaceCar player= gameState.get(key);
			send(player,msg);
		}
	}

	public void send(RaceCar player, String msg){
		DatagramPacket packet;
		byte buf[] = msg.getBytes();
		packet = new DatagramPacket(buf, buf.length, player.getAddress(),player.getPort());
		try{
			serverSocket.send(packet);
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}


	public String stringify(){
		String retval="";
		for(String key : gameState.keySet()){
			retval += gameState.get(key).toString() + ":";
		}
		return retval;
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.print("starting...");
		int playerCount = 0;
		while(true){
			byte[] buf = new byte[256];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try{
     			serverSocket.receive(packet);
			}catch(Exception ioe){ }

			data = new String(buf).trim();
			switch(gameStage){
			case WAITING :
				if (data.startsWith("CONNECT")){
					String tokens[] = data.split(" ");
					startX = Integer.parseInt(tokens[2]);
					startY = Integer.parseInt(tokens[3]);
					int x = startX + 40*(playerCount % 3);
					int y = startY + 40*(playerCount / 3);
					RaceCar racecar=new RaceCar(tokens[1],packet.getAddress(),packet.getPort(), x,y, "gunna.png");
					System.out.println("Player connected: "+tokens[1]);
					if(gameState.containsKey(tokens[1]))
						continue;
					gameState.put(tokens[1].trim(),racecar);
					broadcast("CONNECTED "+tokens[1]);
					playerCount++;
					System.out.println(playerCount);
					if (playerCount==numOfPlayers){
						gameStage=GAME_START;
					}
				}
				break;
			case GAME_START:
				  broadcast("INITIALPLACES:" + stringify());
				  gameStage=IN_PROGRESS;
				  break;
			case IN_PROGRESS:
				  //Player data was received!
				  if (data.startsWith("PLAYER")){
					  //Tokenize:
					  //The format: PLAYER <player name> <x> <y>
					  String[] playerInfo = data.split(" ");
					  String pname = playerInfo[1];
					  int x = Integer.parseInt(playerInfo[2].trim());
					  int y = Integer.parseInt(playerInfo[3].trim());
					  //Get the player from the game state
					  RaceCar racecar = (RaceCar) gameState.get(pname);
					  racecar.setX(x);
					  racecar.setY(y);
						//if(x && y)
					  //Update the game state
					  gameState.put(pname, racecar);
					  //Send to all the updated game state
					  broadcast(stringify());
				  }
				  break;
			}
		}
	}

	public static void main(String[] args){
		try{
			new GameServer(2);
			new ChatServer();
		}catch(Exception e){}
	}
}
