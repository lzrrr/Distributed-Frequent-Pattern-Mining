package Basic_class;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Node implements Comparable<Node>{

    private String id;
    private int weight; //结点权重，单结点为1，多结点为sum
    private String label;
    private Set<String> matchset; //存放该结点融合的上一层结点的id
    private int color; //当前点的分区
    private long Dvalue; //用于初始化分区，与当前分区的外联权重-内联权重
    private HashMap<String,String> neighber; //存放结点邻居id以及和邻居构成的边label(id小->id大)
    private boolean isfixed; //标注该点是否被修改过
    private int tempcolor; //如果当前结点被存入Sv,(表示可能有更好的分区),优先访问结点的暂时分区
    private HashMap<Integer,Long> E_value; //存放与邻居分区形成的外联内联值


    public Node() {
    }

    public Node(String id, String label,int weight) {
        this.id = id;
        this.weight = weight;
        this.label = label;
        this.matchset = new HashSet<>();
        this.color = -1;
        this.Dvalue = 0;
        this.neighber = new HashMap<>();
        this.isfixed = false;
        this.tempcolor = -1;
        this.E_value = new HashMap<>();
    }

    public Node(String id, int weight) {
        this.id = id;
        this.weight = weight;
        this.matchset = new HashSet<>();
        this.color = -1;
        this.Dvalue = 0;
        this.neighber = new HashMap<>();
        this.isfixed = false;
        this.tempcolor = -1;
        this.E_value = new HashMap<>();
    }

    public Node(String id, int wight, Set<String> matchset) {
        this.id = id;
        this.weight = wight;
        this.matchset = matchset;
        this.color = -1; //默认分区为-1（表示尚未划分的结点）
        this.Dvalue = 0; //初始化值为0
        this.neighber = new HashMap<>();
        this.isfixed = false;
        this.tempcolor = -1;
        this.E_value = new HashMap<>();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public Set<String> getMatchset() {
        return matchset;
    }

    public void setMatchset(Set<String> matchset) {
        this.matchset = matchset;
    }

    public Long getDvalue() {
        return Dvalue;
    }

    public void setDvalue(Long dvalue) {
        Dvalue = dvalue;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public HashMap<String,String> getNeighber() {
        return neighber;
    }

    public void setNeighber(HashMap<String,String> neighber) {
        this.neighber = neighber;
    }

    public int getTempcolor() {
        return tempcolor;
    }

    public void setTempcolor(int tempcolor) {
        this.tempcolor = tempcolor;
    }

    public HashMap<Integer, Long> getE_value() {
        return E_value;
    }

    public void setE_value(HashMap<Integer, Long> e_value) {
        E_value = e_value;
    }

    public boolean isIsfixed() {
        return isfixed;
    }

    public void setIsfixed(boolean isfixed) {
        this.isfixed = isfixed;
    }

    @Override
    public int compareTo(Node o) {
        return o.getDvalue().compareTo(this.getDvalue());
    }
}
