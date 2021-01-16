package project.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class LinearProgressBar extends LinearLayout {
    private float progress = 0;
    private int color = Color.parseColor("#ff226d");
    private int backgroundColor = Color.parseColor("#00000000");

    public LinearProgressBar(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public LinearProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public LinearProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    public LinearProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

        float progressWidth = width * progress/100.0f;

        RectF rectF = new RectF(0,0,progressWidth,height);
        canvas.drawRect(rectF,paint);

        rectF = new RectF(progressWidth,0,width,height);
        paint.setColor(backgroundColor);
        canvas.drawRect(rectF,paint);
    }


    public void setProgress(float progress){
        this.progress = progress;
        postInvalidate();
    }

    public float getProgress() {
        return progress;
    }

    public void setProgressColor(int color){
        this.color = color;
        postInvalidate();
    }

    public void setBackgroundColor(int backgroundColor){
        this.backgroundColor = backgroundColor;
        postInvalidate();
    }
}
