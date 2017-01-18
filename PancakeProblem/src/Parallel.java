import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import mpi.MPI;
import mpi.Request;

public class Parallel {

	static boolean random = false;
	static int mode = 2;
	static int numberOfPancakes = 17;
	
	static final int PROC_MAIN = 0;
	static final int RESULT = 101;
	static final int NEEDWORK = 102;
	static final int GETWORK = 103;
	static final int CHECKIN = 104;
	static final int BUFFERSIZE = 10;
	static Object inBuffer[] = new Object[BUFFERSIZE];
	static Object outBuffer[] = new Object[BUFFERSIZE];
	
	public static void main(String[] args) {
		int rank;
		MPI.Init(args);

		rank = MPI.COMM_WORLD.Rank();
		
			if (rank == PROC_MAIN){
				runMasterProcessMode1();

				System.out.println("Finished:" + rank);
			}
			else{
				runWorkerProcess();

				System.out.println("Finished:" + rank);
			}
		
		MPI.Finalize();  
	}
	
	private static void runMasterProcessMode1(){
		int size = MPI.COMM_WORLD.Size();
		
		//prepare initial Stack
		
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
		
		for (int i = 1 ; i <= size - 1; i++){
			int checkInOutBuffer[] = new int[1];
			checkInOutBuffer[0] = 1;
			MPI.COMM_WORLD.Isend(checkInOutBuffer, 0, checkInOutBuffer.length, MPI.INT, i, CHECKIN);
		}
	}
	
	
	
	public static SearchStack solveMode1(Node root){
		int size = MPI.COMM_WORLD.Size();
		SearchStack solutionStack = null;
		int bound =  root.getOptimisticDistanceToSolution();
		int maxBound = bound * 10;
		Request resultRequests[] = new Request[size];
		Request getWorkRequests[] = new Request[size];
		boolean isWorking[] = new boolean[size];
		boolean requestpending[] = new boolean[size];
		
		for (int i = 1 ; i <= size - 1; i++){
			resultRequests[i] = MPI.COMM_WORLD.Irecv(inBuffer, 0, inBuffer.length, MPI.OBJECT, i, RESULT);				
		}
		for (int i = 1 ; i <= size - 1; i++){
			getWorkRequests[i] = MPI.COMM_WORLD.Irecv(inBuffer, 0, inBuffer.length, MPI.OBJECT, i, GETWORK);				
		}

		
		while (solutionStack == null){
			

			int nextbound = Integer.MAX_VALUE;
			SearchStack stack = new SearchStack(root, bound);

			
			System.out.println("Started new with run with bound: " + bound);
			
			isWorking = new boolean[size];
			requestpending  = new boolean[size];
			//send work
			outBuffer[0] = stack;
			MPI.COMM_WORLD.Isend(outBuffer, 0, outBuffer.length, MPI.OBJECT, 1, NEEDWORK);
			//System.out.println("Sent work to " + 1);
			isWorking[1] = true;
			boolean somebodyIsWorking = true;
			
			while (somebodyIsWorking){
				

				
				for (int i = 1 ; i <= size - 1; i++){
					
					if (resultRequests[i].Test() != null){
						resultRequests[i] = MPI.COMM_WORLD.Irecv(inBuffer, 0, inBuffer.length, MPI.OBJECT, i, RESULT);
						SearchStack responseStack = (SearchStack) inBuffer[0];
						//System.out.println("Recieved result from " + i);

						if (responseStack.isSolution()) {
							//System.out.println("ResultSolution " + i);
							solutionStack = responseStack;
							break;
						}
						else{
							
							isWorking[i] = false;

							if (nextbound > responseStack.getMinimumDistanceToSolution()){
								nextbound = responseStack.getMinimumDistanceToSolution();
							}
						}
						if (responseStack.getMinimumDistanceToSolution() >= maxBound){
							return null;
						}
						
					}
				}
				
				if (solutionStack != null){
					break;
				}

				List<Integer> needsWork = new LinkedList<Integer>();
				for (int i = 1 ; i <= size - 1; i++){
					if (!isWorking[i]){
						needsWork.add(i);
					}
				}
					
				//send requests for work
				if (!needsWork.isEmpty()){
					int numberOfRequestsSent = 0;
					
					for (int i = 1 ; i <= size - 1; i++){
						if (isWorking[i] &&  !requestpending[i]){
							int checkInOutBuffer[] = new int[1];
							checkInOutBuffer[0] = 0;
							//System.out.println("Request work from " + i);
							MPI.COMM_WORLD.Isend(checkInOutBuffer, 0, checkInOutBuffer.length, MPI.INT, i, CHECKIN);
							requestpending[i] = true;
							numberOfRequestsSent++;
							if (numberOfRequestsSent >= needsWork.size()){
								break;
							}
						}
						else if(requestpending[i]){
							numberOfRequestsSent++;
							if (numberOfRequestsSent >= needsWork.size()){
								break;
							}
						}
				
					}	

				}
				
				//check if work arrived
				int startingpoint = 1;
				for (int recvID : needsWork){				
					//System.out.println("Now checking for splitstack");
					for (int i = startingpoint ; i <= size - 1; i++){
						if (getWorkRequests[i].Test() != null){							
							outBuffer[0]  = (SearchStack) inBuffer[0];
							getWorkRequests[i] = MPI.COMM_WORLD.Irecv(inBuffer, 0, inBuffer.length, MPI.OBJECT, i, GETWORK);
							requestpending[i] = false;
							//System.out.println("Sent work to " + recvID);
							MPI.COMM_WORLD.Isend(outBuffer, 0, outBuffer.length, MPI.OBJECT, recvID, NEEDWORK);
							isWorking[recvID] = true;
							startingpoint = i + 1;
							break;
						}
					}
				}
				
				somebodyIsWorking = false;
				for (int i = 1 ; i <= size - 1; i++){
					if (isWorking[i]){
						somebodyIsWorking = true;
					}
				}
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
						e.printStackTrace();
				}
				
			}	

			bound = nextbound;
			
		}
		
		return solutionStack;
	}
	
	public static int solveMode2(Node root){
		int size = MPI.COMM_WORLD.Size();
		int bound =  root.getOptimisticDistanceToSolution();
		int maxBound = bound * 10;
		int numberOfSolutions = 0;
		
		Request resultRequests[] = new Request[size];
		Request getWorkRequests[] = new Request[size];
		boolean isWorking[] = new boolean[size];
		boolean requestpending[] = new boolean[size];
		
		for (int i = 1 ; i <= size - 1; i++){
			resultRequests[i] = MPI.COMM_WORLD.Irecv(inBuffer, 0, inBuffer.length, MPI.OBJECT, i, RESULT);				
		}
		
		for (int i = 1 ; i <= size - 1; i++){
			getWorkRequests[i] = MPI.COMM_WORLD.Irecv(inBuffer, 0, inBuffer.length, MPI.OBJECT, i, GETWORK);				
		}

		
		while (numberOfSolutions == 0){
			

			int nextbound = Integer.MAX_VALUE;
			SearchStack stack = new SearchStack(root, bound);
			
			System.out.println("Started new with run with bound: " + bound);
			
			isWorking = new boolean[size];
			requestpending  = new boolean[size];
			//send work
			outBuffer[0] = stack;
			MPI.COMM_WORLD.Isend(outBuffer, 0, outBuffer.length, MPI.OBJECT, 1, NEEDWORK);
			//System.out.println("Sent work to " + 1);
			isWorking[1] = true;
			boolean somebodyIsWorking = true;
			
			while (somebodyIsWorking){
				

				
				for (int i = 1 ; i <= size - 1; i++){
					
					if (resultRequests[i].Test() != null){
						resultRequests[i] = MPI.COMM_WORLD.Irecv(inBuffer, 0, inBuffer.length, MPI.OBJECT, i, RESULT);
						SearchStack responseStack = (SearchStack) inBuffer[0];
						//System.out.println("Recieved result from " + i);

						if (responseStack.isSolution()) {
							
							numberOfSolutions++;
							//System.out.println("Current Total of Solutions: " + numberOfSolutions);
						}
						else{
							
							isWorking[i] = false;

							if (nextbound > responseStack.getMinimumDistanceToSolution()){
								nextbound = responseStack.getMinimumDistanceToSolution();
							}
						}
						if (responseStack.getMinimumDistanceToSolution() >= maxBound){
							return 0;
						}
						
					}
				}

				List<Integer> needsWork = new LinkedList<Integer>();
				for (int i = 1 ; i <= size - 1; i++){
					if (!isWorking[i]){
						needsWork.add(i);
					}
				}
					
				//send requests for work
				if (!needsWork.isEmpty()){
					int numberOfRequestsSent = 0;
					
					for (int i = 1 ; i <= size - 1; i++){
						if (isWorking[i] &&  !requestpending[i]){
							int checkInOutBuffer[] = new int[1];
							checkInOutBuffer[0] = 0;
							//System.out.println("Request work from " + i);
							MPI.COMM_WORLD.Isend(checkInOutBuffer, 0, checkInOutBuffer.length, MPI.INT, i, CHECKIN);
							requestpending[i] = true;
							numberOfRequestsSent++;
							if (numberOfRequestsSent >= needsWork.size()){
								break;
							}
						}
						else if(requestpending[i]){
							numberOfRequestsSent++;
							if (numberOfRequestsSent >= needsWork.size()){
								break;
							}
						}
				
					}	

				}
				
				//check if work arrived
				int startingpoint = 1;
				for (int recvID : needsWork){				
					//System.out.println("Now checking for splitstack");
					for (int i = startingpoint ; i <= size - 1; i++){
						if (getWorkRequests[i].Test() != null){							
							outBuffer[0]  = (SearchStack) inBuffer[0];
							getWorkRequests[i] = MPI.COMM_WORLD.Irecv(inBuffer, 0, inBuffer.length, MPI.OBJECT, i, GETWORK);
							requestpending[i] = false;
							//System.out.println("Sent work to " + recvID);
							MPI.COMM_WORLD.Isend(outBuffer, 0, outBuffer.length, MPI.OBJECT, recvID, NEEDWORK);
							isWorking[recvID] = true;
							startingpoint = i + 1;
							break;
						}
					}
				}
				
				somebodyIsWorking = false;
				for (int i = 1 ; i <= size - 1; i++){
					if (isWorking[i]){
						somebodyIsWorking = true;
					}
				}
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
						e.printStackTrace();
				}
				
			}	

			bound = nextbound;
			
		}
		
		return numberOfSolutions;
	}
	
	private static void runWorkerProcess(){
	int	rank = MPI.COMM_WORLD.Rank();	
	boolean abort = false;
	int checkInInBuffer[] = new int[1];
	Request checkInRequest = MPI.COMM_WORLD.Irecv(checkInInBuffer, 0, inBuffer.length, MPI.INT, PROC_MAIN, CHECKIN);
	
	while (!abort){
		//System.out.println(rank + " looking for work");
		
		Request needWorkRequest = MPI.COMM_WORLD.Irecv(inBuffer, 0, inBuffer.length, MPI.OBJECT, PROC_MAIN, NEEDWORK);
		SearchStack workStack = null;
		
		while (workStack == null){
			if (needWorkRequest.Test() != null){
				workStack = (SearchStack) inBuffer[0];
			}
			else if (checkInRequest.Test() != null){
				checkInRequest = MPI.COMM_WORLD.Irecv(checkInInBuffer, 0, checkInInBuffer.length, MPI.INT, PROC_MAIN, CHECKIN);
				int result;
				
				result = checkInInBuffer[0];
				if (result == 1){
					abort = true;
					break;
				}

			}
			else{
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
						e.printStackTrace();
				}
				
			}
			
		}
		
		if (abort){
			break;
		}
		
		
		
		while(!workStack.isEmpty()){
				workStack = search(workStack, workStack.getBound(), 5000);
				
			if (workStack.isSolution()) {
				//System.out.println("sending result, result is empty:" + workStack.isEmpty());
				outBuffer[0] = workStack;
				MPI.COMM_WORLD.Isend(outBuffer, 0, outBuffer.length, MPI.OBJECT, PROC_MAIN, RESULT);
				workStack.setIsSolution(false);
				workStack.pop();
			}
			
			//System.out.println(rank + " checking in with master Process:");

			if (checkInRequest.Test() != null){
				checkInRequest = MPI.COMM_WORLD.Irecv(checkInInBuffer, 0, checkInInBuffer.length, MPI.INT, PROC_MAIN, CHECKIN);
				int result;
				
				result = checkInInBuffer[0];
				if (result == 1){
					abort = true;
					break;
				}
				else if(result == 0){
					//System.out.println(rank + " splitting the stack");
					//split 
					outBuffer[0] = workStack.split();
					MPI.COMM_WORLD.Isend(outBuffer, 0, outBuffer.length, MPI.OBJECT, PROC_MAIN, GETWORK);
				}

			}

		}
		
		if (workStack.isEmpty()) {
			//System.out.println("sending result, result is empty:" + workStack.isEmpty());
			outBuffer[0] = workStack;
			MPI.COMM_WORLD.Isend(outBuffer, 0, outBuffer.length, MPI.OBJECT, PROC_MAIN, RESULT);
			workStack.pop();
		}

		
	}


		
		
	}
	
	public static SearchStack search(SearchStack stack, int bound, int maxNumberOfExpands){
		//int	rank = MPI.COMM_WORLD.Rank();
		//System.out.println( rank + " now working on run bound: " + bound);
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
