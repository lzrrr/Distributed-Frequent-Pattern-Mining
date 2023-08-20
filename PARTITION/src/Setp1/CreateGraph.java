package Setp1;

import Basic_class.Edge;
import Basic_class.Graph;
import Basic_class.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CreateGraph {
    public static Graph Get_newGraph(String file) throws IOException {
        System.out.println("开始建图");
        Graph g = new Graph();

        HashMap<String, Set<Edge>> TypeEdge_Instance= new HashMap<>(); //存储各个类型边的实例

        BufferedReader br = new BufferedReader(new FileReader(file)); //读取数据
        String line;


        //向图中写入顶点和边
        while ((line=br.readLine())!=null)
        {
            String[] arr =line.split(" ");
            if(arr[0].equals("v"))
            {
                Node node = new Node(arr[1],arr[2],1);
                g.addNode(node);
            }
            else if(arr[0].equals("e"))
            {
                Edge e = new Edge();
                String a = arr[1];
                String b = arr[2];
                Node snode = g.getNodeset().get(a);
                Node tnode = g.getNodeset().get(b);
                String in_label = snode.getLabel();
                String out_label = tnode.getLabel();
                if(Integer.parseInt(in_label)>Integer.parseInt(out_label))
                {
                    String temp = a;
                    a =b;
                    b=temp;
                    String temps = in_label;
                    in_label = out_label;
                    out_label = temps;
                }
                e = new Edge(a,b);
                g.addEdge(e);
                String label = Getedgetype(e,in_label,out_label);
                if(!TypeEdge_Instance.containsKey(label))
                {
                    Set<Edge> set = new HashSet<>();
                    set.add(e);
                    TypeEdge_Instance.put(label,set);
                }
                else
                {
                    TypeEdge_Instance.get(label).add(e);
                }
            }
        }
//        System.out.println("顶点和边加载完成");
        //生成顶点的邻居集和邻居集组成的边类型以及边的频次统计
        Initialfrequt(TypeEdge_Instance,g.getNodeset());
//        System.out.println("邻居加载完成");
//        System.out.println("节点数："+g.getNodeset()+" 边数："+g.getEdgeset());

        return g;
    }

    /**
     * 获取边类型
     */
    public static String Getedgetype(Edge e,String in,String out)
    {
        String label= in + "-" + out;
        e.setElabel(label);
        return label;
    }

    /**
     * 将边频次统计、并赋值给每一个节点
     */
    public static void Initialfrequt(HashMap<String,Set<Edge>>TypeEdge_Instance,HashMap<String, Node> NodeSet)
    {
        for (String key : TypeEdge_Instance.keySet()) {
            //创建计数集合
            Set<String> setA = new HashSet<>();
            Set<String> setB = new HashSet<>();
            //新建顶点集，并获取对应模式边集
            Set<Edge> edgeSet = TypeEdge_Instance.get(key);
            //遍历对应边表
            for (Edge e : edgeSet) {
                setA.add(e.getSid());
                setB.add(e.getTid());
            }
            //统计MNI大小
            long MNI = Math.min(setA.size(),setB.size());
            for (Edge e : edgeSet)
            {
                e.setWight(MNI);
            }
            System.out.println(key+" 频次 "+MNI);
        }
    }
}
