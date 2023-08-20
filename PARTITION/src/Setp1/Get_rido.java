package Setp1;

import Basic_class.Edge;
import Basic_class.Node;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;

public class Get_rido {
    public static void getrido(ArrayList<Edge> edgeset, HashMap<String, Node> nodeset)
    {
        for(Edge edge : edgeset)
        {
            String sid = edge.getSid();
            String tid = edge.getTid();
            Node snode = nodeset.get(sid);
            Node tnode = nodeset.get(tid);
            int swight = snode.getWeight();
            int twight = tnode.getWeight();
            int down = swight*twight;

            long a = down;

            long ewight = edge.getWight();

            ewight = ewight*ewight;

            ewight = ewight/a;

            edge.setRido(ewight);
        }
    }
}
