package com.example.quizadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private EditText email,password;
    private Button loginBtn;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    private TextView forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.email);
        password=findViewById(R.id.password);
        loginBtn=findViewById(R.id.login);
        progressBar=findViewById(R.id.pBar);
        progressBar.setVisibility(View.INVISIBLE);
        forgotPassword=findViewById(R.id.forgot);

        firebaseAuth=FirebaseAuth.getInstance();
        Intent intent=new Intent(this,DepartmentActivity.class);
        if(firebaseAuth.getCurrentUser() != null){
            startActivity(intent);
            finish();
           // Toast.makeText(this, "Place1", Toast.LENGTH_SHORT).show();
            return;
        }

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                FirebaseAuth.getInstance().sendPasswordResetEmail("ravindrabikkina11@gmail.com")
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    showCustomToast(R.drawable.check,"Reset mail sent to your gmail");
                                }else{
                                    showCustomToast(R.drawable.ic_error,"Something went wrong");
                                }
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
            }
        });
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
                firebaseAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            showCustomToast(R.drawable.check,"Logged in successfully");
                            startActivity(intent);
                            finish();
                        }else{
                            progressBar.setVisibility(View.INVISIBLE);
                            showCustomToast(R.drawable.ic_error,"Login attempt Failed");
                            //Toast.makeText(MainActivity.this, "Login attempt Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    /*@Override
    protected void onPause() {
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
                firebaseAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            startActivity(intent);
                            finish();
                        }else{
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        super.onPause();
    }*/

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