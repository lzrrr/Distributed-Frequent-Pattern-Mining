package Mining;

import Base_Class.Pattern;

import java.util.ArrayList;
import java.util.HashMap;

public class Trans_sendPattern {

    /**
     * 将服务器发来的前向模式信息转换成模式，并生成候选模式
     * @param send_pattern
     * @param pattern_map
     * @param candidate_patterns
     */
    public static void trans_forward(HashMap<String, ArrayList<String>> send_pattern,HashMap<String, Pattern> pattern_map,HashMap<String, Pattern> candidate_patterns)
    {
        for (String id : send_pattern.keySet())
        {
            ArrayList<String> info = send_pattern.get(id);
            String father_id = info.get(0);
            if(pattern_map.containsKey(father_id))
            {
                //获取新边信息
                String ex_column = info.get(1);
                String new_column = info.get(2);
                String targetlabel = info.get(3);
                String isoutgoing = info.get(4);
                String newedge = new String();
                boolean isout = false;
                if(isoutgoing.equals("1")) //出度边
                {
                     newedge = ex_column+" out " +new_column;
                     isout = true;
                }
                else
                {
                    newedge = ex_column+" in " +new_column;
                }
                int ex_column_num = Integer.parseInt(ex_column);
                int new_column_num = Integer.parseInt(new_column);

                Pattern newP = pattern_map.get(father_id).clone();
                //重置存储信息
                newP.setInternal_MNI(new HashMap<>());
                newP.setExteranal_MNI(new HashMap<>());
                newP.setAllMNI(new ArrayList<>());
                newP.setLocalMNI(new ArrayList<>());
                newP.setMNI_List(new ArrayList<>());

                newP.setId(id);
                newP.setFatherId(father_id);
                newP.setEx_column(ex_column_num);
                newP.setNew_column(new_column_num);
                newP.getForwardedges().add(newedge);
                newP.setTargetLabel(targetlabel);
                newP.setOutGoing(isout);
                newP.getColumn_label().add(targetlabel);
                candidate_patterns.put(id,newP);

            }
        }
    }

    /**
     * 将服务器发来的前向模式信息转换成模式，并生成候选模式
     * @param send_pattern
     * @param pattern_map
     * @param candidate_patterns
     */
    public static void trans_backward(HashMap<String, ArrayList<String>> send_pattern,HashMap<String, Pattern> pattern_map,HashMap<String, Pattern> candidate_patterns)
    {
        for (String id : send_pattern.keySet())
        {
            ArrayList<String> info = send_pattern.get(id);
            String father_id = info.get(0);
            if(pattern_map.containsKey(father_id))
            {
                //获取新边信息
                String ex_column = info.get(1);
                String new_column = info.get(2);
                String targetlabel = info.get(3);
                String newedge = ex_column+" out "+new_column;
                int ex_column_num = Integer.parseInt(ex_column);
                int new_column_num = Integer.parseInt(new_column);
                Pattern newP = pattern_map.get(father_id).clone();
                //重置存储信息
                newP.setInternal_MNI(new HashMap<>());
                newP.setExteranal_MNI(new HashMap<>());
                newP.setAllMNI(new ArrayList<>());
                newP.setLocalMNI(new ArrayList<>());
                newP.setMNI_List(new ArrayList<>());

                newP.setId(id);
                newP.setFatherId(father_id);
                newP.setEx_column(ex_column_num);
                newP.setNew_column(new_column_num);
                newP.getBackwardedges().add(newedge);
                newP.setTargetLabel(targetlabel);
                newP.setOutGoing(true);
                candidate_patterns.put(id,newP);
            }
        }
    }

}
