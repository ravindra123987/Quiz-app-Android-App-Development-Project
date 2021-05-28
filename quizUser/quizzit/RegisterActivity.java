package com.project.quizzit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private EditText email,password;
    private Button registerBtn;
    private ProgressBar progressBar;
    private TextView forgotPassword;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef=database.getReference();
    private String studentId;
    boolean exists;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.email);
        password=findViewById(R.id.password);
        registerBtn=findViewById(R.id.register);
        progressBar=findViewById(R.id.pBar);
        progressBar.setVisibility(View.INVISIBLE);

        exists=false;

        intent=new Intent(this,LoginActivity.class);


        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (email.getText().toString().isEmpty()) {
                    email.setError("required");
                    return;
                } else {
                    email.setError(null);
                }

                if (!isEmailValid(email.getText().toString())) {
                    email.setError("Not valid");
                    return;
                } else {
                    email.setError(null);
                }


                if (password.getText().toString().isEmpty()) {
                    password.setError("required");
                    return;
                } else {
                    password.setError(null);
                }

                String checker="NAN";
                checker=checkValidity(password.getText().toString());
                if(checker.equals("valid")){
                    progressBar.setVisibility(View.VISIBLE);

                    myRef.child("STUDENTS").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                                String id = dataSnapshot.child("email").getValue().toString();
                                if (id.equals(email.getText().toString())) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    showCustomToast(R.drawable.ic_error, "Email already registered");
                                    return;
                                }

                            }
                            addData();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(RegisterActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }else{
                    showCustomToast(R.drawable.ic_warning,checker);
                    return;
                }



            }
        });
    }


    private void addData(){
        Map<String, Object> map = new HashMap<>();
        map.put("email", email.getText().toString());
        map.put("password", password.getText().toString());
        studentId = UUID.randomUUID().toString();
        myRef.child("STUDENTS").child(studentId).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    showCustomToast(R.drawable.check, "User registration successful");
                    startActivity(intent);
                    finish();
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    showCustomToast(R.drawable.ic_error, "Registration attempt Failed");
                }
            }
        });
    }


    public boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
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

    public String checkValidity(String password){
        int min =7;
        int max=15;
        int digit=0;
        int special=0;
        int upCount=0;
        int loCount=0;
        if(password.length()>=min && password.length()<=max){

            for(int i =0;i<password.length();i++){
                char c = password.charAt(i);
                if(Character.isUpperCase(c)){
                    upCount++;
                }
                if(Character.isLowerCase(c)){
                    loCount++;
                }
                if(Character.isDigit(c)){
                    digit++;
                }
                if(c>=33&&c<=46||c==64){
                    special++;
                }
            }


            if(loCount < 1){
                return  "You need atleast a lower case character";
            }
            else if(upCount<1){
                return "You need atleast a upper case character";
            }
            else if(digit < 1){
                return "You need atleast a digit";
            }

            else if(special < 1){
                return (" You need atleast one special chracter:");
            }
            else{
                return "valid";
            }

        }
        else if(password.length() < min){
            return (" Password must be atleast "+min+" characters");
        }

        else{
            return " Password is too long.Limit is "+max+" chracters";
        }
    }
}