package com.joselestnh.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DayGridAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<LocalDate> daysList;

    public DayGridAdapter(Context context, List<LocalDate> daysList) {
        this.context = context;
        this.daysList = daysList;
    }

    @Override
    public int getCount() {
        return daysList.size();
    }

    @Override
    public Object getItem(int position) {
        return daysList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View gridView = convertView;

        if(gridView == null){
            this.inflater = (LayoutInflater) this.context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            gridView = this.inflater.inflate(R.layout.day_layout, null);
        }

        TextView text = gridView.findViewById(R.id.numberDay);
        LinearLayout linearLayout = gridView.findViewById(R.id.task_list);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,((GridView)parent).getColumnWidth()/4);
        lp.weight = 1;
        linearLayout.removeAllViews();

        final LocalDate date = daysList.get(position);
        int did;
        text.setText(Integer.toString(date.getDayOfMonth()));

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Integer> did_result = executorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return MainActivity.getDb().dayDao().getDid(date.getYear(), date.getMonthValue(),
                        date.getDayOfMonth());
            }
        });

        try {
            did = did_result.get();
        }catch (Exception e ){
            did = -1;
        }

        if(did >= 0){
            final int foundDid = did;
            int nTask;
            Future<Integer> nTask_result = executorService.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return MainActivity.getDb().dayDao().getNumberOfTaskOf(foundDid);
                }
            });

            try{
                nTask = nTask_result.get();
            }catch (Exception e){
                nTask = 0;
            }

            if (nTask > 0){
                List<Integer> colorList;
                Future<List<Integer>> color_result = executorService.submit(new Callable<List<Integer>>() {
                    @Override
                    public List<Integer> call() throws Exception {
                        return MainActivity.getDb().taskDao().getColorListOf(foundDid);
                    }
                });

                try{
                    colorList = color_result.get();
                    ImageView imageView;

                    //revisar tamaño de ntask a la hora de cuantos pintar y que coincida con el tamaño de la lista de colores
                    for (int i = 0; i < nTask && i < 3; i++) {
                        imageView = new ImageView(context);
                        imageView.setImageResource(R.drawable.ball);
                        imageView.setColorFilter(colorList.get(i));
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        imageView.setLayoutParams(lp);
                        linearLayout.addView(imageView,i);

                    }
                    if (nTask>3){
                        imageView = new ImageView(context);
                        imageView.setImageResource(R.drawable.plus);
                        imageView.setColorFilter(colorList.get(3));
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        imageView.setLayoutParams(lp);
                        linearLayout.addView(imageView,3);
                    }

                }catch (Exception e ){
                    colorList = new ArrayList<>();
                }
            }else{
                linearLayout.removeAllViews();

            }


        }

        executorService.shutdown();


        return gridView;
    }

    void updateData(List<LocalDate> daysList){
        this.daysList = daysList;
        notifyDataSetChanged();

    }

}
