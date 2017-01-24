package jp.techacademy.critical_bug.taskapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

class TaskAdapter extends BaseAdapter {
    private final LayoutInflater mLayoutInflater;
    private ArrayList<Task> mTaskArrayList;

    public TaskAdapter(final Context context) {
        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setTaskArrayList(final ArrayList<Task> taskArrayList) {
        this.mTaskArrayList = taskArrayList;
    }

    @Override
    public int getCount() {
        return mTaskArrayList.size();
    }

    @Override
    public Object getItem(final int position) {
        return mTaskArrayList.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_2, null);
        }

        TextView textView1 = (TextView) convertView.findViewById(android.R.id.text1);
        TextView textView2 = (TextView) convertView.findViewById(android.R.id.text2);
        Task task = mTaskArrayList.get(position);

        textView1.setText(task.getTitle());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPANESE);
        textView2.setText(simpleDateFormat.format(task.getDate()));

        return convertView;
    }
}