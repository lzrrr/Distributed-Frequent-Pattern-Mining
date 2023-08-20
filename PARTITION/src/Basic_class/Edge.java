package Basic_class;

import java.math.BigDecimal;

public class Edge implements Comparable<Edge>{

    private String sid;
    private String tid;
    private String elabel;
    private long wight; // 边权重，单边为MNI频次/n(防止频次过大)，融合边为sum; 使用bigdecimal确保精度
    private long rido; //边的融合系数，该系数同时考虑边权值和点权重，可以维持节点的平衡性

    public Edge() {
    }

    public Edge(String sid, String tid) {
        this.sid = sid;
        this.tid = tid;
        this.elabel = new String();
        this.wight = 0;
    }


    public Edge(String sid, String tid, String elabel,long wight) {
        this.sid = sid;
        this.tid = tid;
        this.elabel = elabel;
        this.wight = wight;
    }

    public long getRido() {
        return rido;
    }

    public void setRido(long rido) {
        this.rido = rido;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public long getWight() {
        return wight;
    }

    public void setWight(long wight) {
        this.wight = wight;
    }

    public String getElabel() {
        return elabel;
    }

    public void setElabel(String elabel) {
        this.elabel = elabel;
    }

    @Override
    public int compareTo(Edge e) {
        return Long.compare(e.getRido(),this.getRido());
    }
}
