import javax.swing.*;

import java.awt.*;

public class GameWindow extends JFrame
{
	public final int LARG_DEFAULT=1360;
	public final int ALT_DEFAULT=768;
	public JButton Iniciar = new JButton("Iniciar o Jogo");
	private JPanel p;
		
	public GameWindow()
	{
		// Centraliza a janela.
		Toolkit tk=Toolkit.getDefaultToolkit();
		Dimension screenSize=tk.getScreenSize();
		int sl=screenSize.width;
		int sa=screenSize.height;
		int x=sl/2-LARG_DEFAULT/2;
		int y=sa/2-ALT_DEFAULT/2;
		
		setBounds(x,y,LARG_DEFAULT,ALT_DEFAULT);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH); 
					
		// Adiciona o Painel
		p = new JPanel();
		p.setLayout(null);
		Iniciar.setBounds(200,200,400,50);
		p.add(Iniciar);
		getContentPane().add(p);
	}
	
	public void loadTabuleiro(Interface tabuleiro) {
		p.remove(Iniciar);
		tabuleiro.setBounds(100,50,1360,1000);
		p.add(tabuleiro);
		tabuleiro.repaint();
	}

}