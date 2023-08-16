package Base_Class;

import org.jgrapht.graph.DefaultUndirectedGraph;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * 完整大图：其表示了整个输入图的数据
 */
public class Graph extends DefaultUndirectedGraph<Node,Edge> implements Serializable{
    private static final long serialVersionUID = 4L;

    private HashMap<String, Node> NodeSet;				//	maintains the whole set of nodes in the Graph.

    private Set<String> External_id; //存放虚拟点的id

    public Graph(Class edgeClass){
        super(edgeClass);
        this.NodeSet = new HashMap<String, Node>();
        this.External_id = new HashSet<>();
    }

    public HashMap<String, Node> getNodeSet(){
        return this.NodeSet;
    }

    public void addNode(Node node){
        this.NodeSet.put(node.getId(),node);
        this.addVertex(node);
    }

    public int getNodeSize(){
        return this.NodeSet.size();
    }

    public void setNodeSet(HashMap<String, Node> NodeSet){
        this.NodeSet = NodeSet;
    }

    public Set<String> getExternal_id() {
        return External_id;
    }

    public void setExternal_id(Set<String> external_id) {
        External_id = external_id;
    }

    public Node findNode(String id){
        return this.NodeSet.get(id);
    }

    @Override
    public String toString() {
        return "{G:"+"V="+this.vertexSet().size()+";E="+this.edgeSet().size()+"}";
    }

    @Override
    public Graph clone() {
        Graph outer = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            outer = (Graph) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return outer;
    }

}

