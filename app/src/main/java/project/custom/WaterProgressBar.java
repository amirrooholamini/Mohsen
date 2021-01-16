package project.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class WaterProgressBar extends LinearLayout {

    private OnProgressChangeListener listener;
    private boolean userCanChange = false;
    private int color = Color.parseColor("#8faa1a30");
    private int alpha = 255;

    public interface OnProgressChangeListener {
        void onProgressChange(float progress, boolean userInput, boolean touchDown);
    }

    private float progress = 0;
    float width;
    float height;

    public WaterProgressBar(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public WaterProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public WaterProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    public WaterProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWillNotDraw(false);
    }

    public void setProgress(float progress) {
        this.progress = progress;
        postInvalidate();
    }

    public void setOnProgressChangeListener(OnProgressChangeListener listener) {
        this.listener = listener;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int strokeWidth = 3;

        Paint progressPaint = new Paint();
        progressPaint.setColor(color);
        progressPaint.setAntiAlias(true);
        progressPaint.setStyle(Paint.Style.FILL);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setAlpha(alpha);


        width = getWidth();
        height = getHeight();
        int margin = strokeWidth / 2;

        float top = (height - height * progress / 100.0f);


        RectF rectF = new RectF(
                0,
                0,
                width,
                height
        );

        float sweepAngle = 360.0f * progress / 100.0f;

        canvas.drawArc(rectF, (float) 90.0f - sweepAngle / 2.0f, sweepAngle, false, progressPaint);
        if (listener != null) {
            listener.onProgressChange(progress, false, false);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!userCanChange) {
            return false;
        }
        float y = event.getY();
        if (y < 0) {
            y = height;
        } else if (y > height) {
            y = height;
        }
        float percent = 0;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:
                percent = 1 - y / height;
                progress = 100 * percent;
                postInvalidate();
                listener.onProgressChange(progress, true, true);
                break;

            case MotionEvent.ACTION_UP:

                percent = 1 - y / height;
                progress = 100 * percent;
                postInvalidate();
                listener.onProgressChange(progress, true, false);
                break;
        }
        return true;
    }

    public void setUserCanChange(boolean userCanChange) {
        this.userCanChange = userCanChange;
    }

    public void setProgressColor(int color){
        this.color = color;
    }

    public void setProgressAlpha(int alpha){
        this.alpha = alpha;
    }


}
