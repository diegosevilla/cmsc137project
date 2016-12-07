
public class Camera {
	int x,y;

	public Camera(int x, int y){
		this.x = x;
		this.y = y;
	}

	public void tick(int x, int y){
		this.x = -x + GameLoop.WIDTH/8;
		this.y = -y + GameLoop.HEIGHT/8;
	}
}
