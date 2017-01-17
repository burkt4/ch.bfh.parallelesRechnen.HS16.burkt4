import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainStackElement implements Serializable  {
	//Comment
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Node node;
	private List<Node> localStack;
	
	public MainStackElement(Node root){
		localStack = new ArrayList<Node>();
		node = root;
	}
	
	public void push(Node node){
		localStack.add(node);
	}
	
	public Node pop(){
		if( isEmpty()){
			return null;
		}
		Node node = localStack.get(localStack.size() - 1);
		localStack.remove(localStack.size() - 1);
		return node;
	}
	
	public boolean isEmpty(){
		if (localStack.size() == 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	public void expand(){
		List<Node> children = node.getAllChildNodes();
		for (Node child : children){
			localStack.add(child);
		}
		
	}
	

	
	public Node getRootElement(){
		return node;
	}

	public MainStackElement split() {
		MainStackElement splitElement = new MainStackElement(node);
		List<Node> remainingList = new ArrayList<Node>();
		int i = 0;
		for (Node node : localStack){
			if(i % 2 == 0){
				splitElement.push(node);
			}
			else{
				remainingList.add(node);
			}
			i++;
		}
		localStack = remainingList;
		return splitElement;
	}

}
	