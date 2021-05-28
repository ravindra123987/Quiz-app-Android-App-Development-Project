package com.project.quizzit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.GridView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

public class SetsActivity extends AppCompatActivity {
    private Toolbar toolBar;
    private GridView gridView;
    private List<String> sets;
    private List<String> setCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);
        toolBar= findViewById(R.id.toolBar);
        setSupportActionBar(toolBar);

        getSupportActionBar().setTitle(getIntent().getStringExtra("TITLE"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        gridView = findViewById(R.id.gridView);
        sets=DepartmentActivity.departmentList.get(getIntent().getIntExtra("POSITION", 0)).getSets();
        setCodes=new ArrayList<>();

        FirebaseDatabase db=FirebaseDatabase.getInstance();

        db.getReference().child("Departments").child(getIntent().getStringExtra("KEY")).child("sets").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(String sId: sets) {
                    for(DataSnapshot snapshot1: snapshot.getChildren()){
                        if(snapshot1.getKey().equals(sId)){
                            setCodes.add(snapshot1.getValue().toString());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        GridAdapter gridAdapter=new GridAdapter(sets,setCodes,getIntent().getStringExtra("TITLE"));
        gridView.setAdapter(gridAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


}