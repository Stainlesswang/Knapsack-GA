/**
 * GeneticAlgorithm implements a genetic algorithm to solve the 01-Knapsack Problem.
 * 
 * It allows the user to choose from the following operators:
 * - Roulette Selection
 * - Tournament Selection
 * - N-Slice Crossover
 * - Uniform Crossover
 * - N-Point Mutation
 * - Bit-Inversion Mutation
 * 
 * It loads datasets in the format of those hosted at
 * http://people.sc.fsu.edu/~jburkardt/datasets/knapsack_01/knapsack_01.html.
 * 
 * Then, it generates random initial chromosomes, and uses the GA process to
 * locate the best solution that it can before it terminates by reaching the
 * maximum number of generations.  The optimal solution given is also printed
 * for comparison.
 * 
 * @author Anton Ridgway
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeSet;

public class GeneticAlgorithm
{
	//GA Variables
	static int poolSize = 100;
	static int numGens = 1000;
	static double mutationRate = .01;
	static double crossoverRate = .90;

	static int capacity = 0;
	static int numItems = 0;
	static int totalValue = 0;
	static double penalty = 0;
	static double offset = 0;
	
	static boolean optimalKnown = false;
	static boolean[] optimal;
	static String optSolStr = "";
	static int optSolSize = 0;
	static int optSolVal = 0;
	static double optSolFitness = 0;
	
	static ArrayList<Integer> values = new ArrayList<Integer>();
	static ArrayList<Integer> sizes = new ArrayList<Integer>();
	
	//User-selected parameters
	static int selectionChoice = 0;
	static int crossoverChoice = 0;
	static int mutationChoice = 0;
	static double kValue;
	static int numMutPts;
	static int numSlicePts;
	
	static Random randomizer = new Random();
	
	public static void main( String[] args )
	{
		//-----------------------------------------------------------------------------------------
		// Step 1: Get User Input 
		Scanner inputReader = new Scanner(System.in);
		
		System.out.print("Choose selection algorithm (0 = roulette, 1 = tournament): ");
		selectionChoice = inputReader.nextInt();
		
		if(selectionChoice == 1)
		{
			System.out.print("Choose k-value for tournament selection (.60-.85): ");
			kValue = inputReader.nextDouble();
		}
		
		System.out.print("Choose crossover algorithm (0 = n-slice, 1 = uniform): ");
		crossoverChoice = inputReader.nextInt();
		if(crossoverChoice == 0)
		{
			System.out.print("Choose n-value for n-slice (usually 2-4): ");
			numSlicePts = inputReader.nextInt();
		}
		
		System.out.print("Choose mutation algorithm (0 = n-point, 1 = invert): ");
		mutationChoice = inputReader.nextInt();
		if(mutationChoice == 0)
		{
			System.out.print("Choose number of points (usually 1-4): ");
			numMutPts = inputReader.nextInt();
		}
		
		System.out.print("Input population size (50-300): ");
		poolSize = inputReader.nextInt();
		
		System.out.print("Input number of generations: ");
		numGens = inputReader.nextInt();
		
		System.out.print("Input mutation rate (.005-.01): ");
		mutationRate = inputReader.nextDouble();
		
		System.out.print("Input crossover rate (.80-.95): ");
		crossoverRate = inputReader.nextDouble();
		
		int notFound;
		String prefix;
		System.out.print("Enter dataset prefix: ");
		
		File source = new File(System.getProperty("java.class.path"));
		String binDirectory = source.getAbsoluteFile().getParentFile().toString()+File.separator;
		do
		{
		notFound = 0;
		prefix = inputReader.next();
		File fileChecker = new File(binDirectory+prefix+"_c.txt");
		if(fileChecker.exists())
			notFound++;
		fileChecker = new File(binDirectory+prefix+"_p.txt");
		if(fileChecker.exists())
			notFound++;
		fileChecker = new File(binDirectory+prefix+"_w.txt");
		if(fileChecker.exists())
			notFound++;
		if(notFound < 3) System.out.print("\nComplete dataset could not be found.\nTry again: ");
		} while (notFound < 3);
		
		File fileChecker = new File(binDirectory+prefix+"_s.txt");
		if(fileChecker.exists())
			optimalKnown = true;
		
		inputReader.close();
		
		//-----------------------------------------------------------------------------------------
		// Step 2: Load Selected Dataset
		
		try
		{
			System.out.println("\nGetting dataset " + prefix + "...");
			System.out.println("Getting "+prefix+"_c.txt");
			Scanner cScanner = new Scanner(new File(binDirectory+prefix+"_c.txt"));
			capacity = cScanner.nextInt();
			cScanner.close();
			
			System.out.println("Getting "+prefix+"_w.txt");
			Scanner wScanner = new Scanner(new File(binDirectory+prefix+"_w.txt"));
			while(wScanner.hasNextInt())
			{
				numItems++;
				int nextSize = wScanner.nextInt(); 
				sizes.add(nextSize);
			}
			wScanner.close();
			
			System.out.println("Getting "+prefix+"_p.txt");
			Scanner pScanner = new Scanner(new File(binDirectory+prefix+"_p.txt"));
			while(pScanner.hasNextInt())
			{
				int nextVal = pScanner.nextInt();
				values.add(nextVal);
				totalValue+=nextVal;
			}
			pScanner.close();
			
			//Determine over-capacity penalties.
			//penalty per-unit-over-capacity is equal to the highest value per unit size ratio of any package
			//offset penalty is one third the total value of all packages.
			double temp;
			for(int i = 0; i < numItems; i++)
			{
				offset += values.get(i);
				temp = ((double)values.get(i))/sizes.get(i);
				if( temp > penalty )
					penalty = temp;
			}
			offset *= .3;
			
			
			//Get Optimal Selection
			if(optimalKnown)
			{
				System.out.println("Getting "+prefix+"_s.txt");
				Scanner sScanner = new Scanner(new File(binDirectory+prefix+"_s.txt"));
				optimal = new boolean[numItems];
				for(int i = 0; i < numItems && sScanner.hasNextInt(); i++)
				{
					if( sScanner.nextInt() == 0)
					{
						optimal[i] = false;
						optSolStr += "0";				
					}
					else
					{
						optimal[i] = true;
						optSolStr += "1";
						optSolSize += sizes.get(i);
						optSolVal += values.get(i);
					}
				}
				optSolFitness = fitness(optimal,optSolSize);
				sScanner.close();
			}
		}
		catch(IOException e)
		{
			System.err.println("IOException: " + e);
		}
		System.out.println(" item # |  value |   size |");
		for(int i = 0; i < numItems; i++)
			System.out.printf("%7d |%7d |%7d |\n",i,values.get(i),sizes.get(i));
		System.out.println("Capacity: " + capacity);
		System.out.println("Number of Items: " + numItems);
		System.out.println("Total Value: " + totalValue);
		System.out.println();
		
		//Uncomment to test GA components.
		//testBattery();
		
		//-----------------------------------------------------------------------------------------
		// Step 3: Randomly Generate Initial Chromosomes
		
		boolean[][] chromPool = new boolean[poolSize][numItems];
		double[] fitnessPool = new double[poolSize];
		int[] sizePool = new int[poolSize];
		
		for( int i = 0; i < poolSize; i++ )
		{
			//bit strings as a boolean array
			for(int j = 0; j < numItems; j++)
			{
				chromPool[i][j] = randomizer.nextBoolean();
				if(chromPool[i][j] == true)
					sizePool[i] += sizes.get(j);
			}
			
			//fitness
			fitnessPool[i] = fitness(chromPool[i], sizePool[i]);
			
		}
				
		//-----------------------------------------------------------------------------------------
		// Step 4: Main GA Loop
		
		int genOfBestFound = 0;
		double maxFitnessSoFar = 0;
		
		for( int gens = 0; gens < numGens; gens++ )
		{
			if((gens+1) % (numGens/10) == 0)
			{
				System.out.println("Generation "+(gens+1)+"...");
				if(optimalKnown)
				{
					double percent = (fitnessPool[0]/optSolFitness)*100;
					System.out.printf("Best Fitness: %,.2f%% of known optimal.\n" , percent);
				}
				else
					System.out.println("Best Fitness: " + maxFitnessSoFar);
			}
			
			int[] selectedParents;
			//biased selection
			if(selectionChoice == 0)
				selectedParents = rouletteSelection(chromPool, fitnessPool);
			else
				selectedParents = tournamentSelection(chromPool, fitnessPool, kValue);
			
			//crossover
			boolean[][] childPool;
			if(crossoverChoice == 0)
				childPool = nSliceCrossover(chromPool, selectedParents, numSlicePts, crossoverRate);
			else
				childPool = uniformCrossover(chromPool, selectedParents, crossoverRate);		
			
			//mutate
			for(int i = 0; i < poolSize; i++)
			{
				if (randomizer.nextDouble() < mutationRate)
				{
					if(mutationChoice == 0)
						childPool[i] = nPointMutation(childPool[i],numSlicePts);
					else
						childPool[i] = invertMutation(childPool[i]);
				}
			}
			
			//Elitism: copy best previous solutions into child pool.
			int best = 0;
			int almostBest = 1;
			for(int i = 1; i < poolSize; i++)
			{
				if(fitnessPool[i] > fitnessPool[best])
				{
					almostBest = best;
					best = i;
				}
				else if (fitnessPool[i] > fitnessPool[almostBest])
				{
					almostBest = i;
				}

			}
			
			//Check for a new best solution.
			if(fitnessPool[best] > maxFitnessSoFar)
			{
				maxFitnessSoFar = fitnessPool[best];
				genOfBestFound = gens+1;
			}
			
			//Arbitrarily discard the first two child chromosomes in favor of the elite chromosomes,
			//and copy over their size and fitness.
			childPool[0] = chromPool[best];
			childPool[1] = chromPool[almostBest];
				
			double fitTemp = fitnessPool[almostBest];
			fitnessPool[0] = fitnessPool[best];
			fitnessPool[1] = fitTemp;
			
			int sizeTemp = sizePool[almostBest];
			sizePool[0] = sizePool[best];
			sizePool[1] = sizeTemp;
			
			//make the new children parents, and reevaluate their size and fitness
			chromPool = childPool;
			for(int i = 2; i < poolSize; i++)
			{
				sizePool[i] = 0;
				for(int j = 0; j < numItems; j++)
				{
					if(chromPool[i][j] == true)
						sizePool[i] += sizes.get(j);
				}
				fitnessPool[i] = fitness(chromPool[i], sizePool[i]);
			}
			
		}// End Main GA Loop
		
		//-----------------------------------------------------------------------------------------
		// Step 5: Print Best Solution
		
		//Find the best (potentially found in the last generation, so not always the elitism-best)
		int bestFit = 0;
		for(int i = 1; i < poolSize; i++)
		{
			if(fitnessPool[i] > fitnessPool[bestFit] && sizePool[i] <= capacity)
				bestFit = i;
		}
		
		//Generate data to print.
		String bestSolStr = "";
		int bestSolVal = 0;
		boolean sameAsOptimal = true;
		for(int i = 0; i < numItems; i++)
		{
			if(chromPool[bestFit][i] == true)
			{
				bestSolStr += "1";
				bestSolVal += values.get(i);
			}
			else
				bestSolStr += "0";
			
			if(!optimalKnown || chromPool[bestFit][i] != optimal[i])
				sameAsOptimal = false;
		}

		if(optimalKnown)
		{
			System.out.println("\nOptimal Solution: " + optSolStr);
			System.out.println("Optimal Fitness: " + fitness(optimal, optSolSize));
			System.out.println("Optimal Size out of Capacity: " + optSolSize + "/" + capacity);
			System.out.println("Optimal Value: " + optSolVal );
		}
		
		System.out.println("\nFittest Solution: " + bestSolStr);
		System.out.print("Fitness: " + fitnessPool[bestFit]);
		if(optimalKnown)
		{
			double percent = (fitnessPool[bestFit]/optSolFitness)*100;
			System.out.printf(" (%,.2f%% of optimal)\n" , percent);
		}
		else
			System.out.println();
		System.out.println("Size out of Capacity: " + sizePool[bestFit] + "/" + capacity);
		System.out.println("Value: " + bestSolVal );
		System.out.println("Found on generation " + genOfBestFound + "." );
		
		if(sameAsOptimal)
			System.out.println("==Found the optimal!==" );
	}
	
	/**
	 * fitness represents the following function:
	 *
	 * 		V-(X*(P*(S-C)+O))
	 * 
	 * where
	 * V is the total value of all items selected in a given chromosome.
	 * S is the total size of all items selected.
	 * C is the capacity of the knapsack.
	 * X is 0 when S <= C, and 1 otherwise.
	 * P is the penalty to be given per unit-size-over-capacity for the
	 * 				current dataset (calculated when it is loaded).
	 * O is an offset penalty to be automatically applied when a chromosome
	 * 				is over capacity.
	 * 
	 * Negative values are normalized to zero.
	 * 
	 * @param c chromosome to evaluate
	 * @param s the size of the chromosome
	 * @return the fitness of the chromosome
	 */
	public static double fitness( boolean[] c, int s )
	{
		//Get the chromosome's value
		int runningValue = 0;
		for(int i = 0; i < numItems; i++)
		{
			if(c[i] == true)
			{
				runningValue += values.get(i);
			}
		}
		if( s > capacity )
		{
			double returnMe = runningValue - ((s - capacity) * penalty + offset);
			// keep fitness above zero, to avoid problems with fractional-fitness
			if (returnMe < 0.1)
				return 0.1;
			else
				return returnMe;
		}
		else
			return runningValue;
	}
	
	/**
	 * rouletteSelection is an algorithm that takes a set of chromosomes and
	 * their fitnesses, and performs biased selection, according to their
	 * fractional fitness.  It returns the indices of the selected chromosomes.
	 * Repeats are allowed.
	 * 
	 * @param c the set of chromosomes to select from
	 * @param f the fitness of the chromosomes
	 * @return selectedIndices, the indices of the selected chromosomes
	 */
	public static int[] rouletteSelection( boolean[][] c, double[] f )
	{
		double totalFitness = 0;
		for(int i = 0; i < poolSize; i++) totalFitness += f[i];
		
		//pick the parents based on their percent fitness
		int[] selectedIndices = new int[poolSize];
		for(int i = 0; i < poolSize; i++)
		{
			//pick a spot on the roulette (from 0 to 1), and subtract the fractional fitness
			//until we find a roulette-selected parent. 
			double theSpot = randomizer.nextDouble();
			int j = 0;
			while(j < poolSize && theSpot > 0)
			{
				theSpot -= f[j]/totalFitness;
				j++;
			}
			selectedIndices[i] = j-1;
		}
		return selectedIndices;
	}
	
	/**
	 * tournamentSelection is an algorithm that takes a set of chromosomes and
	 * their fitnesses, and performs biased tournament-style selection, according
	 * to their relative fitness.  It returns the indices of the selected
	 * chromosomes. Repeats are allowed.
	 * 
	 * @param c the set of chromosomes to select from
	 * @param f the fitness of the chromosomes
	 * @param k the k-value for what percent of the time the stronger chromosome
	 * 			should be selected.
	 * @return selectedIndices the indices of the selected chromosomes
	 */
	public static int[] tournamentSelection( boolean[][] c, double[] f, double k )
	{
		int[] selectedIndices = new int[poolSize];
		for( int i = 0; i < poolSize; i++ )
		{
			int ind1 = randomizer.nextInt(poolSize);
			int ind2 = randomizer.nextInt(poolSize);
	
			//pick out the stronger chromosome
			int stronger;
			if(f[ind1] < f[ind2])
				stronger = 0;
			else if (f[ind1] > f[ind2])
				stronger = 1;
			else //equal
				stronger = randomizer.nextInt(2);
			
			double r = randomizer.nextDouble();
			if(r < k) //select the stronger chromosome
			{
				if (stronger == 1) selectedIndices[i] = ind1;
				else selectedIndices[i] = ind2;
			}
			else //select the weaker chromosome
			{
				if (stronger == 1) selectedIndices[i] = ind2;
				else selectedIndices[i] = ind1;
			}
		}
		return selectedIndices;
	}
	
	/**
	 * nSliceCrossover performs the the N-Slice Crossover on a set of chromosomes,
	 * given the indices of the selected parents, and the number of slice points
	 * to use.  Crossovers are performed sequentially, but because selection is
	 * random, this does not have any effect on the randomness of the whole
	 * process.
	 * 
	 * @param c the set of chromosomes to crossover
	 * @param pI the indices of the chromosomes to crossover
	 * @param n the number of slice points to use
	 * @param xProb the probability that crossover will occur
	 * @return children the set of child chromosomes created in the crossover
	 */
	public static boolean[][] nSliceCrossover( boolean[][] c, int[] pI, int n, double xProb )
	{
		boolean[][] children = new boolean[poolSize][numItems];
		boolean[] par1;
		boolean[] par2;
		boolean[] chi1;
		boolean[] chi2;

		for(int i = 0; i < poolSize; i+=2)
		{
			par1 = c[pI[i]];
			chi1 = children[i];
			if( i < poolSize-1 ) //deal with an odd-numbered pool-size
			{
				par2 = c[pI[i+1]];
				chi2 = children[i+1];
			}
			else
			{
				par2 = c[pI[0]];
				chi2 = children[0]; //overwrite the first child
			}
			
			if(randomizer.nextDouble() < xProb)
			{
				//Get n unique, random, sorted indices to slice. (Each slice-number represents the point after
				//chromosome index n and before n+1.) Stop n from exceeding numItems-1, its max value.
				
				TreeSet<Integer> indices = new TreeSet<Integer>();
				int iNeeded;
				if(n < numItems)
					iNeeded = n;
				else
					iNeeded = numItems - 1;
				
				for (int j = 0; j < iNeeded; j++)
				{
					int guess = randomizer.nextInt(numItems-1);
					while(indices.contains(guess))
					{
						guess = (guess+1)%(numItems-1);
					}
					indices.add(guess);
				}
	
				boolean takeFromPar1 = true;
				int j = 0;
				for(Integer currSlice: indices)
				{ 
					while(j <= currSlice)
					{
						if(takeFromPar1)
						{
							chi1[j] = par1[j];
							chi2[j] = par2[j];
						}
						else
						{
							chi1[j] = par2[j];
							chi2[j] = par1[j];
						}
						j++;
					}
					takeFromPar1 = !takeFromPar1;
				}
				while(j < numItems)
				{
					if(takeFromPar1)
					{
						chi1[j] = par1[j];
						chi2[j] = par2[j];
					}
					else
					{
						chi1[j] = par2[j];
						chi2[j] = par1[j];
					}
					j++;
				}
			}
			else //skip this particular crossover
			{
				chi1 = par1;
				chi2 = par2;
			}
		}
		return children;
	}

	/**
	 * uniformCrossover performs a Uniform Crossover on a set of chromosomes,
	 * given the indices of the selected parents.  Crossovers are performed
	 * sequentially, but because selection is random, this does not have any
	 * effect on the randomness of the whole process.
	 * 
	 * @param c the set of chromosomes to crossover
	 * @param pI the indices of the chromosomes to crossover
	 * @param xProb the probability that crossover will occur
	 * @return children the set of child chromosomes created in the crossover
	 */
	public static boolean[][] uniformCrossover( boolean[][] c, int[] pI, double xProb )
	{
		boolean[][] children = new boolean[poolSize][numItems];
		for(int i = 0; i < poolSize; i+=2)
		{
			if(randomizer.nextDouble() < xProb)
			{
				for(int j = 0; j < numItems; j++)
				{		
					if(randomizer.nextBoolean())
					{
						children[i][j] = c[pI[i]][j];
						if(i < poolSize-1)
							children[i+1][j] = c[pI[i+1]][j];
						else
							children[0][j] = c[pI[0]][j];
					}
					else
					{
						if(i < poolSize-1)
						{
							children[i][j] = c[pI[i+1]][j];
							children[i+1][j] = c[pI[i]][j];
						}
						else
						{
							children[i][j] = c[pI[0]][j];
							children[0][j] = c[pI[i]][j];
						}
					}
				}
			}
			else
			{
				for(int j = 0; j < numItems; j++)
				{
					children[i][j] = c[pI[i]][j];
					if(i < poolSize-1)
						children[i+1][j] = c[pI[i+1]][j];
					else
						children[0][j] = c[pI[0]][j];
				}
			}
		}
		return children;
	}
	
	/**
	 * nPointMutation mutates the given chromosome by inverting n random bits.
	 * 
	 * @param c the chromosome to mutate
	 * @param n the number of bits to invert
	 * @return c the mutated chromosome 
	 */
	public static boolean[] nPointMutation( boolean[] c, int n )
	{	
		int spot;
		//invert at these indices
		for(int i = 0; i < n; i++)
		{
			spot = randomizer.nextInt(numItems);
			c[spot] = !c[spot];
		}
		return c;
	}
	
	/**
	 * invertMutation mutates the given chromosome by inverting the entire bit string.
	 * 
	 * @param c the chromosome to mutate
	 * @return c the mutated chromosome
	 */
	public static boolean[] invertMutation( boolean[] c )
	{
		boolean[] newChrom = new boolean[numItems];
		for(int i = 0; i < numItems; i++) newChrom[i] = !c[i];
		return newChrom;
	}
	
	/**
	 * testBattery is a method to test each of the individual components of the GA.
	 */
	public static void testBattery()
	{
		System.out.println("Testing fitness function...");
		boolean[][] testC = new boolean[poolSize][numItems];
		int[] testS = new int[poolSize];
		double[] testF = new double[poolSize];
		
		for(int i = 0; i < numItems; i++)
		{
			testC[0][i] = false;
			testC[1][i] = true;
		}
		for(int i = 2; i < 5; i++)
		{
			for(int j = 0; j < numItems; j++)
				testC[i][j] = randomizer.nextBoolean();
		}
		for(int i = 5; i < poolSize; i++)
		{
			for(int j = 0; j < numItems; j++)
				testC[i][j] = false;
		}
		
		for(int i = 0; i < poolSize; i++)
		{
			testS[i] = getChromSize(testC[i]);
			testF[i] = fitness(testC[i],testS[i]);
		}
		
		System.out.println("Random chromosome: " + chromToString(testC[4]));
		int tempS = getChromSize(testC[4]);
		System.out.println("Total Size: " + tempS);
		System.out.println("Total Value: " + getChromValue(testC[4]));
		System.out.println("Capacity: " + capacity);
		System.out.println("Penalty: " + penalty);
		System.out.println("Fitness Function Output: " + fitness(testC[4], tempS));
		System.out.println("(Hand-calculate fitness and compare to test.)");
		
		System.out.println("\nTesting roulette selection...");
		double totalFitness = 0;
		for(int i = 0; i < 5; i++)
			totalFitness += fitness(testC[i],testS[i]);
		
		System.out.println("5 Random Test Chromosomes (the other poolSize-5 chromosomes are all-zero, for 0 fitness):");
		for(int i = 0; i < 5; i++)
		{
			System.out.println(chromToString(testC[i]) + " | Fractional Fitness: " + (testF[i]/totalFitness));
		}
		int[] indices = rouletteSelection(testC,testF);
		System.out.println("Selection indices:");
		for(int i = 0; i < poolSize; i++)
			System.out.print( indices[i] + ", " );
		
		System.out.println("\n\nTesting tournament selection (K = "+kValue+")...");
		for(int i = 0; i < 5; i++)
		{
			System.out.println(chromToString(testC[i]) + " | Fractional Fitness: " + (testF[i]/totalFitness));
		}
		indices = tournamentSelection(testC,testF,kValue);
		System.out.println("Selection indices:");
		for(int i = 0; i < poolSize; i++)
			System.out.print( indices[i] + ", " );
		
		System.out.println("\n\nTesting n-slice crossover (n = "+numSlicePts+")...");
		int[] pInd = new int[poolSize];
		pInd[0] = 0;
		pInd[1] = 1;
		for(int i = 2; i < poolSize; i++)
			pInd[i] = 0;
		boolean[][] children;
		children = nSliceCrossover(testC,pInd,numSlicePts,1.1);
		System.out.println("Parent 1: " + chromToString(testC[0]));
		System.out.println("Parent 2: " + chromToString(testC[1]));
		System.out.println("Child 1: " + chromToString(children[0]));
		System.out.println("Child 2: " + chromToString(children[1]));
		
		System.out.println("\nTesting uniform crossover...");
		children = uniformCrossover(testC,pInd,1.1);
		System.out.println("Parent 1: " + chromToString(testC[0]));
		System.out.println("Parent 2: " + chromToString(testC[1]));
		System.out.println("Child 1: " + chromToString(children[0]));
		System.out.println("Child 2: " + chromToString(children[1]));
		
		System.out.println("\nTesting n-point mutation (n = "+numMutPts+")...");
		int ind = poolSize-1;
		System.out.println("Initial Chromosome: " + chromToString(testC[ind]));
		testC[ind] = nPointMutation(testC[ind],numMutPts);
		System.out.println("Mutated Chromosome: " + chromToString(testC[ind]));
		
		System.out.println("\nTesting invert mutation...");
		System.out.println("Initial Chromosome: " + chromToString(testC[ind]));
		testC[ind] = invertMutation(testC[ind]);
		System.out.println("Mutated Chromosome: " + chromToString(testC[ind]));
		
		System.out.println("\nTesting is over... resuming normal programming.\n");
	}
	
	/**
	 * chromToString is a method that generates a string from a given chromosome.
	 * 
	 * @param c the chromosome to make the string from
	 * @return temp the string
	 */
	public static String chromToString( boolean[] c )
	{
		String temp = "";
		for(int i = 0; i < numItems; i++ )
		{
			if (c[i] == true)
				temp += "1";
			else
				temp += "0";
		}
		return temp;
	}
	
	/**
	 * getChromValue calculates the total value of a given chromosome.
	 * 
	 * @param c the chromosome to get the value of
	 * @return temp the total value
	 */
	public static int getChromValue( boolean[] c )
	{
		int temp = 0;
		for(int i = 0; i < numItems; i++ )
		{
			if (c[i] == true)
				temp += values.get(i);
		}
		return temp;
	}
	
	/**
	 * getChromSize calculates the total size of the given chromosome.
	 * 
	 * @param c the chromosome to get the size of
	 * @return temp the total size of the chromosome
	 */
	public static int getChromSize( boolean[] c )
	{
		int temp = 0;
		for(int i = 0; i < numItems; i++ )
		{
			if (c[i] == true)
				temp += sizes.get(i);
		}
		return temp;
	}
}