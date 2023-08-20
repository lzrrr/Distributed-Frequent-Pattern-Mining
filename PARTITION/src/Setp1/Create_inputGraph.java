package Setp1;

import Basic_class.Edge;
import Basic_class.Graph;
import Basic_class.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class Create_inputGraph {
   public static Graph create_G(String path) throws IOException {
       //点集：id:node
       HashMap<String, Node> nodeset = new HashMap<>();
       //边集：edge
       ArrayList<Edge> edgeset = new ArrayList<>();
       HashMap<String, Long> edge_weight = new HashMap<>();

       BufferedReader br = new BufferedReader(new FileReader(path));
       String line;
       while ((line=br.readLine())!=null)
       {
           String[] arr = line.split(" ");
           if(arr[0].equals("v"))
           {
               Node node = new Node(arr[1],1);
               nodeset.put(arr[1],node);
           }
           else if(arr[0].equals("e"))
           {
               long weight = Long.parseLong(arr[3]);
               String id1 = arr[1];
               String id2 = arr[2];
               if(Integer.parseInt(id1)>Integer.parseInt(id2))
               {
                   String temp = id1;
                   id1 = id2;
                   id2 = temp;
               }
               String elabel = id1+"->"+id2;
               Edge edge = new Edge(arr[1],arr[2],elabel,weight);
               edgeset.add(edge);
               edge_weight.put(elabel,weight);
               //创建结点的邻接表
               String sid = edge.getSid();
               String tid = edge.getTid();
               Node snode = nodeset.get(sid);
               Node tnode = nodeset.get(tid);
               snode.getNeighber().put(tid,edge.getElabel());
               tnode.getNeighber().put(sid,edge.getElabel());
           }
       }
       return new Graph(nodeset,edgeset,edge_weight);
   }


}
