package Base_Class;

import java.io.*;
import java.util.*;

public class Node implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private final String label;
    private HashMap<String, Set<String>> outgoingneighbers; //频繁点的出度邻居，key:标签，value:对应标签的邻居
    private HashMap<String, Set<String>> incomingneighbers; //频繁点的入度邻居，key:标签，value:对应标签的邻居
    private HashMap<String,Set<String>> neighbers; //频繁点的邻居结点，key:标签，value:对应标签的邻居
    private String p; //结点所在的站点号
    private boolean isvirtual; //该点是否是虚拟点
    //用于判断是否之前已经向虚拟点站点请求过该标签信息,只在前向扩展用到，后向扩展不需要
    private Set<String> virtual_outgoinglabel; //虚拟结点已验证过的其对应站点的出度邻居标签，在生成新模式MNI时，如果扩展点是虚拟点，先检查该虚拟点的对应标签是否验证过，如果之前已经请求验证过（virtual_outgoing.cantains(label)）,则不发送获取请求，如果没有则向目标站点发送请求，并将信息更新到该结点的outgoingneighbers
    private Set<String> virtual_incominglabel; //虚拟结点从其对应站点传送来的入度邻居标签
    private Set<String> virtual_label; //虚拟结点已经验证过的从其对应站点传送来的邻居标签



    public Node(String label) {
        this.label = label;
    }

    public Node(String id, String label) {
        this.id = id;
        this.label = label;
        this.outgoingneighbers = new HashMap<>();
        this.incomingneighbers = new HashMap<>();
        this.virtual_incominglabel = new HashSet<>();
        this.virtual_outgoinglabel = new HashSet<>();
        this.neighbers = new HashMap<>();
        this.virtual_label = new HashSet<>();
    }

    public Node(String id, String label, String p, boolean isvirtual) {
        this.id = id;
        this.label = label;
        this.p = p;
        this.isvirtual = isvirtual;
        this.outgoingneighbers = new HashMap<>();
        this.incomingneighbers = new HashMap<>();
        this.virtual_incominglabel = new HashSet<>();
        this.virtual_outgoinglabel = new HashSet<>();
        this.neighbers = new HashMap<>();
        this.virtual_label = new HashSet<>();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public boolean isIsvirtual() {
        return isvirtual;
    }

    public void setIsvirtual(boolean isvirtual) {
        this.isvirtual = isvirtual;
    }

    public Set<String> getVirtual_outgoinglabel() {
        return virtual_outgoinglabel;
    }

    public void setVirtual_outgoinglabel(Set<String> virtual_outgoinglabel) {
        this.virtual_outgoinglabel = virtual_outgoinglabel;
    }

    public Set<String> getVirtual_incominglabel() {
        return virtual_incominglabel;
    }

    public void setVirtual_incominglabel(Set<String> virtual_incominglabel) {
        this.virtual_incominglabel = virtual_incominglabel;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public HashMap<String, Set<String>> getOutgoingneighbers() {
        return outgoingneighbers;
    }

    public void setOutgoingneighbers(HashMap<String, Set<String>> outgoingneighbers) {
        this.outgoingneighbers = outgoingneighbers;
    }

    public HashMap<String, Set<String>> getIncomingneighbers() {
        return incomingneighbers;
    }

    public void setIncomingneighbers(HashMap<String, Set<String>> incomingneighbers) {
        this.incomingneighbers = incomingneighbers;
    }

    public HashMap<String, Set<String>> getNeighbers() {
        return neighbers;
    }

    public void setNeighbers(HashMap<String, Set<String>> neighbers) {
        this.neighbers = neighbers;
    }

    public Set<String> getVirtual_label() {
        return virtual_label;
    }

    public void setVirtual_label(Set<String> virtual_label) {
        this.virtual_label = virtual_label;
    }

    @Override
    public String toString() {
        return id +"-"+ label;
    }

    @Override
    public Node clone() {
        Node outer = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            outer = (Node) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return outer;
    }
}

