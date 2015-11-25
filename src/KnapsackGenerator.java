import java.io.IOException;
import java.io.PrintWriter;
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
		try
		{
			Scanner inputReader = new Scanner(System.in);
			PrintWriter outputC = new PrintWriter("gen_c.txt");
			PrintWriter outputP = new PrintWriter("gen_p.txt");
			PrintWriter outputW = new PrintWriter("gen_w.txt");
			
			System.out.print("Choose a dataset type. (0 = Toy Problem, 1 = Random Problem): ");
			int answer = inputReader.nextInt();
			if( answer == 0 )
			{
				int temp = 0;
				int runCap = 0;
				int runVal = 0;
				PrintWriter outputS = new PrintWriter("gen_s.txt");
				
				for(int i = 0; i < 20; i++)		//Generate Optimal Values
				{
					temp = r.nextInt(20) + 30;
					runVal += temp;
					outputP.println(temp);
				}
				for(int i = 0; i < 80; i++)		//Generate Filler Values
				{
					temp = r.nextInt(20)+10;
					outputP.println(temp);
				}
				
				for(int i = 0; i < 20; i++)		//Generate Optimal Sizes
				{
					temp = r.nextInt(10)+20;
					runCap += temp;
					outputW.println(temp);
				}
				for(int i = 0; i < 80; i++)		//Generate Filler Sizes
				{
					temp = r.nextInt(10)+30;
					outputW.println(temp);
				}
				
				//Output Total Capacity
				outputC.println(runCap);

				//Output Optimal Solution
				for(int i = 0; i < 100; i++)
				{
					if(i < 20) outputS.println("1");
					else outputS.println("0");
				}
				
				//Output Capacity, Optimal Value, and Optimal Chromosome
				System.out.println("\nCapacity: "+runCap);
				System.out.println("Optimal Value: "+runVal);

				outputS.close();
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
				
				for(int i = 0; i < dataSize; i++)
				{
					percent = r.nextDouble();
					outputP.println((int)(percent * valRange + valOffset));
					percent += .3 * r.nextDouble();
					temp = (int)(percent*sizeRange + sizeOffset);
					runSize += temp;
					storedSize += ""+(temp)+"\n";
				}
				outputW.println(storedSize);
				outputC.println((runSize*(10+r.nextInt(5)-3)/dataSize));
			}//End of Random Dataset Generation
			inputReader.close();
			outputC.close();
			outputP.close();
			outputW.close();
		} catch (IOException e) {
			System.err.println(e);
		}
	}
}
