package com.project.quizzit;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button startBtn,bookmarkBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startBtn=(Button)findViewById(R.id.start_btn);
        bookmarkBtn=(Button)findViewById(R.id.extra_btn);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startTest=new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(startTest);
            }
        });

        bookmarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bookmarksIntent=new Intent(getApplicationContext(),BookmarkActivity.class);
                startActivity(bookmarksIntent);
            }
        });
    }
}