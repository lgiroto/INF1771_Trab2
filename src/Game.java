import org.jpl7.Atom;
import org.jpl7.Query;
import org.jpl7.Term;

import Classes.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Game
{
	private Tabuleiro t;
	private Janela j;
	private static Entity[][] Tiles = new Entity[12][12];
	
	public Game()
	{ 
		j = new Janela();
		j.setVisible(true);
		
		for(int i=0;i<5;i++) {
			j.Iniciar.addActionListener(new IniciarJogo());
		}
	} 
	
	public static void main(String[] args) 
	{
		
		Query q1 = new Query("consult", new Term[] {new Atom("Prolog/teste.pl")});
		System.out.println("consult " + (q1.hasSolution() ? "succeeded" : "failed"));		
		
		try {
			BufferedReader br = new BufferedReader(new FileReader("Map/Map.txt"));
		    String line = br.readLine();
		    int lineCounter = 0;
		    Entity newEntity = null;
		    int friendsNumber = 0;
		    
		    while (line != null) {
		    	int columnCounter = 0;
		    	for (char ch: line.toCharArray()) {
		    		switch(ch)
			        {
		    			case('X'):
		    				newEntity = new Entity("Images/Kong.png");
		    				break;
			        	case('.'):
			        		newEntity = new Entity("Images/Grass.png");
			        		break;
			        	case('O'):
			        		if(friendsNumber == 0){
			        			newEntity = new Friend("Images/Cranky.png");
			        			friendsNumber++;
			        		}
			        		else if(friendsNumber == 1){
			        			newEntity = new Friend("Images/Diddy.png");
			        			friendsNumber++;
			        		}
			        		else
			        			newEntity = new Friend("Images/Dixie.png");
			        		break;
			        	case('P'):
			        		newEntity = new Hole("Images/Hole.png");
			        		break;
			        	case('T'):
			        		newEntity = new Foe("Images/Teleporter.png", 0, 100, true);
			        		break;
			        	case('U'):
			        		newEntity = new Foe("Images/Bee.png", 20, 100, false);
			        		break;
			        	case('D'):
			        		newEntity = new Foe("Images/Troll.png", 30, 100, false);
			        		break;		        
			        }		    		
		    		Tiles[columnCounter][lineCounter] = newEntity;
		    		columnCounter++;
		    	}
	    		line = br.readLine();
		    	lineCounter++;
		    }
		    		    
		    br.close();
		}
		catch(IOException e)
		{
			System.out.println(e.getMessage());
			System.exit(1);
		}
		
		new Game(); 
	}
	
	public class IniciarJogo implements ActionListener
	{		
		public void actionPerformed(ActionEvent evento) 
		{ 
			t = new Tabuleiro(Tiles);
			j.loadTabuleiro(t);
		}
	}
}