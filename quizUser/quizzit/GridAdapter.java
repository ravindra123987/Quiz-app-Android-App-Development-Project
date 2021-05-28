package com.project.quizzit;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GridAdapter extends BaseAdapter {
    private List<String> sets,setcodes;
    private String Department;


    public GridAdapter(List<String> sets,List<String> setcodes,String name){
        this.Department=name;
        this.sets=sets;
        this.setcodes=setcodes;
    }

    @Override
    public int getCount() {
        return sets.size();
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

        gridItemDesign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkAndMoveToQuestionsIntent(parent,position);
                
            }
        });

        ((TextView)gridItemDesign.findViewById(R.id.setNumber)).setText(String.valueOf(position+1));
        return gridItemDesign;
    }

    public void checkDb(ViewGroup parent,int position){
        Dialog loadingDialog;
        loadingDialog=new Dialog(parent.getContext());
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(parent.getContext().getDrawable(R.drawable.department_background));

        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference myRef=db.getReference();
        loadingDialog.show();

        myRef.child("STUDENTS").child(LoginActivity.studentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                long count=snapshot.getChildrenCount();
                if(count!=3){
                    addData(loadingDialog,parent,position);
                }
                else {
                    for (DataSnapshot dataSnapshot : snapshot.child("sets").getChildren()) {

                        String id = dataSnapshot.getKey();
                        if (id.equals(sets.get(position))) {
                            Toast.makeText(parent.getContext(), "You can attempt test only once", Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                            return;
                        }

                    }
                    addData(loadingDialog,parent,position);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(parent.getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void addData(Dialog loadingDialog,ViewGroup parent,int position){

        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference myRef=db.getReference();

        Map<String, Object> map = new HashMap<>();
        myRef.child("STUDENTS").child(LoginActivity.studentId).child("sets").child(sets.get(position)).setValue(0).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    moveIntent(parent,position);
                } else {
                    Toast.makeText(parent.getContext(),"Something went wrong..",Toast.LENGTH_SHORT);
                }
                loadingDialog.dismiss();
            }
        });

    }

    public void moveIntent(ViewGroup parent,int position){
        Intent moveToQuestionsIntent=new Intent(parent.getContext(),QuestionsActivity.class);
        moveToQuestionsIntent.putExtra("DEPARTMENT",Department);
        moveToQuestionsIntent.putExtra("SETID",sets.get(position));
        parent.getContext().startActivity(moveToQuestionsIntent);
    }

    public void checkAndMoveToQuestionsIntent(ViewGroup parent,int position){
        
        Dialog checkDialog;

        checkDialog=new Dialog(parent.getContext());
        checkDialog.setContentView(R.layout.check_setcode);
        checkDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        checkDialog.setCancelable(true);
        checkDialog.getWindow().setBackgroundDrawable(parent.getContext().getDrawable(R.drawable.department_background));


        EditText setCode;
        Button verify;
        verify=checkDialog.findViewById(R.id.verify);
        setCode=checkDialog.findViewById(R.id.setCode);
        checkDialog.show();




        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(setCode.getText().toString().equals(setcodes.get(position))){
                    checkDialog.dismiss();
                    checkDb(parent,position);
                }
                else{
                    checkDialog.dismiss();

                    Toast.makeText(parent.getContext(), "Wrong set code", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        
    }

}
