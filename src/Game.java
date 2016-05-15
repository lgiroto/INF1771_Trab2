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
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

public class Game
{
	private Interface t;
	private GameWindow j;
	private static Entity[][] Tiles = new Entity[12][12];
	private static Entity[][] Knowledge = new Entity[12][12];
	private boolean GameOver = false;
	private int FriendsSaved = 0;
	private int DonkeyEnergy = 100;
	
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
		
		while(!GameOver){
			
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// Verifica Amigos Salvos
			int FriendsSoFar = FriendsSaved;
				
			// Pegar Posição
			Query FindCurrentPos = new Query("posicao(X,Y,P)");
			
			solution = (HashMap) FindCurrentPos.oneSolution();
			if (solution != null){
				CurrentX = Integer.parseInt(solution.get("X").toString());
				CurrentY = Integer.parseInt(solution.get("Y").toString());
				Position = solution.get("P").toString();
				System.out.println(CurrentX + "," + CurrentY + "," + Position);
			}			
			
			AtualizaAdjacentes();
			
			Query q5 = new Query("acao(X)");
			solution = (HashMap) q5.oneSolution();
			if(solution != null){
				String Action = solution.get("X").toString();
				System.out.println(Action);
				
				if(Action.equals("atacar")){					
					int AdjX = 0, AdjY = 0;
					switch(Position){
						case("norte"):
							Tiles[CurrentX][CurrentY].SetImgPath("Images/DK_Attacking_North.png");
							AdjX = CurrentX;
							AdjY = CurrentY - 1;
							break;
						case("sul"):
							Tiles[CurrentX][CurrentY].SetImgPath("Images/DK_Attacking_South.png");
							AdjX = CurrentX;
							AdjY = CurrentY + 1;
							break;
						case("leste"):
							Tiles[CurrentX][CurrentY].SetImgPath("Images/DK_Attacking_Right.png");
							AdjX = CurrentX + 1;
							AdjY = CurrentY;
							break;
						case("oeste"):
							Tiles[CurrentX][CurrentY].SetImgPath("Images/DK_Attacking_Left.png");
							AdjX = CurrentX - 1;
							AdjY = CurrentY;
							break;
					}
					
					Entity AttackedTile = Tiles[AdjX][AdjY];
					
					if(AttackedTile.getClass().getName().equals("Classes.Foe")){
						int Life = ((Foe) AttackedTile).GetLife();
						Random RandomGenerator = new Random();
						int Damage = RandomGenerator.nextInt(30) + 20;
						
						((Foe) AttackedTile).SetLife(Damage);
						Life = ((Foe) AttackedTile).GetLife();
						
						if(Life == 0){
							Query MatarMonstro = new Query("matar_monstro");
							MatarMonstro.oneSolution();
							AttackedTile = new Grass("Images/Grass.png");
						}
						
						DonkeyEnergy = DonkeyEnergy - ((Foe) AttackedTile).GetDamage();
						t.DonkeyEnergy = DonkeyEnergy;
						if(DonkeyEnergy <= 0)
							GameOver = true;
					} else{
						Query AtualizarConhecimento = new Query("atualizar_conhecimento(" + AdjX + "," + AdjY + ",nada)");
						solution = (HashMap) AtualizarConhecimento.oneSolution();	
						
						/*Query VirarParaAtacar = new Query("virar_atacar");
						Query VirarDireita = new Query("virar_direita");
						solution = (HashMap) VirarParaAtacar.oneSolution();
						VirarDireita.oneSolution();
						while(solution == null){							
							solution = (HashMap) VirarParaAtacar.oneSolution();
							VirarDireita.oneSolution();
						}*/
					}
					continue;
				}
				
				if(Action.equals("virar_direita"))
					ChangeDirection(CurrentX, CurrentY, Position);
				t.CustoTotal = (Action.equals("salvar_amigo")) ? t.CustoTotal - 1000 : t.CustoTotal + 1;
				
				if(Action.equals("salvar_amigo")){
					FriendsSaved++;
					Tiles[CurrentX][CurrentY] = new Grass("Images/Grass.png");
				}
				
				Query q6 = new Query(Action);
				solution = (HashMap) q6.oneSolution();
				
				Query GetChangedPos = new Query("posicao(X, Y, P)");
				solution = (HashMap) GetChangedPos.oneSolution();
				if (solution != null){
					NewX = Integer.parseInt(solution.get("X").toString());
					NewY = Integer.parseInt(solution.get("Y").toString());
					Walk(CurrentX, CurrentY, NewX, NewY);
					if(Tiles[NewX][NewY].getClass().getName() == "Classes.Hole"){
						t.CustoTotal += 1000;
						GameOver = true;
					}
				}
			}
			
			Query VerificarConhecimento = new Query("conhecimento(_,_,T,0), (T=nada;T=amigo)");
			solution = (HashMap) VerificarConhecimento.oneSolution();
			if(solution != null)
				System.out.println("Tem não explorado");
			
			t.repaint();
		}
	}
	
	private void AtualizaAdjacentes(){
		HashMap solution;
		HashMap[] solutions;
		
		Query GetAdjacent = new Query("adjacente(X, Y)");
		solutions = (HashMap[]) GetAdjacent.allSolutions();
		if(solutions != null && solutions.length > 0){
			String Sentido = "nada";
			int prioridade = 0;
			for(int i = 0; i < solutions.length; i++){
				int AdjX = Integer.parseInt(solutions[i].get("X").toString());
				int AdjY = Integer.parseInt(solutions[i].get("Y").toString());
				Entity AdjTile = Tiles[AdjX][AdjY];
				String ClassName = AdjTile.getClass().getName();
				
				switch(ClassName){
					case("Classes.Foe"):
						if(prioridade < 3){
							if(((Foe) AdjTile).GetIsTp()){
								Sentido = "teletransportador";
								prioridade = 2;
							} else if(prioridade < 2){
								Sentido = "monstro";
								prioridade = 1;
							}
						}
						break;
					case("Classes.Hole"):
						if(prioridade < 4)
							Sentido = "brisa";
						prioridade = 3;
						break;
					case("Classes.Friend"):
						Sentido = "amigo";
						prioridade = 4;
						break;
				}
			}
			for(int i = 0; i < solutions.length; i++){
				int AdjX = Integer.parseInt(solutions[i].get("X").toString());
				int AdjY = Integer.parseInt(solutions[i].get("Y").toString());
				
				Query VerificarConhecimento = new Query("conhecimento(" + AdjX + "," + AdjY + ",_,_)");
				if(!VerificarConhecimento.hasSolution()){
					Query AdicionarConhecimento = new Query("adicionar_conhecimento(" + AdjX + "," + AdjY + "," + Sentido + ")");
					solution = (HashMap) AdicionarConhecimento.oneSolution();
				} else{
					Query AdicionarConhecimento = new Query("atualizar_conhecimento(" + AdjX + "," + AdjY + "," + Sentido + ")");
					solution = (HashMap) AdicionarConhecimento.oneSolution();						
				}
			}
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
		    				newEntity = new Grass("Images/DK_Right.png");
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

	private void ChangeDirection(int X, int Y, String OldDirection){
		String newPath = "";
		
		switch(OldDirection){
			case("norte"):
				newPath = "Images/DK_Right.png";
				break;
			case("sul"):
				newPath = "Images/DK_Left.png";
				break;
			case("leste"):
				newPath = "Images/DK_Front.png";
				break;
			case("oeste"):
				newPath = "Images/DK_Back.png";
				break;
		}
		
		Tiles[X][Y].SetImgPath(newPath);
	}
	
	private void Walk(int OldX, int OldY, int NextX, int NextY){
		String CurrentPath = Tiles[OldX][OldY].getImgPath();		
		Tiles[OldX][OldY].SetImgPath("Images/GrassVisited.png");
		Tiles[NextX][NextY].SetImgPath(CurrentPath);
	}
}