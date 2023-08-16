package Work;

import Base_Class.Edge;
import Base_Class.Graph;
import Base_Class.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CreateGraph {
    public String path;
    public Graph graph = new Graph(Edge.class);
    public String position = ""; //站点id

    public CreateGraph(String path,String position) throws IOException {
        this.path = path;
        this.position = position;
        CreateNewGraph();
    }

    public void CreateNewGraph() throws IOException {
        //创建标签集
        BufferedReader brr = new BufferedReader(new FileReader(path));
        String line;
        while ((line = brr.readLine())!=null)
        {
            String[] arr = line.split(" ");
            if(arr[0].equals("v"))
            {
                String a = arr[1];
                String b = arr[2];
                String p = arr[3];
                Node node = new Node(a,b);
                if(p.equals(position))
                {
                    node.setP(p);
                    node.setIsvirtual(false);
                    graph.addNode(node);
                }
                else
                {
                    node.setP(p);
                    node.setIsvirtual(true);
                    graph.addNode(node);
                    graph.getExternal_id().add(node.getId());
                }
            }
            else if(arr[0].equals("e"))
            {
                String a = arr[1];
                String b = arr[2];
                Node node1 = graph.getNodeSet().get(a);
                Node node2 = graph.getNodeSet().get(b);
                Edge e = new Edge(node1,node2);
                graph.addEdge(node1,node2,e);
            }
        }
        brr.close();
        System.out.println("Create Graph success! nodes:"+graph.getNodeSize()+" edges:"+graph.edgeSet().size());
    }
}
