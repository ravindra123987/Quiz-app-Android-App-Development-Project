package com.project.quizzit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChangePassword extends AppCompatActivity {

    private Button confirmBtn;
    private EditText oldPassword, newPassword, newPassword1;
    String password = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        Toolbar toolBar=findViewById(R.id.toolBar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("Change Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        confirmBtn = findViewById(R.id.confirm);
        oldPassword = findViewById(R.id.old_password);
        newPassword = findViewById(R.id.new_password);
        newPassword1 = findViewById(R.id.new_password1);
        Bundle b = getIntent().getExtras();

        Dialog loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.department_background));


        FirebaseDatabase.getInstance().getReference().child("STUDENTS").child(LoginActivity.studentId).child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                password = snapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChangePassword.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                finish();
            }
        });



        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldpass = oldPassword.getText().toString();
                String newpass = newPassword.getText().toString();
                String newpass1 = newPassword1.getText().toString();
                if (!oldpass.matches(password)) {
                    showCustomToast(R.drawable.ic_warning,"Old Password does not match");
                    return;
                }

                if (newpass.matches("")) {
                    showCustomToast(R.drawable.ic_warning,"New password cannot be empty");
                    return;
                }

                if (oldpass.matches(newpass)) {
                    showCustomToast(R.drawable.ic_warning,"New password cannot be same as old password");
                    return;
                }

                if (!newpass.matches(newpass1)) {
                    showCustomToast(R.drawable.ic_warning,"Two new passwords does not match");
                    return;
                }
                String checker="NAN";
                checker=checkValidity(newpass);
                if(!checker.equals("valid")){
                    showCustomToast(R.drawable.ic_warning,checker);
                    return;
                }

                loadingDialog.show();

                FirebaseDatabase.getInstance().getReference().child("STUDENTS").child(LoginActivity.studentId).child("password").setValue(newpass).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            loadingDialog.dismiss();
                            showCustomToast(R.drawable.check, "New password updated successfully");
                            Intent i=new Intent(ChangePassword.this,DepartmentActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            loadingDialog.dismiss();
                            Toast.makeText(ChangePassword.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void showCustomToast(int id, String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout,
                (ViewGroup) findViewById(R.id.toast_layout));
        TextView text = (TextView) layout.findViewById(R.id.text);
        ImageView image = (ImageView) layout.findViewById(R.id.image);
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