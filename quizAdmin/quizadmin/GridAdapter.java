package com.example.quizadmin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class GridAdapter extends BaseAdapter {
    public List<String> setsNum;
    private String Department;
    private GridListener listener;
    public List<String> setCodes;
    private String DepartmentKey;

    public GridAdapter(List<String> sets,List<String> setCodes,String name,String DepartmentKey,GridListener listener){
        this.Department=name;
        this.setsNum=sets;
        this.listener=listener;
        this.setCodes=setCodes;
        this.DepartmentKey=DepartmentKey;
    }



    @Override
    public int getCount() {
        return setsNum.size()+1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View gridItemDesign;
        if(convertView==null){
            gridItemDesign= LayoutInflater.from(parent.getContext()).inflate(R.layout.set_item,parent,false);
        }
        else{
            gridItemDesign=convertView;
        }

        if(position==0){
            ((TextView)gridItemDesign.findViewById(R.id.setNumber)).setText("+");
        }else{
            ((TextView)gridItemDesign.findViewById(R.id.setNumber)).setText(String.valueOf(position));
        }


        gridItemDesign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position==0){
                    listener.addSet();
                    notifyDataSetChanged();
                }else {
                    notifyDataSetChanged();
                Intent moveToQuestionsIntent=new Intent(parent.getContext(),QuestionsActivity.class);
                moveToQuestionsIntent.putExtra("DEPARTMENT",Department);
                moveToQuestionsIntent.putExtra("SETID",setsNum.get(position-1));
                moveToQuestionsIntent.putExtra("SETCODE",setCodes.get(position-1));
                moveToQuestionsIntent.putExtra("POSITION",position);
                moveToQuestionsIntent.putExtra("KEY",DepartmentKey);
                parent.getContext().startActivity(moveToQuestionsIntent);
                }
            }
        });

        gridItemDesign.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(position!=0) {
                    listener.onLongClick(setsNum.get(position-1),position);

                }
                return false;
            }
        });


        return gridItemDesign;
    }

    public interface GridListener{
        public void addSet();

        void onLongClick(String setId,int position);
    }
}
