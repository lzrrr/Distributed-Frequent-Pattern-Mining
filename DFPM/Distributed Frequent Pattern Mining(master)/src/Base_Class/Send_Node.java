package Base_Class;

import java.io.Serializable;

/**
 * 发送结点，存储在对应的请求结点的信息中
 */
public class Send_Node implements Serializable {
    private static final long serialVersionUID = 6L;
    private String id; //结点id
    private String p; //结点所在站点号

    public Send_Node(String id, String p) {
        this.id = id;
        this.p = p;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getP() {
        return p;
    }

    public void setP(String p) {
        this.p = p;
    }
}
