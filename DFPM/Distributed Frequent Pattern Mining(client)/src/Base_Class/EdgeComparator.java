package Base_Class;

import java.util.Comparator;

public class EdgeComparator implements Comparator<Edge> {

    @Override
    public int compare(Edge o1, Edge o2) {
        String source1 = o1.getSource().getLabel();
        String source2 = o2.getSource().getLabel();
        String target1 = o1.getTarget().getLabel();
        String target2 = o2.getTarget().getLabel();
        if(source1.equals(source2)&&target1.equals(target2)||source1.equals(target2)&&target1.equals(source2)){
            return 0;
        }
        else
        {
            return 1;
        }
    }
}
