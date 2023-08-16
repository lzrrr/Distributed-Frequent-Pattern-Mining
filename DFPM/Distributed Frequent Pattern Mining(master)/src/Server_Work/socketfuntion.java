package Server_Work;

import Base_Class.ConnectionData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class socketfuntion {


    /**
     * 获取各个客户端发来的虚拟匹配MNI
     * @param getlist 接收信息集合
     * @param id 客户端数量
     * @return
     */
//    public static Map<Integer, Map<String, Set<ArrayList<Integer>>>> GetVirtualPatternInstance(Map<Integer, ConnectionData> getlist, int id){
//        ArrayList<Map<Integer,Map<String, Set<ArrayList<Integer>>>>> TranVirtualPList = new ArrayList<>();
//        for(int i=0;i<id;++i)
//        {
//            TranVirtualPList.add(getlist.get(i).getVirtualPatternAddInstance());
//        }
//        getlist.clear();
//        //汇总所有待转发的实列
//        Map<Integer,Map<String, Set<ArrayList<Integer>>>> TranVirtualPatternAddInstancesum = new HashMap<>();
//        ScFunction.TranVirtualPatternAddInstancesum(TranVirtualPatternAddInstancesum,TranVirtualPList);
//        return TranVirtualPatternAddInstancesum;
//    }


    /**
     * 获取频繁边
     * @param getlist 接收信息集合
     * @param edgek 边数的top%
     * @param theta 模式的阈值
     * @param id 客户端数量
     * @return
     */
//    public static Map<String, Integer> GetFreqentEdges(Map<Integer,ConnectionData> getlist,double edgek,int theta,int id){
//        Map<String, Integer> Frequent;
//        Map<String, ArrayList<Integer>> FiEdgesCounts; //计数各个客户端发来的每种边
//        Map<String, ArrayList<Integer>> ScVertexCounts = new HashMap<>(); //汇总边频次
//        for(int i=0;i<id;++i)
//        {
//            FiEdgesCounts = getlist.get(i).getSupportMap();
//            ScVertexCounts = ScFunction.ScVertexCount(ScVertexCounts,FiEdgesCounts);
//        }
//        getlist.clear();
//        Map<String, Integer> SupportTransform = ScFunction.SupportTransform(ScVertexCounts);
//        System.out.println("edgeall: "+SupportTransform.size());
//        int edgenum = (int) (SupportTransform.size()*edgek);
//        System.out.println("edgenum: "+edgenum);
//        //获取topedge的边频次
//        theta = ScFunction.GetTheta(SupportTransform,edgenum);
//        //theta = 1519;
//        System.out.println("theta: "+theta);
//        Frequent = ScFunction.Frequent(SupportTransform,theta);
//
//        return Frequent;
//    }

    /**
     * 初始化是否发送flag
     * @param sendflaglist 存放是否发送flag的列表
     */
    public static void InitialSendflag(ArrayList<Boolean> sendflaglist){
        //初始化sendflaglist,默认不超过30个客户端
        for(int i=1;i<=30;++i)
        {
            boolean sendflag = false;
            sendflaglist.add(sendflag);
        }
    }

//    /**
//     * 告知各个客户端自己的socket编号
//     * @param sendlist 发送信息列表
//     * @param sendflaglist 是否执行发送操作flag
//     * @param id 总id数
//     */
//    public static void SendClientId(ArrayList<ConnectionData> sendlist,ArrayList<Boolean> sendflaglist,int id) {
//        for (int i = 0; i < id; ++i) {
//            ConnectionData senddata = new ConnectionData(11);
//            senddata.setId(i);
//            sendlist.add(senddata);
//            sendflaglist.set(i, true);
//        }
//    }

    /**
     * 发送各个客户端对应id与点集的起始终止num
     * @param sendlist
     * @param sendflaglist
     * @param Finum
     * @param id
     */
//    public static void SendFinum(ArrayList<ConnectionData> sendlist, ArrayList<Boolean> sendflaglist, Map<Integer,Set<Integer>> Finum, int id)
//    {
//        for (int i = 0; i < id; ++i) {
//            ConnectionData senddata = new ConnectionData(8);
//            senddata.setFinum(Finum);
//            sendlist.add(senddata);
//            sendflaglist.set(i, true);
//        }
//    }
//
//    public static void SendFrequenEdge(ArrayList<ConnectionData> sendlist, ArrayList<Boolean> sendflaglist, Map<String, Integer> frequentEdges,int id)
//    {
//        for (int i = 0; i < id; ++i) {
//            ConnectionData senddata = new ConnectionData(5);
//            senddata.setFrequentEdges(frequentEdges);
//            sendlist.add(senddata);
//            sendflaglist.set(i, true);
//        }
//    }
//
//    /**
//     * 发送模式（频繁边模式、候选集模式）
//     * @param sendlist
//     * @param sendflaglist
//     * @param Pattern
//     * @param id
//     */
//    public static void SendPattern(ArrayList<ConnectionData> sendlist, ArrayList<Boolean> sendflaglist, Map<String, Pattern> Pattern,int id)
//    {
//        for (int i = 0; i < id; ++i) {
//            ConnectionData senddata = new ConnectionData(5);
//            senddata.setPattern(Pattern);
//            sendlist.add(senddata);
//            sendflaglist.set(i, true);
//        }
//    }
//
//    /**
//     * Sc向各个客户端转发含虚点的模式实列
//     * @param sendlist
//     * @param sendflaglist
//     * @param TranVirtualPatternAddInstancesum
//     * @param id
//     */
//    public static void SendVirtuPattern(ArrayList<ConnectionData> sendlist, ArrayList<Boolean> sendflaglist, Map<Integer,Map<String, Set<ArrayList<Integer>>>> TranVirtualPatternAddInstancesum, int id)
//    {
//        for(int i=0;i<id;++i)
//        {
//            ConnectionData senddata = new ConnectionData(6);
//            if(TranVirtualPatternAddInstancesum.containsKey(i))
//            {
//                senddata.setTranVirtualPatternAddInstance(TranVirtualPatternAddInstancesum.get(i));
//            }
//            sendlist.add(senddata);
//            sendflaglist.set(i,true);
//        }
//    }
//
//    /**
//     * 发送扩展结束标志
//     * @param sendlist
//     * @param sendflaglist
//     * @param id
//     */
//    public static void SendStop(ArrayList<ConnectionData> sendlist, ArrayList<Boolean> sendflaglist,int id)
//    {
//        for (int i = 0; i < id; ++i) {
//            ConnectionData senddata = new ConnectionData(9);
//            sendlist.add(senddata);
//            sendflaglist.set(i, true);
//        }
//    }
//
//    /**
//     * 告知客户端当前扩的是哪一层
//     * @param sendlist
//     * @param sendflaglist
//     * @param level
//     * @param id
//     */
//    public static void SendBackLevel(ArrayList<ConnectionData> sendlist, ArrayList<Boolean> sendflaglist,int level,int id)
//    {
//        for (int i = 0; i < id; ++i) {
//            ConnectionData senddata = new ConnectionData(10);
//            senddata.setLevel(level);
//            sendlist.add(senddata);
//            sendflaglist.set(i, true);
//        }
//    }

}
