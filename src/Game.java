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
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

public class Game
{
	private Interface t;
	private GameWindow j;
	private static Entity[][] Tiles = new Entity[12][12];
	private boolean GameOver = false;
	private int FriendsSaved = 0;
	private boolean isAttacking = false;
	private boolean wasPowerUp = false;
	private int PUX = 0, MX = 0;
	private int PUY = 0, MY = 0;
	private boolean wasOnMonster = false;
	private String MonsterImage = "";
	
	@SuppressWarnings("rawtypes")
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
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// Verifica Amigos Salvos
			int FriendsSoFar = FriendsSaved;
			
			// Pegar Vida
			Query PegarVida = new Query("vida(V)");
			solution = (HashMap) PegarVida.oneSolution();
			int Vida = Integer.parseInt(solution.get("V").toString());
			t.DonkeyEnergy = Vida;
			
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
				System.out.println("Posição: " + CurrentX + "," + CurrentY + "," + Position);
			}
			
			// Verifica Vitória
			if(FriendsSoFar == 3 && CurrentX == 1 && CurrentY == 11){
				t.CustoTotal--;
				t.GameWon = true;
				t.repaint();
				break;
			}
			
			if(Tiles[CurrentX][CurrentY].getClass().getName().equals("Classes.Foe")){
				if(((Foe) Tiles[CurrentX][CurrentY]).GetIsTp()){
					Random RandomGenerator = new Random();
					int RandomX = RandomGenerator.nextInt(11);
					int RandomY = RandomGenerator.nextInt(11);
					
					Walk(CurrentX, CurrentY, RandomX, RandomY);
					Tiles[CurrentX][CurrentY].SetImgPath("Images/Teleporter.png");
					CurrentX = RandomX; CurrentY = RandomY;
					
					Query Teleport = new Query("teletransportar(" + CurrentX + "," + CurrentY + ")");
					Teleport.oneSolution();
					System.out.println("teletransportar(" + CurrentX + "," + CurrentY + ")");
					
					switch(Tiles[CurrentX][CurrentY].getClass().getName()){
						case("Classes.Hole"):
							t.CustoTotal -= 1000;
							GameOver = true;
							t.GameOver = true;
							break;
					}
					continue;
				} else {
					wasOnMonster = true;
					MonsterImage = ((Foe) Tiles[CurrentX][CurrentY]).getImgPath();
					MX = CurrentX; MY = CurrentY;
					int damage = ((Foe) Tiles[CurrentX][CurrentY]).GetDamage();
					t.DonkeyEnergy = t.DonkeyEnergy - damage;
					t.CustoTotal -= damage;
				}
			}
			
			// Observa Adjacentes e Amplifica Conhecimento
			ObservaAdjacentes(CurrentX, CurrentY);
			
			// Realiza Ação Sugerida; Andar, Virar à Direita, Atacar
			Query q5 = new Query("acao(X)");
			solution = (HashMap) q5.oneSolution();
			if(solution != null){
				String Action = solution.get("X").toString();
				System.out.println(Action);
				
				// Verifica Mudança Imagem DK
				if(!Action.equals("atacar") && isAttacking){
					isAttacking = false;
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
				
				Query ActionQuery = new Query(Action);			
				if(Action.equals("escapar")){
					List<Position> Positions = new ArrayList<Position>(24);
					Positions = RecuperaCaminho("1", "11", CurrentX, CurrentY);					
					RealizaBusca(Positions, CurrentX, CurrentY, Position);
				}
				else if(Action.equals("salvar_amigo")){
					ActionQuery.oneSolution();
					t.CustoTotal = t.CustoTotal + 1000;
					FriendsSaved++;
				}
				else if(Action.equals("guardar_powerup")){
					ActionQuery.oneSolution();
					wasPowerUp = true;
					PUX = CurrentX; PUY = CurrentY;
				}
				else if(Action.equals("pegar_powerup")){
					ActionQuery.oneSolution();
					t.CustoTotal--;
				}
				else if(Action.equals("virar_direita")){
					ActionQuery.oneSolution();
					t.CustoTotal--;
					ChangeDirection(CurrentX, CurrentY, Position);
				}
				else if(Action.equals("andar")){
					ActionQuery.oneSolution();
					t.CustoTotal--;
					Query GetChangedPos = new Query("posicao(X, Y, P)");
					solution = (HashMap) GetChangedPos.oneSolution();
					if (solution != null){
						NewX = Integer.parseInt(solution.get("X").toString());
						NewY = Integer.parseInt(solution.get("Y").toString());
						Walk(CurrentX, CurrentY, NewX, NewY);
					}
				}
				else if(Action.equals("atacar")){
					ActionQuery.oneSolution();
					isAttacking = true; t.CustoTotal -= 10;
					
					int AdjX = 0, AdjY = 0;
					switch(Position){
						case("norte"):
							Tiles[CurrentX][CurrentY].SetImgPath("Images/DK_Attacking_North.png");
							AdjX = CurrentX; AdjY = CurrentY - 1;
							break;
						case("sul"):
							Tiles[CurrentX][CurrentY].SetImgPath("Images/DK_Attacking_South.png");
							AdjX = CurrentX; AdjY = CurrentY + 1;
							break;
						case("leste"):
							Tiles[CurrentX][CurrentY].SetImgPath("Images/DK_Attacking_Right.png");
							AdjX = CurrentX + 1; AdjY = CurrentY;
							break;
						case("oeste"):
							Tiles[CurrentX][CurrentY].SetImgPath("Images/DK_Attacking_Left.png");
							AdjX = CurrentX - 1; AdjY = CurrentY;
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
							Query MatarMonstro = new Query("ouve_grito(" + AdjX + "," + AdjY + ")");
							System.out.println("ouve_grito(" + AdjX + "," + AdjY + ")");
							MatarMonstro.oneSolution();
							Tiles[AdjX][AdjY] = new Grass("Images/Grass.png");
						}
					} else{
						Query AtualizarConhecimento = new Query("atualizar_conhecimento(" + AdjX + "," + AdjY + ",nada)");
						System.out.println("atualizar_conhecimento(" + AdjX + "," + AdjY + ",nada)");
						solution = (HashMap) AtualizarConhecimento.oneSolution();	
					}
				}
				else if(Action.equals("buscar_amigo") || Action.equals("buscar_livre") ||
						Action.equals("buscar_monstro") || Action.equals("buscar_teletransporte")){
					List<Position> Positions = new ArrayList<Position>(24);
					String SelectedX = "", SelectedY = "";
					int XDistance = 99; int YDistance = 99; int TotalDistance = 99;
					
					if(Action.equals("buscar_amigo")){
						ActionQuery = new Query("buscar_amigo(X,Y)");
						System.out.println("buscar_amigo(X,Y)");
					} else if(Action.equals("buscar_livre")){
						ActionQuery = new Query("buscar_livre(X,Y)");
						System.out.println("buscar_livre(X,Y)");					
					} else if(Action.equals("buscar_monstro")){
						ActionQuery = new Query("buscar_monstro(X,Y)");
						System.out.println("buscar_monstro(X,Y)");					
					} else{
						ActionQuery = new Query("buscar_teletransporte(X,Y)");
						System.out.println("buscar_teletransporte(X,Y)");
					}

					solutions = (HashMap[]) ActionQuery.allSolutions();	
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
					Positions = RecuperaCaminho(SelectedX, SelectedY, CurrentX, CurrentY);					
					RealizaBusca(Positions, CurrentX, CurrentY, Position);
				}
				
				if(!Action.equals("guardar_powerup") && wasPowerUp){
					wasPowerUp = false;
					Tiles[PUX][PUY].SetImgPath("Images/Banana.png");
				} else if(wasOnMonster){
					wasOnMonster = false;
					Tiles[MX][MY].SetImgPath(MonsterImage);
				}
			}
			t.repaint();
		}
	}
	
	private void RealizaBusca(List<Position> Positions, int CurrentX, int CurrentY, String Position){
		HashMap solution;
		HashMap[] solutions;
		int NewX, NewY;
		for (int i=0; i < Positions.size(); i++)
		{
			int DestinyX = Positions.get(i).getX();
			int DestinyY = Positions.get(i).getY();
			Query VirarParaAndar;
			if(DestinyX - CurrentX == 0){
				if(DestinyY - CurrentY > 0){ // Vai pro Sul
					switch(Position){
						case("norte"):
							VirarParaAndar = new Query("virar_direita, virar_direita");
							VirarParaAndar.oneSolution(); t.CustoTotal -= 2;
							System.out.println("virar_direita");
							System.out.println("virar_direita");
							ChangeDirection(CurrentX, CurrentY, Position);
							t.repaint();
							ChangeDirection(CurrentX, CurrentY, "leste");
							t.repaint();
							break;
						case("leste"):
							VirarParaAndar = new Query("virar_direita");
							VirarParaAndar.oneSolution(); t.CustoTotal --;
							ChangeDirection(CurrentX, CurrentY, Position);
							t.repaint();
							break;
						case("oeste"):
							VirarParaAndar = new Query("virar_direita, virar_direita, virar_direita");
							VirarParaAndar.oneSolution(); t.CustoTotal -= 3;
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
							VirarParaAndar.oneSolution(); t.CustoTotal -= 2;
							System.out.println("virar_direita");
							System.out.println("virar_direita");
							ChangeDirection(CurrentX, CurrentY, Position);
							t.repaint();
							ChangeDirection(CurrentX, CurrentY, "oeste");
							t.repaint();
							break;
						case("oeste"):
							VirarParaAndar = new Query("virar_direita");
							VirarParaAndar.oneSolution(); t.CustoTotal --;
							System.out.println("virar_direita");
							ChangeDirection(CurrentX, CurrentY, Position);
							t.repaint();
							break;
						case("leste"):
							VirarParaAndar = new Query("virar_direita, virar_direita, virar_direita");
							VirarParaAndar.oneSolution(); t.CustoTotal -= 3;
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
							VirarParaAndar.oneSolution(); t.CustoTotal--;
							System.out.println("virar_direita");
							ChangeDirection(CurrentX, CurrentY, Position);
							t.repaint();
							break;
						case("sul"):
							VirarParaAndar = new Query("virar_direita, virar_direita, virar_direita");
							VirarParaAndar.oneSolution(); t.CustoTotal -= 3;
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
							VirarParaAndar.oneSolution(); t.CustoTotal -= 2;
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
							VirarParaAndar.oneSolution(); t.CustoTotal -= 3;
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
							VirarParaAndar.oneSolution(); t.CustoTotal --;
							System.out.println("virar_direita");
							ChangeDirection(CurrentX, CurrentY, Position);
							t.repaint();
							break;
						case("leste"):
							VirarParaAndar = new Query("virar_direita, virar_direita");
							VirarParaAndar.oneSolution(); t.CustoTotal -= 2;
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
			Andar.oneSolution(); t.CustoTotal --;
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
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private List<Position> RecuperaCaminho(String SelectedX, String SelectedY, int CurrentX, int CurrentY){
		HashMap solution;
		HashMap[] solutions;
		List<Position> Positions = new ArrayList<Position>(24);
		
		int TotalXDist = Math.abs(CurrentX - Integer.parseInt(SelectedX));
		int TotalYDist = Math.abs(CurrentY - Integer.parseInt(SelectedY));
		
		Query BuscarAdjVisitado = new Query("adjacente(" + CurrentX + "," + CurrentY + ",AX,AY), conhecimento(AX,AY,_,1)");
		solutions = (HashMap[]) BuscarAdjVisitado.allSolutions();
		String NextPosX = ""; String NextPosY = "";
		int XDistance = 99; int YDistance = 99; int TotalDistance = 99;
		while(true){
			String CurrX = "", CurrY = ""; TotalDistance = 99;
			int ShortestX = 99; int ShortestY = 99;
			if(solutions != null){
				for(int i = 0; i< solutions.length; i++){
					CurrX = solutions[i].get("AX").toString();
					CurrY = solutions[i].get("AY").toString();
							
					if(Integer.parseInt(CurrX) == CurrentX && Integer.parseInt(CurrY) == CurrentY)
						continue;
					
					Query AdjCurr = new Query("adjacente(" + CurrX + "," + CurrY + ",AX,AY), (conhecimento(AX,AY,_,1);(AX="+SelectedX+",AY="+SelectedY+"))");
					int AdjQtd = AdjCurr.allSolutions().length;
					if(AdjQtd <= 1)
						continue;
					else if(!NextPosX.equals("")){						
						int searchX = Integer.parseInt(CurrX);
						int searchY = Integer.parseInt(CurrY);
						List<Position> Pos = Positions.stream().filter((pos) -> pos.getX() == searchX && pos.getY() == searchY)
															   .collect(Collectors.toList());	
						if(Pos != null && !Pos.isEmpty())
							continue;
					}
					
					XDistance = Math.abs(Integer.parseInt(CurrX) - Integer.parseInt(SelectedX));
					YDistance = Math.abs(Integer.parseInt(CurrY) - Integer.parseInt(SelectedY));
					int CurrTotalDistance = XDistance + YDistance;
					
					if(NextPosX.equals("") && NextPosY.equals("")){
						NextPosX = CurrX; ShortestX = XDistance;
						NextPosY = CurrY; ShortestY = YDistance;
						TotalDistance = CurrTotalDistance;
					}
					
					if(TotalDistance >= CurrTotalDistance){
						if(TotalXDist == 0 && ShortestY <= YDistance)
							continue;
						else if(TotalYDist == 0 && ShortestX <= XDistance)
							continue;
						NextPosX = CurrX; ShortestX = XDistance;
						NextPosY = CurrY; ShortestY = YDistance;
						TotalDistance = CurrTotalDistance;
					}
				}
			}
			
			if(!NextPosX.equals("") && !NextPosY.equals("") ){
				Position NewPosition = new Position(Integer.parseInt(NextPosX), Integer.parseInt(NextPosY));
				Positions.add(NewPosition);				
			} else
				break;
			
			XDistance = Math.abs(Integer.parseInt(NextPosX) - Integer.parseInt(SelectedX));
			YDistance = Math.abs(Integer.parseInt(NextPosY) - Integer.parseInt(SelectedY));
			if(XDistance + YDistance <= 1)
				break;
			
			BuscarAdjVisitado = new Query("adjacente(" + NextPosX + "," + NextPosY + ",AX,AY), conhecimento(AX,AY,_,1)");
			solutions = (HashMap[]) BuscarAdjVisitado.allSolutions();
		}
		return Positions;
	}
	
	private void ObservaAdjacentes(int CurrentX, int CurrentY){
		HashMap solution;
		HashMap[] solutions;
		
		Query VerificarObs = new Query("verificar_obs");
		if(!VerificarObs.hasSolution()) return;
		
		Query GetAdjacent = new Query("adjacente(" + CurrentX + "," + CurrentY + ",AX,AY)");
		solutions = (HashMap[]) GetAdjacent.allSolutions();
		System.out.println("adjacente(" + CurrentX + "," + CurrentY + ")");
		
		if(solutions != null && solutions.length > 0){
			String Conhecimentos = "nada";
			List<String> Observacoes = new ArrayList<String>();
			
			for(int i = 0; i < solutions.length; i++){
				int AdjX = Integer.parseInt(solutions[i].get("AX").toString());
				int AdjY = Integer.parseInt(solutions[i].get("AY").toString());

				Query ObservaAdj = new Query("observar(" + AdjX + "," + AdjY + ",O)");
				solution = (HashMap) ObservaAdj.oneSolution();
				System.out.println("observar(" + AdjX + "," + AdjY + ",O)");
				
				String Observado = solution.get("O").toString();
				if(!Observado.equals("nada")){
					Observacoes.add(Observado);
					
					if(Conhecimentos.equals("nada"))
						Conhecimentos = Observado;
					else if(Conhecimentos.toLowerCase().contains(Observado.toLowerCase()))
						continue;
					else{
						Conhecimentos = "[";
						for(int j = 0; j < Observacoes.size(); j++){
							String Obs = Observacoes.get(j);
							Conhecimentos = Conhecimentos + Obs;
							Conhecimentos = Conhecimentos + ",";
						}
						Conhecimentos = Conhecimentos.substring(0, Conhecimentos.length()-1);
						Conhecimentos = Conhecimentos + "]";
					}
				}
			}
			for(int i = 0; i < solutions.length; i++){
				int AdjX = Integer.parseInt(solutions[i].get("AX").toString());
				int AdjY = Integer.parseInt(solutions[i].get("AY").toString());
				Query AddConhecimento = new Query("atualizar_conhecimento(" + AdjX + "," + AdjY + "," + Conhecimentos + ")");
				solution = (HashMap) AddConhecimento.oneSolution();						
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