package Main;

import Basic_class.Edge;
import Basic_class.Graph;
import Basic_class.Node;
import Setp1.Coarsing_phase;
import Setp1.CreateGraph;
import Setp1.Create_inputGraph;
import Setp2.Inital_partition;
import setp3.Uncoarsing_phase;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class partition_exe {
    public static void main(String[] args) throws IOException {
        int num_nodes = 50; //分区节点阈值
        int part = 4; //分区数
        num_nodes = num_nodes*part;
        int max_nugrow = 50; //最大无增益移动次数

        ArrayList<Graph> graphs = new ArrayList<>();//存放每一层的图
        long Start1 = System.currentTimeMillis();
        Create_inputGraph create_inputGraph = new Create_inputGraph();
        Graph graph = CreateGraph.Get_newGraph(".\\src\\data\\mico.lg");
        graphs.add(graph);
        long end1 = System.currentTimeMillis();
        System.out.println("time: "+(end1-Start1));

        //进行粗化操作
        int level = 0;
        int g_size = graph.getNodeset().size();
        while (g_size>num_nodes) {
            //取出上一层的图
            Graph lastg = graphs.get(level);
            long Start = System.currentTimeMillis();
            Graph coreseG = Coarsing_phase.coarse(lastg, level++);
            graphs.add(coreseG);
            g_size = coreseG.getNodeset().size();
            long end = System.currentTimeMillis();
            System.out.println("time: " + (end - Start));
            //如果图的大小缩减比例小于20%,则终止
            int last_size = lastg.getNodeset().size();
            double radio = (double) last_size/g_size;
            System.out.println("缩减比例："+radio);
            System.out.println("节点数： "+g_size);

            //3.查看压缩图的点、边，是否正确
//            for (String id : coreseG.getNodeset().keySet())
//            {
//                System.out.print("id: "+id+" ");
//                Node node = coreseG.getNodeset().get(id);
//                System.out.print("weight: "+node.getWeight()+" ");
//                for (String inid : node.getMatchset())
//                {
//                    System.out.print(" inclue: "+inid);
//                }
//                System.out.println();
//            }
//            for (Edge e : coreseG.getEdgeset())
//            {
//                System.out.println(e.getSid()+"->"+e.getTid()+"weight"+e.getWight());
//            }

            if(radio<=1.25)
            {
                break;
            }
        }
        System.out.println("层数："+level);

        //4.进行初始划分--------
        long start2 = System.currentTimeMillis();
        int node_size = graph.getNodeset().size();
        Inital_partition.inital_part(graphs.get(level),node_size,part);
        long end2 = System.currentTimeMillis();
        System.out.println("初始划分time: "+ (end2-start2));

//        for (int i=0;i<part;++i)
//        {
//            System.out.println("第"+i+"份大小： "+graphs.get(level).getSize_p().get(i));
//        }
//        for (String id : graphs.get(level).getNodeset().keySet())
//        {
//            System.out.print("id: "+id+" ");
//            Node node = graphs.get(level).getNodeset().get(id);
//            System.out.print("weight: "+node.getWeight()+" ");
//            for (String inid : node.getMatchset())
//            {
//                System.out.print(" inclue: "+inid);
//            }
//            System.out.print(" color"+node.getColor());
//            System.out.println();
//        }
        System.out.println("---------");

        //5.精确划分-------------
        int maxnum = (int) (graph.getNodeset().size() / part * 1.03);
        int minnum = (int) (graph.getNodeset().size() / part * 0.97);
        System.out.println("限制："+maxnum +" "+minnum);
        while (level>0)
        {
            Uncoarsing_phase.uncoarsing(graphs.get(level),graphs.get(level-1),max_nugrow,maxnum,minnum);
            level--;
        }

        //6.输出分区结果
        BufferedWriter bw = new BufferedWriter(new FileWriter(".\\src\\output\\partition.txt"));
        HashMap<Integer,Integer> color_size = new HashMap<>();
        for (Node n : graph.getNodeset().values())
        {
            String nid = n.getId();
            int ncolor = n.getColor();
            //写入点数和边数
            bw.write(nid+" ");
            bw.write(Integer.toString(ncolor));
            bw.newLine();
            bw.flush();
            if(!color_size.containsKey(ncolor))
            {
                int s = 1;
                color_size.put(ncolor,s);
            }
            else
            {
                int s = color_size.get(ncolor);
                color_size.put(ncolor,s+1);
            }
        }
        bw.close();
        System.out.println(color_size);
    }
}
