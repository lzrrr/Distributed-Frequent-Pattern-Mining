package Base_Class;

import java.util.Comparator;

public class NodeComparator implements Comparator<Node>
{

    @Override
    public int compare(Node o1, Node o2) {
        String label1 = o1.getLabel();
        String label2 = o2.getLabel();
        if(label1.equals(label2)) return 0;
        else return 1;
    }
}
