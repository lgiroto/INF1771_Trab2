package Classes;
public class Entity {

	private String ImagePath;
	
	public Entity(String imgPath){
		ImagePath = imgPath;
	}
	
	public void SetImgPath(String Path){
		ImagePath = Path;
	}
	
	public String getImgPath(){
		return ImagePath;
	}
	
}
