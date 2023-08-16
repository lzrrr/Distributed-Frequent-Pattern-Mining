package Mining;

import Base_Class.Pattern_ex;

import java.util.ArrayList;
import java.util.HashMap;

public class StatisticMNItable {

    /**
     *
     * @param partical_Frequence 各个slave统计的模式每列MNI
     * @param pattern_Frequence 汇总后的模式每列MNI
     */
    public static void Get_Frequence(HashMap<String,HashMap<String, ArrayList<Integer>>> partical_Frequence,HashMap<String, ArrayList<Integer>> pattern_Frequence)
    {
        for (String slave_id : partical_Frequence.keySet())
        {
            HashMap<String,ArrayList<Integer>> pattern_mnitable = partical_Frequence.get(slave_id);
            for (String id : pattern_mnitable.keySet()) {
                ArrayList<Integer> mnitable = pattern_mnitable.get(id); //获取当前模式的部分MNI计数
                if (!pattern_Frequence.containsKey(id)) //如果没有该模式，直接设置为当前MNI
                {
                    pattern_Frequence.put(id,mnitable);
                }
                else
                {
                    for (int i=0;i<mnitable.size();++i) //将MNI计数求和
                    {
                        mnitable.set(i,mnitable.get(i)+pattern_Frequence.get(id).get(i));
                    }
                    pattern_Frequence.put(id,mnitable); //更新
                }
            }
        }
    }

    public static void Get_Frequentpattern(HashMap<String, ArrayList<Integer>> pattern_Frequence,HashMap<String, Pattern_ex> candidate_patterns,int support,HashMap<String, Pattern_ex> patternMap)
    {
        for (String id : pattern_Frequence.keySet())
        {
            boolean flag = true;
            int sup = 999999999;
            for (int mni : pattern_Frequence.get(id))
            {
                if(mni<sup)
                {
                    sup=mni;
                }
                if(support>mni)
                {
                    flag=false;
                    break;
                }
            }
            candidate_patterns.get(id).setSupport(sup);
            if(flag)
            {
                patternMap.put(id,candidate_patterns.get(id));
            }
        }
    }

}
