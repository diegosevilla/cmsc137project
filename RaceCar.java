import java.net.InetAddress;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class RaceCar {
	private int x, y, angle;
	private String name;
	private int playertype;
	private InetAddress address;
	private int port;
	public BufferedImage img;
	
	public RaceCar(String name, int x, int y, String img){
 		this.x = x;
 		this.y = y;
 		this.name = name;
 		try{
 			this.img = ImageIO.read(new File("piks/tgunna.png"));
 		}catch(Exception e){};
 	}

	public RaceCar(String name, InetAddress address, int port, int x, int y, String img){
		this.x = x;
		this.y = y;
		this.name = name;
		this.address = address;
		this.port = port;
		try{
 			this.img = ImageIO.read(new File("piks/t"+img));
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
