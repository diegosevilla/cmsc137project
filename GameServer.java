import java.awt.Canvas;
import java.awt.Graphics;
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

	ArrayList plist; //player list
	int playerCount;
	int numOfPlayers;
	String data;
	static int port = 4444;
	HashMap<String, RaceCar> gameState = null;
	DatagramSocket serverSocket = null;
	int gameStage;
	int startX, startY;

	public GameServer(int numOfPlayers){
		//GameServer
		this.numOfPlayers = numOfPlayers;
		plist = new ArrayList();
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
	 		player.message = msg;
			gameState.put(player.getName(), player);
		}
		for(String key : gameState.keySet()){
			RaceCar player= gameState.get(key);
			send(player);
		}
	}

	public void send(RaceCar player){
		try{
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(5000);
			ObjectOutputStream os = new ObjectOutputStream(byteStream);
			os.flush();
			os.writeObject(gameState);
			os.flush();
			//retrieves byte array
			byte[] sendBuf = byteStream.toByteArray();
			DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, player.getAddress(),player.getPort());

			serverSocket.send(packet);
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}

	public String getPlayer(int pcount){
		return plist.get(pcount).toString();
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
		playerCount = 0;
		while(true){
			byte[] buf = new byte[5000];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			RaceCar racecar = new RaceCar("qwe", 10,10, "launcha");
			try{
				serverSocket.receive(packet);
				ByteArrayInputStream byteStream = new ByteArrayInputStream(buf);
				ObjectInputStream is = new ObjectInputStream(byteStream);
				racecar = (RaceCar)is.readObject(); 					
				racecar.setAddress(packet.getAddress());
				racecar.setPort(packet.getPort());
			}catch(Exception ioe){ }

			data = new String(buf).trim();
			switch(gameStage){
			case WAITING :
				if(racecar.gameStage == 1){
					int x = racecar.getX() + 40*(playerCount % 3);
	 				int y = racecar.getY() + 40*(playerCount / 3);				
	  				if(gameState.containsKey(racecar.getName()))
	 					continue;

	 				plist.add(racecar.getName());
					gameState.put(racecar.getName(),racecar);
					broadcast("CONNECTED "+racecar.getName());
	  				System.out.println(racecar.getName()+" CONNECTED");

					playerCount++;
					System.out.println(playerCount);

					if (playerCount==numOfPlayers){
						gameStage=GAME_START;
					}
				}

				String temp="NAMES "+playerCount+" ";
				for(int i=0; i<plist.size(); i++){
			  		temp += plist.get(i) + " ";
			  	}
			  	broadcast(temp);

				break;
			case GAME_START:
				broadcast("START");
				System.out.println("GAME START");
				gameStage=IN_PROGRESS;
				break;
			case IN_PROGRESS:
				if(racecar.message!=null && racecar.message.startsWith("PLAYER")){
					gameState.put(racecar.getName(), racecar);
					//Send to all the updated game state
					broadcast("PLAYER "+racecar.getName());
				}
				break;
			}
		}
	}
}
