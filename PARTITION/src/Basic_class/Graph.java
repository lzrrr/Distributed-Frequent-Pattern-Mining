package Basic_class;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Graph {
    //点集：id:node
    private HashMap<String,Node> nodeset = new HashMap<>();
    //边集：edge
    private ArrayList<Edge> edgeset = new ArrayList<>();
    //每种类型边的权重
    private HashMap<String, Long> edge_weight = new HashMap<>();
    //统计各个分区的点数
    private HashMap<Integer,Integer> size_p = new HashMap<>();

    public Graph() {
    }

    public Graph(HashMap<String, Node> nodeset, ArrayList<Edge> edgeset, HashMap<String, Long> edge_weight) {
        this.nodeset = nodeset;
        this.edgeset = edgeset;
        this.edge_weight = edge_weight;
    }

    public void addNode(Node node){
        this.nodeset.put(node.getId(),node);
    }

    public void addEdge(Edge edge)
    {
        this.edgeset.add(edge);
    }

    public HashMap<String, Node> getNodeset() {
        return nodeset;
    }

    public void setNodeset(HashMap<String, Node> nodeset) {
        this.nodeset = nodeset;
    }

    public ArrayList<Edge> getEdgeset() {
        return edgeset;
    }

    public void setEdgeset(ArrayList<Edge> edgeset) {
        this.edgeset = edgeset;
    }

    public HashMap<String, Long> getEdge_weight() {
        return edge_weight;
    }

    public void setEdge_weight(HashMap<String, Long> edge_weight) {
        this.edge_weight = edge_weight;
    }

    public HashMap<Integer, Integer> getSize_p() {
        return size_p;
    }

    public void setSize_p(HashMap<Integer, Integer> size_p) {
        this.size_p = size_p;
    }
}
