package gitlet;
import java.io.Serializable;

public class Branch implements Serializable{
    private String HEAD;
    private String master;

    public Branch(String curCoomitID) {
        this.HEAD = curCoomitID;
        this.master = curCoomitID;
    }

    public String getHEAD() {
        return this.HEAD;
    }

    public String getMaster() {
        return this.master;
    }
}
