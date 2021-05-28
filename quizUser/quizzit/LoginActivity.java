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

public class LoginActivity extends AppCompatActivity {

    private EditText email,password;
    private Button loginBtn,registerBtn;
    private ProgressBar progressBar;
    private TextView forgotPassword;
    public static String studentId;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef=database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        email = findViewById(R.id.email);
        password=findViewById(R.id.password);
        loginBtn=findViewById(R.id.login);
        registerBtn=findViewById(R.id.register);
        progressBar=findViewById(R.id.pBar);
        progressBar.setVisibility(View.INVISIBLE);

        Intent intent=new Intent(this,DepartmentActivity.class);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(email.getText().toString().isEmpty()){
                    email.setError("required");
                    return;
                }
                else{
                    email.setError(null);
                }

                if(!isEmailValid(email.getText().toString())){
                    email.setError("Not valid");
                    return;
                }
                else{
                    email.setError(null);
                }


                if(password.getText().toString().isEmpty()){
                    password.setError("required");
                    return;
                }
                else{
                    password.setError(null);
                }

               progressBar.setVisibility(View.VISIBLE);


                myRef.child("STUDENTS").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                            String id = dataSnapshot.child("email").getValue().toString();
                            if (id.equals(email.getText().toString())) {

                                if(password.getText().toString().equals(dataSnapshot.child("password").getValue().toString())){
                                    studentId=dataSnapshot.getKey();
                                    showCustomToast(R.drawable.check, "Login attempt successful");
                                    startActivity(intent);
                                    finish();
                                    return;
                                }
                                else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    showCustomToast(R.drawable.ic_error, "Invalid password");
                                    return;
                                }
                            }

                        }
                        progressBar.setVisibility(View.INVISIBLE);
                        showCustomToast(R.drawable.ic_error, "Email not registered");

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(i);
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
}