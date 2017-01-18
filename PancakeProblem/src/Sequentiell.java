import java.util.ArrayList;
import java.util.List;

import mpi.MPI;

public class Sequentiell {
	//Anzahl pancakes des Stacks. Der Teller wird nicht in die Anzahl einberechnet
	static int numberOfPancakes = 17;
	//Bei true wird eine zufällige Startkonfiguration gewählt. bei false werden die pancakes paarweise vertauscht
	static boolean random = false;
	//Bei 1 wird die erste optimale Lösung ausgegeben. Bei 2 werden alle optimalen Lösungen gezählt
	static int mode = 2;
	
	public static void main(String[] args) {
		int rank;
		MPI.Init(args);

		rank = MPI.COMM_WORLD.Rank();
		if (rank == 0){
			//creates a new start configuration 
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
			
			Node root = new Node(list, 0, -1);
			
			System.out.println("Start Sequentiell:");
			System.out.println("Modus: " + mode);
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
	
	/**
	 * Searches for the first optimal solution and returns it
	 * @param root
	 * @return
	 */
	public static SearchStack solveMode1(Node root){
		SearchStack solutionStack = null;
		int bound =  root.getOptimisticDistanceToSolution();
		
		int maxBound = bound * 10;
		
		while (solutionStack == null){
			SearchStack stack = new SearchStack(root, bound);
			System.out.println("Started new with run with bound: " + bound);
			
			while(!stack.isEmpty()){
				stack = search(stack, bound);
				
				if (stack.isSolution()) {
					solutionStack = stack;
					break;
				}
				if (stack.getMinimumDistanceToSolution() >= maxBound){
					return null;
				}
				
			}

			bound = stack.getMinimumDistanceToSolution();
			
		}
		
		return solutionStack;
	}
	
	/**
	 * Searches for all possible optimal solutions and returns the result
	 * @param root
	 * @return
	 */
	public static int solveMode2(Node root){
		int bound =  root.getOptimisticDistanceToSolution();
		
		int maxBound = bound * 10;
		int numberOfSolutions = 0;
		
		while (numberOfSolutions == 0){
			SearchStack stack = new SearchStack(root, bound);
			System.out.println("Started new with run with bound: " + bound);

			while(!stack.isEmpty()){
				stack = search(stack, bound);
				
				if (stack.isSolution()) {
					stack.setIsSolution(false);
					stack.pop();
					numberOfSolutions++;
					//System.out.println("Current number of solutions: " + numberOfSolutions);
				}
				if (stack.getMinimumDistanceToSolution() >= maxBound){
					return 0;
				}

			}

			bound = stack.getMinimumDistanceToSolution();
			
		}
		
		return numberOfSolutions;
	}
	
	public static SearchStack search(SearchStack stack, int bound){
		
		MainStackElement currentMainStackElement = stack.peek();
		
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
