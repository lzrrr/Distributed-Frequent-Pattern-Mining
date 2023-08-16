package Server_Work;

import Base_Class.*;
import Mining.*;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.esotericsoftware.kryo.util.Null;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    // 服务器对象，用于监听TCP端口
    private ServerSocket serverSocket = null;
    // 分配给socket连接的id，用于区分不同的socket连接
    private static int id = 1;

    private static final int numofclient = 1; //**client个数

    public static final Object lock = new Object(); //用于接收消息时锁住getlist再写入信息

    public static volatile int condition = 0; //用于等待接收消息时主线程判断是否所有的客服端都返回了结果

    public static volatile double byteall = 0.0; //用于记录datashipment,单位：字节

    public static long getdatashiptime = 0;

    //分别给客户端创建一个getData避免线程冲突
    public static volatile HashMap<Integer,ConnectionData> getlist = new HashMap<>();

    //分别给客户端创建一个sendData避免线程冲突
    public static volatile HashMap<Integer,ConnectionData> sendlist = new HashMap<>();

    public static volatile ArrayList<Boolean> sendflaglist = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        //初始化sendflaglist,默认不超过30个客户端
        socketfuntion.InitialSendflag(sendflaglist); //

        //启动服务器
        Server server = new Server(9999);
        server.start();

        int support = 2000; //**阈值
        int fegesize = 18;
        System.out.println("挖掘阈值： "+support);
        int K = 10000; //**要挖掘的总模式数
        ArrayList<HashMap<String, Pattern_ex>> T = new ArrayList<>(); //code-tree,用于存储每一层对应的频繁模式树
        HashMap<String,Set<String>> Seed_map = new HashMap<>(); //seed边的one-hopmap,用于加速候选模式生成
        HashMap<String, Pattern_ex> patternMap = new HashMap<>(); //用于扩展下一层的父模式
        HashMap<String,String> Edges_id = new HashMap<>(); //存放发送给各个slave的频繁边,key:边的String，value:边的id
        long sendtimecandi = 0;
        long sendtimeedge = 0;
        long sendtimepattern = 0;

        HashMap<String,HashMap<String, ArrayList<Integer>>> partical_Frequence = new HashMap<>();
        //master接受slave发送来的模式每列MNI,存入partical_Frequence(1r)
        synchronized (lock)
        {
            while (condition<numofclient) //保证每个客服端的消息都接收到
            {
                try {
                    lock.wait(); //当不满足条件时，主程序在此处等待
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        //当接收到所有的客户端发来的消息后，将条件置为0,存入消息,并清空接收消息队列
        condition = 0;
        for (int i=1;i<=numofclient;++i)
        {
            partical_Frequence.put(Integer.toString(i),getlist.get(i).getEdge_Frequence());
        }
        getlist.clear();
        System.out.println("收到客服端发来的边频次信息");


        HashMap<String, ArrayList<Integer>> pattern_Frequence = new HashMap<>();
        //master汇总求和slave发送来的模式每列MNI,存入pattern_Frequence（统计全局支持度）
        StatisticMNItable.Get_Frequence(partical_Frequence,pattern_Frequence);

        for (String s : pattern_Frequence.keySet())
        {
            System.out.println(s+" "+pattern_Frequence.get(s));
        }


        //master筛选出频繁边
        Frequent_Edge_topk frequent_edgeTopk = new Frequent_Edge_topk(support,T,Seed_map,patternMap,pattern_Frequence,Edges_id);

        ArrayList<Pattern_ex> p_list = new ArrayList<>();
        for (Pattern_ex pattern_ex : patternMap.values())
        {
            p_list.add(pattern_ex);
        }
        p_list.sort(Pattern_ex::compareTo);
        System.out.println("频繁边数："+p_list.size());
        for (Pattern_ex pattern_ex : p_list)
        {
            System.out.println(pattern_ex.getId());
            LinkedHashMap<Integer, Node> NodeSet_sequence = pattern_ex.getPatternGraph().getNodeSet_sequence();
            for(int pid=0;pid<NodeSet_sequence.size();++pid)
            {
                String label = NodeSet_sequence.get(pid).getLabel();
                System.out.print("v "+pid+" "+label);
                System.out.println();
            }
            //打印边
            for(String ps : pattern_ex.getForwardedges())
            {
                System.out.println("e "+ps);
            }
            System.out.println("支持度："+pattern_ex.getSupport());
        }

        //master将频繁边String:source->target和频繁边模式id发送给slave(2s)
        for (int i=1;i<=numofclient;++i) //将发送标志置为true
        {
            //将发送数据写入
            ConnectionData connectionData = new ConnectionData();
            connectionData.setEdges_id(Edges_id);
            connectionData.setSupport(support);
            connectionData.setPositionsum(numofclient);
            sendlist.put(i,connectionData);
            //将发送信号设为true,开始发送
            sendflaglist.set(i,true);
        }
        System.out.println("将频繁边string和边模式id发送给客服端");

        //master等待所有客服端都返回接收消息后再继续执行前向挖掘
        synchronized (lock)
        {
            while (condition<numofclient) //保证每个客服端的消息都接收到
            {
                try {
                    lock.wait(); //当不满足条件时，主程序在此处等待
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        //当接收到所有的客户端发来的消息后，将条件置为0,存入消息,并清空消息队列
        condition = 0;
        getlist.clear();
        System.out.println("收到客服端发来的可以继续前向扩展标志");

        System.out.println("---------------开始前向扩展-------------------");

        long startall = System.currentTimeMillis();

        //master通过频繁边进行前向扩展
        while (patternMap.size()>0)
        {

            System.out.println("\n--------------循环扩展-------------------");

            long start1 = System.currentTimeMillis();

            //利用父模式和seed进行前向扩展
            HashMap<String, Pattern_ex> candidate_patterns = new HashMap<>(); //用于存放候选模式对应的模式图,key:模式ID，value:模式图
            HashMap<String,ArrayList<String>> send_pattern = new HashMap<>(); //存放发送给各个slave的候选模式，key:模式id,value：1，父模式id,2.扩展列号，3.新列号，4.新列标签,5.是否是出度边:1出度
            long genstart = System.currentTimeMillis();
            TreeGenForward treeGenForward = new TreeGenForward(candidate_patterns,patternMap,Seed_map,send_pattern);
            long genend = System.currentTimeMillis();
            System.out.println("模式生成时间： "+(genend-genstart)/1000);
            //获取模式大小
            int patternsize = 0;
            if(candidate_patterns.size()>0)
            {patternsize = candidate_patterns.entrySet().iterator().next().getValue().getColumn_label().size();}
            //master将候选模式以send_pattern的形式发送给slave(4s)
            for (int i=1;i<=numofclient;++i) //将发送标志置为true
            {
                //将发送数据写入
                ConnectionData connectionData = new ConnectionData();
                connectionData.setSend_pattern(send_pattern);
                connectionData.setPatternsize(patternsize);
                sendlist.put(i,connectionData);
                //将发送信号设为true,开始发送
                sendflaglist.set(i,true);
            }
            System.out.println("发送候选模式");

            //master接收需要的缺失边信息请求
            synchronized (lock)
            {
                while (condition<numofclient) //保证每个客服端的消息都接收到
                {
                    try {
                        lock.wait(); //当不满足条件时，主程序在此处等待
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            condition = 0;


            //当接收到所有的客户端发来的消息后，将条件置为0,存入消息,并清空接收消息队列
            HashMap<Integer,HashMap<String, HashMap<String, Ex_Node>>> need_message = new HashMap<>(); //存储每个站点需要的信息
            HashMap<Integer,HashMap<Integer,HashMap<String,Ex_Node>>> tran_message = new HashMap<>(); //目标站点id,不同id的站点需要目标站点的信息,需要查找的信息
            for (int i=1;i<=numofclient;++i)
            {
                need_message.put(i,getlist.get(i).getNeed_message());
            }
            getlist.clear();
            System.out.println("收到缺失边请求");
            //初始化查找信息
            for (int i=1;i<=numofclient;++i)
            {
                tran_message.put(i,new HashMap<>());
            }

            //创建查找map
            for (int i=1;i<=numofclient;++i)
            {
                HashMap<String, HashMap<String, Ex_Node>> map = need_message.get(i);
                for (String s : map.keySet())
                {
                    int p = Integer.parseInt(s);
                    tran_message.get(p).put(i,map.get(s));//需求站点id,请求站点id,请求信息
                }
            }

            //发送查找map
            for (int i=1;i<=numofclient;++i) //将发送标志置为true
            {
                //将发送数据写入
                ConnectionData connectionData = new ConnectionData();
                connectionData.setTrans_message(tran_message.get(i));
                sendlist.put(i,connectionData);
                //将发送信号设为true,开始发送
                sendflaglist.set(i,true);
            }
            System.out.println("发送查找map");

            //master接收查找到的信息请求
            synchronized (lock)
            {
                while (condition<numofclient) //保证每个客服端的消息都接收到
                {
                    try {
                        lock.wait(); //当不满足条件时，主程序在此处等待
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            condition = 0;
            HashMap<Integer,HashMap<String,Ex_Node>> need_nodes = new HashMap<>();//存储每个站点需要的信息，要返回的站点号,已经查找到的信息
            for (int i=1;i<=numofclient;++i)
            {
                need_nodes.put(i,new HashMap<>());
            }
            for (int i=1;i<=numofclient;++i)
            {
                HashMap<Integer,HashMap<String,Ex_Node>> trans_nodes = getlist.get(i).getTrans_nodes();
                for (int j : trans_nodes.keySet())
                {
                    need_nodes.get(j).putAll(trans_nodes.get(j));
                }
            }
            getlist.clear();
            System.out.println("收到查找结果");

            //发送查找结果
            //--------------------时间测试--------------------
            long sendstart = System.currentTimeMillis();
            System.out.println("收到查找结果时间： "+sendstart);
            //--------------------时间测试--------------------

            for (int i=1;i<=numofclient;++i) //将发送标志置为true
            {
                //将发送数据写入
                ConnectionData connectionData = new ConnectionData();
                connectionData.setNeed_nodes(need_nodes.get(i));
                sendlist.put(i,connectionData);
                //将发送信号设为true,开始发送
                sendflaglist.set(i,true);
            }
            System.out.println("发送查找结果");

            //-------------时间测试----------------
            //master等待所有客服端都返回接收消息后再继续执行前向挖掘
            synchronized (lock)
            {
                while (condition<numofclient) //保证每个客服端的消息都接收到
                {
                    try {
                        lock.wait(); //当不满足条件时，主程序在此处等待
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            //当接收到所有的客户端发来的消息后，将条件置为0,存入消息,并清空消息队列
            condition = 0;
            getlist.clear();
            long sendstart2 = System.currentTimeMillis();
            System.out.println("缺失边发送到收到总时间： "+(sendstart2-sendstart)/1000);

            for (int i=1;i<=numofclient;++i) //将发送标志置为true
            {
                //将发送数据写入
                ConnectionData connectionData = new ConnectionData();
                sendlist.put(i,connectionData);
                //将发送信号设为true,开始发送
                sendflaglist.set(i,true);
            }
            System.out.println("测试1结束");

            //-------------时间测试----------------

            //master接收client发来的转发虚拟模式的mni
            synchronized (lock)
            {
                while (condition<numofclient) //保证每个客服端的消息都接收到
                {
                    try {
                        lock.wait(); //当不满足条件时，主程序在此处等待
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            condition = 0;
            HashMap<Integer, HashMap<String, HashMap<Integer, Set<String>>>> trans_external = new HashMap<>(); //存储转发给各个站点的虚拟MNI，key:站点id
            for (int i=1;i<=numofclient;++i)
            {
                trans_external.put(i,new HashMap<>());
            }
            for (int i=1;i<=numofclient;++i)
            {
                HashMap<String, HashMap<String, HashMap<Integer, Set<String>>>> send_external = getlist.get(i).getSend_external();
                for (String s : send_external.keySet())
                {
                    int pid = Integer.parseInt(s); //获取对应站点id
                    for (String patternid : send_external.get(s).keySet())
                    {
                        if(!trans_external.get(pid).containsKey(patternid))  //判断是否有当前模式
                        {
                            trans_external.get(pid).put(patternid,new HashMap<>());
                        }
                        for (int col : send_external.get(s).get(patternid).keySet())
                        {
                            if(!trans_external.get(pid).get(patternid).containsKey(col)) //判断是否有当前列
                            {
                                trans_external.get(pid).get(patternid).put(col,new HashSet<>());
                            }
                            trans_external.get(pid).get(patternid).get(col).addAll(send_external.get(s).get(patternid).get(col));
                        }
                    }
//                    trans_external.get(pid).putAll(send_external.get(s)); //放入对应站点id的map
                }
            }
            getlist.clear();
            System.out.println("收到待转发虚拟匹配MNI");

            //发送待转发虚拟匹配MNI
            //--------------------时间测试--------------------
            long sendstart_v = System.currentTimeMillis();
            //--------------------时间测试--------------------
            for (int i=1;i<=numofclient;++i) //将发送标志置为true
            {
                //将发送数据写入
                ConnectionData connectionData = new ConnectionData();
                connectionData.setExternal_mni(trans_external.get(i));
                sendlist.put(i,connectionData);
                //将发送信号设为true,开始发送
                sendflaglist.set(i,true);
            }
            System.out.println("发送待转发虚拟匹配MNI");

            //-------------时间测试----------------
            //master等待所有客服端都返回接收消息后再继续执行前向挖掘
            synchronized (lock)
            {
                while (condition<numofclient) //保证每个客服端的消息都接收到
                {
                    try {
                        lock.wait(); //当不满足条件时，主程序在此处等待
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            //当接收到所有的客户端发来的消息后，将条件置为0,存入消息,并清空消息队列
            condition = 0;
            getlist.clear();
            long sendstart2_v = System.currentTimeMillis();
            System.out.println("虚拟模式发送到收到总时间： "+(sendstart2_v-sendstart_v)/1000);
            for (int i=1;i<=numofclient;++i) //将发送标志置为true
            {
                //将发送数据写入
                ConnectionData connectionData = new ConnectionData();
                sendlist.put(i,connectionData);
                //将发送信号设为true,开始发送
                sendflaglist.set(i,true);
            }
            System.out.println("测试2结束");
            //-------------时间测试----------------

            //master接受slave发送来的模式每列MNI,存入partical_Frequence(8r)
            partical_Frequence = new HashMap<>();
            pattern_Frequence = new HashMap<>();
            patternMap = new HashMap<>();
            synchronized (lock)
            {
                while (condition<numofclient) //保证每个客服端的消息都接收到
                {
                    try {
                        lock.wait(); //当不满足条件时，主程序在此处等待
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            condition = 0;
            for (int i=1;i<=numofclient;++i)
            {
                String pid = Integer.toString(i);
                partical_Frequence.put(pid,getlist.get(i).getPattern_mnitable());
            }
            getlist.clear();
            System.out.println("收到各个站点统计的模式MNI");

            //master汇总求和slave发送来的模式每列MNI,存入pattern_Frequence（统计全局支持度）
            StatisticMNItable.Get_Frequence(partical_Frequence,pattern_Frequence);


            StatisticMNItable.Get_Frequentpattern(pattern_Frequence,candidate_patterns,support,patternMap);

            HashMap<String, ArrayList<Integer>> frequentpatternid = new HashMap<>();
            for (String s : patternMap.keySet())
            {
                frequentpatternid.put(s,pattern_Frequence.get(s));
            }

            //master将频繁的模式id广播给slave(9s)
            for (int i=1;i<=numofclient;++i) //将发送标志置为true
            {
                //将发送数据写入
                ConnectionData connectionData = new ConnectionData();
                connectionData.setFrequentpatternid(frequentpatternid);
                sendlist.put(i,connectionData);
                //将发送信号设为true,开始发送
                sendflaglist.set(i,true);
            }
            System.out.println("广播频繁模式id给各个slave");

            //master等待所有客服端都返回接收消息后再继续执行前向挖掘
            synchronized (lock)
            {
                while (condition<numofclient) //保证每个客服端的消息都接收到
                {
                    try {
                        lock.wait(); //当不满足条件时，主程序在此处等待
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            //当接收到所有的客户端发来的消息后，将条件置为0,存入消息,并清空消息队列
            condition = 0;
            getlist.clear();
            long end1 = System.currentTimeMillis();

            System.out.println("前向时间： "+(end1-start1));

            System.out.println("新模式个数 "+frequentpatternid.size());

            if(patternMap.size()>0){
                T.add(patternMap);
            }

            for (String s : frequentpatternid.keySet())
            {
                System.out.println(s);
            }

        }

        //后向扩展
        //获取层数
        int level = T.size();
        System.out.println("\n--------------后向扩展循环-------------------");

        //1.后向扩展终止标志
        boolean flagbackword = false;

        //2.建立一个MaxItrs表，存放T中每一层后向扩展可能的最大值
        ArrayList<Integer> MaxItrs = new ArrayList<>();
        for (int i = 0; i < level; ++i) {
            int vnum = i+1;
            int Qitrs = 2*vnum+(vnum)*(vnum-1);
            MaxItrs.add(Qitrs);
        }

        //3.建立总map存放Topk
        HashMap<Integer,HashMap<String, Pattern_ex>> SumItrsOfP = new HashMap<>();
        //是否top-k终止
        boolean k_flag = false;
        int numall = 0;

        //4.从T的最顶层开始后向扩展
        for(int j=level-1;j>=0;--j)
        {

            //若不用继续扩
            if(flagbackword)
            {
                break;
            }
            //5.T的当前层扩展终止条件
            boolean Tlevelflag = false;

            //6.建立一个存当前层扩展后Qitrs的map
            HashMap<Integer, HashMap<String, Pattern_ex>> ItrsOfP = new HashMap<>();

            //7.初始化该表
            int backlevel = (j+2)*2-1;
            ItrsOfP.put(backlevel,T.get(j));

            //8.开始扩展T的当前层
            while (!Tlevelflag)
            {
                //1.取出当前层的patter
                HashMap<String, Pattern_ex> fatherpatternMap = ItrsOfP.get(backlevel);
                HashMap<String, Pattern_ex> candidate_patterns = new HashMap<>();
                HashMap<String,ArrayList<String>> send_pattern = new HashMap<>(); //存放发送给各个slave的候选模式，key:模式id,value：1，父模式id,2.扩展列号，3.新列号，4.新列标签,5.是否是出度边:1出度
                long start1 = System.currentTimeMillis();
                //2.利用频繁边模式，生成候选模式
                TreeGenBackward treeGenBackward = new TreeGenBackward(candidate_patterns,fatherpatternMap,Seed_map,T,send_pattern);
                //获取模式大小
                int patternsize = 0;
                if(candidate_patterns.size()>0)
                {patternsize = candidate_patterns.entrySet().iterator().next().getValue().getColumn_label().size();}
                //3.master将候选模式以send_pattern的形式发送给slave(11s)
                for (int i=1;i<=numofclient;++i) //将发送标志置为true
                {
                    //将发送数据写入
                    ConnectionData connectionData = new ConnectionData();
                    connectionData.setSend_pattern(send_pattern);
                    connectionData.setPatternsize(patternsize);
                    sendlist.put(i,connectionData);
                    //将发送信号设为true,开始发送
                    sendflaglist.set(i,true);
                }
                System.out.println("广播后向候选模式给各个slave");

                //master接收client发来的转发虚拟模式的mni(12r)
                synchronized (lock)
                {
                    while (condition<numofclient) //保证每个客服端的消息都接收到
                    {
                        try {
                            lock.wait(); //当不满足条件时，主程序在此处等待
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                condition = 0;
                HashMap<Integer, HashMap<String, HashMap<Integer, Set<String>>>> trans_external = new HashMap<>(); //存储转发给各个站点的虚拟MNI，key:站点id
                for (int i=1;i<=numofclient;++i)
                {
                    trans_external.put(i,new HashMap<>());
                }
                for (int i=1;i<=numofclient;++i)
                {
                    HashMap<String, HashMap<String, HashMap<Integer, Set<String>>>> send_external = getlist.get(i).getSend_external();
                    for (String s : send_external.keySet())
                    {
                        int pid = Integer.parseInt(s); //获取对应站点id
                        trans_external.get(pid).putAll(send_external.get(s)); //放入对应站点id的map
                    }
                }
                getlist.clear();
                System.out.println("收到待转发虚拟匹配MNI");

                //发送待转发虚拟匹配MNI(7s)
                for (int i=1;i<=numofclient;++i) //将发送标志置为true
                {
                    //将发送数据写入
                    ConnectionData connectionData = new ConnectionData();
                    connectionData.setExternal_mni(trans_external.get(i));
                    sendlist.put(i,connectionData);
                    //将发送信号设为true,开始发送
                    sendflaglist.set(i,true);
                }
                System.out.println("发送待转发虚拟匹配MNI");

                //master接受slave发送来的模式每列MNI,存入partical_Frequence(8r)
                partical_Frequence = new HashMap<>();
                pattern_Frequence = new HashMap<>();
                patternMap = new HashMap<>();
                synchronized (lock)
                {
                    while (condition<numofclient) //保证每个客服端的消息都接收到
                    {
                        try {
                            lock.wait(); //当不满足条件时，主程序在此处等待
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                condition = 0;
                for (int i=1;i<=numofclient;++i)
                {
                    String pid = Integer.toString(i);
                    partical_Frequence.put(pid,getlist.get(i).getPattern_mnitable());
                }
                getlist.clear();
                System.out.println("收到各个站点统计的模式MNI");

                //master汇总求和slave发送来的模式每列MNI,存入pattern_Frequence（统计全局支持度）
                StatisticMNItable.Get_Frequence(partical_Frequence,pattern_Frequence);


                StatisticMNItable.Get_Frequentpattern(pattern_Frequence,candidate_patterns,support,patternMap);
                HashMap<String, ArrayList<Integer>> frequentpatternid = new HashMap<>();
                for (String s : patternMap.keySet())
                {
                    frequentpatternid.put(s,pattern_Frequence.get(s));
                }

                //这里修改过！！！！！3-1号
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                //master将频繁的模式id广播给slave(9s)
                for (int i=1;i<=numofclient;++i) //将发送标志置为true
                {
                    //将发送数据写入
                    ConnectionData connectionData = new ConnectionData();
                    connectionData.setFrequentpatternid(frequentpatternid);
                    sendlist.put(i,connectionData);
                    //将发送信号设为true,开始发送
                    sendflaglist.set(i,true);
                }
                System.out.println("广播频繁模式id给各个slave");

                //master等待所有客服端都返回接收消息后再继续执行前向挖掘
                synchronized (lock)
                {
                    while (condition<numofclient) //保证每个客服端的消息都接收到
                    {
                        try {
                            lock.wait(); //当不满足条件时，主程序在此处等待
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //当接收到所有的客户端发来的消息后，将条件置为0,存入消息,并清空消息队列
                condition = 0;
                getlist.clear();
                long end1 = System.currentTimeMillis();
                System.out.println("后向时间： "+(end1-start1));
                System.out.println("新模式个数 "+frequentpatternid.size());

                for (String s : frequentpatternid.keySet())
                {
                    System.out.println(s);
                }

                //5.存入ItrsOfP
                if(patternMap.size()>0)
                {
                    backlevel++;
                    ItrsOfP.put(backlevel,patternMap);

                }
                //6.若无法继续扩展
                else if(patternMap.size()==0)
                {
                    Tlevelflag = true;
                }

            }
            //判断是否已经获取了TOPk个patter
            //1.向sumItrs中添加该level的后向扩展topk
            SumItrsOfP = Backward_topk.AddTopk(SumItrsOfP,ItrsOfP,K);

            int num = Backward_topk.NumofItrsOfP(SumItrsOfP);
            System.out.println("当前topk个数 "+num);
            //2.若已经装了k个
            if(j>0)
            {
                if(num>=K)
                {
                    //比较与下一层max的大小
                    int minofk = Backward_topk.IntrsOfK(SumItrsOfP);
                    System.out.println("minofk "+minofk);
                    //大于下一层最大值，不用继续扩
                    if(minofk>=MaxItrs.get(j-1))
                    {
                        //3.master向slave发送后向扩展结束标志
                        for (int i=1;i<=numofclient;++i) //将发送标志置为true
                        {
                            //将发送数据写入
                            ConnectionData connectionData = new ConnectionData();
                            connectionData.setFlag(-1);
                            sendlist.put(i,connectionData);
                            //将发送信号设为true,开始发送
                            sendflaglist.set(i,true);
                        }
                        System.out.println("广播后向扩展终止标志给各个slave");
                        k_flag = true;
                        //将向后扩展终止标志置为true
                        flagbackword = true;
                    }
                }
            }
            numall = num;

        }


        //3.如果不是k终止，master向slave发送后向扩展结束标志
        if(!k_flag)
        {
            for (int i=1;i<=numofclient;++i) //将发送标志置为true
            {
                //将发送数据写入
                ConnectionData connectionData = new ConnectionData();
                connectionData.setFlag(-1);
                sendlist.put(i,connectionData);
                //将发送信号设为true,开始发送
                sendflaglist.set(i,true);
            }
            System.out.println("广播后向扩展终止标志给各个slave");
        }

        long endall = System.currentTimeMillis();
        System.out.println("alltime: "+(endall-startall)/1000.0+"s");
        System.out.println("数据量计算时间： "+getdatashiptime/1000.0);
        double datashipment = byteall/1024;
        System.out.println("datashipment: "+datashipment+"kb");
        System.out.println("模式总数： "+numall);
        //打印top-k挖掘结果
        ArrayList<Pattern_ex> fplist = new ArrayList<>();
        for(Integer i : SumItrsOfP.keySet())
        {
            for(String s : SumItrsOfP.get(i).keySet())
            {
                Pattern_ex p = SumItrsOfP.get(i).get(s);
                System.out.println(p.getId());
                LinkedHashMap<Integer, Node> NodeSet_sequence = p.getPatternGraph().getNodeSet_sequence();
                for(int pid=0;pid<NodeSet_sequence.size();++pid)
                {
                    String label = NodeSet_sequence.get(pid).getLabel();
                    System.out.print("v "+pid+" "+label);
                    System.out.println();
                }
                //打印边
                int edgesize = 0;
                for(String ps : p.getForwardedges())
                {
                    edgesize++;
                    System.out.println("e "+ps);
                }
                for(String ps : p.getBackwardedges())
                {
                    edgesize++;
                    System.out.println("e "+ps);
                }
                if(edgesize>1)
                {
                    fplist.add(p);
                }
                System.out.println("------------");
            }
        }

        System.out.println("非单边模式个数："+fplist.size());
        System.out.println("单边个数："+p_list.size());
        //计算每条频繁边在频繁模式图大于单边中的占比
        HashMap<Pattern_ex,String> pmap = new HashMap<>();
        int i=1;
        for (Pattern_ex ep : p_list)
        {
            LinkedHashMap<Integer, Node> NodeSet_sequence = ep.getPatternGraph().getNodeSet_sequence();
            String ep_edge = new String();
            String flabel =  NodeSet_sequence.get(0).getLabel();
            String tlabel =  NodeSet_sequence.get(1).getLabel();
            ep_edge=flabel+"→"+tlabel;
            pmap.put(ep,ep_edge);
            int sum = 0;
            for (Pattern_ex p : fplist)
            {
                if (p.getEdges().contains(ep_edge))
                {
                    sum++;
                    continue;
                }
            }
            System.out.println(sum);
            double s = 0.0+sum;
            double d = 0.0+fplist.size();
            double tol = s/d;
            System.out.println(i+"边占比： "+tol*100+"%");
            i++;
        }
        //支持度top10的边在频繁模式图中的占比
        int sum = 0;
        for (Pattern_ex p : fplist)
        {
            for (int l=0;l<fegesize;++l)
            {
                pmap.get(p_list.get(l));
                if(p.getEdges().contains(pmap.get(p_list.get(l))))
                {
                    sum++;
                    break;
                }
            }
        }
        double s = 0.0+sum;
        double d = 0.0+fplist.size();
        double tol = s/d;
        System.out.println("前"+fegesize+"边占比： "+tol*100+"%");
    }

    public Server(int port) {
        try{
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start()
    {
        try {
            System.out.println("Socket服务器开始运行...");
            while (id<=numofclient)
            {
                Socket socket= serverSocket.accept();
                System.out.println(id + ":客户端接入:"+socket.getInetAddress() + ":" + socket.getPort());
                new Thread(new Server_Listen(id,socket)).start();
                new Thread(new Server_send(id,socket)).start();
                id++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

class Server_Listen implements Runnable
{
    private int id;
    private Socket socket;
    private ConnectionData data;

    public Server_Listen(int id,Socket socket) {
        this.id = id;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            Kryo kryo = new Kryo();
            kryo.setReferences(true);
            kryo.setRegistrationRequired(true);
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            kryo.register(Ex_Node.class,new JavaSerializer());
            kryo.register(Send_Node.class,new JavaSerializer());
            kryo.register(ConnectionData.class,new JavaSerializer());
            Input input = new Input(socket.getInputStream());
            while(true)
            {
                System.out.println(id+"等待");
//                if(input == null){System.out.println("真是空");}
                data = kryo.readObject(input,ConnectionData.class);


                synchronized (Server.lock)
                {
                    //
                    long start = System.currentTimeMillis();
                    byte[] bytes;
                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
                    ObjectOutputStream oo = new ObjectOutputStream(bo);
                    oo.writeObject(data);
                    bytes = bo.toByteArray();
                    Server.byteall+=bytes.length;
                    bo.close();
                    oo.close();
                    long end = System.currentTimeMillis();
                    Server.getdatashiptime+=(end-start);
                    //存放接收信息
                    Server.getlist.put(id,data);
                    //将等待条件加一
                    Server.condition++;
                    //唤醒主程序看是否满足进入下一步的条件
                    Server.lock.notifyAll();
                }
                System.out.println("接收消息"+id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class Server_send implements Runnable
{
    private Socket socket;
    private int id;
    public Server_send(int id,Socket socket) {
        this.id = id;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            //ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            Kryo kryo = new Kryo();
            kryo.setReferences(true);
            kryo.setRegistrationRequired(true);
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            kryo.register(Ex_Node.class,new JavaSerializer());
            kryo.register(Send_Node.class,new JavaSerializer());
            kryo.register(ConnectionData.class,new JavaSerializer());
            Output output = new Output(socket.getOutputStream());
            while (true)
            {
                if(Server.sendflaglist.get(id))
                {
                    long startTime=System.currentTimeMillis();
                    kryo.writeObject(output,Server.sendlist.get(id));
                    output.flush();
                    long endTime=System.currentTimeMillis();
                    System.out.println(id+"发送时间： "+(endTime-startTime)/1000+"ms");
                    synchronized (Server.lock)
                    {
                        long start = System.currentTimeMillis();
                        byte[] bytes;
                        ByteArrayOutputStream bo = new ByteArrayOutputStream();
                        ObjectOutputStream oo = new ObjectOutputStream(bo);
                        oo.writeObject(Server.sendlist.get(id));
                        bytes = bo.toByteArray();
                        Server.byteall+=bytes.length;
                        bo.close();
                        oo.close();
                        long end = System.currentTimeMillis();
                        Server.getdatashiptime+=(end-start);
                        //重置发送标志
                        Server.sendflaglist.set(id,false);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}