import java.util.ArrayList;
import java.util.List;

import mpi.MPI;

public class Sequentiell {
	static int numberOfPancakes = 17;
	static boolean random = false;
	static int mode = 1;
	
	public static void main(String[] args) {
		int rank, size;
		MPI.Init(args);

		rank = MPI.COMM_WORLD.Rank();
		if (rank == 0){
			
			List<Integer> list = new ArrayList<Integer>();
			if (random){
				for (int i = 0; i < numberOfPancakes;i++){
					list.add(i + 1);
				}		
				java.util.Collections.shuffle(list);
			}
			else{

				if (numberOfPancakes % 2 == 0){
					for (int i = 0; i < numberOfPancakes;i++){
						if( i % 2 == 0){
							list.add(i + 2);
						}
						else{
							list.add(i);
						}
						
					}
				}
				else{
					for (int i = 0; i < numberOfPancakes - 1;i++){
						if( i % 2 == 0){
							list.add(i + 2);
						}
						else{
							list.add(i);
						}
						
					}
					list.add(numberOfPancakes);
				}

			}
			Node root = new Node(list, 0);
			
			System.out.println("Start:");
			System.out.println(root.toString());
			System.out.println("Optimistic Distance To Solution: " + root.getOptimisticDistanceToSolution());
			
			long starttime = System.currentTimeMillis();			
			if (mode == 1){

				SearchStack result = solveMode1(root);
					
				if (result != null){
					System.out.println("Result:");
					for (Node node : result.getPath()){		
						System.out.println(node.toString());
					}

				}
				else{
					System.out.println("Result: not found!");
				}

			}
			else{
				int result = solveMode2(root);
				
				if (result != 0){
					System.out.println("Result:");
	
					System.out.println("number of solutions: " + result);


				}
				else{
					System.out.println("Result: not found!");
				}
			}
			long endtime = System.currentTimeMillis();
			System.out.println("time: " + ((endtime - starttime)/1000.0) + " sec");
		}
		
		 
		
		
		MPI.Finalize();      
	}
	
	
	public static SearchStack solveMode1(Node root){
		SearchStack solutionStack = null;
		int bound =  root.getOptimisticDistanceToSolution();
		
		int maxBound = bound * 10;
		
		while (solutionStack == null){
			SearchStack stack = new SearchStack(root, bound);
			System.out.println("Started new with run with bound: " + bound);
			
			while(!stack.isEmpty()){
				stack = search(stack, bound, 100);
				
				if (stack.isSolution()) {
					solutionStack = stack;
					break;
				}
				if (stack.getMinimumDistanceToSolution() >= maxBound){
					return null;
				}
				//System.out.println("Taking a quick break!");
			}

			bound = stack.getMinimumDistanceToSolution();
			
		}
		
		return solutionStack;
	}
	
	public static int solveMode2(Node root){
		int bound =  root.getOptimisticDistanceToSolution();
		
		int maxBound = bound * 10;
		int numberOfSolutions = 0;
		
		while (numberOfSolutions == 0){
			SearchStack stack = new SearchStack(root, bound);
			System.out.println("Started new with run with bound: " + bound);

			while(!stack.isEmpty()){
				stack = search(stack, bound, 100);
				
				if (stack.isSolution()) {
					stack.setIsSolution(false);
					stack.pop();
					numberOfSolutions++;
					System.out.println("Current number of solutions: " + numberOfSolutions);
				}
				if (stack.getMinimumDistanceToSolution() >= maxBound){
					return 0;
				}
				//System.out.println("Taking a quick break!");
			}

			bound = stack.getMinimumDistanceToSolution();
			
		}
		
		return numberOfSolutions;
	}
	
public static SearchStack search(SearchStack stack, int bound, int maxNumberOfExpands){
	
	MainStackElement currentMainStackElement = stack.peek();
	int numberOfExpanse = 0;
	
	while (currentMainStackElement != null){
				
		while (!currentMainStackElement.isEmpty()){
			Node currentNode = currentMainStackElement.pop();
					
			int minimumDistanceToSolution = currentNode.getNumberOfMovesDone() + currentNode.getOptimisticDistanceToSolution();
			if (minimumDistanceToSolution >  bound){
				if(stack.getMinimumDistanceToSolution() > minimumDistanceToSolution){
					stack.setMinimumDistanceToSolution(minimumDistanceToSolution);
				}
			}
			else{
				currentMainStackElement = new MainStackElement(currentNode);
				currentMainStackElement.expand();
				stack.push(currentMainStackElement);
				numberOfExpanse++;
				if(numberOfExpanse >= maxNumberOfExpands){
					return stack;
				}
				
			}
			if (currentNode.isSolution()){
				stack.setIsSolution(true);
				return stack;
			}
		}
		stack.pop();
		currentMainStackElement = stack.peek();
	}

	return stack;
	
}
	
	
	
}
