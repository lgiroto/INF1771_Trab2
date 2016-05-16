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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

public class Game
{
	private Interface t;
	private GameWindow j;
	private static Entity[][] Tiles = new Entity[12][12];
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
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// Verifica Amigos Salvos
			int FriendsSoFar = FriendsSaved;
			if(FriendsSoFar == 3){
				List<Position> Positions = new ArrayList<Position>();
				Positions = RecuperaCaminho(Integer.toString(0), Integer.toString(11), CurrentX, CurrentY);					
				RealizaBusca(Positions, CurrentX, CurrentY, Position);
				t.GameWon = true;
				t.repaint();
				break;
			}
			
			// Pegar Munição
			Query PegarMunicao = new Query("municao(M)");
			solution = (HashMap) PegarMunicao.oneSolution();
			int QtdMunicao = Integer.parseInt(solution.get("M").toString());
			t.Municao = QtdMunicao;
				
			// Pegar Posição
			Query FindCurrentPos = new Query("posicao(X,Y,P)");
			solution = (HashMap) FindCurrentPos.oneSolution();
			if (solution != null){
				CurrentX = Integer.parseInt(solution.get("X").toString());
				CurrentY = Integer.parseInt(solution.get("Y").toString());
				Position = solution.get("P").toString();
				System.out.println(CurrentX + "," + CurrentY + "," + Position);
			}
			
			if(Tiles[CurrentX][CurrentY].getClass().getName().equals("Classes.Foe")){
				if(((Foe) Tiles[CurrentX][CurrentY]).GetIsTp()){
					System.out.println("Teletransporta");
					
					Random RandomGenerator = new Random();
					int RandomX = RandomGenerator.nextInt(11);
					int RandomY = RandomGenerator.nextInt(11);
					
					Walk(CurrentX, CurrentY, RandomX, RandomY);
					Tiles[CurrentX][CurrentY].SetImgPath("Images/Teleporter.png");
					
					CurrentX = RandomX;
					CurrentY = RandomY;
					
					Query Teleport = new Query("teletransportar(" + CurrentX + "," + CurrentY + ")");
					Teleport.oneSolution();
					
					switch(Tiles[CurrentX][CurrentY].getClass().getName()){
						case("Classes.Hole"):
							t.CustoTotal += 1000;
							GameOver = true;
							t.GameOver = true;
							break;
						case("Classes.Foe"):
							DonkeyEnergy = DonkeyEnergy - ((Foe) Tiles[CurrentX][CurrentY]).GetDamage();
							if(DonkeyEnergy < 0)
								DonkeyEnergy = 0;
							t.DonkeyEnergy = DonkeyEnergy;
							t.CustoTotal += ((Foe) Tiles[CurrentX][CurrentY]).GetDamage();
							if(DonkeyEnergy <= 0){
								GameOver = true;
								t.GameOver = true;
								t.CustoTotal += 1000;
							}
							((Foe) Tiles[CurrentX][CurrentY]).SetEngaged();
							break;
					}
					
					continue;
				}
			}
			
			if(Tiles[CurrentX][CurrentY].getClass().getName().equals("Classes.PowerUp")){
				Query GetPowerUp = new Query("pegar_powerup");
				GetPowerUp.oneSolution();
				t.DonkeyEnergy += 50;
				Tiles[CurrentX][CurrentY] = new Grass("Images/Grass.png");
				System.out.println("pegar_powerup");
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
						t.CustoTotal += 10;
						
						((Foe) AttackedTile).SetLife(Damage);
						Life = ((Foe) AttackedTile).GetLife();
						int FoeDamage = ((Foe) AttackedTile).GetDamage();
						
						if(Life == 0){
							Query MatarMonstro = new Query("matar_monstro(" + AdjX + "," + AdjY + ")");
							MatarMonstro.oneSolution();
							AttackedTile = new Grass("Images/Grass.png");
						} else{
							if(!((Foe) AttackedTile).GetEngaged()){
								DonkeyEnergy = DonkeyEnergy - FoeDamage;
								t.DonkeyEnergy = DonkeyEnergy;
								t.CustoTotal += FoeDamage;
								if(DonkeyEnergy <= 0){
									GameOver = true;
									t.GameOver = true;
									t.CustoTotal += 1000;
								}
								((Foe) AttackedTile).SetEngaged();
								
								Query Attack = new Query("atacar(" + FoeDamage + ")");
								Attack.oneSolution();
							}
							Query Attack = new Query("atacar(0)");
							Attack.oneSolution();
						}
					} else{
						Query AtualizarConhecimento = new Query("atualizar_conhecimento(" + AdjX + "," + AdjY + ",nada)");
						solution = (HashMap) AtualizarConhecimento.oneSolution();
						
						Query Attack = new Query("atacar(0)");
						Attack.oneSolution();
					}
					
					t.repaint();
					continue;
				} else{
					switch(Position){
						case("norte"):
							Tiles[CurrentX][CurrentY].SetImgPath("Images/DK_Back.png");
							break;
						case("sul"):
							Tiles[CurrentX][CurrentY].SetImgPath("Images/DK_Front.png");
							break;
						case("leste"):
							Tiles[CurrentX][CurrentY].SetImgPath("Images/DK_Right.png");
							break;
						case("oeste"):
							Tiles[CurrentX][CurrentY].SetImgPath("Images/DK_Left.png");
							break;
					}
				}
				
				if(Action.equals("virar_direita"))
					ChangeDirection(CurrentX, CurrentY, Position);
				t.CustoTotal = (Action.equals("salvar_amigo")) ? t.CustoTotal - 1000 : t.CustoTotal + 1;
				
				if(Action.equals("salvar_amigo")){
					FriendsSaved++;
					Tiles[CurrentX][CurrentY] = new Grass("Images/Grass.png");
				}
				
				if(Action.equals("buscar_livre")){
					List<Position> Positions = new ArrayList<Position>();
					String SelectedX = "", SelectedY = "";
					int XDistance = 99; int YDistance = 99; int TotalDistance = 99;
					
					// Pega monstro mais próximo
					Query BuscarLivres = new Query("conhecimento(X,Y,T,0),(T=nada;T=amigo)");
					solutions = (HashMap[]) BuscarLivres.allSolutions();
					if(solutions != null){
						for(int i = 0; i< solutions.length; i++){
							String CurrX = solutions[i].get("X").toString();
							String CurrY = solutions[i].get("Y").toString();
							XDistance = Math.abs(Integer.parseInt(CurrX) - CurrentX);
							YDistance = Math.abs(Integer.parseInt(CurrY) - CurrentY);
							int CurrTotalDistance = XDistance + YDistance;
							if((SelectedX == "" && SelectedY == "") || TotalDistance > CurrTotalDistance){
								SelectedX = CurrX;
								SelectedY = CurrY;
								TotalDistance = CurrTotalDistance;
							}
						}
					}
					
					// Pega caminho até o monstro selecionado
					Positions = RecuperaCaminho(SelectedX, SelectedY, CurrentX, CurrentY);					
					RealizaBusca(Positions, CurrentX, CurrentY, Position);					
					continue;
				}
				
				if(Action.equals("buscar_powerup")){
					List<Position> Positions = new ArrayList<Position>();
					String SelectedX = "", SelectedY = "";
					int XDistance = 99; int YDistance = 99; int TotalDistance = 99;
					
					// Pega monstro mais próximo
					Query BuscarPU = new Query("conhecimento(X,Y,powerup,_)");
					solutions = (HashMap[]) BuscarPU.allSolutions();
					if(solutions != null){
						for(int i = 0; i< solutions.length; i++){
							String CurrX = solutions[i].get("X").toString();
							String CurrY = solutions[i].get("Y").toString();
							XDistance = Math.abs(Integer.parseInt(CurrX) - CurrentX);
							YDistance = Math.abs(Integer.parseInt(CurrY) - CurrentY);
							int CurrTotalDistance = XDistance + YDistance;
							if((SelectedX == "" && SelectedY == "") || TotalDistance > CurrTotalDistance){
								SelectedX = CurrX;
								SelectedY = CurrY;
								TotalDistance = CurrTotalDistance;
							}
						}
					}
					
					// Pega caminho até o powerup
					Positions = RecuperaCaminho(SelectedX, SelectedY, CurrentX, CurrentY);					
					RealizaBusca(Positions, CurrentX, CurrentY, Position);
					Query Andar = new Query("andar");
					Andar.oneSolution();
					System.out.println("andar");
					continue;
				}
				
				if(Action.equals("buscar_monstro")){
					List<Position> Positions = new ArrayList<Position>();
					String SelectedX = "", SelectedY = "";
					int XDistance = 99; int YDistance = 99; int TotalDistance = 99;
					
					// Pega monstro mais próximo
					Query BuscarMonstros = new Query("conhecimento(X,Y,monstro,0)");
					solutions = (HashMap[]) BuscarMonstros.allSolutions();
					if(solutions != null){
						for(int i = 0; i< solutions.length; i++){
							String CurrX = solutions[i].get("X").toString();
							String CurrY = solutions[i].get("Y").toString();
							XDistance = Math.abs(Integer.parseInt(CurrX) - CurrentX);
							YDistance = Math.abs(Integer.parseInt(CurrY) - CurrentY);
							int CurrTotalDistance = XDistance + YDistance;
							if((SelectedX == "" && SelectedY == "") || TotalDistance > CurrTotalDistance){
								SelectedX = CurrX;
								SelectedY = CurrY;
								TotalDistance = CurrTotalDistance;
							}
						}
					}
					
					// Pega caminho até o monstro selecionado
					Positions = RecuperaCaminho(SelectedX, SelectedY, CurrentX, CurrentY);					
					RealizaBusca(Positions, CurrentX, CurrentY, Position);					
					continue;
				}
				
				if(Action.equals("buscar_teletransporte")){
					List<Position> Positions = new ArrayList<Position>();
					String SelectedX = "", SelectedY = "";
					int XDistance = 99; int YDistance = 99; int TotalDistance = 99;
					
					// Pega teletransporte mais próximo
					Query BuscarTeletransportes = new Query("conhecimento(X,Y,teletransportador,0)");
					solutions = (HashMap[]) BuscarTeletransportes.allSolutions();
					if(solutions == null){
						BuscarTeletransportes = new Query("conhecimento(X,Y,teletransportador,_)");
						solutions = (HashMap[]) BuscarTeletransportes.allSolutions();
					}
					if(solutions != null){
						for(int i = 0; i< solutions.length; i++){
							String CurrX = solutions[i].get("X").toString();
							String CurrY = solutions[i].get("Y").toString();
							XDistance = Math.abs(Integer.parseInt(CurrX) - CurrentX);
							YDistance = Math.abs(Integer.parseInt(CurrY) - CurrentY);
							int CurrTotalDistance = XDistance + YDistance;
							if((SelectedX == "" && SelectedY == "") || TotalDistance > CurrTotalDistance){
								SelectedX = CurrX;
								SelectedY = CurrY;
								TotalDistance = CurrTotalDistance;
							}
						}
					}
					
					// Pega caminho até o teletransporte selecionado
					Positions = RecuperaCaminho(SelectedX, SelectedY, CurrentX, CurrentY);
					RealizaBusca(Positions, CurrentX, CurrentY, Position);
					continue;
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
						t.GameOver = true;
					}
				}
			}

			t.repaint();
		}
	}
	
	private void RealizaBusca(List<Position> Positions, int CurrentX, int CurrentY, String Position){
		HashMap solution;
		HashMap[] solutions;
		int NewX, NewY;
		for (int i=Positions.size()-1;i>=0;i--)
		{
			int DestinyX = Positions.get(i).getX();
			int DestinyY = Positions.get(i).getY();
			Query VirarParaAndar;
			if(DestinyX - CurrentX == 0){
				if(DestinyY - CurrentY > 0){ // Vai pro Sul
					switch(Position){
						case("norte"):
							VirarParaAndar = new Query("virar_direita, virar_direita");
							VirarParaAndar.oneSolution();
							System.out.println("virar_direita");
							System.out.println("virar_direita");
							ChangeDirection(CurrentX, CurrentY, Position);
							t.repaint();
							ChangeDirection(CurrentX, CurrentY, "leste");
							t.repaint();
							break;
						case("leste"):
							VirarParaAndar = new Query("virar_direita");
							VirarParaAndar.oneSolution();
							ChangeDirection(CurrentX, CurrentY, Position);
							t.repaint();
							break;
						case("oeste"):
							VirarParaAndar = new Query("virar_direita, virar_direita, virar_direita");
							VirarParaAndar.oneSolution();
							System.out.println("virar_direita");
							System.out.println("virar_direita");
							System.out.println("virar_direita");
							ChangeDirection(CurrentX, CurrentY, Position);
							t.repaint();
							ChangeDirection(CurrentX, CurrentY, "norte");
							t.repaint();
							ChangeDirection(CurrentX, CurrentY, "leste");
							t.repaint();
							break;
					}
					Position = "sul";
				}
				else { // Vai pro Norte
					switch(Position){
						case("sul"):
							VirarParaAndar = new Query("virar_direita, virar_direita");
							VirarParaAndar.oneSolution();
							System.out.println("virar_direita");
							System.out.println("virar_direita");
							ChangeDirection(CurrentX, CurrentY, Position);
							t.repaint();
							ChangeDirection(CurrentX, CurrentY, "oeste");
							t.repaint();
							break;
						case("oeste"):
							VirarParaAndar = new Query("virar_direita");
							VirarParaAndar.oneSolution();
							System.out.println("virar_direita");
							ChangeDirection(CurrentX, CurrentY, Position);
							t.repaint();
							break;
						case("leste"):
							VirarParaAndar = new Query("virar_direita, virar_direita, virar_direita");
							VirarParaAndar.oneSolution();
							System.out.println("virar_direita");
							System.out.println("virar_direita");
							System.out.println("virar_direita");
							ChangeDirection(CurrentX, CurrentY, Position);
							t.repaint();
							ChangeDirection(CurrentX, CurrentY, "sul");
							t.repaint();
							ChangeDirection(CurrentX, CurrentY, "oeste");
							t.repaint();
							break;
					}
					Position = "norte";
				}
			}
			if(DestinyY - CurrentY == 0){
				if(DestinyX - CurrentX > 0){ // Vai para Leste
					switch(Position){
						case("norte"):
							VirarParaAndar = new Query("virar_direita");
							VirarParaAndar.oneSolution();
							System.out.println("virar_direita");
							ChangeDirection(CurrentX, CurrentY, Position);
							t.repaint();
							break;
						case("sul"):
							VirarParaAndar = new Query("virar_direita, virar_direita, virar_direita");
							VirarParaAndar.oneSolution();
							System.out.println("virar_direita");
							System.out.println("virar_direita");
							System.out.println("virar_direita");
							ChangeDirection(CurrentX, CurrentY, Position);
							t.repaint();
							ChangeDirection(CurrentX, CurrentY, "oeste");
							t.repaint();
							ChangeDirection(CurrentX, CurrentY, "norte");
							t.repaint();
							break;
						case("oeste"):
							VirarParaAndar = new Query("virar_direita, virar_direita");
							VirarParaAndar.oneSolution();
							System.out.println("virar_direita");
							System.out.println("virar_direita");
							ChangeDirection(CurrentX, CurrentY, Position);
							t.repaint();
							ChangeDirection(CurrentX, CurrentY, "norte");
							t.repaint();
							break;
					}
					Position = "leste";
				}
				else { // Vai para Oeste
					switch(Position){
						case("norte"):
							VirarParaAndar = new Query("virar_direita, virar_direita, virar_direita");
							VirarParaAndar.oneSolution();
							System.out.println("virar_direita");
							System.out.println("virar_direita");
							System.out.println("virar_direita");
							ChangeDirection(CurrentX, CurrentY, Position);
							t.repaint();
							ChangeDirection(CurrentX, CurrentY, "leste");
							t.repaint();
							ChangeDirection(CurrentX, CurrentY, "sul");
							t.repaint();
							break;
						case("sul"):
							VirarParaAndar = new Query("virar_direita");
							VirarParaAndar.oneSolution();
							System.out.println("virar_direita");
							ChangeDirection(CurrentX, CurrentY, Position);
							t.repaint();
							break;
						case("leste"):
							VirarParaAndar = new Query("virar_direita, virar_direita");
							VirarParaAndar.oneSolution();
							System.out.println("virar_direita");
							System.out.println("virar_direita");
							ChangeDirection(CurrentX, CurrentY, Position);
							t.repaint();
							ChangeDirection(CurrentX, CurrentY, "sul");
							t.repaint();
							break;
					}
					Position = "oeste";
				}
			}
			Query Andar = new Query("andar");
			Andar.oneSolution();
			System.out.println("andar");
			
			Query GetChangedPos = new Query("posicao(X, Y, P)");
			solution = (HashMap) GetChangedPos.oneSolution();
			if (solution != null){
				NewX = Integer.parseInt(solution.get("X").toString());
				NewY = Integer.parseInt(solution.get("Y").toString());
				String NewPosition = solution.get("P").toString();
				Walk(CurrentX, CurrentY, NewX, NewY);
				CurrentX = NewX;
				CurrentY = NewY;
				System.out.println(NewX + "," + NewY + "," + NewPosition);
			}
			
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private List<Position> RecuperaCaminho(String SelectedX, String SelectedY, int CurrentX, int CurrentY){
		HashMap solution;
		HashMap[] solutions;
		List<Position> Positions = new ArrayList<Position>();
		
		Query BuscarAdjVisitado = new Query("adjacente_q(" + SelectedX + "," + SelectedY + ",AX,AY), conhecimento(AX,AY,_,1)");
		solutions = (HashMap[]) BuscarAdjVisitado.allSolutions();
		SelectedX = ""; SelectedY = "";
		int XDistance = 99; int YDistance = 99; int TotalDistance = 99;
		while(true){
			String CurrX = "", CurrY = ""; TotalDistance = 99;
			if(solutions != null){
				for(int i = 0; i< solutions.length; i++){
					CurrX = solutions[i].get("AX").toString();
					CurrY = solutions[i].get("AY").toString();
					if(Integer.parseInt(CurrX) == CurrentX && Integer.parseInt(CurrY) == CurrentY)
						break;
					XDistance = Math.abs(Integer.parseInt(CurrX) - CurrentX);
					YDistance = Math.abs(Integer.parseInt(CurrY) - CurrentY);
					int CurrTotalDistance = XDistance + YDistance;
					if((SelectedX == "" && SelectedY == "") || TotalDistance >= CurrTotalDistance){
						SelectedX = CurrX;
						SelectedY = CurrY;
						TotalDistance = CurrTotalDistance;
					}
				}
			}
			if(Integer.parseInt(CurrX) == CurrentX && Integer.parseInt(CurrY) == CurrentY)
				break;
			Positions.add(new Position(Integer.parseInt(SelectedX), Integer.parseInt(SelectedY)));
			BuscarAdjVisitado = new Query("adjacente_q(" + SelectedX + "," + SelectedY + ",AX,AY), conhecimento(AX,AY,_,1)");
			solutions = (HashMap[]) BuscarAdjVisitado.allSolutions();
		}
		return Positions;
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
				case("Classes.Friend"):
					if(prioridade < 2)
						Sentido = "amigo";
					prioridade = 1;
					break;
					case("Classes.Foe"):
						if(prioridade < 4){
							if(((Foe) AdjTile).GetIsTp()){
								Sentido = "teletransportador";
								prioridade = 3;
							} else if(prioridade < 3){
								Sentido = "monstro";
								prioridade = 2;
							}
						}
						break;
					case("Classes.Hole"):
						Sentido = "brisa";
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
		try {
			ReadPrologTiles();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Query q1 = new Query("consult", new Term[] {new Atom("Prolog/Tiles.pl")});
		System.out.println("consult " + (q1.hasSolution() ? "succeeded" : "failed"));	
		Query q2 = new Query("consult", new Term[] {new Atom("Prolog/MainProlog.pl")});
		System.out.println("consult " + (q2.hasSolution() ? "succeeded" : "failed"));	
		
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
			        	case('U'):
			        		newEntity = new PowerUp("Images/Banana.png");
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
			        	case('d'):
			        		newEntity = new Foe("Images/Bee.png", 20, 100, false);
			        		break;
			        	case('D'):
			        		newEntity = new Foe("Images/Troll.png", 50, 100, false);
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

	private static void ReadPrologTiles() throws IOException{
		PrintWriter writer = new PrintWriter("Prolog/Tiles.pl", "UTF-8");
		BufferedReader br = new BufferedReader(new FileReader("Map/Map.txt"));
	    String line = br.readLine();
	    int lineCounter = 0;
	    int friendsNumber = 0;
	    writer.println(":-dynamic tile/3.");
	    
	    while (line != null) {
	    	int columnCounter = 0;
	    	for (char ch: line.toCharArray()) {
	    		switch(ch)
		        {
	    			case('X'):
	    				writer.println("tile(" + Integer.toString(columnCounter) + "," + Integer.toString(lineCounter) + ",nada)." );
	    				break;
		        	case('.'):
		        		writer.println("tile(" + Integer.toString(columnCounter) + "," + Integer.toString(lineCounter) + ",nada)." );
		        		break;
		        	case('O'):
	        			writer.println("tile(" + Integer.toString(columnCounter) + "," + Integer.toString(lineCounter) + ",amigo)." );
		        		break;
		        	case('P'):
		        		writer.println("tile(" + Integer.toString(columnCounter) + "," + Integer.toString(lineCounter) + ",buraco)." );
		        		break;
		        	case('T'):
		        		writer.println("tile(" + Integer.toString(columnCounter) + "," + Integer.toString(lineCounter) + ",teletransporte)." );
		        		break;
		        	case('U'):
		        		writer.println("tile(" + Integer.toString(columnCounter) + "," + Integer.toString(lineCounter) + ",powerup)." );
		        		break;
		        	case('D'):
		        		writer.println("tile(" + Integer.toString(columnCounter) + "," + Integer.toString(lineCounter) + ",monstro)." );
		        		break;	
		        	case('d'):
		        		writer.println("tile(" + Integer.toString(columnCounter) + "," + Integer.toString(lineCounter) + ",monstro)." );
		        		break;
		        }		    		
	    		
	    		columnCounter++;
	    	}
    		line = br.readLine();
	    	lineCounter++;
	    }
	    		    
	    br.close();
		writer.close();
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