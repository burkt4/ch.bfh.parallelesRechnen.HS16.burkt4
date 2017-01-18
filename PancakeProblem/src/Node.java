import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a stack of pancakes
 * @author t.buerk
 *
 */
public class Node implements Serializable  {
	//Comment
	private List<Integer> pancakes;
	private int numberOfMovesDone;
	private int previousFlip;
	
	public Node(List<Integer> pancakes, int numberOfMovesDone, int previousFlip){
		
		this.pancakes = pancakes;
		this.numberOfMovesDone = numberOfMovesDone;
		this.previousFlip = previousFlip;
	}
	/**
	 * Get the optimistic distance to the solution with gap heuristic
	 * @return
	 */
	public int getOptimisticDistanceToSolution(){
		int distance = 0;
		
		for(int i = 0; i < pancakes.size(); i++ ){
			int gap;
			if( !(i + 1 == pancakes.size())){
				gap =  pancakes.get(i) - pancakes.get(i + 1);				
			}
			else{
				gap =  pancakes.get(i) - pancakes.size() + 1;
			}
			if( !(gap == 1  || gap == -1 )){
				distance++;
			}
		}
		
		return distance;
	}
	
	public String toString(){
		
		String output = "";
		
		for (int i = 0; i < pancakes.size() ; i++){
			if(output == ""){
				output = "Stack: " + pancakes.get(i);
			}
			else{
				output += 	" " + pancakes.get(i);
			}
			if(i == previousFlip){
				output += 	" |" ;
			}
		}
		return output;
		
	}
	
	/**
	 * Preforms the flip operation on the current stack of pancake at the given position
	 * @param position
	 * @return
	 */
	public Node flip(int position){
		List<Integer> newStack = new ArrayList<Integer>();
		
		//add Elements from 0 to flip in reverse order
		for (int i = position; i >= 0 ; i--){
			newStack.add(pancakes.get(i));
		}
		
		//add the rest in same order
		for (int i = position + 1; i < pancakes.size() ; i++){
			newStack.add(pancakes.get(i));
		}
		
		Node newNode = new Node(newStack, numberOfMovesDone + 1, position);
		return newNode;
	}
	
	/**
	 * Generate a list of all child nodes that have a chance to lead to an optimal solution
	 * @return
	 */
	public List<Integer> getAllChildNodes(){
		List<Integer> children = new ArrayList<Integer>();
		
		//fliping the top most pancake is  ignored, since it does not change the order of the pancakes
		for(int i = 1; i < pancakes.size(); i++ ){
			//reversing the last flip is ignored
			if ( !(previousFlip == i)){
				children.add(i);
			}			
		}
		
		return children;
		
	}
	
	public int getNumberOfMovesDone(){
		return numberOfMovesDone;
	}
	
	/**
	 * Returns whether the current order of pancakes is a solution
	 * @return
	 */
	public boolean isSolution(){
		if (this.getOptimisticDistanceToSolution() == 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	public int getPreviousFlip(){
		return previousFlip;
	}

}
