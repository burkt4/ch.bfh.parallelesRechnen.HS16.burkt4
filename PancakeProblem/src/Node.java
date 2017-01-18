import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Node implements Serializable  {
	//Comment
	private List<Integer> pancakes;
	private int numberOfMovesDone;
	
	public Node(List<Integer> pancakes, int numberOfMovesDone){
		
		this.pancakes = pancakes;
		this.numberOfMovesDone = numberOfMovesDone;
		
	}
	
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
		
		for (int pancake : pancakes){
			if(output == ""){
				output = "Stack: " + pancake;
			}
			else{
				output += 	", " + pancake;
			}
		}
		return output;
		
	}
	
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
		
		Node newNode = new Node(newStack, numberOfMovesDone + 1);
		return newNode;
	}
	
	public List<Node> getAllChildNodes(){
		List<Node> children = new ArrayList<Node>();
		
		for(int i = 1; i < pancakes.size(); i++ ){
			children.add(this.flip(i));
		}
		
		return children;
		
	}
	
	public int getNumberOfMovesDone(){
		return numberOfMovesDone;
	}
	
	public boolean isSolution(){
		if (this.getOptimisticDistanceToSolution() == 0){
			return true;
		}
		else{
			return false;
		}
	}

}
