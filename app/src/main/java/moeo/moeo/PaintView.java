package moeo.moeo;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PaintView extends View {
    ArrayList<Point> list;
    static final int PINK_STATE=0;
    static final int RED_STATE = 1;
    static final int YELLOW_STATE = 2;
    static final int PUPPLE_STATE = 3;
    static final int BLUE_STATE = 4;
    static final int GREEN_STATE= 5;
    static final int ORANGE_STATE= 6;
    static final int ERAZER_STATE = 7;
    int colorState = RED_STATE;
    Paint[] paintList = new Paint[8];

    public PaintView(Context context) {
        super(context);
        init();
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        list = new ArrayList<Point>();
        Paint pinkPaint = new Paint();
        pinkPaint.setColor(Color.rgb(243,97,166));
        pinkPaint.setStrokeWidth(5);
        pinkPaint.setAntiAlias(true);

        Paint redPaint = new Paint();
        redPaint.setColor(Color.RED);
        redPaint.setStrokeWidth(5);
        redPaint.setAntiAlias(true);

        Paint bluePaint = new Paint();
        bluePaint.setColor(Color.BLUE);
        bluePaint.setStrokeWidth(5);
        bluePaint.setAntiAlias(true);

        Paint yellowPaint = new Paint();
        yellowPaint.setColor(Color.YELLOW);
        yellowPaint.setStrokeWidth(5);
        yellowPaint.setAntiAlias(true);

        Paint pupplePaint = new Paint();
        pupplePaint.setColor(Color.rgb(165,102,255));
        pupplePaint.setStrokeWidth(5);
        pupplePaint.setAntiAlias(true);

        Paint greenPaint = new Paint();
        greenPaint.setColor(Color.rgb(107,153,0));
        greenPaint.setStrokeWidth(5);
        greenPaint.setAntiAlias(true);

        Paint orangePaint = new Paint();
        orangePaint.setColor(Color.rgb(242,150,97));
        orangePaint.setStrokeWidth(5);
        orangePaint.setAntiAlias(true);
        Paint erazerPaint = new Paint();
        erazerPaint.setColor(Color.rgb(255,255,255));
        erazerPaint.setStrokeWidth(10);
        erazerPaint.setAntiAlias(true);
        paintList[0] = pinkPaint;
        paintList[1] = redPaint;
        paintList[2] = yellowPaint;
        paintList[3] = pupplePaint;
        paintList[4] = bluePaint;
        paintList[5] = greenPaint;
        paintList[6] = orangePaint;
        paintList[7] = erazerPaint;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        for (int i = 0; i < list.size(); i++) {
            Point p = list.get(i);
            if (!(p.isStart)) {
                canvas.drawLine(list.get(i - 1).x,
                        list.get(i - 1).y,
                        list.get(i).x,
                        list.get(i).y,
                        paintList[list.get(i).colorState]);

            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int eventX = (int) event.getX();
        int eventY = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Point p = new Point(eventX, eventY, true, colorState);
                list.add(p);
                break;
            case MotionEvent.ACTION_MOVE:
                Point p2 = new Point(eventX, eventY, false, colorState);
                list.add(p2);
                invalidate();
                break;
        }

        return true;
    }
}
