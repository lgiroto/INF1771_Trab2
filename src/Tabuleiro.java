import javax.imageio.ImageIO;
import javax.swing.*;

import Classes.Entity;

import java.awt.*;
import java.awt.geom.*;
import java.io.File;
import java.io.IOException;
import java.lang.Integer;

public class Tabuleiro extends JPanel
{
	private Entity[][] Tiles;
	private int tabWidth = 650;
	private int tabHeight = 650;
	private int x = 0;
	private int y = 0;
	private Graphics2D g2d;
	
	public Tabuleiro(Entity[][] tiles)
	{
		Tiles = tiles;
	}
	
	public void paintComponent(Graphics g)
	{
		Image img;		
		super.paintComponent(g);
		g2d=(Graphics2D) g;

		for(int j=0; j<12;j++){
			for(int i = 0; i<12; i++){
				try {
					img = ImageIO.read(new File(Tiles[i][j].getImgPath()));
					g2d.drawImage(img, x+(i*50), y+(j*50), 50, 50, null);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
