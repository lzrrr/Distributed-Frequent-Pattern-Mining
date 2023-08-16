package Mining;

import Base_Class.Node;
import Base_Class.Pattern_ex;

import java.util.*;

import static java.lang.Integer.parseInt;

public class Backward_topk {
    //输入前向扩展生成的code-tree，后向在此基础上进行扩展
    ArrayList<HashMap<String, Pattern_ex>> T;
    HashMap<String,ArrayList<Set<String>>> Seed_map;
    int support; //支持度
    int K;

    /**
     * 将每一层后向扩展的Topk结果汇总到map
     * @param SumItrsOfP 要汇总的map
     * @param ItrsOfP   当前层扩出的结果
     * @param k TopK大小
     */
    public static HashMap<Integer,HashMap<String, Pattern_ex>> AddTopk(HashMap<Integer,HashMap<String, Pattern_ex>> SumItrsOfP, HashMap<Integer,HashMap<String, Pattern_ex>> ItrsOfP, int k)
    {
        ArrayList<Integer> numofp = new ArrayList();
        for(Integer i : ItrsOfP.keySet())
        {
            numofp.add(i);
        }
        Collections.sort(numofp, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2-o1;
            }
        });

        //将Itrsp中topk加入sumofp
        int book = 0;
        for (int i=0;i<numofp.size();++i)
        {
            int intrs = numofp.get(i);
            int nump = ItrsOfP.get(intrs).size();
            HashMap<String, Pattern_ex> combineResultMap = new HashMap<>();
            if(SumItrsOfP.containsKey(intrs))
            {
                combineResultMap.putAll(SumItrsOfP.get(intrs));
            }
            combineResultMap.putAll(ItrsOfP.get(intrs));
            SumItrsOfP.put(intrs,combineResultMap);
            book =book + nump;
            if(book>=k)
            {
                break;
            }
        }

        //提取SumItrsOfP中的topk
        ArrayList<Integer> numofs = new ArrayList();
        for(Integer i :SumItrsOfP.keySet())
        {
            numofs.add(i);
        }
        Collections.sort(numofs, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2-o1;
            }
        });

        book = 0;

        //新建一个map存放topk
        HashMap<Integer,HashMap<String, Pattern_ex>> newTopk = new HashMap<>();

        for(int i=0;i<numofs.size();++i)
        {
            int intrs = numofs.get(i);
            HashMap<String, Pattern_ex> map = new HashMap<>();
            for(String s : SumItrsOfP.get(intrs).keySet())
            {
                map.put(s,SumItrsOfP.get(intrs).get(s));
                book++;
                if(book>=k)
                {
                    break;
                }
            }
            newTopk.put(intrs,map);
            if(book>=k)
            {
                break;
            }
        }
        return newTopk;
    }

    public static int NumofItrsOfP(HashMap<Integer,HashMap<String, Pattern_ex>> newTopk)
    {
        int sum = 0;
        for (Integer i : newTopk.keySet())
        {
            sum = sum + newTopk.get(i).size();
        }
        return sum;
    }

    /**
     * 获取topk中最小的intersting值
     * @param newTopk
     * @return
     */
    public static int IntrsOfK(HashMap<Integer,HashMap<String, Pattern_ex>> newTopk)
    {
        //提取SumItrsOfP中的topk
        ArrayList<Integer> numofs = new ArrayList();
        for(Integer i :newTopk.keySet())
        {
            numofs.add(i);
        }
        Collections.sort(numofs, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1-o2;
            }
        });

        return numofs.get(0);
    }

}
