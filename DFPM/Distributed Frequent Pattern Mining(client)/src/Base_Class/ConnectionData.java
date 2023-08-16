package Base_Class;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ConnectionData implements Serializable {
    private static final long serialVersionUID = -6743567631108323096L;

    private HashMap<String, ArrayList<Integer>> Edge_Frequence = new HashMap<>(); //存放每种类型边的MNI,发送给master--C:1s--M:1r

    private HashMap<String,String> Edges_id = new HashMap<>(); //存放发送给各个slave的频繁边,key:边的String，value:边的id--M:2s--C:2r

    private int flag = 0; //扩展标志--M:3s--C:3r

    private int patternsize = 0;

    private HashMap<String,ArrayList<String>> send_pattern = new HashMap<>(); //存放发送给各个slave的候选模式--M:4s--C:4r --M:11s--C:11r

    private HashMap<String, HashMap<String, Ex_Node>> need_message = new HashMap<>(); //其他站点发来的请求需要的虚结点信息--C:5s--C:5r

    private HashMap<Integer,HashMap<String,Ex_Node>> trans_message = new HashMap<>(); //将请求信息转发给对应站点

    private HashMap<Integer,HashMap<String,Ex_Node>> trans_nodes = new HashMap<>(); //将已经查找到的请求信息转发给master

    private HashMap<String,Ex_Node> need_nodes = new HashMap<>();  //接收其他站点发来的回复信息--C:6s--C:6r

    private HashMap<String, HashMap<String, HashMap<Integer, Set<String>>>> send_external = new HashMap<>(); //发送给服务器的其他站点的虚拟匹配的MNI，key:对应站点，value：模式的Exteranal_MNI

    private HashMap<String,HashMap<Integer, Set<String>>> external_mni = new HashMap<>(); //接受其他站点传来的虚拟匹配--C:7s--C:7r --C:12s--C:12r

    private HashMap<String, ArrayList<Integer>> pattern_mnitable = new HashMap<>(); //将候选模式的MNI发送给master--C:8s--M:8r --C:13s--C:13r

    private HashMap<String, ArrayList<Integer>> frequentpatternid = new HashMap<>(); //master将频繁的模式id广播给slave--M:9s--C:9r  --M:14s--C:14r

    private int support = 0;

    private int positionsum = 0;

    public ConnectionData() {
    }

    public int getPositionsum() {
        return positionsum;
    }

    public void setPositionsum(int positionsum) {
        this.positionsum = positionsum;
    }

    public int getSupport() {
        return support;
    }

    public void setSupport(int support) {
        this.support = support;
    }

    public int getPatternsize() {
        return patternsize;
    }

    public void setPatternsize(int patternsize) {
        this.patternsize = patternsize;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public HashMap<String, ArrayList<Integer>> getEdge_Frequence() {
        return Edge_Frequence;
    }

    public void setEdge_Frequence(HashMap<String, ArrayList<Integer>> edge_Frequence) {
        Edge_Frequence = edge_Frequence;
    }

    public HashMap<String, String> getEdges_id() {
        return Edges_id;
    }

    public void setEdges_id(HashMap<String, String> edges_id) {
        Edges_id = edges_id;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public HashMap<String, ArrayList<String>> getSend_pattern() {
        return send_pattern;
    }

    public void setSend_pattern(HashMap<String, ArrayList<String>> send_pattern) {
        this.send_pattern = send_pattern;
    }

    public HashMap<String, HashMap<String, Ex_Node>> getNeed_message() {
        return need_message;
    }

    public void setNeed_message(HashMap<String, HashMap<String, Ex_Node>> need_message) {
        this.need_message = need_message;
    }

    public HashMap<String, Ex_Node> getNeed_nodes() {
        return need_nodes;
    }

    public void setNeed_nodes(HashMap<String, Ex_Node> need_nodes) {
        this.need_nodes = need_nodes;
    }

    public HashMap<String, HashMap<Integer, Set<String>>> getExternal_mni() {
        return external_mni;
    }

    public void setExternal_mni(HashMap<String, HashMap<Integer, Set<String>>> external_mni) {
        this.external_mni = external_mni;
    }

    public HashMap<String, ArrayList<Integer>> getPattern_mnitable() {
        return pattern_mnitable;
    }

    public void setPattern_mnitable(HashMap<String, ArrayList<Integer>> pattern_mnitable) {
        this.pattern_mnitable = pattern_mnitable;
    }

    public HashMap<String, ArrayList<Integer>> getFrequentpatternid() {
        return frequentpatternid;
    }

    public void setFrequentpatternid(HashMap<String, ArrayList<Integer>> frequentpatternid) {
        this.frequentpatternid = frequentpatternid;
    }

    public HashMap<Integer, HashMap<String, Ex_Node>> getTrans_message() {
        return trans_message;
    }

    public void setTrans_message(HashMap<Integer, HashMap<String, Ex_Node>> trans_message) {
        this.trans_message = trans_message;
    }

    public HashMap<Integer, HashMap<String, Ex_Node>> getTrans_nodes() {
        return trans_nodes;
    }

    public void setTrans_nodes(HashMap<Integer, HashMap<String, Ex_Node>> trans_nodes) {
        this.trans_nodes = trans_nodes;
    }

    public HashMap<String, HashMap<String, HashMap<Integer, Set<String>>>> getSend_external() {
        return send_external;
    }

    public void setSend_external(HashMap<String, HashMap<String, HashMap<Integer, Set<String>>>> send_external) {
        this.send_external = send_external;
    }
}
