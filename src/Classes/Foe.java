package Classes;

public class Foe extends Entity {

	private int Damage;
	private int Life;
	private boolean IsTeleporter;
	
	public Foe(String imgPath, int setDamage, int setLife, boolean setIsTp)
	{
		super(imgPath);
		Damage = setDamage;
		Life = setLife;
		IsTeleporter = setIsTp;
	}
	
	public void SetLife(int Damage){
		Life = (Life - Damage < 0) ? 0 : Life - Damage;
	}
	
	public int GetDamage(){
		return Damage;
	}
	
	public int GetLife(){
		return Life;
	}
	
	public boolean GetIsTp(){
		return IsTeleporter;
	}
	
}

