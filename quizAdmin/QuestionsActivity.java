package com.example.quizadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class QuestionsActivity extends AppCompatActivity {

    private Button add,excel;
    private RecyclerView recyclerView;
    private QuestionAdapter adapter;
    public static List<QuestionModel> list;
    private Dialog loadingDialog;
    private DatabaseReference myRef;
    public static final int cellCount=6;
    private String setCode,setNo;
    public static String setId;
    private String DepartmentName;
    private TextView loadingText;

    private static final String CHAR_LIST =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int RANDOM_STRING_LENGTH = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        myRef=FirebaseDatabase.getInstance().getReference();

        Toolbar toolBar=findViewById(R.id.toolBar1);
        setSupportActionBar(toolBar);
        DepartmentName=getIntent().getStringExtra("DEPARTMENT");
        setId=getIntent().getStringExtra("SETID");
        setCode=getIntent().getStringExtra("SETCODE");
        setNo="set"+String.valueOf(getIntent().getIntExtra("POSITION",0));
        String heading=DepartmentName+" / "+setNo;
        getSupportActionBar().setTitle(heading);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        add=findViewById(R.id.addQuestion);
        excel=findViewById(R.id.addQuestionExcel);
        recyclerView=findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(recyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        list=new ArrayList<>();
        loadingDialog=new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.department_background));
        loadingText=loadingDialog.findViewById(R.id.loading_dialog);

        adapter=new QuestionAdapter(list, DepartmentName, new QuestionAdapter.DeleteListener() {
            @Override
            public void onLongClick(int position, String id) {
                new AlertDialog.Builder(QuestionsActivity.this,R.style.Theme_AppCompat_DayNight_Dialog)
                        .setTitle("Delete question")
                        .setMessage("Are you sure you want to delete this question ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingText.setText("Deleting...");
                                loadingDialog.show();
                                myRef.child("SETS").child(setId).child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            list.remove(position);
                                            adapter.notifyItemRemoved(position);
                                            adapter.notifyDataSetChanged();
                                            showCustomToast(R.drawable.ic_delete,"Question deleted Successfully");
                                        }else{
                                            Toast.makeText(QuestionsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
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

        getData(DepartmentName,setId);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent moveToAddQuestion=new Intent(QuestionsActivity.this,AddQuestionActivity.class);
                moveToAddQuestion.putExtra("DEPARTMENT",DepartmentName);
                moveToAddQuestion.putExtra("SETID",setId);
                startActivity(moveToAddQuestion);
            }
        });

        excel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ActivityCompat.checkSelfPermission(QuestionsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    selectFile();
                }else{
                    ActivityCompat.requestPermissions(QuestionsActivity.this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},101);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==101){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                selectFile();
            }else{
                Toast.makeText(this, "Please grant permissions!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }

        if(item.getItemId()==R.id.copy){
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(
                    "text label",
                    setCode);
            clipboard.setPrimaryClip(clip);
            showCustomToast(R.drawable.ic_copy,"Set code copied to clip board");
            //Toast.makeText(QuestionsActivity.this, "Set code copied to clip board", Toast.LENGTH_SHORT).show();
        }

        if(item.getItemId()==R.id.result){
            Intent moveToResultIntent=new Intent(QuestionsActivity.this,ResultActivity.class);
            moveToResultIntent.putExtra("SETID",setId);
            startActivity(moveToResultIntent);
        }

        if(item.getItemId()==R.id.changeCode){

            loadingDialog.show();
            FirebaseDatabase database=FirebaseDatabase.getInstance();


            String newCode=generateRandomString();
            database.getReference().child("Departments").child(getIntent().getStringExtra("KEY")).child("sets").child(setId).setValue(newCode).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        int index=SetsActivity.setCodes.indexOf(setCode);
                        SetsActivity.setCodes.set(index,newCode);
                        setCode=newCode;
                        showCustomToast(R.drawable.check,"Set Code changed successfully");
                    }else{
                        Toast.makeText(QuestionsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                    loadingDialog.dismiss();
                }
            });





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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_copy,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.notifyDataSetChanged();
    }

    private void getData(String DepartmentName,String setId){
        loadingDialog.show();
        FirebaseDatabase.getInstance().getReference()
                .child("SETS")
                .child(setId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.getKey();
                    String question = snapshot.child("question").getValue().toString();
                    String correctOption = snapshot.child("correctOption").getValue().toString();
                    String option1 = snapshot.child("option1").getValue().toString();
                    String option2 = snapshot.child("option2").getValue().toString();
                    String option3 = snapshot.child("option3").getValue().toString();
                    String option4 = snapshot.child("option4").getValue().toString();

                    list.add(new QuestionModel(id, correctOption, option1, option2, option3, option4, question, setId));
                }
                loadingDialog.dismiss();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuestionsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                finish();
            }
        });


    }



    private void selectFile(){
        Intent getFile=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        getFile.setType("*/*"); //all categories of files
        getFile.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(getFile,"Select File"),102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==102){
            if(resultCode==RESULT_OK){
                String filePath=data.getData().getPath();
                if(filePath.endsWith(".xlsx")){
                    ReadExcelFile(data.getData());
                }else{
                    showCustomToast(R.drawable.ic_error,"Please choose an Excel file");
                    //Toast.makeText(QuestionsActivity.this, "Please choose an Excel file", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void ReadExcelFile(Uri fileUri){
        loadingText.setText("Scanning questions..");
        loadingDialog.show();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {


        HashMap<String,Object> parentMap=new HashMap<>();
        List<QuestionModel> tempList=new ArrayList<>();
        try {
            InputStream inputStream=getContentResolver().openInputStream(fileUri);
            XSSFWorkbook workbook= new XSSFWorkbook(inputStream);
            XSSFSheet sheet=workbook.getSheetAt(0);

            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator(); //To convert to strings

            int rowsCount=sheet.getPhysicalNumberOfRows();

            if(rowsCount > 0){
                for(int i=0;i<rowsCount;i++){
                    Row row= sheet.getRow(i);

                    if(row.getPhysicalNumberOfCells() == cellCount) {
                        String question = getCellData(row,0,formulaEvaluator);
                        String option1 = getCellData(row,1,formulaEvaluator);
                        String option2 = getCellData(row,2,formulaEvaluator);
                        String option3 = getCellData(row,3,formulaEvaluator);
                        String option4 = getCellData(row,4,formulaEvaluator);
                        String correctOption = getCellData(row,5,formulaEvaluator);

                        if(correctOption.equals(option1) || correctOption.equals(option2) || correctOption.equals(option3) || correctOption.equals(option4)){

                            HashMap<String,Object> questionMap=new HashMap<>();
                            questionMap.put("question",question);
                            questionMap.put("option1",option1);
                            questionMap.put("option2",option2);
                            questionMap.put("option3",option3);
                            questionMap.put("option4",option4);
                            questionMap.put("correctOption",correctOption);
                            questionMap.put("setId",setId);
                            long t=System.currentTimeMillis();
                            String id= String.valueOf(t)+UUID.randomUUID().toString();
                            parentMap.put(id,questionMap);

                            tempList.add(new QuestionModel(id,correctOption,option1,option2,option3,option4,question,setId));

                        }else{
                            int finalI1 = i;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadingDialog.dismiss();
                                    loadingText.setText("Loading...");
                                    String errorMessageAt="In Row no "+String.valueOf(finalI1 +1)+", correct option not matching with given options";
                                    showCustomToast(R.drawable.ic_error,errorMessageAt);
                                    //Toast.makeText(QuestionsActivity.this, errorMessageAt, Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }
                    }else{
                        int finalI = i;
                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                        loadingDialog.dismiss();
                        loadingText.setText("Loading...");
                        String errorMessageAt="Row no "+String.valueOf(finalI+1)+" has invalid format or data";
                        showCustomToast(R.drawable.ic_error,errorMessageAt);
                        //Toast.makeText(QuestionsActivity.this, errorMessageAt, Toast.LENGTH_SHORT).show();
                                          }
                        });
                        return;
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                loadingText.setText("Uploading...");
                FirebaseDatabase.getInstance().getReference().child("SETS")
                        .child(setId).updateChildren(parentMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            list.addAll(tempList);
                            adapter.notifyDataSetChanged();
                            String textI="Questions uploaded successfully";
                            showCustomToast(R.drawable.check,textI);
                        }else{
                            Toast.makeText(QuestionsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        loadingDialog.dismiss();
                        loadingText.setText("Loading...");
                    }
                });

                    }
                });


            }else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingDialog.dismiss();
                        loadingText.setText("Loading...");
                        showCustomToast(R.drawable.ic_error,"Excel file is empty");
                        //Toast.makeText(QuestionsActivity.this, "Excel file is empty", Toast.LENGTH_SHORT).show();
                    }
                });

                return;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingDialog.dismiss();
                    loadingText.setText("Loading...");
                    Toast.makeText(QuestionsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingText.setText("Loading...");
                    loadingDialog.dismiss();
                    Toast.makeText(QuestionsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
            }
        });


    }

    private String getCellData(Row row,int cellPosition,FormulaEvaluator formulaEvaluator){
        String value="";
        Cell cell=row.getCell(cellPosition);

        switch(cell.getCellType()){
            case Cell.CELL_TYPE_BOOLEAN :
                return value+cell.getBooleanCellValue();

            case Cell.CELL_TYPE_NUMERIC:
                return value+cell.getNumericCellValue();

            case Cell.CELL_TYPE_STRING:
                return value+cell.getStringCellValue();

            default:
                return value;
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


    /*public static int find(String[] a, String target)
    {
        for (int i = 0; i < a.length; i++)
        {
            if (a[i].equals(target)) {
                return i;
            }
        }

        return -1;
    }*/
}