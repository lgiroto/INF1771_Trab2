import org.jpl7.Atom;
import org.jpl7.Compound;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Variable;

import Classes.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

public class Game
{
	private Interface t;
	private GameWindow j;
	private static Entity[][] Tiles = new Entity[12][12];
	private static Entity[][] Knowledge = new Entity[12][12];
	
	public Game()
	{ 
		j = new GameWindow();
		j.setVisible(true);
		t = new Interface(Tiles);
		j.loadTabuleiro(t);
		
		String Position = "";
		int CurrentX = 0, NewX = 0, CurrentY = 11, NewY = 11;
		HashMap solution;
		HashMap[] solutions;
		
		while(true){
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
				
			// Pegar Posição
			Query FindCurrentPos = new Query("posicao(X,Y,P)");
			
			solution = (HashMap) FindCurrentPos.oneSolution();
			if (solution != null){
				CurrentX = Integer.parseInt(solution.get("X").toString());
				CurrentY = Integer.parseInt(solution.get("Y").toString());
				Position = solution.get("P").toString();
				System.out.println(CurrentX + "," + CurrentY + "," + Position);
			}			
			
			// Pegar informação dos adjacentes
			Query GetAdjacent = new Query("adjacente(X, Y)");
			solutions = (HashMap[]) GetAdjacent.allSolutions();
			if(solutions != null && solutions.length > 0){
				String Sentido = "nada";
				for(int i = 0; i < solutions.length; i++){
					int AdjX = Integer.parseInt(solutions[i].get("X").toString());
					int AdjY = Integer.parseInt(solutions[i].get("Y").toString());
					Entity AdjTile = Tiles[AdjX][AdjY];
					String ClassName = AdjTile.getClass().getName();
					
					switch(ClassName){
						case("Classes.Foe"):
							if(Sentido != "brisa" && Sentido != "teletransportador")
								Sentido = (((Foe) AdjTile).GetIsTp()) ? "teletransportador" : "monstro";
							break;
						case("Classes.Hole"):
							Sentido = "brisa";
							break;
						case("Classes.Friend"):
							if(Sentido == "nada")
								Sentido = "amigo";
							break;
					}
				}
				for(int i = 0; i < solutions.length; i++){
					int AdjX = Integer.parseInt(solutions[i].get("X").toString());
					int AdjY = Integer.parseInt(solutions[i].get("Y").toString());
					Query AdicionarConhecimento = new Query("adicionar_conhecimento(" + AdjX + "," + AdjY + "," + Sentido + ")");
					solution = (HashMap) AdicionarConhecimento.oneSolution();
				}
			}
			
			//t.CustoTotal++;
			
			Query q5 = new Query("acao(X)");
			solution = (HashMap) q5.oneSolution();
			if(solution != null)
				System.out.println(solution.get("X").toString());
			
			/*while(solution == null){
				Query q6 = new Query("virar_direita");
				q6.oneSolution();				
				solution = (HashMap) q5.oneSolution();
			}*/
			Query q6 = new Query(solution.get("X").toString());
			solution = (HashMap) q6.oneSolution();
			
			Query GetChangedPos = new Query("posicao(X, Y, P)");
			solution = (HashMap) GetChangedPos.oneSolution();
			if (solution != null){
				NewX = Integer.parseInt(solution.get("X").toString());
				NewY = Integer.parseInt(solution.get("Y").toString());
				Walk(CurrentX, CurrentY, NewX, NewY);				
			}
			
			t.repaint();
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
		    				newEntity = new Grass("Images/DonkeyGIF.gif");
		    				break;
			        	case('.'):
			        		newEntity = new Grass("Images/Grass.png");
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
		
		for(int j=0; j<12;j++){
			for(int i = 0; i<12; i++){
				Knowledge[j][i] = null;
			}
		}
		Knowledge[0][11] = Tiles[0][11];
		
		new Game();
	}

	
	private void ChangeDirection(int X, int Y, String Direction){
		String newPath = "";
		
		switch(Direction){
			case("norte"):
				newPath = "Images/DK_North.png";
				break;
			case("sul"):
				newPath = "Images/DK_South.png";
				break;
			case("leste"):
				newPath = "Images/DK_East.png";
				break;
			case("oeste"):
				newPath = "Images/DK_West.png";
				break;
		}
		
		Tiles[X][Y].SetImgPath(newPath);
	}
	
	private void Walk(int OldX, int OldY, int NextX, int NextY){
		String CurrentPath = Tiles[OldX][OldY].getImgPath();		
		Tiles[OldX][OldY].SetImgPath("Images/Grass.png");
		Tiles[NextX][NextY].SetImgPath(CurrentPath);
	}
}