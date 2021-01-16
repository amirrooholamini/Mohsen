package project.custom;

import android.annotation.SuppressLint;
import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class LinearLoader extends LinearLayout {

    private boolean enable = false;
    private float progressWidth;
    private float startPoint = 0;
    @SuppressLint("ResourceType")
    private int color = Color.parseColor(App.getContext().getString(R.color.foregroundColor));
    private int backgroundColor = Color.parseColor("#00000000");

    public LinearLoader(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public LinearLoader(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public LinearLoader(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    public LinearLoader(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);

        float width = getWidth();
        float height = getHeight();

        progressWidth = width/3;
        if(startPoint>width){
            startPoint = -progressWidth;
        }

        RectF rectF = new RectF(0,0,width,height);
        paint.setColor(backgroundColor);
        canvas.drawRect(rectF,paint);




        if(enable) {
            paint.setColor(color);
            rectF = new RectF(startPoint, 0, startPoint + progressWidth, height);
            canvas.drawRect(rectF, paint);
        }


    }


    public void setProgressColor(int color){
        this.color = color;
        postInvalidate();
    }

    public void setBackgroundColor(int backgroundColor){
        this.backgroundColor = backgroundColor;
        postInvalidate();
    }

    public void start(){
        if(enable){
            return;
        }
        enable = true;
        startPoint -= progressWidth;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (enable){
                    try {
                        Thread.sleep(10);
                        startPoint +=5;
                        postInvalidate();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                startPoint = -2*progressWidth;
                postInvalidate();
                startPoint = 0;
            }
        }).start();

    }

    public void stop(){
        enable = false;
    }
}
