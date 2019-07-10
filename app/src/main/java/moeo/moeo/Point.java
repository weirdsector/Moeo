package moeo.moeo;

import java.io.Serializable;

public class Point implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7177961138256300130L;
    int x, y;
    boolean isStart = false;
    int colorState;

    public Point(int x, int y, boolean isStart) {
        this.x = x;
        this.y = y;
        this.isStart = isStart;
    }

    public Point(int x, int y, boolean isStart, int colorState) {
        this.x = x;
        this.y = y;
        this.isStart = isStart;
        this.colorState = colorState;
    }
}
