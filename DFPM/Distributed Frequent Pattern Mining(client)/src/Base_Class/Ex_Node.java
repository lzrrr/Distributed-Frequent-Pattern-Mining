package Base_Class;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

/**
 * 请求点，包含了虚拟节点id和需要的信息
 */
public class Ex_Node implements Serializable {
    private static final long serialVersionUID = 5L;
    private String id; //结点id
    private HashMap<String, Set<Send_Node>> outgoingneighbers; //频繁点的出度邻居，key:标签，value:对应标签的邻居
    private HashMap<String, Set<Send_Node>> incomingneighbers; //频繁点的入度邻居，key:标签，value:对应标签的邻居
    private HashMap<String,Set<Send_Node>>  neighbers; //用于无向图

    public Ex_Node(String id) {
        this.id = id;
        this.outgoingneighbers = new HashMap<>();
        this.incomingneighbers = new HashMap<>();
        this.neighbers = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, Set<Send_Node>> getNeighbers() {
        return neighbers;
    }

    public void setNeighbers(HashMap<String, Set<Send_Node>> neighbers) {
        this.neighbers = neighbers;
    }

    public HashMap<String, Set<Send_Node>> getOutgoingneighbers() {
        return outgoingneighbers;
    }

    public void setOutgoingneighbers(HashMap<String, Set<Send_Node>> outgoingneighbers) {
        this.outgoingneighbers = outgoingneighbers;
    }

    public HashMap<String, Set<Send_Node>> getIncomingneighbers() {
        return incomingneighbers;
    }

    public void setIncomingneighbers(HashMap<String, Set<Send_Node>> incomingneighbers) {
        this.incomingneighbers = incomingneighbers;
    }
}
