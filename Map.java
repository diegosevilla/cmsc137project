import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.awt.event.KeyEvent;

public class Map extends JPanel{
	int height = 0;
	int width = 0;
	int mapLegend[][];
	int pixSizeWidth;
	int pixSizeHeight;
	int startX = -1;
	int startY = -1;

	BufferedImage mapImage;

	public Map(String filename, int width, int height){
		this.height = height*2;
		this.width = width*2;
		int row = 0;
		this.setPreferredSize(new Dimension(width, height));
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
			String line = br.readLine();
			String[] pixDim = line.split(" ");
			mapLegend = new int[Integer.parseInt(pixDim[1])][Integer.parseInt(pixDim[0])];
			pixSizeWidth = this.width/Integer.parseInt(pixDim[0]);
			pixSizeHeight = this.height/Integer.parseInt(pixDim[1]);
			while((line = br.readLine()) != null) {
				String[] textures = line.split(" ");
				for (int col = 0; col < textures.length; col++) {
					mapLegend[row][col] = Integer.parseInt(textures[col]);
				}
				row++;
			}
			createImage();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void createImage(){
		mapImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		BufferedImage img = null;
		Graphics2D g2d = (Graphics2D) mapImage.createGraphics();
		//super.repaint();
		for(int i = 0 ; i < mapLegend.length ; i++){
			for(int j = 0 ; j < mapLegend[i].length ; j++){
				try{
					switch(mapLegend[i][j]){
						case 1:
							img = ImageIO.read(new File("piks/start.png"));
							if(startX == -1) startX = j*pixSizeHeight;
							if(startY == -1) startY = i*pixSizeWidth;
							break;
						case 2:
							img = ImageIO.read(new File("piks/path.png"));
							break;
						case 3:
							img = ImageIO.read(new File("piks/wall.png"));
							break;
						case 4:
							img = ImageIO.read(new File("piks/brick.png"));
					}

				}catch (Exception e){
					e.printStackTrace();
				}
				img = resize(img, pixSizeWidth, pixSizeHeight);
				g2d.drawImage(img, j*pixSizeWidth, i*pixSizeHeight, null);
			}
		}
	}

	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(mapImage, 0,0, null);
	}

	public BufferedImage resize(BufferedImage img, int newW, int newH) {
		Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
		BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2d = dimg.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();

		return dimg;
	}

	public boolean checkCollision(int x, int y, int dir){
		switch(dir){
			case KeyEvent.VK_S:
					x = x/ pixSizeWidth;
					y = (y+25)/pixSizeHeight;
					break;
			case KeyEvent.VK_W:
					x = x/pixSizeWidth;
					y = y/pixSizeHeight;
					break;
			case KeyEvent.VK_D:
					x = (x+35)/pixSizeWidth;
					y = y/pixSizeHeight;
					break;
			case KeyEvent.VK_A:
					x = x/pixSizeWidth;
					y = y/pixSizeHeight;
					break;
		}
		if(mapLegend[y][x] == 3) return false;
		return true;
	}

	public boolean checkWin(int x, int y, int dir){
		x = x/pixSizeWidth;
		y = y/pixSizeHeight;
		if(mapLegend[y][x] == 4) return true;
		return false;
	}
}
