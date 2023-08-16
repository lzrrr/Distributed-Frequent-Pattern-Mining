package Mining;

import Base_Class.*;
import org.jgrapht.alg.isomorphism.VF2GraphIsomorphismInspector;

import java.util.*;

import static java.lang.Integer.parseInt;

public class TreeGenBackward {
    //全局
    public HashMap<String, Pattern_ex> candidate_patterns; //用于存放候选模式对应的模式图,key:模式ID，value:模式图
    public HashMap<String, Pattern_ex> fatherpatternMap; //用于扩展的父模式集合
    public HashMap<String, Set<String>> Seed_map;
    public ArrayList<HashMap<String, Pattern_ex>> T;
    public HashMap<String,ArrayList<String>> send_pattern;


    public TreeGenBackward(HashMap<String, Pattern_ex> candidate_patterns, HashMap<String, Pattern_ex> fatherpatternMap, HashMap<String, Set<String>> Seed_map, ArrayList<HashMap<String, Pattern_ex>> T,HashMap<String,ArrayList<String>> send_pattern) {
        this.candidate_patterns = candidate_patterns;
        this.fatherpatternMap = fatherpatternMap;
        this.Seed_map = Seed_map;
        this.T = T;
        this.send_pattern = send_pattern;
        Get_Backward(this.candidate_patterns,this.fatherpatternMap,Seed_map);
    }

    public void Get_Backward(HashMap<String, Pattern_ex> candidate_patterns, HashMap<String, Pattern_ex> fatherpatternMap, HashMap<String, Set<String>> Seed_map) {

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
            //对模式图中的点两辆组合看是否可以扩展
            for (int i = 0; i < len; i++) {
                for (int j=0;j<len;j++)
                {
                    if (i == j) {
                        continue;
                    }
                    Node t1 = nodeSet.get(i);
                    Node t2 = nodeSet.get(j);
                    Edge e1 = graph.getEdge(t1, t2);
                    Edge e2 = graph.getEdge(t2,t1);
                    //如果两个节点之间存在边或者两个结点无法扩展，跳过
                    if (e2!=null||e1 != null||!Seed_map.get(t1.getLabel()).contains(t2.getLabel())) {
                        continue;
                    }
                    Pattern_ex newP = pattern.clone();
                    Graph_ex newgraph = newP.getPatternGraph();
                    Node newTempNode1 = newgraph.findNode(i);
                    Node newTempNode2 = newgraph.findNode(j);
                    newgraph.addEdge(newTempNode1,newTempNode2,new Edge(newTempNode1,newTempNode2));
                    String info = i + " out " + j; //新边的info
                    newP.getBackwardedges().add(info);
                    String labels =GetPatternLabel(newgraph);
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
                        newP.setNew_column(j);
                        newP.setTargetLabel(t2.getLabel());
                        newP.setOutGoing(true);
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
        ArrayList<String> InfoListForBack = newpattern.getBackwardedges();
        ArrayList<String> InfoList = newpattern.getForwardedges();
        int nodesize = newpattern.getPatternGraph().getNodeSize();

        //获取InfoList.size层对应的模式graph
        ArrayList<Graph_ex> pgraph = new ArrayList<>();
        Map<String, Pattern_ex> upmap = T.get(InfoList.size()-1);
        for(String s : upmap.keySet())
        {
            pgraph.add(upmap.get(s).getPatternGraph());
        }

        //1.每次仅保留一条后向边，其余后向边删掉
        for(String bs : InfoListForBack)
        {
            ArrayList<String> removelistforback = (ArrayList<String>) InfoListForBack.clone();
            removelistforback.remove(bs);
            int id1 = parseInt(bs.split(" ")[0]);
            int id2 = parseInt(bs.split(" ")[2]);

            Graph_ex graph = newpattern.getPatternGraph().clone();

            //2.删除剩余的后向边
            for(String rbs : removelistforback)
            {
                int leftInfo = parseInt(rbs.split(" ")[0]);
                String midInfo = rbs.split(" ")[1];
                int rightInfo = parseInt(rbs.split(" ")[2]);
                Node sourcenode;
                Node targetnode;
                if(midInfo.equals("out"))
                {
                    sourcenode = graph.findNode(leftInfo);
                    targetnode = graph.findNode(rightInfo);
                }
                else {
                    sourcenode = graph.findNode(rightInfo);
                    targetnode = graph.findNode(leftInfo);
                }
                graph.removeEdge(sourcenode,targetnode);
            }

            //3.删除与保留边相邻的一条前向边
            for(String fs : InfoList)
            {
                int fid1 = parseInt(fs.split(" ")[0]);
                String midInfo1 = fs.split(" ")[1];
                int fid2 = parseInt(fs.split(" ")[2]);
                if(fid1==id1||fid1==id2||fid2==id1||fid2==id2)
                {
                    ArrayList<String> newInfoList = (ArrayList<String>) InfoList.clone();
                    newInfoList.add(bs);
                    newInfoList.remove(fs);
                    //4.判断删除该边后图是否连通
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

                    if(hashMap.size()<nodesize)
                    {continue;}
                    //进行子图同构验证
                    else
                    {
                        Graph_ex checkgraph = graph.clone();
                        //先删边
                        if(midInfo1.equals("out"))
                        {
                            Node sourcenode1 = checkgraph.findNode(fid1);
                            Node targetnode1 = checkgraph.findNode(fid2);
                            checkgraph.removeEdge(sourcenode1,targetnode1);
                        }
                        else {
                            Node sourcenode1 = checkgraph.findNode(fid2);
                            Node targetnode1 = checkgraph.findNode(fid1);
                            checkgraph.removeEdge(sourcenode1,targetnode1);
                        }
                        int flag = 1;//验证该模式是否已经被扩展出来过
                        for(Graph_ex graph1 : pgraph)
                        {
                            VF2GraphIsomorphismInspector<Node, Edge> vf = new VF2GraphIsomorphismInspector<>(checkgraph, graph1, new NodeComparator(), new EdgeComparator());
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

        }
        return true;
    }
}
