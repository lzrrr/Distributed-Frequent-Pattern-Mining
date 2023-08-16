# Boosting Distributed Frequent Pattern Mining with Tailored Partitions
# Overview
The algorithm is a tailored k-way partitioning algorithm based on this metric then, we develop a distributed algorithm along with its optimizations to discover frequent patterns on tailored partitions. By making comparisons with existing counterparts in real-life and synthetic graphs, we experimentally verify that our algorithms are superior in terms of effectiveness as well as efficiency and scalability.
# Contents
Data ---All test dataset files

DFPM --- We propose an algorithm which can be divided into a client and a master.

ScaleMine --- A major comparsion algorithm.

# Experiment setting
There are a few dependencies which must be satisfied in order to run DMINER.
1. JDK11.
2. JGraphT1.5.0(https://jgrapht.org/).
# Installation

# Running
1. Distributed Frequent Pattern Mining(master) is a server program, before running, it is necessary to specify the number of client nodes, threshold size and top-K value size.
2. Distributed Frequent Pattern Mining(client) is a client program, before running, you need to specify the client serial number, maximum number of threads, client ip address, split subgraph path.
3. Mining process: first run the server program in the server node, and then start the client program from 1 to n in each client node.
# Output
DMINER outputs the elapsed time,datashipment and frequent patterns on the standard output.
