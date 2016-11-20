import java.net.InetAddress;


public class RaceCar {
	private int x, y;
	private String name;
	private InetAddress address;
	private int port;
	
	public RaceCar(String name, InetAddress address, int port, String x, String y){
		this.name = name;
		this.address = address;
		this.port = port;
		this.x = Integer.parseInt(x);
		this.y = Integer.parseInt(y);
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
		retval+=y;
		return retval;
	}
	
}
