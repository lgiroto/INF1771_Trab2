package Classes;

public class Foe extends Entity {

	private int Damage;
	private int Life;
	private boolean IsTeleporter;
	private boolean HasEngaged;
	
	public Foe(String imgPath, int setDamage, int setLife, boolean setIsTp)
	{
		super(imgPath);
		Damage = setDamage;
		Life = setLife;
		IsTeleporter = setIsTp;
		HasEngaged = false;
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
	
	public boolean GetEngaged(){
		return HasEngaged;
	}
	
	public void SetEngaged(){
		HasEngaged = true;
	}
}

