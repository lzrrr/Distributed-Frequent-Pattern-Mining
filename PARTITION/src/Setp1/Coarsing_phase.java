package Setp1;

import Basic_class.Edge;
import Basic_class.Graph;
import Basic_class.Node;

import java.math.BigDecimal;
import java.util.*;

public class Coarsing_phase {

    /**
     * 生成压缩图
     * @param g 上一层的图
     * @param level 层数
     * @return 压缩图
     */
    public static Graph coarse(Graph g,int level) {
        HashMap<String, Node> nodeset = (HashMap<String, Node>) g.getNodeset().clone();
        ArrayList<Edge> edgeset = g.getEdgeset();
        Get_rido.getrido(edgeset,nodeset);
        //将边集按照权重大小由大->小排序
        Collections.sort(edgeset);
        //初始点的id
        int id = -1;
        //标注点是否被matched,且点属于哪个新点,key：被压缩的点，value:新点
        HashMap<String, String> map = new HashMap<>();
        //新图的点集和边集和边权重集
        HashMap<String, Node> new_nodeset = new HashMap<>();
        ArrayList<Edge> new_edgeset = new ArrayList<>();
        HashMap<String, Long> new_edge_weight = new HashMap<>();
        //不能融合的边集，这些边集作为新图的边集
        ArrayList<Edge> un_edgeset = new ArrayList<>();
        //新图的边集,key:新点id(String大的值)->新点id(string小的值),每次有边要融合时，将权重相加
        HashMap<String, Edge> edgemap = new HashMap<>();
        //遍历排序好的边集，将还未融合的点对进行融合
        for (Edge e : edgeset) {
            String id1 = e.getSid();
            String id2 = e.getTid();
            //两个点都尚未融合，合并生成新点
            if (!map.containsKey(id1) && !map.containsKey(id2)) {
                //生成新点的标签
                id++;
                String label = Integer.toString(id);
                map.put(id1, label);
                map.put(id2, label);
                //计算新点的权重
                Node node1 = nodeset.get(id1);
                Node node2 = nodeset.get(id2);
                int weight = node1.getWeight() + node2.getWeight();
                //生成新包含的点集
                Set<String> matchset = new HashSet<>();
                matchset.add(id1);
                matchset.add(id2);
                //生成新点并加入新图的点集
                Node node = new Node(label, weight, matchset);
                new_nodeset.put(label, node);
                //将已融合的点从克隆点集中移除
                nodeset.remove(id1);
                nodeset.remove(id2);
            } else {
                //将不能融合的边加入后续的边集
                un_edgeset.add(e);
            }
        }
        //对于没有matched的点，将其单独生成新点，并加入下一层
        for (String nid : nodeset.keySet()) {
            //生成新点的标签
            id++;
            String label = Integer.toString(id);
            map.put(nid, label);
            //计算新点的权重,等于自身权重
            Node node1 = nodeset.get(nid);
            int weight = node1.getWeight();
            //生成新包含的点集,只包含自身
            Set<String> matchset = new HashSet<>();
            matchset.add(nid);
            //生成新点并加入新图的点集
            Node node = new Node(label, weight, matchset);
            new_nodeset.put(label, node);
        }

        //遍历不能融合的边集，生成新图的边集
        for (Edge e : un_edgeset) {
            //获取源点id
            String oid1 = e.getSid();
            String oid2 = e.getTid();
            //获取在新图中连接的点id
            String nid1 = map.get(oid1);
            String nid2 = map.get(oid2);
            //按照新id由小->大生成边标签
            String elabel = new String();
            if (Integer.parseInt(nid1) > Integer.parseInt(nid2)) {
                String temp = nid1;
                nid1 = nid2;
                nid2 = temp;
            }
            elabel = nid1 + "->" + nid2;
            //获取边权重
            long wight = e.getWight();
            //如果边未生成，则生成新边
            if (!edgemap.containsKey(elabel)) {
                Edge edge = new Edge(nid1, nid2, elabel,wight);
                edgemap.put(elabel, edge);
                //创建结点的邻接表
                Node snode = new_nodeset.get(nid1);
                Node tnode = new_nodeset.get(nid2);
                snode.getNeighber().put(nid2,edge.getElabel());
                tnode.getNeighber().put(nid1,edge.getElabel());
            } else {
                Edge edge = edgemap.get(elabel);
                wight = wight+edge.getWight();
                edge.setWight(wight);
                edgemap.put(elabel, edge);
            }
        }

        //获取图的新边集
        new_edgeset.addAll(edgemap.values());
        //获取图的边权重map
        for (String eid : edgemap.keySet())
        {
            new_edge_weight.put(eid,edgemap.get(eid).getWight());
        }

        //返回压缩图
        return new Graph(new_nodeset,new_edgeset,new_edge_weight);
    }
}
