package Base_Class;

import org.jgrapht.graph.DefaultEdge;

import java.io.Serializable;

public class Edge extends DefaultEdge implements Serializable{

    private static final long serialVersionUID = 2L;

    private final Node fnode;
    private final Node tnode;

    public Edge(Node fn, Node tn) {
        this.fnode = fn;
        this.tnode = tn;
    }

    public String getSourceLabel(){
        return fnode.getLabel();
    }

    public String getTargetLabel(){
        return tnode.getLabel();
    }

    @Override
    public Node getSource(){
        return this.fnode;
    }

    @Override
    public Node getTarget(){
        return this.tnode;
    }

    @Override
    public int hashCode(){
        int result;
        result = (this.getSource().toString() == null?0:this.getSource().toString().hashCode());
        result = 37*result + (this.getTarget().toString() == null?0:this.getTarget().toString().hashCode());
        return result;
    }

}

