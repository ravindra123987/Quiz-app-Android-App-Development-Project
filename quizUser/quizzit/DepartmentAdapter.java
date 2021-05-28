package com.project.quizzit;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.Viewholder> {

    private List<DepartmentModel> DepartmentModelList;

    public DepartmentAdapter(List<DepartmentModel> departmentModelList) {
        DepartmentModelList = departmentModelList;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View departmentLayout= LayoutInflater.from(parent.getContext()).inflate(R.layout.department_item,parent,false);
        return new Viewholder(departmentLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        holder.setData(DepartmentModelList.get(position).getUrl(),DepartmentModelList.get(position).getName(),position);
    }

    @Override
    public int getItemCount() {

        return DepartmentModelList.size();
    }

    class Viewholder extends RecyclerView.ViewHolder{
        private CircleImageView departmentImage;
        private TextView departmentName;

        public Viewholder(@NonNull View itemView) {
            super(itemView);

            departmentImage=(CircleImageView)itemView.findViewById(R.id.department_image);
            departmentName=(TextView)itemView.findViewById(R.id.department_name);

        }

        private void setData(String imageUrl,String name,int position){
            Glide.with(itemView.getContext()).load(imageUrl).into(departmentImage);
            departmentName.setText(name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(itemView.getContext(), String.valueOf(sets), Toast.LENGTH_SHORT).show();
                    Intent setIntent = new Intent(itemView.getContext(),SetsActivity.class);
                    setIntent.putExtra("TITLE",name);
                    setIntent.putExtra("KEY",DepartmentModelList.get(position).getKey());
                    setIntent.putExtra("POSITION",position);
                    itemView.getContext().startActivity(setIntent);
                }
            });
        }
    }
}
