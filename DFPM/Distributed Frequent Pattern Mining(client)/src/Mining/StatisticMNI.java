package Mining;

import Base_Class.Pattern;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class StatisticMNI {

    public static void Get_static(HashMap<String,HashMap<Integer, Set<String>>> external,HashMap<String, Pattern> candidate_patterns,int patternsize,HashMap<String, ArrayList<Integer>> pattern_mnitable)
    {
        HashMap<String, Pattern> virtual_candidate_patterns = new HashMap<>();//存放只有虚拟匹配没有本地实例的候选模式
        for (String id : external.keySet())
        {
            if(candidate_patterns.containsKey(id))
            {
                Pattern p = candidate_patterns.get(id);
                p.setExteranal_MNI(external.get(id));
            }
            else
            {
                Pattern p = new Pattern(id,null);
                p.setExteranal_MNI(external.get(id));
                virtual_candidate_patterns.put(id,p);
            }
        }
        for (Pattern p : candidate_patterns.values())
        {
            StatisticMNI.Get_MNIlist(p,pattern_mnitable,patternsize);
        }
        for (Pattern p : virtual_candidate_patterns.values())
        {
            StatisticMNI.Get_MNIlist(p,pattern_mnitable,patternsize);
        }
    }

    public static void Get_MNIlist(Pattern pattern, HashMap<String, ArrayList<Integer>> pattern_mnitablem,int max)
    {
        if(pattern.getFatherId()==null)
        {
            ArrayList<Integer> arrayList = new ArrayList<>();
            HashMap<Integer,Set<String>> Exteranal_MNI = pattern.getExteranal_MNI();
            for (int i=0;i<max;++i)
            {
                if(Exteranal_MNI.containsKey(i))
                {
                    arrayList.add(Exteranal_MNI.get(i).size());
                }
                else
                {
                    arrayList.add(0);
                }
            }
            pattern_mnitablem.put(pattern.getId(),arrayList);
        }
        else {
        HashMap<Integer, Set<String>> Internal_MNI = pattern.getInternal_MNI();
        HashMap<Integer,Set<String>> Exteranal_MNI = pattern.getExteranal_MNI();
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i=0;i<Internal_MNI.size();++i)
        {
            int size = 0;
            if(Exteranal_MNI.containsKey(i))
            {
                Set<String> internal = Internal_MNI.get(i); //获取对应列实结点集合
                Set<String> external = Exteranal_MNI.get(i); //获取对应列外连实结点集合
                //存入提前终止信息
                int localsize = pattern.getMNI_List().get(i).size()-external.size();
                if(localsize<0)
                {localsize=0;}
                pattern.getLocalMNI().add(localsize);
                external.removeAll(internal); //由于只需在统计MNI时使用一次所以不用拷贝直接去重
                size = internal.size()+external.size(); //实际MNI为两个实结点集合之和
            }
            else
            {
                size = Internal_MNI.get(i).size();
                //存入提前终止信息
                pattern.getLocalMNI().add(size);
            }
            arrayList.add(size);
        }
            pattern_mnitablem.put(pattern.getId(),arrayList);
        }
    }
}
