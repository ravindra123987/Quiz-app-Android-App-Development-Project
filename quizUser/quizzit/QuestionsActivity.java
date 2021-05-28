package com.project.quizzit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuestionsActivity extends AppCompatActivity {
    public static final String FILE_NAME="QUIZZIT";
    public static final String KEY_NAME="QUESTIONS";
    private static final long COUNTDOWN_IN_MILLIS = 30000;

    private Toolbar toolBar;
    private TextView question,question_number;
    private FloatingActionButton bookmarkBtn;
    private LinearLayout optionsContainer;
    private Button nextBtn;
    private int count=0;
    private List<QuestionModel> list;
    private int position=0,score=0;
    public static String setId;
    private Dialog loadingDialog;

    long backPressedTime;
    private TextView textViewCountDown;
    private ColorStateList textColorDefaultCd;
    private static CountDownTimer countDownTimer;
    private long timeLeftInMillis;

    private List<QuestionModel> bookmarksList;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Gson gson;
    private int matchedQuestionPosition;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef=database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        toolBar=(Toolbar)findViewById(R.id.toolBarNew);
        question=(TextView)findViewById(R.id.question_display);
        question_number=(TextView)findViewById(R.id.questionNumber);
        bookmarkBtn=(FloatingActionButton)findViewById(R.id.bookmarkBtn);
        optionsContainer=(LinearLayout)findViewById(R.id.optionsHolder);
        nextBtn=(Button)findViewById(R.id.nextBtn);
        textViewCountDown = findViewById(R.id.textview_countdown);
        textColorDefaultCd = textViewCountDown.getTextColors();

        preferences=getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        editor=preferences.edit();
        gson=new Gson();

        getBookmarks();

        bookmarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(matchQuestion()){
                    bookmarksList.remove(matchedQuestionPosition);
                    bookmarkBtn.setImageDrawable(getDrawable(R.drawable.bookmark_off));
                }
                else{
                    bookmarksList.add(list.get(position));
                    bookmarkBtn.setImageDrawable(getDrawable(R.drawable.bookmark_on));
                }
            }
        });

        setId=getIntent().getStringExtra("SETID");

        loadingDialog=new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.department_background));

        list=new ArrayList<>();


        String s=position+1+"/"+list.size();
        question_number.setText(s);

        loadingDialog.show();
        myRef.child("SETS").child(setId).addListenerForSingleValueEvent(new ValueEventListener() {
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

                if(list.size() > 0){

                    for(int i=0;i<4;i++){
                        optionsContainer.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                countDownTimer.cancel();
                                countDownTimer=null;
                                checkAnswer((Button) v);
                            }
                        });
                    }

                    playAnimation(question,0,list.get(position).getQuestion());
                    nextBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            nextBtn.setEnabled(false);
                            nextBtn.setAlpha(0.7f);
                            enableOptions(true);

                            position++;
                            if(position==list.size()){
                                Intent moveToScoreIntent= new Intent(QuestionsActivity.this,ScoreActivity.class);
                                moveToScoreIntent.putExtra("SCORE",score);
                                moveToScoreIntent.putExtra("TOTAL",position);
                                moveToScoreIntent.putExtra("SETID",setId);
                                startActivity(moveToScoreIntent);
                                finish();
                                return;
                            }
                            count=0;
                            playAnimation(question,0,list.get(position).getQuestion());
                        }
                    });


                }
                else{
                    showCustomToast(R.drawable.ic_no,"No questions in this set");
                    Intent i=new Intent(QuestionsActivity.this,DepartmentActivity.class);
                    startActivity(i);

                    finish();
                    return;
                    //Toast.makeText(QuestionsActivity.this, "No questions in this set", Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuestionsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                finish();
            }
        });



    }



    @Override
    protected void onPause() {
        super.onPause();
        Log.e("CHECK","onPause called");
        setBookmarks();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer=null;
        }
        Intent moveToScoreIntent= new Intent(QuestionsActivity.this,ScoreActivity.class);
        moveToScoreIntent.putExtra("SCORE",score);
        moveToScoreIntent.putExtra("TOTAL",list.size());
        startActivity(moveToScoreIntent);
        finish();
        return;
    }

    private void playAnimation(View view, int value, String data){
        view.animate()
                .alpha(value)
                .scaleX(value)
                .scaleY(value)
                .setDuration(500)
                .setStartDelay(100)
                .setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if(value==0 && count<4){
                    String option="";
                    if(count==0){
                        option=list.get(position).getOption1();
                    }
                    else if(count==1){
                        option=list.get(position).getOption2();
                    }
                    else if(count==2){
                        option=list.get(position).getOption3();
                    }
                    else if(count==3){
                        option=list.get(position).getOption4();
                    }
                    playAnimation(optionsContainer.getChildAt(count),0,option);
                    count++;
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) { //to change questions
                if(value==0){

                    try {
                        ((TextView)view).setText(data);
                        String s=position+1+"/"+list.size();
                        question_number.setText(s);
                        if(matchQuestion()){
                            bookmarkBtn.setImageDrawable(getDrawable(R.drawable.bookmark_on));
                        }
                        else{
                            bookmarkBtn.setImageDrawable(getDrawable(R.drawable.bookmark_off));
                        }
                    }
                    catch (ClassCastException ex){
                        ((Button)view).setText(data);
                    }
                    view.setTag(data);
                    playAnimation(view,1,data);
                    timeLeftInMillis=COUNTDOWN_IN_MILLIS;
                    if(countDownTimer!=null){
                        countDownTimer.cancel();
                    }
                    countDownTimer=null;
                    startCountDown();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }




    public void checkAnswer(Button selectedOption){

        enableOptions(false);
        nextBtn.setEnabled(true);
        nextBtn.setAlpha(1);

        if(selectedOption.getText().toString().equals(list.get(position).getCorrectOption())){
            selectedOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            score++;
        }
        else{
            selectedOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            //Button correctOption=(Button)(optionsContainer.findViewWithTag(list.get(position).getCorrectOption()));
            //correctOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        }

    }

    private void enableOptions(boolean enableState){
        for(int i=0;i<4;i++){
            optionsContainer.getChildAt(i).setEnabled(enableState);
            if(enableState){
                optionsContainer.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D9D9D9")));
            }
        }
    }

    private void getBookmarks(){
        String json=preferences.getString(KEY_NAME,"");
        Type type=new TypeToken<List<QuestionModel>>(){}.getType();
        bookmarksList=gson.fromJson(json,type);

        if(bookmarksList==null){
            bookmarksList=new ArrayList<>();
        }
    }

    private boolean matchQuestion(){
        boolean matched=false;
        int i=0;
        for(QuestionModel model: bookmarksList){
            if(model.getQuestion().equals(list.get(position).getQuestion()) &&
                    model.getCorrectOption().equals(list.get(position).getCorrectOption()) &&
                    model.getSetId().equals(list.get(position).getSetId())){
                matched=true;
                matchedQuestionPosition=i;
            }
            i++;
        }
        return matched;
    }

    private void setBookmarks(){
        String json=gson.toJson(bookmarksList);
        editor.putString(KEY_NAME,json);
        editor.commit();
    }




    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            Intent moveToScoreIntent= new Intent(QuestionsActivity.this,ScoreActivity.class);
            moveToScoreIntent.putExtra("SCORE",score);
            moveToScoreIntent.putExtra("TOTAL",list.size());
            startActivity(moveToScoreIntent);
            finish();
            return;
        } else {
            showCustomToast(R.drawable.ic_warning,"Press back again to confirm finishing");
            //Toast.makeText(this, "Press back again to confirm finishing", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
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


    private void startCountDown() {
        countDownTimer = new CountDownTimer(timeLeftInMillis+100, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }
            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                updateCountDownText();
                enableOptions(false);
                nextBtn.setEnabled(true);
                nextBtn.setAlpha(1);
                showCustomToast(R.drawable.ic_warning,"Oops! time is up for attempting");
                /*for(int i=0;i<4;i++){
                    Button option=(Button)optionsContainer.getChildAt(i);
                    if(option.getText().toString().equals(list.get(position).getCorrectOption())){
                        //option.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                        break;
                    }
                }*/
            }
        }.start();
    }
    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        textViewCountDown.setText(timeFormatted);
        if (timeLeftInMillis < 10000) {
            textViewCountDown.setTextColor(Color.RED);
        } else {
            textViewCountDown.setTextColor(textColorDefaultCd);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer=null;
        }

        Log.e("CHECK","onDestroy called");



    }

}