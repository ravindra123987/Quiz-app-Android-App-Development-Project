package com.example.quizadmin;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class DepartmentActivity extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef=database.getReference();
    private Dialog loadingDialog,departmentDialog;
    private RecyclerView recyclerView;
    public static List<DepartmentModel> departmentList;
    private DepartmentAdapter adapter;

    private CircleImageView addImage;
    private EditText deptName;
    private Button addBtn;
    private Uri image;
    private String downloadUrl;
    private TextView loadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department);

        loadingDialog=new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.department_background));
        loadingText=loadingDialog.findViewById(R.id.loading_dialog);

        setDepartmentDialog();

        Toolbar toolBar=findViewById(R.id.toolBar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setTitle("Departments");

        recyclerView=findViewById(R.id.rv);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(recyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        departmentList= new ArrayList<>();


        adapter = new DepartmentAdapter(departmentList, new DepartmentAdapter.DeleteListener() {
            @Override
            public void onDelete(String key,int position) {

                new AlertDialog.Builder(DepartmentActivity.this,R.style.Theme_AppCompat_DayNight_Dialog)
                        .setTitle("Delete Department")
                        .setMessage("Are you sure,all data related to this department gets deleted permanently ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingText.setText("Deleting...");
                                loadingDialog.show();
                                myRef.child("Departments").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            for(String setid: departmentList.get(position).getSets()){
                                                myRef.child("SETS").child(setid).removeValue();
                                            }
                                            departmentList.remove(position);
                                            adapter.notifyDataSetChanged();
                                            showCustomToast(R.drawable.ic_delete,"Department deleted successfully");
                                        }else{
                                            Toast.makeText(DepartmentActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                        }
                                        loadingDialog.dismiss();
                                        loadingText.setText("Loading...");

                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel",null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });
        recyclerView.setAdapter(adapter);

        loadingDialog.show();
        myRef.child("Departments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){

                    List<String> sets=new ArrayList<>();

                    for(DataSnapshot snapshot1 :dataSnapshot.child("sets").getChildren()){
                        sets.add(snapshot1.getKey());
                    }

                    departmentList.add(new DepartmentModel(dataSnapshot.child("url").getValue().toString(),
                            dataSnapshot.child("name").getValue().toString(),
                            dataSnapshot.getKey(),
                            sets));
                }
                adapter.notifyDataSetChanged();
                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DepartmentActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.add){
            departmentDialog.show();
        }

        if(item.getItemId()==R.id.logout){
            new AlertDialog.Builder(DepartmentActivity.this,R.style.Theme_AppCompat_DayNight_Dialog)
                    .setTitle("Logout Request")
                    .setMessage("Are you sure you want to logout ?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadingDialog.show();
                            FirebaseAuth.getInstance().signOut();
                            Intent intent=new Intent(DepartmentActivity.this,MainActivity.class);
                            showCustomToast(R.drawable.check,"Logged out successfully");
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("No",null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setDepartmentDialog(){
        departmentDialog=new Dialog(this);
        departmentDialog.setContentView(R.layout.add_department);
        departmentDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        departmentDialog.setCancelable(true);

        addImage=departmentDialog.findViewById(R.id.image);
        addBtn=departmentDialog.findViewById(R.id.add);
        deptName=departmentDialog.findViewById(R.id.deptName);

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent,201);
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(deptName.getText().toString().isEmpty()){
                    deptName.setError("required");
                    return;
                }
                for(DepartmentModel model: departmentList){
                    if(deptName.getText().toString().equals(model.getName())){
                        showCustomToast(R.drawable.ic_error,"Department name already exists");
                        //deptName.setError("Department name already exists");
                        return;
                    }
                }
                if(image==null){
                    showCustomToast(R.drawable.ic_error,"Please select your image");
                    //Toast.makeText(DepartmentActivity.this, "Please select your image", Toast.LENGTH_SHORT).show();
                    return;
                }
                departmentDialog.dismiss();
                uploadData();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==201){
            if(resultCode==RESULT_OK){
                image=data.getData();
                addImage.setImageURI(image);
            }
        }
    }

    private void uploadData(){
        loadingDialog.show();

        StorageReference storageReference= FirebaseStorage.getInstance().getReference();

        StorageReference imageReference=storageReference.child("Departments").child(image.getLastPathSegment());

        UploadTask uploadTask = imageReference.putFile(image);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            downloadUrl=task.getResult().toString();
                            uploadCategoryName();
                        }else{
                            loadingDialog.dismiss();
                            Toast.makeText(DepartmentActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                } else {
                    loadingDialog.dismiss();
                    // Handle failures
                    // ...
                    Toast.makeText(DepartmentActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadCategoryName(){
        Map<String,Object> map=new HashMap<>();
        map.put("name",deptName.getText().toString());
        map.put("sets",0);
        map.put("url",downloadUrl);


        FirebaseDatabase database=FirebaseDatabase.getInstance();
        String id= String.valueOf(System.currentTimeMillis())+UUID.randomUUID().toString();

        database.getReference().child("Departments").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    departmentList.add(new DepartmentModel(downloadUrl,deptName.getText().toString(),id,new ArrayList<String>()));
                    adapter.notifyDataSetChanged();
                    loadingDialog.dismiss();
                    showCustomToast(R.drawable.check,"Department added successfully");
                    addImage.setImageDrawable(getDrawable(R.drawable.ic_add_photo));
                    deptName.setText("");
                }else{
                    Toast.makeText(DepartmentActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                }

            }
        });
    }

    private boolean checkConnection() {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            connected = true;
        } else
            connected = false;

        return connected;
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