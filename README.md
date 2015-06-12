01-Knapsack Solvers
===================

This project includes both a genetic algorithm and a simulated annealing approach to solving the 01-Knapsack problem. In this problem, the goal is to take a set of P packages, each with a size S and value V, and select the correct subset of them that maximizes the total value of all selected packages, without allowing the total size of all those selected to surpass the knapsack capacity C.

Both take command-line user inputs, and are intended to be run on the included datasets, if moved to the root directory. The datasets prefixed "p_" were previously hosted at http://people.sc.fsu.edu/~jburkardt/datasets/knapsack_01/knapsack_01.html and have since been retired. A separate program (KnapsackGenerator) is included with this project which allows both toy problems and random problems to be generated in the same format. Some example output of this program is included in the data directory and prefixed "m_".

The fitness function for the genetic algorithm is a simple expression of the form f = V-(X*(P*(S-C)+O)), where:
 - V is the total value of all items selected in the chromosome.
 - S is the total size of all items selected.
 - C is the knapsack capacity.
 - X is 0 when S <= C, and 1 otherwise (to punish infeasible solutions)
 - P is a per-unit-size penalty, derived from the highest value per unit-size ratio of any single package in the dataset.
 - O is the penalty offset, calculated at about 1/3 of the value of all packages (designed to keep infeasibles from possibly outdoing the best feasibles found so far, without making them totally unfit)

The genetic algorithm allows the user to set:
 - The initial population size
 - The number of generations to run before termination
 - The mutation rate of chromosomes
 - The crossover rate of chromosomes

It also provides the following operators to choose from:
 - Roulette Selection - Use the fractional fitness of each chromosome to make a biased random selection for crossover.
 - Tournament Selection - Compare the fitness of two chromosomes randomly, without bias. Biasedly allow the more fit of the two to be selected K percent of the time.
 - N-Slice Crossover - Generate N random, unique integers (between 0 and P-2, and N < chromosome length). Take each integer to represent a random "slice" point at which to swap alternating segments of the two parents.
 - Uniform Crossover - Generate uniform-random bit strings which determine which of the two parent chromosomes each bit of the child chromosome is to be taken from.
 - N-Point Mutation - Invert N random points in the chromosome.
 - Bit-Inversion Mutation - Invert all bits in the chromosome.

The simulated annealing allows the testing parameters to be set at the start of each run. It includes a toggle which allows it to behave as a foolish hill-climber, and also provides the following settings:
 - The user can choose between an N-Point Perturbation, which inverts N random, unique points in the current solution, and N-Slice Perturbation, which picks N unique, random slice points (as above) and inverts half of the resulting segments.
 - The user can set the cooling factor alpha, the time factor beta, the initial temperature T0, and the initial number of iterations I0.
 - The user can set the fraction of the initial temperature at which to terminate the annealing process.
