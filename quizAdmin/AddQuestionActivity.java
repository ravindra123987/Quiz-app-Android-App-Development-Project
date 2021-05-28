package com.example.quizadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.UUID;

public class AddQuestionActivity extends AppCompatActivity {

    private EditText question;
    private RadioGroup options;
    private LinearLayout choices;
    private String DepartmentName;
    private Button uploadBtn;
    private int pos;
    private String setId;
    private Dialog loadingDialog;
    private QuestionModel questionModel;
    private String id;
    private Toolbar tb;
    TextView loadingText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        DepartmentName=getIntent().getStringExtra("DEPARTMENT");
        setId=getIntent().getStringExtra("SETID");

        pos=getIntent().getIntExtra("POSITION",-1);

        question=findViewById(R.id.question1);
        options=findViewById(R.id.options);
        choices=findViewById(R.id.choices);
        uploadBtn=findViewById(R.id.uploadBtn);


        tb=findViewById(R.id.toolBar3);
        setSupportActionBar(tb);
        getSupportActionBar().setTitle("Add question");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(setId==null){
            finish();
            return;
        }

        if(pos!=-1){
            questionModel=QuestionsActivity.list.get(pos);
            editData();
        }

        loadingDialog=new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.department_background));

        loadingText=loadingDialog.findViewById(R.id.loading_dialog);

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(question.getText().toString().isEmpty()){
                    question.setError("Required");
                    return;
                }
                upload();
            }
        });
    }

    private void upload() {
        int correct = -1;
        //Toast.makeText(this, String.valueOf(options.getChildCount()), Toast.LENGTH_SHORT).show();
        for (int i = 0; i < options.getChildCount(); i++) {
            EditText choice = (EditText) (choices.getChildAt(i));
            if (choice.getText().toString().isEmpty()) {
                choice.setError("Required");
                return;
            }

            RadioButton r = (RadioButton) options.getChildAt(i);
            if (r.isChecked()) {
                correct = i;
            }

        }

        if (correct == -1) {
            Toast.makeText(this, "Please mark correct answer", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("question", question.getText().toString());
        map.put("correctOption", ((EditText) choices.getChildAt(correct)).getText().toString());
        map.put("option1", ((EditText) choices.getChildAt(0)).getText().toString());
        map.put("option2", ((EditText) choices.getChildAt(1)).getText().toString());
        map.put("option3", ((EditText) choices.getChildAt(2)).getText().toString());
        map.put("option4", ((EditText) choices.getChildAt(3)).getText().toString());
        map.put("setId", setId);

        if (pos != -1) {
            id = questionModel.getId();
        } else {
            long t = System.currentTimeMillis();
            id = String.valueOf(t) + UUID.randomUUID().toString();
        }
        loadingText.setText("Uploading...");



            loadingDialog.show();
            FirebaseDatabase.getInstance().getReference()
                    .child("SETS")
                    .child(setId)
                    .child(id)
                    .setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        QuestionModel questionModel = new QuestionModel(id, map.get("correctOption").toString(), map.get("option1").toString(), map.get("option2").toString(), map.get("option3").toString(), map.get("option4").toString(), map.get("question").toString(), map.get("setId").toString());
                        if(pos==-1) {
                            QuestionsActivity.list.add(questionModel);
                            showCustomToast(R.drawable.check, "Question added successfully");
                            //Toast.makeText(AddQuestionActivity.this, "Question added succesfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }else{
                            QuestionsActivity.list.set(pos, questionModel);
                            showCustomToast(R.drawable.check, "Question updated succesfully");
                           //Toast.makeText(AddQuestionActivity.this, "Question updated succesfully", Toast.LENGTH_SHORT).show();
                           finish();
                        }

                    } else {
                        Toast.makeText(AddQuestionActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                    loadingDialog.dismiss();
                    loadingText.setText("Loading...");
                }
            });

    }

    private void editData(){

        question.setText(questionModel.getQuestion());

        ((EditText)choices.getChildAt(0)).setText(questionModel.getOption1());
        ((EditText)choices.getChildAt(1)).setText(questionModel.getOption2());
        ((EditText)choices.getChildAt(2)).setText(questionModel.getOption3());
        ((EditText)choices.getChildAt(3)).setText(questionModel.getOption4());

        for(int i=0;i<choices.getChildCount();i++){
           if(((EditText)choices.getChildAt(i)).getText().toString().equals(questionModel.getCorrectOption())){
               RadioButton radioButton=(RadioButton)options.getChildAt(i);
               radioButton.toggle();
               break;
           }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
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