package Base_Class;

import org.jgrapht.graph.DefaultUndirectedGraph;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * 模式图：用于候选模式生成，其结点顺序是按照新结点加入顺序存放，且每个模式图对应一个模式
 */
public class Graph_ex extends DefaultUndirectedGraph<Node,Edge> implements Serializable {
    private static final long serialVersionUID = 5L;

    private LinkedHashMap<Integer, Node> NodeSet_sequence; //用于候选模式生成，该点集按照模式点的扩展顺序存储, key:模式点的加入次序，value:模式点

    public Graph_ex(Class edgeClass){
        super(edgeClass);
        this.NodeSet_sequence = new LinkedHashMap<>();
    }

    public LinkedHashMap<Integer, Node> getNodeSet_sequence(){
        return this.NodeSet_sequence;
    }

    /**
     * 用于候选模式生成，如果一个点可以扩展，将其加入模式图
     * @param node
     */
    public void addNode(Node node){
        this.NodeSet_sequence.put(this.getNodeSize(),node);
        this.addVertex(node);
    }

    public int getNodeSize(){
        return this.NodeSet_sequence.size();
    }

    public void setNodeSet_sequence(LinkedHashMap<Integer, Node> NodeSet){
        this.NodeSet_sequence = NodeSet;
    }

    public Node findNode(int id){
        return this.NodeSet_sequence.get(id);
    }

    @Override
    public String toString() {
        return "{G:"+"V="+this.vertexSet().size()+";E="+this.edgeSet().size()+"}";
    }

    @Override
    public Graph_ex clone() {
        Graph_ex outer = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            outer = (Graph_ex) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return outer;
    }


}
