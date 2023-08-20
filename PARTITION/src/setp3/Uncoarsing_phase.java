package setp3;

import Basic_class.Edge;
import Basic_class.Graph;
import Basic_class.Node;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class Uncoarsing_phase {
    /**
     * 对上一层的图进行细化操作
     * @param G_c 粗化且划分了的图
     * @param G_u 需要细化的下一层的图
     * @param t 最大Umax不增加迭代次数
     */
    public static void uncoarsing(Graph G_c,Graph G_u,int t,int maxnum,int minnum){
        long U_max = -Long.MAX_VALUE;
        long U_temp = 0;
        ArrayList<String> Sv = new ArrayList<>(); //按顺序存放需要移动结点
        int flag = 0; //用于判断是否终止迭代
        int j = 1; //用于记录当前轮次
        int K = 1; //用于记录最优结果在第几轮，（在Sv中的前几个）
        //投射划分结果
        projected_back(G_c,G_u);
        HashMap<Integer,Integer> size_p = (HashMap<Integer, Integer>) G_u.getSize_p().clone();
        //计算边界点的Max_Dvalue
        PriorityQueue<Node> queue = getbunderygain(G_u);
        //每次选择gain最大的点作为交换点
        while (flag<=t&&!queue.isEmpty()) {
            Node node = queue.poll(); //取出队首
//            System.out.println("id: "+node.getId()+" 增益： "+node.getDvalue()+" 移动位置："+node.getTempcolor());
            int fromsize = size_p.get(node.getColor());
            int targetsize = size_p.get(node.getTempcolor());
            node.setIsfixed(true);
            //如果分区的节点数已经超过上限，跳过
            if(fromsize-node.getWeight()<minnum||targetsize+node.getWeight()>maxnum)
            {
//                System.out.println("超过节点数");
                continue;
            }
            Sv.add(node.getId());
            U_temp = U_temp+node.getDvalue();
//            System.out.println("U_Temp "+U_temp);

            size_p.put(node.getColor(),fromsize-node.getWeight());
            size_p.put(node.getTempcolor(),targetsize+node.getWeight());
//            System.out.println("节点数1："+node.getColor()+" "+size_p.get(node.getColor())+" 节点数2："+node.getTempcolor()+" "+size_p.get(node.getTempcolor()));
            //如果当前值大于最大值
            if(U_temp>U_max)
            {
                U_max = U_temp;
                K=j;
                flag = 0;
            }
            else
            {
                flag++;
            }
            j++;
            //对当前结点的邻居Dvalue进行更新和变动分区相关节点
            Update_neighber(node,G_u,queue,size_p);
            //(要插入一个最大值，并poll它，队列才会更新)
            Node nmax = new Node("-1",1);
            nmax.setDvalue(Long.MAX_VALUE);
            queue.add(nmax);
            queue.poll();
        }
        //---------------------------
        System.out.println("U_max:"+U_max+" K:"+K);
        //---------------------------
        //如果U_max值大于0,表明有优化的解则进行交换
        if(U_max>0)
        {
            HashMap<String,Node> nodeset = G_u.getNodeset();
            HashMap<Integer,Integer> size_p2 = G_u.getSize_p();
            for (int i=0;i<K;++i)
            {
                String id = Sv.get(i);
                Node node = nodeset.get(id);
                //更新分区大小
                int fromsize2 = size_p2.get(node.getColor());
                int targetsize2 = size_p2.get(node.getTempcolor());
                size_p2.put(node.getColor(),fromsize2- node.getWeight());
                size_p2.put(node.getTempcolor(),targetsize2+node.getWeight());
                node.setColor(node.getTempcolor()); //设置点的分区为修改分区
            }
//            System.out.println("当前结点数："+size_p2);
        }
    }

    /**
     * 将上一层的图划分结果投射到下一层的图上
     * @param G_c
     * @param G_u
     */
    public static void projected_back(Graph G_c,Graph G_u)
    {
        HashMap<String, Node> nodeset_c = G_c.getNodeset();
        HashMap<String,Node> nodeset_u = G_u.getNodeset();
        for (Node node : nodeset_c.values())
        {
            int color = node.getColor();
            for (String uid : node.getMatchset())
            {
                Node unode = nodeset_u.get(uid);
                unode.setColor(color);
            }
        }
        //设置各个分区的点权重G_u和G_c一样
        G_u.setSize_p(G_c.getSize_p());
    }

    /**
     * 思路：遍历图的边集，当边的两个端点不在同一分区时，这两个点被视为边界点
     * @param G_u
     */
    public static PriorityQueue<Node> getbunderygain(Graph G_u)
    {
        PriorityQueue<Node> queue = new PriorityQueue<>();
        HashMap<String,Node> nodeset = G_u.getNodeset();
        ArrayList<Edge> edgeset = G_u.getEdgeset();
        HashMap<Integer,Integer> size_p = G_u.getSize_p();
        HashMap<String, Long> edge_weight = G_u.getEdge_weight();
        HashSet<String> bunderyid = new HashSet<>();
        for (Edge edge : edgeset)
        {
            String id1 = edge.getSid();
            String id2 = edge.getTid();
            Node node1 = nodeset.get(id1);
            Node node2 = nodeset.get(id2);
            int part1 = node1.getColor();
            int part2 = node2.getColor();
            //当两端点不在同一分区，该点被视为边界点，计算其外联和内联值
            if(part1!=part2)
            {
                bunderyid.add(id1);
                bunderyid.add(id2);
            }
        }

        //计算边界点的gain值
        for (String id : bunderyid)
        {
            Node node = nodeset.get(id);
            int color = node.getColor();
            HashMap<Integer,Long> E_value = node.getE_value();
            for (String nid : node.getNeighber().keySet())
            {
                Node neighber = nodeset.get(nid);
                int ncolor = neighber.getColor();
                String elabel = node.getNeighber().get(nid);
                Long weight = edge_weight.get(elabel);

                //将值加入
                if(!E_value.containsKey(ncolor))
                {
                    E_value.put(ncolor,weight);
                }
                else{
                    E_value.put(ncolor,weight+E_value.get(ncolor));
                }
            }
            //计算并设置gain值(当前分区点数-要移动去的分区点数)+(外联减内联)
            long max_ex = -Long.MAX_VALUE;
            int tempcolor = -1;

            if(!E_value.containsKey(color))
            {
                E_value.put(color,0L);
            }
            Long e_in = E_value.get(color);

            for (int ncolor : E_value.keySet())
            {
                long temp;
                if(color!=ncolor)
                {
                    long e_w = E_value.get(ncolor);
                    e_w = e_w-e_in;
                    temp = e_w;
                    //当前值大于最大值
                    if(temp>max_ex)
                    {
                        max_ex = temp;
                        tempcolor = ncolor;
                    }
                }
            }
            if(tempcolor==-1)
            {continue;}
            node.setDvalue(max_ex);
            node.setTempcolor(tempcolor);
            queue.add(node);
        }
        return queue;
    }

    public static void Update_neighber(Node node,Graph G_u,PriorityQueue<Node> queue,HashMap<Integer,Integer> size_p)
    {
        HashMap<String,Node> nodeset = G_u.getNodeset();
        HashMap<String, Long> edge_weight = G_u.getEdge_weight();
        HashMap<String,String> neighber = node.getNeighber();
        String id = node.getId();
        int color = node.getColor();
        int tempcolor = node.getTempcolor();
        for (String neighberid : neighber.keySet())
        {
            Node neighbernode = nodeset.get(neighberid);
            int neighbercolor = neighbernode.getColor();
            HashMap<Integer,Long> E_value = neighbernode.getE_value();
            //如果邻居已经修改过，跳过
            if(neighbernode.isIsfixed())
            {continue;}
            //更新邻居的值：有三种情况：1.邻居已经是边界点，修改后也是边界点，只需要更新值；2.邻居已经是边界点，修改后不再是边界点，需要移除队列 3.邻居不是边界点，修改后变成边界点，更新值并加入队列；
            //获取边权重
            String elabel = neighbernode.getNeighber().get(id);
            Long weight = edge_weight.get(elabel);
            //邻居结点是边界点
            if(queue.contains(neighbernode))
            {
                    //1.邻居E_value减去与node形成的边权重
                    Long bigDecimal1 = E_value.get(color);
                    bigDecimal1 = bigDecimal1-weight;
                    //如果当前结点与邻居不在同一分区且邻居与之前的分区外联值为0(如果内联为0是不能移除内联的)
                    if(bigDecimal1==0&&color!=neighbercolor)
                    {
                        E_value.remove(color);
                    }
                    else
                    {
                        E_value.put(color,bigDecimal1);
                    }
                    //2.邻居E_value的新分区加上node的边权重
                    if(!E_value.containsKey(tempcolor))
                    {
                        E_value.put(tempcolor,weight);
                    }
                    else
                    {
                        long bigDecimal2 = E_value.get(tempcolor);
                        bigDecimal2 = bigDecimal2+weight;
                        E_value.put(tempcolor,bigDecimal2);
                    }
                    //3.判断邻居是否还是边界点
                    if(E_value.size()>1)
                    {
                        //更新邻居的dvalue
                        updatedvalue(neighbernode,G_u,size_p);
                    }
                    else
                    {
                        //将邻居从队列移除
                        queue.remove(neighbernode);
                    }
            }
            //邻居不是边界点
            else
            {
                //1.由于该点与邻居在同一分区，所以移动后，需要重新计算外联内联
                for (String nid : neighbernode.getNeighber().keySet())
                {
                    Node neighber_n = nodeset.get(nid);
                    int ncolor=-1;
                    //如果邻居已经被修改，采用修改后的分区
                    if(neighber_n.isIsfixed())
                    {
                        ncolor = neighber_n.getTempcolor();
                    }
                    else
                    {
                        ncolor = neighber_n.getColor();
                    }
                    String elabel_n = neighbernode.getNeighber().get(nid);
                    long weight_n = edge_weight.get(elabel_n);
                    //将值加入
                    if(!E_value.containsKey(ncolor))
                    {
                        E_value.put(ncolor,weight_n);
                    }
                    else{
                        E_value.put(ncolor,weight_n+E_value.get(ncolor));
                    }
                }
                //更新邻居的dvalue
                updatedvalue(neighbernode,G_u,size_p);
                //将邻居加入队列
                queue.add(neighbernode);
            }
        }
        for (Node node1 : queue)
        {
            if(!neighber.containsKey(node1.getId()))
            {
                if(node1.getColor()==color||node1.getColor()==tempcolor||node1.getTempcolor()==tempcolor||node1.getTempcolor()==color)
                {
                    updatedvalue(node1,G_u,size_p);
                }
            }
        }
    }

    //更新结点的davlue
    public static void updatedvalue(Node node,Graph G_u,HashMap<Integer,Integer> size_p)
    {
        HashMap<Integer,Long> E_value = node.getE_value();
        int color = node.getColor();
        //计算并设置gain值(当前分区点数-要移动去的分区点数)+(外联减内联)
        long max_ex = -Long.MAX_VALUE;
        int tempcolor = -1;
        if(!E_value.containsKey(color))
        {
            E_value.put(color,0L);
        }
        long e_in = E_value.get(color);
        for (int ncolor : E_value.keySet())
        {
            long temp;
            if(color!=ncolor)
            {
                long e_w = E_value.get(ncolor);
                e_w = e_w-e_in;
                temp = e_w;
                //当前值大于最大值
                if(temp>max_ex)
                {
                    max_ex = temp;
                    tempcolor = ncolor;
                }
            }
        }
        node.setDvalue(max_ex);
        node.setTempcolor(tempcolor);
    }

}
