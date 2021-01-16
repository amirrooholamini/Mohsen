package project.custom;

import android.com.i3center.rooholamini.mohsen.App;
import android.com.i3center.rooholamini.mohsen.R;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


public class CustomToast {

    private static Toast customToast = new Toast(App.getCurrentActivity());

    public static void showToast(String message){
        View toastView = App.getLayoutInflater().inflate(R.layout.custom_toast,(ViewGroup)App.getCurrentActivity().findViewById(R.id.toastRoot));
        TextView txtToast = (TextView) toastView.findViewById(R.id.txtToast);
        txtToast.setText(message);
        customToast.cancel();
        customToast = new Toast(App.getContext());
        customToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        customToast.setDuration(Toast.LENGTH_SHORT);
        customToast.setView(toastView);
        customToast.show();
    }
}
