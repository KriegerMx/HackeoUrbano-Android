package mx.krieger.hackeourbano.object;

import java.io.Serializable;
import java.util.List;

public class UITrail implements Serializable {
    public long id;
    public String originName;
    public String destinationName;
    public String branchName;
    public List<UIPoint> points;
}
