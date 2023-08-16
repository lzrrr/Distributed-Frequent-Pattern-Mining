package Work;

import Base_Class.*;
import Mining.*;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class Client {

    public static boolean connection_state = false;
    public static volatile boolean send_flag = false;//是否发送标志
    public static volatile boolean recive_flag = false; //是否等待接收标志
    public static final Object lock = new Object(); //用于接收消息时锁住getlist再写入信息
    //发送data
    public static volatile ConnectionData senddata;
    //接收data
    public static volatile ConnectionData getdata;

    public static String ipadd = "127.0.0.1";

    public static void main(String[] args) throws IOException, InterruptedException {
        String position = "1"; //站点id
        int positionid = Integer.parseInt(position);
        int positionsum = 0;
        int corenum = 8;
        //创建图
        CreateGraph createGraph = new CreateGraph("D:\\aaMyProjects\\lab_data\\METIS\\5-26\\Mico\\mico2.lg", position);
        Graph g = createGraph.graph;
        int support = 0;
        //与服务器连接
        connect();

        HashMap<String, Pattern> pattern_map = new HashMap<>(); //存放本地所有模式id,和模式

        HashMap<String, ArrayList<Integer>> Edge_Frequence = new HashMap<>(); //存放每种类型边的MNI,发送给master
        //统计所有边的MNI
        Frequent_Edge frequent_edge = new Frequent_Edge(g, Edge_Frequence);
        //将边频次发送服务器(1s)
        {
            //将发送数据写入
            ConnectionData connectionData = new ConnectionData();
            connectionData.setEdge_Frequence(Edge_Frequence);
            senddata = connectionData;
            //将发送信号设为true,开始发送
            send_flag = true;
        }
        System.out.println("将边频次发送给服务器");

        HashMap<String, String> Edges_id = new HashMap<>(); //存放master发来的频繁边,key:边的String，value:边的id
        //等待master返回频繁边string和id(2r)
        synchronized (lock)
        {
            while (!recive_flag)
            {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        recive_flag = false;
        Edges_id = getdata.getEdges_id();
        support = getdata.getSupport();
        positionsum = getdata.getPositionsum();
//        System.out.println("收到服务器发送来的频繁边模式 "+Edges_id);
        //利用服务器返回的频繁边，生成频繁边模式和边的onehopmap
        frequent_edge.Get_Frequent_Edgepattern(Edges_id, pattern_map);
        System.out.println(Edges_id.keySet());

        //slave向服务器发送可以前向扩展标志
        {
            //将发送数据写入
            ConnectionData connectionData = new ConnectionData();
            connectionData.setFlag(0);
            senddata = connectionData;
            //将发送信号设为true,开始发送
            send_flag = true;
        }
        System.out.println("通知服务器可以进行前向扩展");

        int patternsize = 2; //模式大小

        System.out.println("------------------前向扩展-----------------------");

        while (true) {

            //等待master返回候选模式(4r)
            synchronized (lock)
            {
                while (!recive_flag)
                {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            recive_flag = false;
            System.out.println("收到候选模式");
            HashMap<String, ArrayList<String>> send_pattern = new HashMap<>(); //存放发来的候选模式，key:模式id,value：1，父模式id,2.扩展列号，3.新列号，4.新列标签,5.是否是
            send_pattern = getdata.getSend_pattern();
            patternsize = getdata.getPatternsize();
//            System.out.println("收到服务器发来的候选模式： "+send_pattern.keySet());

            //将发来的候选模式转换成新模式,并存入候选模式集
            HashMap<String, Pattern> candidate_patterns = new HashMap<>();//本地可扩的候选模式
            Trans_sendPattern.trans_forward(send_pattern, pattern_map, candidate_patterns);
            //将虚拟扩展点需要的信息建立list，发送给对master转发给对应站点请求缺失信息
            HashMap<String, HashMap<String, Ex_Node>> need_message = new HashMap<>(); //需要的虚结点信息,key:站点id,value：对应虚结点的信息

            //-------------------运行时间测试---------------------------
                long needmesg_start = System.currentTimeMillis();
            //-------------------运行时间测试---------------------------

            Get_Needmessage get_needmessage = new Get_Needmessage(g, need_message, pattern_map, candidate_patterns, positionid, positionsum);

            //-------------------运行时间测试---------------------------
               long needmesg_end = System.currentTimeMillis();
            System.out.println("查找需要的边时间： "+(needmesg_end-needmesg_start));
            //-------------------运行时间测试---------------------------

            //将需要的缺失边信息请求发送给master(5s)，和6r可以异步执行
            {
                //将发送数据写入
                ConnectionData connectionData = new ConnectionData();
                connectionData.setNeed_message(need_message);
                senddata = connectionData;
                //将发送信号设为true,开始发送
                send_flag = true;
            }
            System.out.println("向服务器发送需要请求的缺失边信息");

            //接收master发来的其他站点的请求(5r)
            HashMap<Integer,HashMap<String,Ex_Node>> trans_message = new HashMap<>(); //保存其他站点的请求
            synchronized (lock)
            {
                while (!recive_flag)
                {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            recive_flag = false;
            trans_message = getdata.getTrans_message();
            System.out.println("收到服务器转发的其他站点请求的缺失边信息");

            for (int j : trans_message.keySet())
            {
                System.out.println(j+"站点请求虚点个数： "+trans_message.get(j).size());
            }

            //按照请求查找信息

            //-------------------运行时间测试---------------------------
            long needtran_start = System.currentTimeMillis();
            //-------------------运行时间测试---------------------------

            for (int j : trans_message.keySet())
            {
                HashMap<String,Ex_Node> exnodes = trans_message.get(j);
                Get_Needmessage.Getneedmessage(exnodes, g);
            }

            //-------------------运行时间测试---------------------------
            long needtran_end = System.currentTimeMillis();
            System.out.println("查询其他站点请求点的时间： "+(needtran_end-needtran_start));
            //-------------------运行时间测试---------------------------

            //将查找到的请求信息返回给master

            //-------------时间测试-----------------

            long quststart = System.currentTimeMillis();
            System.out.println("开始序列化： "+quststart);
            //-------------时间测试-----------------

            {
                //将发送数据写入
                ConnectionData connectionData = new ConnectionData();
                connectionData.setTrans_nodes(trans_message);
                senddata = connectionData;
                //将发送信号设为true,开始发送
                send_flag = true;
            }
            System.out.println("将查找到的缺失边请求信息返回给服务器");


            //接收查找结果
            HashMap<String, Ex_Node> neednodes = new HashMap<>();
            synchronized (lock)
            {
                while (!recive_flag)
                {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            recive_flag = false;
            neednodes = getdata.getNeed_nodes();
            System.out.println("收到服务器转发来的需要的缺失边");

            //------------时间测试-------------------
            //slave向服务器发送可以前向扩展标志
            {
                //将发送数据写入
                ConnectionData connectionData = new ConnectionData();
                connectionData.setFlag(0);
                senddata = connectionData;
                //将发送信号设为true,开始发送
                send_flag = true;
            }
            synchronized (lock)
            {
                while (!recive_flag)
                {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            recive_flag = false;
            System.out.println("时间测试1结束");
            //-------------时间测试---------------------

            //接收到所有需求信息后将接收信息存入本地
            Get_Needmessage.savemessage(neednodes, g);

            //对候选模式集进行扩展生成每种模式的MNIlist
            HashMap<String, HashMap<String, HashMap<Integer, Set<String>>>> send_external = new HashMap<>(); //本地生成模式的实例中含有虚点需要发送给对应站点模式，key:对应站点，value：模式的Exteranal_MNI

            //-----------------------------------------------------------------------------------------------
            long start1 = System.currentTimeMillis();
            //1.将扩展改为多线程，首先将候选模式分成16份，以同样的方式放入程序进行前向扩展，然后，将扩展的结果汇总
            //2.对候选模式进行划分
            int partnum = 0; //记录该存储到第几个线程
            int threadnum = 0; //记录线程个数
            ArrayList<HashMap<String,Pattern>> candidate_patterns_arr = new ArrayList<>();//存储每个线程分配的候选模式
            for (int i=0;i<corenum;++i)
            {
                candidate_patterns_arr.add(new HashMap<>());
            }
            for (String pid : candidate_patterns.keySet())
            {
                candidate_patterns_arr.get(partnum).put(pid,candidate_patterns.get(pid));
                partnum++;
                partnum = partnum%corenum;
                if(threadnum<corenum)
                {
                    threadnum++;
                }
            }
            System.out.println("线程个数："+threadnum);
            //3.按照线程个数划分send_external
            ArrayList<HashMap<String, HashMap<String, HashMap<Integer, Set<String>>>>> send_external_arr = new ArrayList<>();
            for (int i=0;i<threadnum;++i)
            {
                HashMap<String, HashMap<String, HashMap<Integer, Set<String>>>> send_external_thread = new HashMap<>();
                send_external_arr.add(send_external_thread);
            }
            //4.将分配好的候选模式和send_external分配给线程进行前向扩展
            ArrayList<Thread> threads_list = new ArrayList<>();
            for (int i=0;i<threadnum;++i)
            {
                HashMap<String,Pattern> t_candidate_patterns = candidate_patterns_arr.get(i);
                HashMap<String, HashMap<String, HashMap<Integer, Set<String>>>> t_send_external = send_external_arr.get(i);
                int finalSupport = support;
                Thread thread = new Thread(()->{
                    ForwardEx forwardEx = new ForwardEx(t_candidate_patterns,pattern_map,g,t_send_external, finalSupport);
                });
                thread.start();
                threads_list.add(thread);
            }
            //5.等待所有的线程都执行完毕
            for (int i=0;i<threadnum;++i)
            {
                threads_list.get(i).join();
            }
            //5.将各个线程扩展好的模式在主进程中进行更新
            candidate_patterns = new HashMap<>();
            send_external = new HashMap<>();
            for (int i=0;i<threadnum;++i)
            {
                candidate_patterns.putAll(candidate_patterns_arr.get(i));
                for (String pid : send_external_arr.get(i).keySet())
                {
                    if(!send_external.containsKey(pid))
                    {
                        send_external.put(pid,send_external_arr.get(i).get(pid));
                    }
                    else
                    {
                        send_external.get(pid).putAll(send_external_arr.get(i).get(pid));
                    }
                }
            }


            long end1 = System.currentTimeMillis();

            //----------------------------------------------------------------------------------------------

            System.out.println("完成本地前向扩展-forwardtime: "+(end1-start1));

            //将待转发虚拟匹配发送给master(7s)
            {
                //send send_external
                //将发送数据写入
                ConnectionData connectionData = new ConnectionData();
                connectionData.setSend_external(send_external);
                senddata = connectionData;
                //将发送信号设为true,开始发送
                send_flag = true;
            }
            System.out.println("已向服务器发送虚拟MNItable");

            //222
            //接收服务器转发来的其他站点传来的虚拟匹配(7r)
            HashMap<String, HashMap<Integer, Set<String>>> external = new HashMap<>();
            synchronized (lock)
            {
                while (!recive_flag)
                {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            recive_flag = false;
            external = getdata.getExternal_mni();
            System.out.println("已接收服务器发送来的虚拟MNItable");

            //------------时间测试-------------------
            //slave向服务器发送可以前向扩展标志
            {
                //将发送数据写入
                ConnectionData connectionData = new ConnectionData();
                connectionData.setFlag(0);
                senddata = connectionData;
                //将发送信号设为true,开始发送
                send_flag = true;
            }
            synchronized (lock)
            {
                while (!recive_flag)
                {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            recive_flag = false;
            System.out.println("时间测试2结束");
            //-------------时间测试---------------------

            //统计每个候选模式的MNI;将external存入候选模式
            HashMap<String, ArrayList<Integer>> pattern_mnitable = new HashMap<>();
            StatisticMNI.Get_static(external, candidate_patterns, patternsize, pattern_mnitable);


            //将候选模式的MNI发送给master(8s)
            {
                //将发送数据写入
                ConnectionData connectionData = new ConnectionData();
                connectionData.setPattern_mnitable(pattern_mnitable);
                senddata = connectionData;
                //将发送信号设为true,开始发送
                send_flag = true;
            }
            System.out.println("已向服务器发送候选模式MNI");

            //等待master发送频繁模式id(9r)
            //收到前向扩展终止标志跳出循环
            HashMap<String, ArrayList<Integer>> frequentpatternid = new HashMap<>();
            synchronized (lock)
            {
                while (!recive_flag)
                {
                    System.out.println(recive_flag);
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            recive_flag = false;
            frequentpatternid = getdata.getFrequentpatternid();
            System.out.println("已接收服务器发送来的频繁模式id");

            //slave向服务器发送可以前向扩展标志
            {
                //将发送数据写入
                ConnectionData connectionData = new ConnectionData();
                connectionData.setFlag(0);
                senddata = connectionData;
                //将发送信号设为true,开始发送
                send_flag = true;
            }
            System.out.println("向服务器发送可以继续前向扩展标志");

            if (frequentpatternid.size()==0) {
                System.out.println("前向扩展结束");
                break;
            }

            //将存在本地扩展的频繁模式存入patternmap
            System.out.println("本栋频繁模式");
            for (String id : frequentpatternid.keySet()) {
                if (candidate_patterns.containsKey(id)) {
//                    System.out.println(id);
                    pattern_map.put(id, candidate_patterns.get(id));
                    //存入该模式的全局支持度
                    candidate_patterns.get(id).setAllMNI(frequentpatternid.get(id));
                }
            }

        }

        System.out.println("----------------后向扩展-------------------");

        while (true) {

            //等待master发送后向候选模式(11r)
            HashMap<String, ArrayList<String>> send_pattern = new HashMap<>(); //存放发来的候选模式，key:模式id,value：1，父模式id,2.扩展列号，3.新列号，4.新列标签,5.是否是
            synchronized (lock)
            {
                while (!recive_flag)
                {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            recive_flag = false;
            if(getdata.getFlag()==-1)
            {
                System.out.println("后向扩展终止");
                break;
            }
            send_pattern = getdata.getSend_pattern();
            patternsize = getdata.getPatternsize();
            System.out.println("已接收服务器发送来的后向候选模式");

            //将发来的候选模式转换成新模式,并存入候选模式集
            HashMap<String, Pattern> candidate_patterns = new HashMap<>();//本地可扩的候选模式
            Trans_sendPattern.trans_backward(send_pattern, pattern_map, candidate_patterns);
            System.out.println(candidate_patterns.keySet());
            //对候选模式集进行扩展生成每种模式的MNIlist
            HashMap<String, HashMap<String, HashMap<Integer, Set<String>>>> send_external = new HashMap<>(); //本地生成模式的实例中含有虚点需要发送给对应站点模式，key:对应站点，value：模式的Exteranal_MNI
            //-----------------------------------------------------------------------------------------------
            long start2 = System.currentTimeMillis();
            //1.将扩展改为多线程，首先将候选模式分成16份，以同样的方式放入程序进行前向扩展，然后，将扩展的结果汇总
            //2.对候选模式进行划分
            int partnum = 0; //记录该存储到第几个线程
            int threadnum = 0; //记录线程个数
            ArrayList<HashMap<String,Pattern>> candidate_patterns_arr = new ArrayList<>();//存储每个线程分配的候选模式
            for (int i=0;i<corenum;++i)
            {
                candidate_patterns_arr.add(new HashMap<>());
            }
            for (String pid : candidate_patterns.keySet())
            {
                candidate_patterns_arr.get(partnum).put(pid,candidate_patterns.get(pid));
                partnum++;
                partnum = partnum%corenum;
                if(threadnum<corenum)
                {
                    threadnum++;
                }
            }
            System.out.println("线程个数："+threadnum);
            //3.按照线程个数划分send_external
            ArrayList<HashMap<String, HashMap<String, HashMap<Integer, Set<String>>>>> send_external_arr = new ArrayList<>();
            for (int i=0;i<threadnum;++i)
            {
                HashMap<String, HashMap<String, HashMap<Integer, Set<String>>>> send_external_thread = new HashMap<>();
                send_external_arr.add(send_external_thread);
            }
            //4.将分配好的候选模式和send_external分配给线程进行前向扩展
            ArrayList<Thread> threads_list = new ArrayList<>();
            for (int i=0;i<threadnum;++i)
            {
                HashMap<String,Pattern> t_candidate_patterns = candidate_patterns_arr.get(i);
                HashMap<String, HashMap<String, HashMap<Integer, Set<String>>>> t_send_external = send_external_arr.get(i);
                int finalSupport = support;
                Thread thread = new Thread(()->{
                    BackwardEx backwardEx = new BackwardEx(t_candidate_patterns,pattern_map,g,t_send_external, finalSupport);
                });
                thread.start();
                threads_list.add(thread);
            }
            //5.等待所有的线程都执行完毕
            for (int i=0;i<threadnum;++i)
            {
                threads_list.get(i).join();
            }
            //5.将各个线程扩展好的模式在主进程中进行更新
            candidate_patterns = new HashMap<>();
            send_external = new HashMap<>();
            for (int i=0;i<threadnum;++i)
            {
                candidate_patterns.putAll(candidate_patterns_arr.get(i));
                for (String pid : send_external_arr.get(i).keySet())
                {
                    if(!send_external.containsKey(pid))
                    {
                        send_external.put(pid,send_external_arr.get(i).get(pid));
                    }
                    else
                    {
                        send_external.get(pid).putAll(send_external_arr.get(i).get(pid));
                    }
                }
            }

            long end2 = System.currentTimeMillis();

            //----------------------------------------------------------------------------------------------
            System.out.println("完成本地后向扩展-backwardtime: "+(end2-start2));

            //将待转发虚拟匹配发送给master(12s)
            {
                //send send_external
                //将发送数据写入
                ConnectionData connectionData = new ConnectionData();
                connectionData.setSend_external(send_external);
                senddata = connectionData;
                //将发送信号设为true,开始发送
                send_flag = true;
            }
            System.out.println("已向服务器发送后向虚拟MNItable");

            //接收服务器转发来的其他站点传来的虚拟匹配(7r)
            HashMap<String, HashMap<Integer, Set<String>>> external = new HashMap<>();
            synchronized (lock)
            {
                while (!recive_flag)
                {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            recive_flag = false;
            external = getdata.getExternal_mni();
            System.out.println("已接收服务器发送来的后向虚拟MNItable");

            //统计每个候选模式的MNI;将external存入候选模式
            HashMap<String, ArrayList<Integer>> pattern_mnitable = new HashMap<>();
            StatisticMNI.Get_static(external, candidate_patterns, patternsize, pattern_mnitable);

            //将候选模式的MNI发送给master(8s)
            {
                //将发送数据写入
                ConnectionData connectionData = new ConnectionData();
                connectionData.setPattern_mnitable(pattern_mnitable);
                senddata = connectionData;
                //将发送信号设为true,开始发送
                send_flag = true;
            }
            System.out.println("已向服务器发送候选模式MNI");

            //等待master发送频繁模式id(9r)
            //收到前向扩展终止标志跳出循环
            HashMap<String, ArrayList<Integer>> frequentpatternid = new HashMap<>();
            synchronized (lock)
            {
                while (!recive_flag)
                {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            recive_flag = false;
            frequentpatternid = getdata.getFrequentpatternid();
            System.out.println("已接收服务器发送来的频繁模式id");

            //slave向服务器发送可以前向扩展标志
            {
                //将发送数据写入
                ConnectionData connectionData = new ConnectionData();
                connectionData.setFlag(0);
                senddata = connectionData;
                //将发送信号设为true,开始发送
                send_flag = true;
            }
            System.out.println("向服务器发送可以继续后向扩展标志");

            //将存在本地扩展的频繁模式存入patternmap
            if(frequentpatternid.size()>0)
            {
                for (String id : frequentpatternid.keySet()) {
                    if (candidate_patterns.containsKey(id)) {
                        pattern_map.put(id, candidate_patterns.get(id));
                        pattern_map.get(id).setAllMNI(frequentpatternid.get(id));
                    }
                }
            }
        }
    }
    private static void connect()
    {
        try {
            Socket socket = new Socket(ipadd, 9999);
            connection_state = true;
            // ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            //ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            new Thread(new Client_listen(socket)).start();
            new Thread(new Client_send(socket)).start();
            // new Thread(new Client_heart(socket,oos)).start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            connection_state = false;
        }
    }

    public static void reconnect()
    {
        while (!connection_state){
            System.out.println("正在尝试重新连接...");
            connect();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}


class Client_listen implements Runnable{
    private final Socket socket;
    public Client_listen(Socket socket) {
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
            while (true)
            {
                Client.getdata = kryo.readObject(input,ConnectionData.class);
                synchronized (Client.lock)
                {
//                    System.out.println("收到消息");
                    Client.recive_flag = true;
                    Client.lock.notifyAll(); //唤醒等待的主进程
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Client_send implements Runnable
{
    private final Socket socket;

    public Client_send(Socket socket) {
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
            Output output = new Output(socket.getOutputStream());
            while (true)
            {
                if(Client.send_flag){
                    System.out.println("开始发送");
                    long startTime=System.currentTimeMillis();
                    kryo.writeObject(output,Client.senddata);
                    output.flush();
                    long endTime=System.currentTimeMillis();
                    System.out.println("发送时间： "+(endTime-startTime)+"ms");
                    Client.send_flag=false;
                    //统计数据发送量
//                    long start = System.currentTimeMillis();
//                    byte[] bytes;
//                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
//                    ObjectOutputStream oo = new ObjectOutputStream(bo);
//                    oo.writeObject(Client.senddata);
//                    bytes = bo.toByteArray();
//                    double sizedata = bytes.length;
//                    bo.close();
//                    oo.close();
//                    long end = System.currentTimeMillis();
//                    System.out.println("发送数据量："+sizedata/1024+"kb"+" time: "+(end-start));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
