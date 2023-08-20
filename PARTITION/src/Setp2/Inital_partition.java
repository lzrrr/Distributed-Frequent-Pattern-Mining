package Setp2;

import Basic_class.Edge;
import Basic_class.Graph;
import Basic_class.Node;

import java.math.BigDecimal;
import java.util.*;

public class Inital_partition {

    /**
     * 通过选取最大（外联-内联）的BFS选点，来对粗化图进行
     * @param graph 粗化的图
     * @param node_weight 原始图的结点权重和
     * @param m 要划分的份数
     */
    public static void inital_part(Graph graph,int node_weight,int m)
    {
        //检测是否能整除
        int remainder = node_weight%m;
        System.out.println("remain: "+remainder);
        //每一份的最大点数
        int[] size = new int[m];
        for (int i=0;i<m;++i)
        {
            size[i] = node_weight/m;
            if (remainder > 0)
            {
                size[i]+=1;
            }
            remainder--;
            System.out.println(size[i]);
        }

        HashMap<String, Node> nodeset = graph.getNodeset();
        ArrayList<Edge> edgeset = graph.getEdgeset();
        HashMap<String, Long> edge_weight = graph.getEdge_weight();
        HashMap<Integer,Integer> size_p = graph.getSize_p();

        //存储未访问过的结点
        ArrayList<Node> unmarkedlist = new ArrayList<>();
        int allwight = 0;
        for (Node node : nodeset.values())
        {
            allwight+=node.getWeight();
            unmarkedlist.add(node);
        }
        //将结点按照度大小排序
        Collections.sort(unmarkedlist, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return o1.getNeighber().size()-o2.getNeighber().size();
            }
        });
        System.out.println("umsize "+unmarkedlist.size());

        for (int i=0;i<m-1;++i)
        {
            int sum = 0;
            boolean flag = true;
            while (flag) {
                //初始化bfs访问队列(该队列为优先队列、对于外联权重（仅针对当前的分区）-内联（与尚未访问的结点的连接）权重大的点，优先出队)
                PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
                //选择邻居最少的点作为起点
                Node snode = unmarkedlist.get(0);
                priorityQueue.add(snode);
                while (!priorityQueue.isEmpty())
                {
                    Node node = priorityQueue.poll(); //取出队首
                    node.setColor(i); //设置分区
                    unmarkedlist.remove(node);//从未分区的点集中移除当前点
                    sum+=node.getWeight();
//                    System.out.println("当前分区点数："+sum);
                    if(sum>=size[i]) //如果已经超出分区容量上限了，跳出循环
                    {
                        flag = false;
                        break;
                    }
                    //将当前结点的邻居（且尚未分区）（不在队列中）更新dvalue值加入队列//更新队列中与当前结点相连的点的dvalue值(要插入一个最大值，并poll它，队列才会更新)
                    for (String nid : node.getNeighber().keySet())
                    {
                        Node neighber = nodeset.get(nid);
                        if(neighber.getColor()!=-1) //如果邻居已经分配给其他分区了，则不考虑
                        {
                            continue;
                        }
                        //获取两点的边标签
                        String elabel = node.getNeighber().get(nid);
                        if(priorityQueue.contains(neighber)) //如果邻居在队列中，则更新dvalue
                        {
                            Long ndvalue = neighber.getDvalue();
                            Long eweight = edge_weight.get(elabel);
                            ndvalue = ndvalue+eweight+eweight; //加两倍的值
                            neighber.setDvalue(ndvalue);
                        }
                        else //如果邻居不在队列中，则计算其dvalue
                        {
                            Long ndvalue = neighber.getDvalue(); //获取默认值0
                            for (String n_nid : neighber.getNeighber().keySet())
                            {
                                Node n_neighber = nodeset.get(n_nid);
                                //获取两点的边标签
                                String n_elabel = neighber.getNeighber().get(n_nid);
                                Long eweight = edge_weight.get(n_elabel);
                                if(n_neighber.getColor()==i) //如果邻居已经分配给当前分区，则进行加
                                {
                                    ndvalue = ndvalue+eweight;
                                }
                                else if(n_neighber.getColor()==-1) //如果还未划分，视为内联，进行减操作
                                {
                                    ndvalue = ndvalue-eweight;
                                }
                            }
                            neighber.setDvalue(ndvalue);
                            priorityQueue.add(neighber);
                        }
                    }
                    //(要插入一个最大值，并poll它，队列才会更新)
                    Node nmax = new Node("-1",1);
                    nmax.setDvalue(9999999999L);
                    priorityQueue.add(nmax);
                    priorityQueue.poll();
                }
            }
//            System.out.println("umremain "+unmarkedlist.size());
//            System.out.println("sum "+sum);
            size_p.put(i,sum);
        }
        //将剩余节点划分
//        System.out.println("剩余节点划分");
        int w = 0;
        for (Node n : unmarkedlist)
        {
            n.setColor(m-1);
            w+=n.getWeight();
        }
        size_p.put(m-1,w);
    }
}
