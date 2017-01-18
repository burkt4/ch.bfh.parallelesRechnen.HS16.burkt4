import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class represents the SearchTree for the pancake problem
 * @author t.buerk
 *
 */
public class SearchStack implements Serializable {

	private static final long serialVersionUID = 1L;
	List<MainStackElement> mainSearchStack;
	private int minimumDistanceToSolution;
	private boolean isSolution;
	private int bound;
	
	public SearchStack(int minimumDistanceToSolution,int bound){
		this.minimumDistanceToSolution = minimumDistanceToSolution;
		this.bound = bound;
		isSolution = false;
		mainSearchStack = new ArrayList<MainStackElement>();
	}
	
	public SearchStack(Node root, int bound){
		minimumDistanceToSolution = Integer.MAX_VALUE;
		this.bound = bound;
		isSolution = false;
		mainSearchStack = new ArrayList<MainStackElement>();
		MainStackElement mainRoot = new MainStackElement(root);
		mainRoot.expand();
		mainSearchStack.add(mainRoot);
	}
	
	public void push(MainStackElement newElement){
		
		mainSearchStack.add(newElement);
	}
	
	public MainStackElement pop(){
		
			if ( isEmpty()){
				return null;
			}
			MainStackElement topMainElement =  mainSearchStack.get(mainSearchStack.size() -1);
			mainSearchStack.remove(mainSearchStack.size() -1);	
			return topMainElement;
	}
	
	public MainStackElement peek(){
		
		if ( isEmpty()){
			return null;
		}
		MainStackElement topMainElement =  mainSearchStack.get(mainSearchStack.size() -1);
	
		return topMainElement;
	}
	
	public boolean isEmpty(){
		if (mainSearchStack.size() == 0){
			return true;
		}
		else{
			return false;
		}
	}
	

	/**
	 * gets the current Path of the main stack as a List of Nodes
	 * @return
	 */
	public List<Node> getPath(){
		ArrayList<Node> mainPathList = new ArrayList<Node>();
		for (MainStackElement element : mainSearchStack){
			mainPathList.add(element.getRootElement());
		}
		return mainPathList;
	}
	
	public int getMinimumDistanceToSolution(){
		return minimumDistanceToSolution;
	}
	
	public void setMinimumDistanceToSolution(int minimumDistanceToSolution){
		this.minimumDistanceToSolution = minimumDistanceToSolution;
	}
	
	public int getBound(){
		return bound;
	}
	
	public void setBound(int bound){
		this.bound = bound;
	}
	
	public boolean isSolution(){
		return isSolution;
	}
	
	public void setIsSolution(boolean isSolution){
		this.isSolution = isSolution;
	}
	
	/**
	 * Splits the SearchStack an returns one half of the Stack as a new SearchStack
	 * @return
	 */
	public SearchStack split(){
		SearchStack splitStack = new SearchStack(minimumDistanceToSolution, bound);
		//split each MainElement and add split off MainElement to the splitStack
		for (MainStackElement element : mainSearchStack){
			splitStack.push(element.split());
		}
		
		return splitStack;
	}

}
