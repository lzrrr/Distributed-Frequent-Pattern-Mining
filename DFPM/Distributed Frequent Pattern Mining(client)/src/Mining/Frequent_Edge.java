package Mining;

import Base_Class.*;

import java.util.*;

public class Frequent_Edge {

    //main传入
    public Graph graph;
    public HashMap<String,ArrayList<Integer>> Edge_Frequence;//key:边的类型：source->target,value:边的实结点频次


    //本地构建
    public HashMap<String, Set<Edge>> TypeEdge_Instance = new HashMap<>(); //每种类型边的实例，String:source->target
    public Set<String> Frequentnodes_Set = new HashSet<>(); //所有频繁点的集合
    public HashMap<String, ArrayList<Set<String>>> Edge_MNItable = new HashMap<>(); //所有边的MNItable

    public Frequent_Edge(Graph graph,HashMap<String,ArrayList<Integer>> Edge_Frequence) {
        this.graph = graph;
        this.Edge_Frequence = Edge_Frequence;
        Get_TypeEdge_Instance();
        Get_TypeEdge_Frequent();
    }

    //将边频次发送给master，并等待master返回频繁边模式
    public void Send_edgefrequence()
    {
        //send(Edge_Frequence);

        //recive Edgepattern;
    }


    /**
     * 统计每种类型边的实例
     */
    public void Get_TypeEdge_Instance()
    {
        for (Edge e : graph.edgeSet()) {

            String label1;
            String label2;
            String label;

            label1 = e.getSourceLabel() + "→" + e.getTargetLabel();
            label2 = e.getTargetLabel() + "→" + e.getSourceLabel();

            String labeln1 = e.getSourceLabel();
            String labeln2 = e.getTargetLabel();

            //去除标签一样的
//            if(labeln1.equals(labeln2))
//            {
//
//                continue;
//            }

            int i1 = Integer.parseInt(labeln1);
            int i2 = Integer.parseInt(labeln2);

            if(i1<=i2) //标签都遵循小的在前
            {
                label = label1;
                if (!TypeEdge_Instance.containsKey(label)) {
                    TypeEdge_Instance.put(label, new HashSet<>());
                }
                Set<Edge> edgeSet = TypeEdge_Instance.get(label);
                edgeSet.add(e);
                if(i1==i2) //当标签相同时,对称结构点左右各存一次
                {
                    edgeSet.add(new Edge(e.getTarget(),e.getSource()));
                }
            }
            else
            {
                label = label2;
                if (!TypeEdge_Instance.containsKey(label)) {
                    TypeEdge_Instance.put(label, new HashSet<>());
                }
                Set<Edge> edgeSet = TypeEdge_Instance.get(label);
                edgeSet.add(new Edge(e.getTarget(),e.getSource()));
            }
        }
    }

    /**
     * 统计边类型，采用MNI计数 , 并存入频繁边的MNItable
     */
    public void Get_TypeEdge_Frequent(){

        for (String key : TypeEdge_Instance.keySet()) {
            //创建计数集合
            Set<String> setA = new HashSet<>();
            Set<String> setB = new HashSet<>();
            Set<String> setA_i = new HashSet<>();
            Set<String> setB_i = new HashSet<>();
            //新建顶点集，并获取对应模式边集
            Set<Edge> edgeSet = TypeEdge_Instance.get(key);
            //遍历对应边表
            for (Edge e : edgeSet) {
                setA.add(e.getSource().getId());
                setB.add(e.getTarget().getId());
                if(!e.getSource().isIsvirtual())
                {
                    setA_i.add(e.getSource().getId());
                }
                if(!e.getTarget().isIsvirtual())
                {
                    setB_i.add(e.getTarget().getId());
                }
            }
                //存入边的MNItable
                ArrayList<Set<String>> list = new ArrayList<>();
                list.add(setA);
                list.add(setB);
                Edge_MNItable.put(key,list);
                //获取每种类型边的本地支持度，只计算实结点的个数
                ArrayList<Integer> MNIlist = new ArrayList<>();
                MNIlist.add(setA_i.size());
                MNIlist.add(setB_i.size());
                Edge_Frequence.put(key,MNIlist);
        }
    }

    /**
     * 将master发来的频繁边类型和对应模式id转换成频繁边模式
     */
    public void Get_Frequent_Edgepattern(HashMap<String,String> Edges_id,HashMap<String,Pattern> pattern_map){

        for (String type : Edges_id.keySet())
        {
            //使用Entry对象中的方法getKey()和getValue()获取键与值
            String[] arr = type.split("→");
            String a = arr[0];// source
            String b = arr[1];// target
            //新建一个pattern
            Pattern pattern = new Pattern(Edges_id.get(type), null);
            //生成前向边和每列对应的标签
            //设置infoList
            String edge_info = "0 out 1";
            pattern.getForwardedges().add(edge_info);
            pattern.getColumn_label().add(a);
            pattern.getColumn_label().add(b);
            //设置MNI_List
            pattern.setMNI_List(Edge_MNItable.get(type));
            pattern_map.put(pattern.getId(),pattern);
            //构建one-hop map
            //创建计数集合
            Set<String> setA = new HashSet<>();
            Set<String> setB = new HashSet<>();
            //新建顶点集，并获取对应模式边集
            Set<Edge> edgeSet = TypeEdge_Instance.get(type);
            //遍历对应边表
            for (Edge e : edgeSet) {
                setA.add(e.getSource().getId());
                setB.add(e.getTarget().getId());
            }
            Get_Fnodes(setA,setB); //获取频繁点集合
        }
        GetOnehopmap(); //生成one-hop
    }


    /**
     * 获取频繁点的集合
     */
    public void Get_Fnodes(Set<String> setA,Set<String> setB){
        Frequentnodes_Set.addAll(setA);
        Frequentnodes_Set.addAll(setB);
    }

    /**
     * 生成点的one_hopmap,直接存入频繁点的类中
     */
    public void GetOnehopmap()
    {
        for(String id : Frequentnodes_Set)
        {
            ArrayList<Map<String,Set<String>>> list = new ArrayList<>();
            HashMap<String,Set<String>> Map_n = new HashMap<>();
            Node node = graph.getNodeSet().get(id);
            Set<Edge> edgeSet = graph.edgesOf(node);

            for (Edge edge : edgeSet)
            {
                String out_id = edge.getTarget().getId();
                String in_id = edge.getSource().getId();
                if(out_id!=id)
                {
                    String out_label = edge.getTargetLabel();
                    if(Frequentnodes_Set.contains(out_id))
                    {
                        if(!Map_n.containsKey(out_label))
                        {
                            Set<String> set = new HashSet<>(); //如果没有该标签，新建set
                            set.add(out_id);
                            Map_n.put(out_label,set);
                        }
                        else
                        {
                            Map_n.get(out_label).add(out_id); //如果已经有了该标签的set,直接存入id
                        }
                    }
                }
                else
                {
                    String in_label = edge.getSourceLabel();
                    if(Frequentnodes_Set.contains(in_id))
                    {
                        if(!Map_n.containsKey(in_label))
                        {
                            Set<String> set = new HashSet<>(); //如果没有该标签，新建set
                            set.add(in_id);
                            Map_n.put(in_label,set);
                        }
                        else
                        {
                            Map_n.get(in_label).add(in_id); //如果已经有了该标签的set,直接存入id
                        }
                    }
                }
            }
            node.setNeighbers(Map_n);
        }
    }


}
