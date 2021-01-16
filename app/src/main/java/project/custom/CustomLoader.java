package project.custom;

import android.com.i3center.rooholamini.mohsen.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

public class CustomLoader extends LinearLayout {

    private Paint arcPaint;
    private int startAngle;
    private int sweepAngle;
    private int sleepTime;
    private boolean fillTime;
    private int color;
    private boolean randomColor;
    private int red;
    private int green;
    private int blue;
    private float loaderWidth = 5;

    private boolean rotationEnable = true;

    public CustomLoader(Context context) {
        super(context);
        setWillNotDraw(false);
        initialize();
    }

    public CustomLoader(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        initialize();
    }

    public CustomLoader(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        initialize();
    }

    public CustomLoader(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWillNotDraw(false);
        initialize();

    }

    public CustomLoader color(@ColorInt int color) {
        this.color = color;
        return this;
    }

    public CustomLoader color(@ColorRes String color) {
        this.color = Color.parseColor(color);
        return this;
    }

    public CustomLoader color(int alpha, int red, int green, int blue) {
        this.color = Color.argb(alpha, red, green, blue);
        this.red = red;
        this.green = green;
        this.blue = blue;
        return this;
    }

    public CustomLoader color(int red, int green, int blue) {
        this.color = Color.rgb(red, green, blue);
        this.red = red;
        this.green = green;
        this.blue = blue;
        return this;
    }

    public CustomLoader randomColor() {
        randomColor = true;
        return this;
    }

    public CustomLoader width(float dp){
        arcPaint.setStrokeWidth(dpToPx(dp));
        return this;
    }

    private float dpToPx(float dp){
        float px = dp * getResources().getDisplayMetrics().density;
        return px;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        RectF arcRect = new RectF(
                0 - arcPaint.ascent(),
                0 - arcPaint.ascent(),
                width + arcPaint.ascent(),
                height + arcPaint.ascent()
        );
        canvas.drawArc(arcRect, startAngle, sweepAngle, false, arcPaint);


    }

    private void initialize() {
        red = 255;
        green = 255;
        blue = 255;
        color = getResources().getColor(R.color.golden);
        arcPaint = new Paint();
        arcPaint.setColor(color);
        arcPaint.setAntiAlias(true);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(loaderWidth);
        fillTime = true;
        randomColor = false;
        sleepTime = 10;
        startAngle = 0;
        sweepAngle = 60;
        startThread();
    }

    private void startThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (rotationEnable) {
                        arcPaint.setColor(color);
                        Thread.sleep(sleepTime);
                        if (fillTime) {
                            startAngle += 3;
                            startAngle = startAngle % 360;
                            sweepAngle += 4;

                        } else {  // Empty time
                            startAngle += 8;
                            startAngle = startAngle % 360;
                            sweepAngle -= 6;
                        }
                        postInvalidate();
                        if (sweepAngle > 330) {
                            sleep();
                            fillTime = false;

                        } else if (sweepAngle < 30) {
                            sleep();
                            fillTime = true;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stopRotation(){
        rotationEnable = false;
    }

    public void startRotation(){
        rotationEnable = true;
        startThread();
    }


    private void sleep() {
        int angle = 15;
        int start = 0;
        if (randomColor) {
           int red = (int) (Math.random() * 200 + 55);
           int green = (int) (Math.random() * 200 + 55);
           int blue = (int) (Math.random() * 200 + 55);
           color = Color.rgb(red,green,blue);
        }
        while (start < angle) {
            try {
                start++;
                startAngle += 3;
                startAngle = startAngle % 360;
                postInvalidate();
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
