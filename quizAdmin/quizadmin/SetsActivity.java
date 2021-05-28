package com.example.quizadmin;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class SetsActivity extends AppCompatActivity {
    private Toolbar toolBar;
    private GridView gridView;
    private Dialog loadingDialog;
    private GridAdapter gridAdapter;
    private TextView loadingText;
    private String DepartmentName;
    private DatabaseReference myRef;
    private List<String> sets;
    public static List<String> setCodes;
    private String setCode;
    private static final String CHAR_LIST =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int RANDOM_STRING_LENGTH = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);
        toolBar= findViewById(R.id.toolBar);
        setSupportActionBar(toolBar);

        setCodes=new ArrayList<>();

        loadingDialog=new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.department_background));
        loadingText=loadingDialog.findViewById(R.id.loading_dialog);

        getSupportActionBar().setTitle(getIntent().getStringExtra("DEPARTMENTNAME"));
        DepartmentName=getIntent().getStringExtra("DEPARTMENTNAME");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        gridView = findViewById(R.id.gridView);
        myRef=FirebaseDatabase.getInstance().getReference();

        sets=DepartmentActivity.departmentList.get(getIntent().getIntExtra("POSITION", 0)).getSets();

        FirebaseDatabase db=FirebaseDatabase.getInstance();

            db.getReference().child("Departments").child(getIntent().getStringExtra("KEY")).child("sets").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(String sId: sets) {
                        for(DataSnapshot snapshot1: snapshot.getChildren()){
                            if(snapshot1.getKey().toString().equals(sId)){
                                setCodes.add(snapshot1.getValue().toString());
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        gridAdapter=new GridAdapter(sets,setCodes,getIntent().getStringExtra("DEPARTMENTNAME"),getIntent().getStringExtra("KEY"), new GridAdapter.GridListener() {
            @Override
            public void addSet() {
                loadingDialog.show();
                FirebaseDatabase database=FirebaseDatabase.getInstance();
                long t=System.currentTimeMillis();
                String id= String.valueOf(t) + UUID.randomUUID().toString();
                //String timeuuid = com.datastax.driver.core.utils.UUIDs.timeBased().toString();
                setCode=generateRandomString();
                database.getReference().child("Departments").child(getIntent().getStringExtra("KEY")).child("sets").child(id).setValue(setCode).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            sets.add(id);
                            setCodes.add(setCode);
                            gridAdapter.notifyDataSetChanged();
                            showCustomToast(R.drawable.check,"Set added successfully");
                        }else{
                            Toast.makeText(SetsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                        loadingDialog.dismiss();
                    }
                });
            }

            @Override
            public void onLongClick(String setId,int position) {
                new AlertDialog.Builder(SetsActivity.this,R.style.Theme_AppCompat_DayNight_Dialog)
                        .setTitle("Delete Set")
                        .setMessage("Are you sure you want to delete this set ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingText.setText("Deleting...");
                                loadingDialog.show();
                                myRef.child("SETS")
                                        .child(setId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            myRef.child("Departments")
                                                    .child(DepartmentActivity.departmentList.get(getIntent().getIntExtra("POSITION", 0)).getKey())
                                                    .child("sets").child(setId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        sets.remove(setId);
                                                        setCodes.remove(setCode);
                                                        gridAdapter.notifyDataSetChanged();
                                                        showCustomToast(R.drawable.ic_delete,"Set deleted successfully");
                                                    }else{
                                                        Toast.makeText(SetsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                    }
                                                    loadingDialog.dismiss();
                                                    loadingText.setText("Loading...");
                                                }
                                            });
                                        } else {
                                            Toast.makeText(SetsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                            loadingDialog.dismiss();
                                        }

                                    }
                                });




                            }
                        })
                        .setNegativeButton("Cancel",null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });


        gridView.setAdapter(gridAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    public String generateRandomString(){

        StringBuffer randStr = new StringBuffer();
        for(int i=0; i<RANDOM_STRING_LENGTH; i++){
            int number = getRandomNumber();
            char ch = CHAR_LIST.charAt(number);
            randStr.append(ch);
        }
        return randStr.toString();
    }

    private int getRandomNumber() {
        int randomInt = 0;
        Random randomGenerator = new Random();
        randomInt = randomGenerator.nextInt(CHAR_LIST.length());
        if (randomInt - 1 == -1) {
            return randomInt;
        } else {
            return randomInt - 1;
        }
    }

    public void showCustomToast(int id,String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout,
                (ViewGroup) findViewById(R.id.toast_layout));
        TextView text = (TextView) layout.findViewById(R.id.text);
        ImageView image=(ImageView)layout.findViewById(R.id.image);
        image.setImageDrawable(getDrawable(id));
        text.setText(message);
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    
}