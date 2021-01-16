package project.adapters;

import android.com.i3center.rooholamini.mohsen.R;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public abstract class ListViewAdapter<T> extends ArrayAdapter<T> {
    private LayoutInflater inflater;

    public ListViewAdapter(Context context, ArrayList<T> list) {
        super(context, 0, list);
        inflater = LayoutInflater.from(context);
    }

    public static class ViewHolder {}

    public abstract ViewHolder assign(View convertView);
    public abstract void fill(ViewHolder upcastedViewHolder,T item);

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        T item = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.struct_music_exist, parent, false);
            viewHolder = assign(convertView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        fill(viewHolder,item);

        return convertView;
    }
}
