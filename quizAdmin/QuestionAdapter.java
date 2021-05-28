package com.example.quizadmin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.viewHolder> {
    private List<QuestionModel> list;
    private String DepartmentName;
    private DeleteListener listener;

    public QuestionAdapter(List<QuestionModel> list,String departmentName,DeleteListener listener1) {
        this.list = list;
        DepartmentName=departmentName;
        this.listener=listener1;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.question_item,parent,false);
        return new viewHolder(view);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        String question=list.get(position).getQuestion();
        String answer=list.get(position).getCorrectOption();

        holder.setData(question,answer,position);
    }

    class viewHolder extends RecyclerView.ViewHolder{

        private TextView question,answer;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            question=itemView.findViewById(R.id.question1);
            answer=itemView.findViewById(R.id.answer);
        }

        private void setData(String que,String ans,int position){
            String q=String.valueOf(position+1)+"."+que;
            String a="Ans. "+ans;
            this.question.setText(q);
            this.answer.setText(a);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent editQuestionIntent=new Intent(itemView.getContext(),AddQuestionActivity.class);
                    editQuestionIntent.putExtra("DEPARTMENT",DepartmentName);
                    editQuestionIntent.putExtra("SETID",list.get(position).getSetId());
                    editQuestionIntent.putExtra("POSITION",position);
                    itemView.getContext().startActivity(editQuestionIntent);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onLongClick(position,list.get(position).getId());
                    notifyDataSetChanged();
                    return false;
                }
            });
        }

    }

    public interface DeleteListener{
        void onLongClick(int position,String id);
    }
}
