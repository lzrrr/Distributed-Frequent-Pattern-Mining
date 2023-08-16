package Mining;

import Base_Class.*;

import java.util.*;

public class Frequent_Edge_topk {

    //main传入
    public int support; //最小支持度
    public ArrayList<HashMap<String, Pattern_ex>> T; //code-tree,用于存储每一层对应的频繁模式树
    public HashMap<String,Set<String>> Seed_map; //seed边的one-hopmap,用于加速候选模式生成
    public HashMap<String, Pattern_ex> patternMap ; //用于扩展下一层的父模式
    public HashMap<String,ArrayList<Integer>> Edge_Frequence; //从slave发送来的各个边模式的MNItalble
    public HashMap<String,String> Edges_id; //存储发送给slave的频繁边string和id

    //本地构建
    public HashMap<String,Integer> FrequentEdge_Set = new HashMap<>(); //所有频繁边String的集合

    public Frequent_Edge_topk(int support, ArrayList<HashMap<String, Pattern_ex>> t, HashMap<String, Set<String>> seed_map, HashMap<String, Pattern_ex> patternMap, HashMap<String, ArrayList<Integer>> edge_Frequence, HashMap<String,String> Edges_id) {
        this.support = support;
        T = t;
        Seed_map = seed_map;
        this.patternMap = patternMap;
        Edge_Frequence = edge_Frequence;
        this.Edges_id = Edges_id;
        Get_TypeEdge_Frequent();
        Get_FrequentEdgepattern();
    }

    /**
     * 根据从slave收到的边的MNItable，筛选出频繁边并生成频繁边模式
     */
    public void Get_TypeEdge_Frequent(){

        for (String key : Edge_Frequence.keySet()) {
            //新建顶点集，并获取对应模式边集
            ArrayList<Integer> MNISet = Edge_Frequence.get(key);

            //统计MNI大小
            int MNI = Math.min(MNISet.get(0),MNISet.get(1));
            if(MNI>=support)
            {
                Get_Seedmap(key); //构建seedmap
                //生成频繁边模式
                FrequentEdge_Set.put(key,MNI);
            }
        }

    }

    /**
     * 构建频繁点的one-hopmap加速候选模式生成
     * @param key 频繁边的string
     */
    public void Get_Seedmap(String key)
    {
        String[] arr = key.split("→");
        if(!Seed_map.containsKey(arr[0]))
        {
            Set<String> set = new HashSet<>();
            Seed_map.put(arr[0],set);
        }
        if(!Seed_map.containsKey(arr[1]))
        {
            Set<String> set = new HashSet<>();
            Seed_map.put(arr[1],set);
        }
        Seed_map.get(arr[0]).add(arr[1]);
        Seed_map.get(arr[1]).add(arr[0]);
    }


    /**
     * 生成频繁边模式的模式图，并存入code-tree第一层
     */
    public void Get_FrequentEdgepattern(){
        SingleID singleID = SingleID.getInstance();
        String layer = singleID.getLayer();
        singleID.ZERO();
        //只需要遍历其key即label即可
        for (String label : FrequentEdge_Set.keySet()) {
            //使用Entry对象中的方法getKey()和getValue()获取键与值
            String[] arr = label.split("→");
            String a = arr[0];// source
            String b = arr[1];// target

            Node A = new Node(a);// source
            Node B = new Node(b);// target
            Edge E = new Edge(A, B);
            //新建一个pattern
            //组合ID
            String ID = layer + singleID.getId();
            Pattern_ex pattern = new Pattern_ex(ID, null);
            //构建pattern图
            pattern.patternGraphAddNode(A);// source在前
            pattern.patternGraphAddNode(B);// target在后
            pattern.patternGraphAddEdge(A, B, E);
            //生成前向边和每列对应的标签
            //设置infoList
            String edge_info = "0 out 1";
            pattern.getForwardedges().add(edge_info);
            pattern.getColumn_label().add(A.getLabel());
            pattern.getColumn_label().add(B.getLabel());
            pattern.setSupport(FrequentEdge_Set.get(label));

            //最后把pattern加入pattern集合中
            patternMap.put(ID, pattern);
            Edges_id.put(label,ID);
        }
        //将频繁边模式图存入code-tree
        T.add(patternMap);
    }

}
