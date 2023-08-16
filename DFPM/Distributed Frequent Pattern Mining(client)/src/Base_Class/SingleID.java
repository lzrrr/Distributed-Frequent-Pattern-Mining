package Base_Class;

public class SingleID {
    //创建 SingleObject 的一个对象
    private static final SingleID instance = new SingleID();
    private static int ID = 0;
    private static int layer = 0;

    //让构造函数为 private，这样该类就不会被实例化
    private SingleID(){}

    //获取唯一可用的对象
    public static SingleID getInstance(){
        return instance;
    }

    public String getId(){
        ID++;
        return "-"+ID;
    }

    public void ZERO(){
        ID = 0;
    }

    public int Count(){
        return ID;
    }

    public String getLayer(){
        layer++;
        return "L"+layer;
    }
}
