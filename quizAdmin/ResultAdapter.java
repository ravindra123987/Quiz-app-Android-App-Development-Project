package com.example.quizadmin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private List<ResultModel> list;

    public ResultAdapter(List<ResultModel> lst){
        this.list=lst;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.result_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(list.get(position).getEmail(),list.get(position).getMarks(),position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView question,answer;
        private Button deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            question=itemView.findViewById(R.id.bk_question);
            answer=itemView.findViewById(R.id.bk_answer);
            deleteBtn=itemView.findViewById(R.id.delete_btn);
        }

        private void setData(String email,String marks,final int position){
            this.question.setText(email);
            String s=marks;
            this.answer.setText(s);

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    list.remove(position);
                    notifyItemRemoved(position);
                    FirebaseDatabase.getInstance().getReference().child("STUDENTS").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String key1 = "";
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                if (snapshot1.child("email").getValue().toString().equals(email)) {
                                    key1 = snapshot1.getKey();
                                    break;
                                }
                            }

                            FirebaseDatabase.getInstance().getReference().child("STUDENTS").child(key1).child("sets").child(QuestionsActivity.setId).removeValue();
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    notifyDataSetChanged();
                }
            });
        }
    }

}