package com.joselestnh.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.time.LocalTime;
import java.util.List;

public class ScheduleListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<ScheduleItem> scheduleItemList;

    public ScheduleListAdapter(Context context, List<ScheduleItem> scheduleItemList) {
        this.context = context;
        this.scheduleItemList = scheduleItemList;
    }

    @Override
    public int getCount() {
        return scheduleItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return scheduleItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listView = convertView;
        this.inflater = (LayoutInflater) this.context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        listView = this.inflater.inflate(scheduleItemList.get(position).getLayout(), null);


        switch (scheduleItemList.get(position).getMode()){
            case ScheduleItem.SEPARATOR:
                break;
            case ScheduleItem.TASK:
                ((TextView)listView.findViewById(R.id.taskName)).
                        setText(scheduleItemList.get(position).getTask().getTask_name());
                ((TextView)listView.findViewById(R.id.startTimeTag)).setText(LocalTime.
                        ofSecondOfDay(scheduleItemList.get(position).getStartTime()).toString());
                ((TextView)listView.findViewById(R.id.endTimeTag)).setText(LocalTime.
                        ofSecondOfDay(scheduleItemList.get(position).getEndTime()).toString());
                listView.setBackgroundColor(scheduleItemList.get(position).getTask().getColor());
                break;
        }

        return listView;
    }

    public void updateData(List<ScheduleItem> scheduleItemList){
        this.scheduleItemList = scheduleItemList;
        notifyDataSetChanged();
    }
}
