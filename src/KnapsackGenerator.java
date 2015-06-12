import java.util.Random;
import java.util.Scanner;

/**
 * KnapsackGenerator is a simple program that creates either toy or random datasets for the 01-knapsack problem.
 * For a toy dataset, the first twenty values always comprise the optimal, because their value range is higher,
 * their weight range is lower, and the knapsack capacity is set to their total size.
 * 
 * @author Anton Ridgway
 */
public class KnapsackGenerator
{
	public static void main(String[] args)
	{
		Random r = new Random();
		Scanner inputReader = new Scanner(System.in);
		
		System.out.print("Choose a dataset type. (0 = Toy Problem, 1 = Random Problem): ");
		int answer = inputReader.nextInt();
		if( answer == 0 )
		{
			int temp = 0;
			int runCap = 0;
			int runVal = 0;
			
			System.out.println("Values: ");
			for(int i = 0; i < 20; i++)		//Generate Optimal Values
			{
				temp = r.nextInt(20) + 30;
				runVal += temp;
				System.out.println(temp);
			}
			for(int i = 0; i < 80; i++)		//Generate Filler Values
			{
				temp = r.nextInt(20)+10;
				System.out.println(temp);
			}
			
			System.out.println("\n\nSizes: ");
			for(int i = 0; i < 20; i++)		//Generate Optimal Sizes
			{
				temp = r.nextInt(10)+20;
				runCap += temp;
				System.out.println(temp);
			}
			for(int i = 0; i < 80; i++)		//Generate Filler Sizes
			{
				temp = r.nextInt(10)+30;
				System.out.println(temp);
			}
	
			//Output Capacity, Optimal Value, and Optimal Chromosome
			System.out.println("\n\n\nCapacity: "+runCap);
			System.out.println("Optimal Value: "+runVal);
			System.out.println("\n\n\nOptimal Chromosome: ");
			for(int i = 0; i < 100; i++)
			{
				if(i < 20) System.out.println("1");
				else System.out.println("0");
			}
		}//End of Toy Dataset Generation
		
		else
		{			
			System.out.print("Choose a dataset size: ");
			int dataSize = inputReader.nextInt();
			
			System.out.print("Choose an average package value: ");
			int valOffset = inputReader.nextInt();
			System.out.print("How far to deviate: ");
			int valRange = inputReader.nextInt();
			valOffset -= valRange;
			valRange *= 2;
			
			System.out.print("Choose an average package size: ");
			int sizeOffset = inputReader.nextInt();
			System.out.print("How far to deviate: ");
			int sizeRange = inputReader.nextInt();
			sizeOffset -= sizeRange;
			sizeRange *= 2;

			double percent = 0;
			int temp = 0;
			int runSize = 0;
			String storedSize = "";
			
			System.out.println("\nValues:");
			for(int i = 0; i < dataSize; i++)
			{
				percent = r.nextDouble();
				System.out.println((int)(percent * valRange + valOffset));
				percent += .3 * r.nextDouble();
				temp = (int)(percent*sizeRange + sizeOffset);
				runSize += temp;
				storedSize += ""+(temp)+"\n";
			}
			
			System.out.println("\n\nSizes:\n"+storedSize);
			
			System.out.println("Capacity: "+(runSize*(10+r.nextInt(5)-3)/dataSize));
		}//End of Random Dataset Generation
		inputReader.close();
	}
}
