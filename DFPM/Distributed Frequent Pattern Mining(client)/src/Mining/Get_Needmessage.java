package Mining;

import Base_Class.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Get_Needmessage {
    //输入
    Graph graph; //本地图
    HashMap<String,HashMap<String,Ex_Node>> need_message; //存放需要的信息
    HashMap<String,Pattern> pattern_map;//父模式的map
    HashMap<String, Pattern> candidate_patterns;//候选模式map
    int positionid = 0;
    int positionsum = 0;

    public Get_Needmessage(Graph graph, HashMap<String,HashMap<String,Ex_Node>> need_message, HashMap<String, Pattern> pattern_map, HashMap<String, Pattern> candidate_patterns, int positionid, int positionsum) {
        this.graph = graph;
        this.need_message = need_message;
        this.pattern_map = pattern_map;
        this.candidate_patterns = candidate_patterns;
        this.positionid = positionid;
        this.positionsum = positionsum;
        getsendmessage();
    }

    /**
     * 获取每个候选模式中虚点缺失的信息
     */
    public void getsendmessage()
    {
        //为其他站点创建map
        for (int i=1;i<=positionsum;++i)
        {
            if(i==positionid)
            {continue;}
            HashMap<String,Ex_Node> newmap = new HashMap<>();
            need_message.put(Integer.toString(i),newmap);
        }

        for (Pattern pattern : candidate_patterns.values()) {
            Pattern fatherpattern = pattern_map.get(pattern.getFatherId());
            Set<String> father_excolumn = fatherpattern.getMNI_List().get(pattern.getEx_column()); //从父模式的获取扩展列
            //扩展信息
            String targetlabel = pattern.getTargetLabel();
            Set<String> external = new HashSet<>();
            external.addAll(father_excolumn);
            external.retainAll(graph.getExternal_id()); //获取扩展点中的虚拟点id
            for (String exid : external)
            {
                 Node n = graph.findNode(exid);
                 HashMap<String,Ex_Node> newmap = need_message.get(n.getP());
                 //如果该点的该标签未进行过请求
                 if(!n.getVirtual_label().contains(targetlabel))
                 {
                      n.getVirtual_label().add(targetlabel); //标记已请求过该点的该出度标签信息
                      if(!newmap.containsKey(exid)) //新建请求点信息
                      {
                             Ex_Node ex_node = new Ex_Node(exid);
                             newmap.put(exid,ex_node);
                      }
                         //存入需要的信息
                      newmap.get(exid).getNeighbers().put(targetlabel,new HashSet<Send_Node>());
                 }
            }
        }
    }

    /**
     * 存放从其他结点发送来的缺失信息
     */
    public static void savemessage(HashMap<String, Ex_Node> neednodes,Graph graph)
    {
           for (Ex_Node ex_node : neednodes.values())
           {
               Node node = graph.findNode(ex_node.getId()); //获取虚拟点在本地的存储
               for (String target: ex_node.getNeighbers().keySet())
               {
                    if(ex_node.getNeighbers().get(target).size()>0)  //如果该点可以扩
                    {
                        if(!node.getNeighbers().containsKey(target)) //如果本地点没有该标签邻居
                        {
                            Set<String> set = new HashSet<>();
                            node.getNeighbers().put(target,set);
                        }
                        for (Send_Node send_node : ex_node.getNeighbers().get(target))
                        {
                            node.getNeighbers().get(target).add(send_node.getId());
                            if(!graph.getNodeSet().containsKey(send_node.getId())) //如果原图不含该点，将该点添加进图中
                            {
                                Node node1 = new Node(send_node.getId(),target, send_node.getP(), true);
                                graph.getNodeSet().put(node1.getId(),node1);
                                graph.getExternal_id().add(node1.getId());
                            }
                            Node snode = graph.findNode(send_node.getId());
                            if(!snode.getNeighbers().containsKey(node.getLabel()))//如果新点不含原点标签
                            {
                                Set<String> set = new HashSet<>();
                                snode.getNeighbers().put(node.getLabel(),set);
                            }
                            snode.getNeighbers().get(node.getLabel()).add(node.getId()); //新点也要和原点连边
                        }
                    }
               }
           }
    }

    /**
     * 生成其他结点需要的信息
     */
    public static void Getneedmessage(HashMap<String,Ex_Node> exnodes,Graph graph)
    {
        for (String nid : exnodes.keySet())
        {
            Node node = graph.findNode(nid);
            Ex_Node ex_node = exnodes.get(nid);
            //获取对应的需求信息
            for (String target : ex_node.getNeighbers().keySet())
            {
                if(node.getNeighbers().containsKey(target))
                {
                    for (String tid : node.getNeighbers().get(target))
                    {
                        Node node1 = graph.findNode(tid);
                        Send_Node send_node = new Send_Node(tid,node1.getP());
                        ex_node.getNeighbers().get(target).add(send_node);
                    }
                }
            }
        }
    }

}
