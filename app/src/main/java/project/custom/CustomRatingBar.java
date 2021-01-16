package custom;

import java.util.ArrayList;

import android.com.i3center.rooholamini.mohsen.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class CustomRatingBar extends LinearLayout {

    private ArrayList<ImageView> images    = new ArrayList<ImageView>();
    private float                rate      = 0;
    private boolean              canChange = false;


    public CustomRatingBar(Context context) {
        super(context);
        initialize(context);
    }


    public CustomRatingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public void userCanChangeRate(boolean canChange){
        this.canChange = canChange;
    }

    private OnRateChangeListener listener;

    public interface OnRateChangeListener{
        void onRateChanged(float rate , boolean byUser);
    }

    public void setOnRateChangeListener(OnRateChangeListener listener){
        this.listener = listener;
    }


    public void setRating(float value) {
        for (int i = 0; i < 5; i++) {
            images.get(i).setImageResource(R.drawable.star_empty);
        }
        rate = value;
        if(rate<0){
            rate = 0;
        }else if(rate>5){
            rate = 5;
        }
        int intRate = (int) (value);
        for (int i = 0; i < intRate; i++) {
            images.get(i).setImageResource(R.drawable.star_fill);
        }
        float distance = value - intRate;

        if (distance > 0.25 && distance < 0.75) {
            images.get(intRate).setImageResource(R.drawable.star_half);
        } else if (distance > 0.75) {
            images.get(intRate).setImageResource(R.drawable.star_fill);
        }

        // int integerRate = (int) rate;
        // for (int i = integerRate; i < images.size(); i++) {
        //    images.get(i).setImageResource(R.drawable.star_empty);
        //}

        invalidate();

    }


    public float getRate() {
        return rate;

    }


    private void initialize(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.custom_rating_bar, this, true);
        ImageView img1 = (ImageView) view.findViewById(R.id.img1);
        ImageView img2 = (ImageView) view.findViewById(R.id.img2);
        ImageView img3 = (ImageView) view.findViewById(R.id.img3);
        ImageView img4 = (ImageView) view.findViewById(R.id.img4);
        ImageView img5 = (ImageView) view.findViewById(R.id.img5);
        images.add(img1);
        images.add(img2);
        images.add(img3);
        images.add(img4);
        images.add(img5);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!canChange){
            return false;
        }
        float x = event.getX();
        float width = getWidth();
        float rate = 0;
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(x<=width/5){
                    rate = 1;
                    setRating(rate);
                }else if(x>width/5 && x<=2*width/5){
                    rate = 2;
                    setRating(rate);
                }else if(x>2*width/5 && x<=3*width/5){
                    rate = 3;
                    setRating(rate);
                }else if(x>3*width/5 && x<=4*width/5){
                    rate = 4;
                    setRating(rate);
                }else {
                    rate = 5;
                    setRating(rate);
                }
                if(listener!=null){
                    listener.onRateChanged(rate,true);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if(x<=width/5){
                    rate = 1;
                    setRating(rate);
                }else if(x>width/5 && x<=2*width/5){
                    rate = 2;
                    setRating(rate);
                }else if(x>2*width/5 && x<=3*width/5){
                    rate = 3;
                    setRating(rate);
                }else if(x>3*width/5 && x<=4*width/5){
                    rate = 4;
                    setRating(rate);
                }else {
                    rate = 5;
                    setRating(rate);
                }
                if(listener!=null){
                    listener.onRateChanged(rate,true);
                }
                break;

            case MotionEvent.ACTION_UP:
                // touch up code
                break;
        }
        return true;
    }
}
