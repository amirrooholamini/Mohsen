package project.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class CustomMusicBottomBar extends LinearLayout {

    private int progressWidth = 3;
    private float percent = 0;
    private int backgroundColor = Color.parseColor("#7f110011");
    private int strockColor = Color.parseColor("#ff226d");

    public CustomMusicBottomBar(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public CustomMusicBottomBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public CustomMusicBottomBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    public CustomMusicBottomBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWillNotDraw(false);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float centerX = width/2;
        float centerY = height/2;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(strockColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(dpToPx(progressWidth));

        RectF rectF = new RectF(
                0,
                0,
                width,
                2.5f*height
        );

        canvas.drawArc(rectF,180,180,true,paint);

        paint.setColor(backgroundColor);
        rectF = new RectF(
                0,
                5,
                width,
                2.5f*height + 5
        );

        canvas.drawArc(rectF,180,180,true,paint);
     //   canvas.drawArc(rectF,180,180,true,paint);
//        rectF = new RectF(
//                -width/8,
//                height/3-margin,
//                width+width/8,
//                5*height/2-margin
//        );

    }

    public void setBackgroundColor(int color){
        backgroundColor = color;
        postInvalidate();
    }

    private float dpToPx(float dp){
        float px = dp * getResources().getDisplayMetrics().density;
        return px;
    }
}
