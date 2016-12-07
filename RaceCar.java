import java.net.InetAddress;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.Serializable;

public class RaceCar implements Serializable{
	private final int WAITING = 1;
	private final int GAME_START = 2;
	private final int IN_PROGRESS = 3;

	private int x, y, angle, health, ammo, place, ammoLimit;
	private String name;
	private String playertype;
	private InetAddress address;
	private int port;
	transient BufferedImage img;
	public int gameStage;
	public String message;

	public RaceCar(String name, int x, int y, String playertype){
 		this.x = x;
 		this.y = y;
 		this.name = name;
 		this.playertype = playertype;
 		this.place = 1;
 		if(playertype.equals("ramma")){
			this.ammo = 0;
			this.health = 200;
		}else if(playertype.equals("gunna")){
			this.ammo = 200;
			this.health = 100;
		} else if(playertype.equals("launcha")){
			this.ammo = 50;
			this.health = 100;
		}
		this.ammoLimit = this.ammo;
 		try{
 			this.img = ImageIO.read(new File("piks/t" + playertype + ".png"));
 		}catch(Exception e){};
 		this.gameStage = 0;
 	}

	public RaceCar(String name, InetAddress address, int port, int x, int y, String playertype){
		this.x = x;
		this.y = y;
		this.name = name;
		this.address = address;
		this.port = port;
		 this.playertype = playertype;
 		this.gameStage = 0;
		try{
 			this.img = ImageIO.read(new File("piks/t" + playertype + ".png"));
 		}catch(Exception e){}
	}

	/* setters and getters*/

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getAngle() {
		return angle;
	}

	public void setAngle(int angle) {
		this.angle = angle;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPlayerType() {
		return playertype;
	}

	public void setPlayerType(String playertype) {
		this.playertype = playertype;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public int getAmmo() {
		return ammo;
	}

	public int getAmmoLimit() {
		return ammoLimit;
	}

	public void setAmmo(int ammo) {
		this.ammo = ammo;
	}

	public int getPlace() {
		return place;
	}

	public void setPlace(int place) {
		this.place = place;
	}

	public BufferedImage getImage() {
		return img;
	}

	public void setImage(String playertype) {
		this.playertype = playertype;
		try{
 			this.img = ImageIO.read(new File("piks/t" + playertype + ".png"));
 		}catch(Exception e){};
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String toString(){
		String retval="";
		retval+="PLAYER ";
		retval+=name+" ";
		retval+=x+" ";
		retval+=y+" ";
		retval+=angle;
		return retval;
	}

}
