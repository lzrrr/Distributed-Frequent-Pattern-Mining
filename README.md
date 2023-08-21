# Boosting Distributed Frequent Pattern Mining with Tailored Partitions
# Overview
The algorithm is a tailored k-way partitioning algorithm based on this metric then, we develop a distributed algorithm along with its optimizations to discover frequent patterns on tailored partitions. By making comparisons with existing counterparts in real-life and synthetic graphs, we experimentally verify that our algorithms are superior in terms of effectiveness as well as efficiency and scalability.
# Contents
Data ---All test dataset files

PARTITION --- the tailored k-way partitioning algorithm.

DFPM(DMINER) --- We propose an algorithm which can be divided into a client and a master.

ScaleMine --- A major comparsion algorithm.

# Experiment setting
There are a few dependencies which must be satisfied in order to run DMINER.
1. JDK 11.
2. JGraphT 1.5.0(https://jgrapht.org/).

# Running
1. Running PARTITION to obtain a partition.
2. Distributed Frequent Pattern Mining(master) is a server program, before running, it is necessary to specify the number of client nodes, threshold size and top-K value size.
3. Distributed Frequent Pattern Mining(client) is a client program, before running, you need to specify the client serial number, maximum number of threads, client ip address, split subgraph path.
4. Mining process: first run the server program in the server node, and then start the client program from 1 to n in each client node.

# Examples
Partition:
1. int num_nodes = 50; //Maximum number of nodes per component after coarsening
2. int part = 4; //The number of components
3. int max_nugrow = 50; //Refining operation no longer proceeds if the total gain cannot be enlarged after successive max_nugrow operations
4. Graph graph = CreateGraph.Get_newGraph("path"); //Path of the input graph

DMINER:

--Server:
1. int numofclient = 2; //The number of Clients
2. int support = 2000; //The support threshold

--Client:
1. String ipadd = "127.0.0.1";//The ip address
2. String position = "1"; //Client id;
3. int corenum = 8;//Maximum number of threads
4. CreateGraph createGraph = new CreateGraph("path", position);//Path of the component

# Output
DMINER outputs the elapsed time,datashipment and frequent patterns on the standard output.
