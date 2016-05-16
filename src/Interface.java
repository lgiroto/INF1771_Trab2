import javax.imageio.ImageIO;
import javax.swing.*;

import Classes.Entity;

import java.awt.*;
import java.awt.geom.*;
import java.io.File;
import java.io.IOException;
import java.lang.Integer;

public class Interface extends JPanel
{
	private Entity[][] Tiles;
	public int CustoTotal = 0;
	public int DonkeyEnergy = 100;
	public int Municao = 10;
	public boolean GameOver = false;
	public boolean GameWon = false;
	private int tabWidth = 650;
	private int tabHeight = 650;
	private int x = 0;
	private int y = 0;
	private Graphics2D g2d;
	
	public Interface(Entity[][] tiles)
	{
		Tiles = tiles;
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g2d=(Graphics2D) g;

		for(int j=0; j<12;j++){
			for(int i = 0; i<12; i++){
				String Path; Image Image;
				try{
					Path = Tiles[i][j].getImgPath();
					if (!Path.substring(Path.length() - 4).equalsIgnoreCase(".gif"))
						Image = ImageIO.read(new File(Tiles[i][j].getImgPath()));
					else
						Image = new ImageIcon(Tiles[i][j].getImgPath()).getImage();
					g2d.drawImage(Image, x+(i*50), y+(j*50), 50, 50, this);
				} catch(Exception e){
					System.out.println(e);
				}
			}
		}
		
        g2d.setPaint(Color.red);
        g2d.setFont(new Font("Serif", Font.BOLD, 40));
	    g2d.drawString("Custo Total:", 650, 30);
	    g2d.setFont(new Font("Serif", Font.BOLD, 25));
	    g2d.drawString(Integer.toString(CustoTotal), 650, 70);
	    
        g2d.setFont(new Font("Serif", Font.BOLD, 40));
	    g2d.drawString("Vida | Munição:", 650, 120);
	    g2d.setFont(new Font("Serif", Font.BOLD, 25));
	    g2d.drawString(Integer.toString(DonkeyEnergy) + " | " + Municao, 650, 160);
	    
	    if(GameOver){
	    	g2d.setFont(new Font("Serif", Font.BOLD, 50));
		    g2d.drawString("GAME OVER", 650, 220);
	    }
	    if(GameWon){
	    	g2d.setFont(new Font("Serif", Font.BOLD, 50));
		    g2d.drawString("CONGRATULATIONS!", 650, 220);	    	
	    }
	}
}
