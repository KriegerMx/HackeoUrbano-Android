package mx.krieger.hackeourbano.object;

import java.io.Serializable;

public class UIPoint implements Serializable {
    public double latitude;
    public double longitude;

    public UIPoint() {}

    public UIPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
