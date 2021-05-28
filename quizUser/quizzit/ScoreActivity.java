package com.project.quizzit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ScoreActivity extends AppCompatActivity {
    private TextView scored,total;
    private Button doneBtn;
    private String setId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        scored=findViewById(R.id.text2);
        total=findViewById(R.id.text3);
        doneBtn=findViewById(R.id.done);
        String score=String.valueOf(getIntent().getIntExtra("SCORE",0));
        scored.setText(score);
        //setId=getIntent().getStringExtra("SETID");

        FirebaseDatabase db=FirebaseDatabase.getInstance();
        DatabaseReference myRef=db.getReference();

        myRef.child("STUDENTS").child(LoginActivity.studentId).child("sets").child(QuestionsActivity.setId).setValue(score).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    showCustomToast(R.drawable.check,"Result saved successfully");
                }else{
                    showCustomToast(R.drawable.ic_error,"Error in saving result");
                }
            }
        });


        String str="OUT OF "+String.valueOf(getIntent().getIntExtra("TOTAL",0));
        total.setText(str);

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(ScoreActivity.this,DepartmentActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
            }
        });
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