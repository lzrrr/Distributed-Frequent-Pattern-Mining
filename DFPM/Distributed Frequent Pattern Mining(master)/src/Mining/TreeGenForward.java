package Mining;

import Base_Class.*;
import org.jgrapht.alg.isomorphism.VF2GraphIsomorphismInspector;

import java.util.*;

import static java.lang.Integer.parseInt;

public class TreeGenForward {

    //全局
    public HashMap<String, Pattern_ex> candidate_patterns; //用于存放候选模式对应的模式图,key:模式ID，value:模式图
    public HashMap<String, Pattern_ex> fatherpatternMap; //用于扩展的父模式集合
    public HashMap<String,Set<String>> Seed_map;
    public HashMap<String,ArrayList<String>> send_pattern; //存放发送给各个slave的候选模式，key:模式id,value：1，父模式id,2.扩展列号，3.新列号，4.新列标签


    public TreeGenForward(HashMap<String, Pattern_ex> candidate_patterns, HashMap<String, Pattern_ex> fatherpatternMap, HashMap<String,Set<String>> Seed_map,HashMap<String,ArrayList<String>> send_pattern) {
        this.candidate_patterns = candidate_patterns;
        this.fatherpatternMap = fatherpatternMap;
        this.Seed_map = Seed_map;
        this.send_pattern = send_pattern;
        Get_Forward();
    }

    public void Get_Forward() {

        SingleID singleID = SingleID.getInstance();
        String layer = SingleID.getInstance().getLayer();
        singleID.ZERO();

        //在vf2比较时，只比较所有点标签都一样的模式,构建all_label-map
        HashMap<String, ArrayList<Graph_ex>> labelMap = new HashMap<>();

        //遍历模式Map,对每一个父模式模式进行扩展
        for (String key : fatherpatternMap.keySet()) {
            //获取当前模式
            Pattern_ex pattern = fatherpatternMap.get(key);
            //获取当前模式图
            Graph_ex graph = pattern.getPatternGraph();
            //获取模式图的点集
            LinkedHashMap<Integer, Node> nodeSet = graph.getNodeSet_sequence();
            //利用一个变量固定原点集大小
            int len = nodeSet.size();
            //对原有点进行遍历 （扩展）
            for (int i = 0; i < len; i++) {
                Node tempNode = nodeSet.get(i);
                //对每个点都用频繁边进行 扩展尝试
                //用点的out进行扩展
                for (String outlabel : Seed_map.get(tempNode.getLabel()))
                {
                    //复制出一个一样的模式，并对其进行前向扩展   注：前向扩展会加入新节点
                    Pattern_ex newP = pattern.clone();
                    //获取新模式的模式图
                    Graph_ex newgraph = newP.getPatternGraph();
                    Node newTempNode = newgraph.findNode(i);
                    //扩大模式图的点集    同时记录 扩展点ID(即扩展位置) 以及 扩展边Target点的标签
                    Node node = new Node(outlabel); //点的ID就是点的加入顺序
                    newgraph.addNode(node);
                    //2.该模式中没有重复节点
                    Set<String> nodecheck = new HashSet<>();
                    int ff = 0;
                    for (Node n: newgraph.getNodeSet_sequence().values())
                    {
                        String nlabel = n.getLabel();
                        if(!nodecheck.contains(nlabel))
                        {
                            nodecheck.add(nlabel);
                        }
                        else
                        {
                            ff=1;
                        }
                    }
                    if (ff==1)
                    {
                        System.out.println("重复标签");
                        continue;
                    }
                    Edge edge = new Edge(newTempNode, node);
                    newgraph.addEdge(newTempNode, node, edge);
                    String info = i + " out " + (newgraph.getNodeSize()-1); //新边的info
                    newP.getForwardedges().add(info);
                    String labels = GetPatternLabel(newgraph);
                    String labeln1 = edge.getSourceLabel();
                    String labeln2 = edge.getTargetLabel();
                    int i1 = Integer.parseInt(labeln1);
                    int i2 = Integer.parseInt(labeln2);
                    String edgelabel = new String();
                    if(i1<=i2)
                    {
                        edgelabel = edge.getSourceLabel()+"→"+edge.getTargetLabel();
                    }
                    else
                    {
                        edgelabel = labeln2+"→"+labeln1;
                    }
                    int flag = 1;//验证该模式是否已经被扩展出来过
                    if (!labelMap.containsKey(labels)) {
                        ArrayList<Graph_ex> list = new ArrayList<>();
                        labelMap.put(labels, list);
                    } else {
                        for (Graph_ex tempG : labelMap.get(labels)) {
                            VF2GraphIsomorphismInspector<Node, Edge> vf = new VF2GraphIsomorphismInspector<>(newgraph, tempG, new NodeComparator(), new EdgeComparator());
                            if (vf.isomorphismExists()) {
                                flag = 0;
                                break;
                            }
                        }
                    }
                    if(flag==1&&!checkfrequent(newP))
                    {
                        flag=0;
                    }
                    if (flag == 1) {
                        String ID = layer + singleID.getId();
                        //如果可以构成候选模式,定义新模式的各个属性,并存入候选模式集合，并且只加入对应标签的labelmap
                        newP.setFatherId(newP.getId());
                        newP.setId(ID);
                        newP.setEx_column(i);
                        newP.setNew_column(newgraph.getNodeSize()-1);
                        newP.getColumn_label().add(outlabel);
                        newP.setTargetLabel(outlabel);
                        newP.setOutGoing(true);
                        newP.getEdges().add(edgelabel);
                        candidate_patterns.put(ID, newP);
                        //生成要发送给slave的信息
                        ArrayList<String> list = new ArrayList<>();
                        list.add(newP.getFatherId());
                        list.add(Integer.toString(newP.getEx_column()));
                        list.add(Integer.toString(newP.getNew_column()));
                        list.add(newP.getTargetLabel());
                        list.add("1");
                        send_pattern.put(newP.getId(),list);
                        labelMap.get(labels).add(newgraph);
                    }
                }
            }
        }

        System.out.println("层数: " + layer + "   候选模式生成数量： " + candidate_patterns.size());

    }

    /**
     * 返回一个图的字符串序列，用于剪枝子图同构操作次数
     *
     * @param g 图
     * @return 根据生成的字符串
     */
    private static String GetPatternLabel(Graph_ex g) {
        HashMap<Integer, Node> nodeSet = g.getNodeSet_sequence();
        List<Map.Entry<Integer, Node>> list = new ArrayList(nodeSet.entrySet());
        list.sort(new Comparator<Map.Entry<Integer, Node>>() {
            @Override
            public int compare(Map.Entry<Integer, Node> o1, Map.Entry<Integer, Node> o2) {
                return o1.getValue().getLabel().compareTo(o2.getValue().getLabel());
            }
        });
        StringBuilder s = new StringBuilder(list.get(0).getValue().getLabel());
        for (int i = 1; i < list.size(); ++i) {
            s.append(list.get(i).getValue().getLabel());
        }
        return s.toString();
    }

    /**
     *输入：newpattern,模式Tree;每次删除一个新点n-1vertex结构之外的点，检查n-1模式是否频繁
     * @param newpattern 新模式
     * @return 判断该模式是否一定不频繁
     */
    public boolean checkfrequent(Pattern_ex newpattern)
    {
        //获取新加入点的位置
        int start = newpattern.getPatternGraph().getNodeSize();
        //获取前向边的信息
        ArrayList<String> InfoList = newpattern.getForwardedges();
        //获取减去新结点后的模式大小
        int max = start-1;

        //每次移除一个点，看图是否是n-1连通图
        for(int i=0;i<max;++i)
        {
            Graph_ex graph = newpattern.getPatternGraph().clone();
            ArrayList<String> newInfoList = new ArrayList<>();
            //移除点，只保留除i以外的点
            for(String s : InfoList)
            {
                int leftInfo = parseInt(s.split(" ")[0]);
                int rightInfo = parseInt(s.split(" ")[2]);
                if(leftInfo!=i&&rightInfo!=i)
                {newInfoList.add(s);}
            }
            if(newInfoList.size()>0)
            {
                HashMap<Integer,Integer> hashMap = new HashMap<>();
                String s1 = newInfoList.get(0);
                newInfoList.remove(s1);
                int leftInfo1 = parseInt(s1.split(" ")[0]);
                int rightInfo1 = parseInt(s1.split(" ")[2]);
                hashMap.put(leftInfo1,1);
                hashMap.put(rightInfo1,1);
                int j = hashMap.size()-1;

                //验证是否完整(直到hashmap的值不再增长)
                while(hashMap.size()>j)
                {
                    j=hashMap.size();
                    ArrayList<String> removes = new ArrayList<>();
                    for(String s : newInfoList)
                    {
                        int leftInfo = parseInt(s.split(" ")[0]);
                        int rightInfo = parseInt(s.split(" ")[2]);
                        if(hashMap.containsKey(leftInfo)||hashMap.containsKey(rightInfo))
                        {
                            hashMap.put(leftInfo,1);
                            hashMap.put(rightInfo,1);
                            removes.add(s);
                        }
                    }
                    for(String s : removes)
                    {
                        newInfoList.remove(s);
                    }
                }
                if(hashMap.size()<max)
                {continue;}
                //进行子图同构验证
                else
                {
                    graph.removeVertex(graph.findNode(i));
                    int flag = 1;//验证该模式是否已经被扩展出来过
                    for(String fatherid : fatherpatternMap.keySet())
                    {
                        Graph_ex graph1 = fatherpatternMap.get(fatherid).getPatternGraph();
                        VF2GraphIsomorphismInspector<Node, Edge> vf = new VF2GraphIsomorphismInspector<>(graph, graph1, new NodeComparator(), new EdgeComparator());
                        if (vf.isomorphismExists()) {
                            flag = 0;
                            break;
                        }
                    }
                    if(flag==1)
                    {
                        return false;
                    }
                }
            }

        }
        return true;
    }


}
