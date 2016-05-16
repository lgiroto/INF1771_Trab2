package Classes;
public class Position {

	private int X;
	private int Y;
	
	public Position(int XValue, int YValue){
		X = XValue;
		Y = YValue;
	}
	
	public int getX(){
		return X;
	}
	public int getY(){
		return Y;
	}
	public void setX(int newX){
		X = newX;
	}
	public void setY(int newY){
		Y = newY;
	}
	
}
