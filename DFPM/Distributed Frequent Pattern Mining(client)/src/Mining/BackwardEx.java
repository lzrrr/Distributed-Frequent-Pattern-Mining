package Mining;

import Base_Class.Graph;
import Base_Class.Node;
import Base_Class.Pattern;

import java.util.*;

import static java.lang.Integer.parseInt;

public class BackwardEx {
    //输入
    HashMap<String, Pattern> candidate_patterns;//候选模式
    HashMap<String,Pattern> pattern_map;//输入时为父模式，输出时为更新的新模式
    Graph graph; //原图
    int support;
    HashMap<String,HashMap<String,HashMap<Integer,Set<String>>>> send_external;//本地生成模式的实例中含有虚点需要发送给对应站点模式，key:对应站点，value：模式的Exteranal_MNI

    public BackwardEx(HashMap<String, Pattern> candidate_patterns, HashMap<String, Pattern> pattern_map, Graph graph, HashMap<String, HashMap<String, HashMap<Integer, Set<String>>>> send_external,int support) {
        this.candidate_patterns = candidate_patterns;
        this.pattern_map = pattern_map;
        this.graph = graph;
        this.support = support;
        this.send_external = send_external;
        Backward_extend();
    }

    public void Backward_extend()
    {
        Set<String> cannotexpattern = new HashSet<>(); //存放不能扩展和没有实例的模式
        for (String pattern_id : candidate_patterns.keySet())
        {
            Pattern pattern = candidate_patterns.get(pattern_id);
            Pattern fatherpattern = pattern_map.get(pattern.getFatherId()).clone();
            //用于判断是否可以提前终止
            boolean flagtermi = false;

            ArrayList<Set<String>> candidateMNI_List = new ArrayList<>();
            if(!GetcandidateMNI(pattern,candidateMNI_List,fatherpattern)) //如果无法扩展出新列
            {
                cannotexpattern.add(pattern_id);
                continue;
            }
            //每列依次验证新模式的MNI
            ArrayList<Set<String>> newMNI_list = new ArrayList<>();//新模式的MNI_List
            int maxSize = pattern_map.get(pattern.getFatherId()).getMNI_List().size();
            ArrayList<String> result = new ArrayList<>(); //初始化存放结果的list
            boolean flag = false;
            //生成新模式的MNItable存放结果
            for (int i = 0; i < maxSize; i++) {
                newMNI_list.add(new HashSet<>());
                result.add("-1");
            }
            for (int i=0;i<candidateMNI_List.size();++i)
            {
                ArrayList<String> infolist = pattern.Getorder(i);//获取每列的验证顺序
                Set<Integer> checked_column = new HashSet<>();
                Set<String> removeset = new HashSet<>();
                //拷贝后向边
                ArrayList<String> infoback = (ArrayList<String>) pattern.getBackwardedges().clone();
                //获取后向点的扩展信息
                HashMap<Integer,ArrayList<String>> backmap = pattern.Getback(i,infolist,infoback);


                for (String id : candidateMNI_List.get(i))
                {
                    if(newMNI_list.get(i).contains(id))
                    {continue;}
                    result.set(i,id);//放入当前点
                    checked_column.add(i);
                    //拷贝后向边
                    flag = Backward_check(graph,candidateMNI_List,backmap,infolist,pattern,newMNI_list,checked_column,result,maxSize,0);
                    if (!flag)
                    {
                        removeset.add(id);
                        checked_column.clear();
                        //判断该列是否满足提前终止条件
                        if(candidateMNI_List.size()>2)
                        {
                        int elsenum = fatherpattern.getAllMNI().get(i)-fatherpattern.getLocalMNI().get(i);
                        int rest = candidateMNI_List.get(i).size() - removeset.size();
                        if(rest+elsenum<support) //如果满足终止条件
                        {
                            flagtermi = true;
                            cannotexpattern.add(pattern_id);
                            System.out.println(pattern_id+" 提前终止");
                            break;
                        }
                        }
                    }
                }
                if(flagtermi)
                {break;}
                candidateMNI_List.get(i).removeAll(removeset);
                checked_column.clear();
            }
            //将扩展MNI存入pattern
            pattern.setMNI_List(newMNI_list);
            for(Set<String> set : pattern.getMNI_List())
            {
                if (set.size()==0)
                {
                    cannotexpattern.add(pattern_id);
                    break;
                }
            }
            if(!cannotexpattern.contains(pattern_id))
            {
                Get_external(pattern);
            }
        }
        //去掉没有实例的模式
        for (String s : cannotexpattern)
        {
            candidate_patterns.remove(s);
        }
    }

    public boolean Backward_check(Graph graph, ArrayList<Set<String>> candidateMNI_List, HashMap<Integer,ArrayList<String>> backmap, ArrayList<String> infolist, Pattern pattern, ArrayList<Set<String>> newMNI_list, Set<Integer> checked_column, ArrayList<String> result, int max, int level)
    {
        boolean flag = false;
        //获取扩展信息
        String info = infolist.get(level);
        int leftinfo = parseInt(info.split(" ")[0]);
        String isout = info.split(" ")[1];
        int rightinfo = parseInt(info.split(" ")[2]);
        //可扩展点
        Set<String> nodeset = new HashSet<>();
        //扩展列
        int new_column = -1;
        if(checked_column.contains(leftinfo))
        {
            new_column = rightinfo;
            String id = result.get(leftinfo);
            String targetlabel = pattern.getColumn_label().get(rightinfo);
            Node node = graph.findNode(id);
            nodeset.addAll(node.getNeighbers().get(targetlabel));
        }
        else if(checked_column.contains(rightinfo))
        {
            new_column = leftinfo;
            String id = result.get(rightinfo);
            String targetlabel = pattern.getColumn_label().get(leftinfo);
            Node node = graph.findNode(id);
            nodeset.addAll(node.getNeighbers().get(targetlabel));
        }
        nodeset.removeAll(result); //去掉重复的点
        nodeset.retainAll(candidateMNI_List.get(new_column));

        //从已有点的one-hop邻居，根据后向边来求交集，去掉不能扩展的后向点
        //如果当前点和之前点存在后向边，先验证后向边
        if(backmap.containsKey(new_column))
        {
            for (String infoForBack : backmap.get(new_column)) {
                Set<String> backnodeset = new HashSet<>();//验证后向边存在的点集
                int leftInfoForBack = parseInt(infoForBack.split(" ")[0]);
                int rightInfoForBack = parseInt(infoForBack.split(" ")[2]);
                if(checked_column.contains(leftInfoForBack))
                {
                    String id = result.get(leftInfoForBack);
                    String targetlabel = pattern.getColumn_label().get(rightInfoForBack);
                    Node node = graph.findNode(id);
                    backnodeset.addAll(node.getNeighbers().get(targetlabel));
                }
                else if(checked_column.contains(rightInfoForBack))
                {
                    String id = result.get(rightInfoForBack);
                    String targetlabel = pattern.getColumn_label().get(leftInfoForBack);
                    Node node = graph.findNode(id);
                    backnodeset.addAll(node.getNeighbers().get(targetlabel));
                }
                nodeset.retainAll(backnodeset);
            }
        }

        if(nodeset.size()==0)
        {
            return flag;
        }


        for (String nid : nodeset)
        {
            result.set(new_column,nid);
            checked_column.add(new_column);
            if(level==max-2)
            {
                for (int j=0;j<max;++j)
                {
                    newMNI_list.get(j).add(result.get(j));
                }
                flag = true;
                result.set(new_column,"-1");
                checked_column.remove(new_column);
                break;
            }
            else{
                flag = Backward_check(graph,candidateMNI_List,backmap,infolist,pattern,newMNI_list,checked_column,result,max,level+1);
                //如果成功
                if(flag)
                {
                    result.set(new_column,"-1");
                    checked_column.remove(new_column);
                    break;
                }
                result.set(new_column,"-1");
                checked_column.remove(new_column);
            }
        }
        return flag;
    }


    /**
     * 生成候选模式的候选MNItable
     * @param pattern
     * @param candidateMNI_List
     * @return 如果新列可以扩展，返回true,如果不行，返回false
     */
    public boolean GetcandidateMNI(Pattern pattern, ArrayList<Set<String>> candidateMNI_List,Pattern fatherpattern)
    {
        Set<String> newcolumn = new HashSet<>();
        Set<String> excolumn = new HashSet<>();
        Set<String> father_excolumn = fatherpattern.getMNI_List().get(pattern.getEx_column()); //从父模式的获取扩展列

        //扩展信息
        String targetlabel = pattern.getTargetLabel();
        boolean isout = pattern.isOutGoing();

        //在提前终止中去除实点
        int newnum1 = 0;
        int newnum2 = 0;
        for (String id : father_excolumn)
        {
            Node node = graph.findNode(id);

                if(node.getNeighbers().containsKey(targetlabel))
                {
                    Set<String> nodesets = node.getNeighbers().get(targetlabel);
                    newcolumn.addAll(nodesets);
                    excolumn.add(id);
                }
                else
                {
                    if(!graph.getExternal_id().contains(id))
                    {
                        newnum1++;
                    }
                }
        }
        //在提前终止中去除实点
        if(fatherpattern.getMNI_List().size()>2)
        {
        int lastnum1 = fatherpattern.getLocalMNI().get(pattern.getEx_column());
        newnum1 = lastnum1 - newnum1;
        if(newnum1<0)
        {
            newnum1=0;
        }
        fatherpattern.getLocalMNI().set(pattern.getEx_column(),newnum1);
        //拷贝父模式新列
        Set<String> lastnew = new HashSet<>();
        lastnew.addAll(fatherpattern.getMNI_List().get(pattern.getNew_column()));
        lastnew.removeAll(newcolumn);
        for (String s : lastnew)
        {
            if(!graph.getExternal_id().contains(s))
            {
                newnum2++;
            }
        }
        //在提前终止中去除实点
        int lastnum2 = fatherpattern.getLocalMNI().get(pattern.getNew_column());
        newnum2 = lastnum2 - newnum2;
        if(newnum2<0)
        {
            newnum2=0;
        }
        fatherpattern.getLocalMNI().set(pattern.getNew_column(),newnum2);
        }


            //拷贝父模式MNItable
            for (int i=0;i<fatherpattern.getMNI_List().size();++i)
            {
                HashSet<String> set = new HashSet<>();
                set.addAll(fatherpattern.getMNI_List().get(i));
                candidateMNI_List.add(set);
            }
            //更新扩展列和新列
            candidateMNI_List.get(pattern.getEx_column()).retainAll(excolumn);
            candidateMNI_List.get(pattern.getNew_column()).retainAll(newcolumn);
            if(candidateMNI_List.get(pattern.getNew_column()).size()==0||candidateMNI_List.get(pattern.getEx_column()).size()==0)
            {
                return false;
            }
            return true;
    }

    //生成该模式需要发送给对应站点externalmnilist
    public void Get_external(Pattern pattern)
    {
        //遍历每一列
        for (int i=0;i<pattern.getMNI_List().size();++i)
        {
            Set<String> oldset = pattern.getMNI_List().get(i);
            Set<String> exset = new HashSet<>();
            exset.addAll(oldset); //拷贝总的集合
            exset.retainAll(graph.getExternal_id()); //选出虚拟点
            Set<String> inset = new HashSet<>();
            inset.addAll(oldset);
            inset.removeAll(exset); //选出实结点
            pattern.getInternal_MNI().put(i,inset); //存入实结点

            //存入发送map
            for(String exid : exset)
            {
                Node n = graph.findNode(exid);
                String position = n.getP();
                if(!send_external.containsKey(position))
                {
                    HashMap<String,HashMap<Integer,Set<String>>> newmap = new HashMap<>();
                    send_external.put(position,newmap);
                }
                if(!send_external.get(position).containsKey(pattern.getId()))
                {
                    HashMap<Integer,Set<String>> newmap = new HashMap<>();
                    send_external.get(position).put(pattern.getId(),newmap);
                }
                if(!send_external.get(position).get(pattern.getId()).containsKey(i))
                {
                    Set<String> newset = new HashSet<>();
                    send_external.get(position).get(pattern.getId()).put(i,newset);
                }
                send_external.get(position).get(pattern.getId()).get(i).add(exid);
            }

        }

    }



}
