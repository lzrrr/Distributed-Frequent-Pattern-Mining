package Base_Class;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Integer.parseInt;

public class Pattern implements Serializable {
    private static final long serialVersionUID = 3L;
    //模式应携带的信息
    private ArrayList<String> forwardedges;
    private ArrayList<String> backwardedges;
    private ArrayList<Set<String>> MNI_List;
    private ArrayList<String> column_label; //当前模式每列对应的标签

    //总的MNI为两者之和
    private HashMap<Integer,Set<String>> Internal_MNI; //实点的MNI
    private HashMap<Integer,Set<String>> Exteranal_MNI; //当前站点的实点在其他站点形成的模式实例的MNI;key:MNI列号，value:点的id集合
    //提前终止信息
    private ArrayList<Integer> AllMNI; //该模式全局支持度
    private ArrayList<Integer> localMNI; //该模式当前站点实点MNI-其他站点可验证的点MNI(该值>=0)

    //实例扩展的需要的辅助信息
    private String Id; //当前模式id
    private String fatherId;    //父Id
    private int ex_column; //扩展点的位置
    private int new_column; //新点的位置
    private String targetLabel; //新点的标签
    private boolean isOutGoing; //true:outgoingEdge;  false:incomingEdge


    public Pattern(String Id, String fatherId) {
        this.Id = Id;
        this.fatherId = fatherId;
        this.MNI_List = new ArrayList<>();
        this.forwardedges = new ArrayList<>();
        this.backwardedges = new ArrayList<>();
        this.column_label = new ArrayList<>();
        this.Exteranal_MNI = new HashMap<>();
        this.Internal_MNI = new HashMap<>();
        this.AllMNI = new ArrayList<>();
        this.localMNI = new ArrayList<>();
    }


    public String getId() {
        return Id;
    }

    public String getFatherId() {
        return fatherId;
    }

    public void setId(String id) {
        Id = id;
    }

    public void setFatherId(String fatherId) {
        this.fatherId = fatherId;
    }

    public int getEx_column() {
        return ex_column;
    }

    public void setEx_column(int ex_column) {
        this.ex_column = ex_column;
    }

    public int getNew_column() {
        return new_column;
    }

    public void setNew_column(int new_column) {
        this.new_column = new_column;
    }

    public String getTargetLabel() {
        return targetLabel;
    }

    public void setTargetLabel(String targetLabel) {
        this.targetLabel = targetLabel;
    }

    public boolean isOutGoing() {
        return isOutGoing;
    }

    public void setOutGoing(boolean outGoing) {
        isOutGoing = outGoing;
    }

    public ArrayList<Set<String>> getMNI_List() {
        return MNI_List;
    }

    public void setMNI_List(ArrayList<Set<String>> MNI_List) {
        this.MNI_List = MNI_List;
    }

    public ArrayList<String> getForwardedges() {
        return forwardedges;
    }

    public void setForwardedges(ArrayList<String> forwardedges) {
        this.forwardedges = forwardedges;
    }

    public ArrayList<String> getBackwardedges() {
        return backwardedges;
    }

    public void setBackwardedges(ArrayList<String> backwardedges) {
        this.backwardedges = backwardedges;
    }

    public ArrayList<String> getColumn_label() {
        return column_label;
    }

    public void setColumn_label(ArrayList<String> column_label) {
        this.column_label = column_label;
    }

    public HashMap<Integer, Set<String>> getExteranal_MNI() {
        return Exteranal_MNI;
    }

    public void setExteranal_MNI(HashMap<Integer, Set<String>> exteranal_MNI) {
        Exteranal_MNI = exteranal_MNI;
    }

    public HashMap<Integer, Set<String>> getInternal_MNI() {
        return Internal_MNI;
    }

    public void setInternal_MNI(HashMap<Integer, Set<String>> internal_MNI) {
        Internal_MNI = internal_MNI;
    }

    public ArrayList<Integer> getAllMNI() {
        return AllMNI;
    }

    public void setAllMNI(ArrayList<Integer> allMNI) {
        AllMNI = allMNI;
    }

    public ArrayList<Integer> getLocalMNI() {
        return localMNI;
    }

    public void setLocalMNI(ArrayList<Integer> localMNI) {
        this.localMNI = localMNI;
    }

    /*@Override
    public String toString() {
        if(MNI_List!=null){
            String s = "{patternGraph:" + patternGraph + "+" + "MNI: [";
            int i;
            for (i = 0; i < MNI_List.size() - 1; i++) {
                s += MNI_List.get(i).size() + ", ";
            }
            s += MNI_List.get(i).size() + "]}";
            return s;
        }else {
            return  "{patternGraph:" + patternGraph + "+" + "MNI: [ ]}";
        }
    }*/


    //按照前向生成的点的顺序，生成对应后向边的访问列表
    public HashMap<Integer,ArrayList<String>> Getback(int column,ArrayList<String> Getorder,ArrayList<String> backedge)
    {
        HashMap<Integer,ArrayList<String>> backmap = new HashMap<>();
        ArrayList<Integer> idorder = new ArrayList<>();
        idorder.add(column);

        for (String info : Getorder) {
            ArrayList<String> list = new ArrayList<>();
            ArrayList<String> rmlist = new ArrayList<>();
            int leftInfo = parseInt(info.split(" ")[0]);
            int rightInfo = parseInt(info.split(" ")[2]);
            int nowid = -1;
            if (idorder.contains(leftInfo)) {
                nowid = rightInfo;
            } else {
                nowid = leftInfo;
            }
            for (String infoback : backedge)
            {
                int leftInfoback = parseInt(infoback.split(" ")[0]);
                int rightInfoback = parseInt(infoback.split(" ")[2]);
                if((leftInfoback==nowid && idorder.contains(rightInfoback))||(rightInfoback==nowid && idorder.contains(leftInfoback)))
                {
                    if(!backmap.containsKey(nowid))
                    {backmap.put(nowid,list);}
                    backmap.get(nowid).add(infoback);
                    rmlist.add(infoback);
                }
            }
            idorder.add(nowid);
            backedge.removeAll(rmlist);
        }


//        System.out.println(column+" backmap");
//        System.out.println(Getorder);
//        for(int nid : backmap.keySet())
//        {
//            System.out.println(nid);
//            System.out.println(backmap.get(nid));
//        }

        return backmap;
    }

    public ArrayList<String> Getorder(int column)
    {
        ArrayList<String> list = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        visited.add(column);
        int size = forwardedges.size();
        ArrayList<String> newInfoList = new ArrayList<>();
        newInfoList.addAll(forwardedges);
        while (list.size()<size)
        {
            for (String edge_s : newInfoList)
            {
                int leftInfo = parseInt(edge_s.split(" ")[0]);
                int rightInfo = parseInt(edge_s.split(" ")[2]);
                if(visited.contains(leftInfo)||visited.contains(rightInfo))
                {
                    list.add(edge_s);
                    visited.add(leftInfo);
                    visited.add(rightInfo);
                }
            }
            newInfoList.removeAll(list);
        }
        //测试
//        System.out.println("获取顺序："+list);
        return list;
    }

    @Override
    public Pattern clone() {
        Pattern outer = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            outer = (Pattern) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return outer;
    }
}
