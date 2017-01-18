import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a main stack element 
 * @author t.buerk
 *
 */
public class MainStackElement implements Serializable  {

	private static final long serialVersionUID = 1L;
	private Node node;
	private List<Integer> localStack;
	
	public MainStackElement(Node root){
		localStack = new ArrayList<Integer>();
		node = root;
	}
	
	public void push(Integer flip){
		localStack.add(flip);
	}
	

	public Node pop(){
		if( isEmpty()){
			return null;
		}
		int flip = localStack.get(localStack.size() - 1);
		localStack.remove(localStack.size() - 1);
		
		return node.flip(flip);
	}
	
	public boolean isEmpty(){
		if (localStack.size() == 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * expands the current node and adds the child nodes to the localstack
	 */
	public void expand(){
		List<Integer> children = node.getAllChildNodes();
		for (int flip : children){
			localStack.add(flip);
		}
		
	}
	

	
	public Node getRootElement(){
		return node;
	}

	/**
	 * The MainElement creates a copy of itself and distributes the elements in the local stack among itself 
	 * and the copy and then returns the copy
	 * @return
	 */
	public MainStackElement split() {
		//makes a copy of itself
		MainStackElement splitElement = new MainStackElement(node);
		//distributes remaining child nodes among itself and the copy
		List<Integer> remainingList = new ArrayList<Integer>();
		int i = 0;
		for (int flip : localStack){
			if(i % 2 == 0){
				splitElement.push(flip);
			}
			else{
				remainingList.add(flip);
			}
			i++;
		}
		localStack = remainingList;
		return splitElement;
	}

}
	