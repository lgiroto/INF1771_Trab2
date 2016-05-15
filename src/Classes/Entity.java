package Classes;
public class Entity {

	private String ImagePath;
	private boolean Visited;
	
	public Entity(String imgPath){
		ImagePath = imgPath;
	}
	
	public void SetImgPath(String Path){
		ImagePath = Path;
	}
	
	public String getImgPath(){
		return ImagePath;
	}
	
	public void SetVisited(){
		Visited = true;
	}
	
	public boolean GetVisited(){
		return Visited;
	}
	
}
