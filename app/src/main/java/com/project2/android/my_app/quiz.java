package com.project2.android.my_app;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.zolad.zoominimageview.ZoomInImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//import androidx.appcompat.R;

public class quiz extends AppCompatActivity {

    Boolean storage_permission = true;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();  // to access firestore

    String phone; // to store entered phone number in sign up screen
    Map<String, Object> user; // to store all user data entered and store in firestore

    /* layouts L_sign_up, login to make visible or gone according to use,
    qtn_lyttP, qtn_lyttC, qtn_lyttM is physics ,chemistry, maths layout in which respective questions are displayed*/
    LinearLayout L_sign_up, login, qtn_lyttP, qtn_lyttC, qtn_lyttM;

    View inner_qtn_lyt; // to refer qtn_lyt.xml which contains card view of question and options
    int i, j;
    public static int Phy_Questions = 0, Che_Questions = 0, Mat_Questions = 0; // to store how many questions

    // to store reference of each radio button of all questions, to validate which radio button is checked(selected)
    // 1000 questions each has 5 options, 5th is not visible as it contains answer(it is required to validate with selected option)
    RadioButton[][] Phy_Opt = new RadioButton[1000][5];
    RadioButton[][] Che_Opt = new RadioButton[1000][5];
    RadioButton[][] Mat_Opt = new RadioButton[1000][5];

    /* answer: to store correct answer in option 5 of each question during validation
       s_phone: to store phone number entered in sign in screen
       codeByFirebase: to store otp came from firebase, to check with entered otp
       stop: to store stoping time*/
    String answer, s_phone, codeByFirbase, stop;
    Button reg;
    TextInputEditText l_phone, l_password, Name, Phone, Phone_Otp, Sem, Email, College, Place, Password, C_Password;
    ProgressBar s_progress;
    Boolean start = false; // to know user entered quiz or not
    public static Boolean call = true; // to make false when phone call comes
    Boolean in_signup = false; // to know user in sign up or not
    Boolean back = false;
    int[] Result = new int[3]; // to store result of 3 subjects
    public static String[][] Responses = new String[3][1000]; //3 subject 1000 options(one option from a question)
    private static final long START_TIME_IN_MILLIS = 10860000; //3 hr 1 min in milliseconds
    private TextView mTextViewCountDown, phyTxt, cheTxt, matTxt;
    private CountDownTimer mCountDownTimer;
    private long mTimeLeftInMillis = START_TIME_IN_MILLIS;
    Handler handler;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);


        if (ContextCompat.checkSelfPermission(quiz.this, Manifest.permission.READ_PHONE_STATE) +
                ContextCompat.checkSelfPermission(quiz.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //when permission not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(quiz.this, Manifest.permission.READ_PHONE_STATE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(quiz.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(quiz.this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
            } else {
                ActivityCompat.requestPermissions(quiz.this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
            }
        }

        // to hide navigation bar during quiz
        View quizPage = findViewById(R.id.quiz);
        quizPage.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


        // tab view for 3 subjects
        TabHost tabHost = findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec spec = tabHost.newTabSpec("Tab One");
        spec.setContent(R.id.tab1);
        spec.setIndicator("PHYSICS");
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("Tab Two");
        spec.setContent(R.id.tab2);
        spec.setIndicator("CHEMISTRY");
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("Tab Three");
        spec.setContent(R.id.tab3);
        spec.setIndicator("MATHS");
        tabHost.addTab(spec);


        mTextViewCountDown = (TextView) findViewById(R.id.timer_txt);

        phyTxt = findViewById(R.id.phyTxt);
        cheTxt = findViewById(R.id.cheTxt);
        matTxt = findViewById(R.id.matTxt);

        qtn_lyttP = findViewById(R.id.qtn_lyttP);
        // to add questions according to question number
        db.collection("PHYSICS").orderBy("Question_no").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots1) {

                        //test.setText(c_test);
                        int i = 0;

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots1) {


                            inner_qtn_lyt = getLayoutInflater().inflate(R.layout.qtn_lyt, null, false);
                            qtn_lyttP.addView(inner_qtn_lyt);

                            TextView qtn_no = inner_qtn_lyt.findViewById(R.id.c_qutnn_no);
                            TextView qtn = inner_qtn_lyt.findViewById(R.id.c_qutnn);
                            ZoomInImageView q_img = inner_qtn_lyt.findViewById(R.id.q_img);
                            RadioButton r1 = inner_qtn_lyt.findViewById(R.id.radioButton);
                            Phy_Opt[i][0] = r1;
                            RadioButton r2 = inner_qtn_lyt.findViewById(R.id.radioButton1);
                            Phy_Opt[i][1] = r2;
                            RadioButton r3 = inner_qtn_lyt.findViewById(R.id.radioButton2);
                            Phy_Opt[i][2] = r3;
                            RadioButton r4 = inner_qtn_lyt.findViewById(R.id.radioButton3);
                            Phy_Opt[i][3] = r4;
                            RadioButton r5 = inner_qtn_lyt.findViewById(R.id.answer);
                            Phy_Opt[i][4] = r5;

                            qtn_no.setVisibility(View.VISIBLE);


                            qtn_no.setText(documentSnapshot.getId() + ".");
                            String qquestion = documentSnapshot.getData().get("Question").toString();
                            Glide.with(quiz.this)
                                    .load(qquestion)
                                    .into(q_img);

                            r5.setText(documentSnapshot.getData().get("Answer").toString());
                            r1.setText(documentSnapshot.getData().get("Option1").toString() + " ");
                            r2.setText(documentSnapshot.getData().get("Option2").toString() + " ");
                            r3.setText(documentSnapshot.getData().get("Option3").toString() + " ");
                            r4.setText(documentSnapshot.getData().get("Option4").toString() + " ");
                            i++;
                        }
                        Phy_Questions = i; // stores total questions in physics
                    }
                });


        qtn_lyttC = findViewById(R.id.qtn_lyttC);
        db.collection("CHEMISTRY").orderBy("Question_no").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots1) {

                        int i = 0;

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots1) {


                            inner_qtn_lyt = getLayoutInflater().inflate(R.layout.qtn_lyt, null, false);
                            qtn_lyttC.addView(inner_qtn_lyt);
                            TextView qtn_no = inner_qtn_lyt.findViewById(R.id.c_qutnn_no);
                            ZoomInImageView q_img = inner_qtn_lyt.findViewById(R.id.q_img);
                            RadioButton r1 = inner_qtn_lyt.findViewById(R.id.radioButton);
                            Che_Opt[i][0] = r1;
                            RadioButton r2 = inner_qtn_lyt.findViewById(R.id.radioButton1);
                            Che_Opt[i][1] = r2;
                            RadioButton r3 = inner_qtn_lyt.findViewById(R.id.radioButton2);
                            Che_Opt[i][2] = r3;
                            RadioButton r4 = inner_qtn_lyt.findViewById(R.id.radioButton3);
                            Che_Opt[i][3] = r4;
                            RadioButton r5 = inner_qtn_lyt.findViewById(R.id.answer);
                            Che_Opt[i][4] = r5;

                            qtn_no.setVisibility(View.VISIBLE);
                            qtn_no.setText(documentSnapshot.getId() + ".");

                            String qquestion = documentSnapshot.getData().get("Question").toString();
                            Glide.with(quiz.this)
                                    .load(qquestion)
                                    .into(q_img);

                            r5.setText(documentSnapshot.getData().get("Answer").toString());
                            r1.setText(documentSnapshot.getData().get("Option1").toString() + " ");
                            r2.setText(documentSnapshot.getData().get("Option2").toString() + " ");
                            r3.setText(documentSnapshot.getData().get("Option3").toString() + " ");
                            r4.setText(documentSnapshot.getData().get("Option4").toString() + " ");

                            i++;

                        }
                        Che_Questions = i;


                    }
                });


        qtn_lyttM = findViewById(R.id.qtn_lyttM);
        db.collection("MATHEMATICS").orderBy("Question_no").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots1) {

                        int i = 0;

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots1) {


                            inner_qtn_lyt = getLayoutInflater().inflate(R.layout.qtn_lyt, null, false);
                            qtn_lyttM.addView(inner_qtn_lyt);
                            TextView qtn_no = inner_qtn_lyt.findViewById(R.id.c_qutnn_no);
                            ZoomInImageView q_img = inner_qtn_lyt.findViewById(R.id.q_img);
                            RadioButton r1 = inner_qtn_lyt.findViewById(R.id.radioButton);
                            Mat_Opt[i][0] = r1;
                            RadioButton r2 = inner_qtn_lyt.findViewById(R.id.radioButton1);
                            Mat_Opt[i][1] = r2;
                            RadioButton r3 = inner_qtn_lyt.findViewById(R.id.radioButton2);
                            Mat_Opt[i][2] = r3;
                            RadioButton r4 = inner_qtn_lyt.findViewById(R.id.radioButton3);
                            Mat_Opt[i][3] = r4;
                            RadioButton r5 = inner_qtn_lyt.findViewById(R.id.answer);
                            Mat_Opt[i][4] = r5;

                            qtn_no.setVisibility(View.VISIBLE);
                            qtn_no.setText(documentSnapshot.getId() + ".");

                            String qquestion = documentSnapshot.getData().get("Question").toString();
                            Glide.with(quiz.this)
                                    .load(qquestion)
                                    .into(q_img);


                            r5.setText(documentSnapshot.getData().get("Answer").toString());
                            r1.setText(documentSnapshot.getData().get("Option1").toString() + " ");
                            r2.setText(documentSnapshot.getData().get("Option2").toString() + " ");
                            r3.setText(documentSnapshot.getData().get("Option3").toString() + " ");
                            r4.setText(documentSnapshot.getData().get("Option4").toString() + " ");

                            i++;

                        }
                        Mat_Questions = i;

                    }
                });


        // login() is in try block because parse function of SimpleDateFormat is used in it
        try {
            login();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        CardView Submit = findViewById(R.id.submit);
        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(quiz.this);
                if (storage_permission) {
                    builder.setMessage("Have you attended ALL THE SUBJECTS, Are you sure you want to SUBMIT?" +
                            "\n\nif YES: your response will be stored in Responses.pdf");
                } else {
                    builder.setMessage("Have you attended ALL THE SUBJECTS, Are you sure you want to SUBMIT?");
                }
                builder.setCancelable(false)
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                validate_quiz();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // to hide navigation bar during quiz
                                        View quizPage = findViewById(R.id.quiz);
                                        quizPage.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                                    }
                                }, 5000);
                            }
                        });
                final AlertDialog dialog = builder.create();

                //show dialog box
                dialog.show();

            }
        });


    }


    private void validate_quiz() {

        for (i = 0; i < 3; i++)
            Result[i] = 0;

        for (i = 0; i < 3; i++)
            for (j = 0; j < Phy_Questions + Che_Questions + Mat_Questions; j++)
                Responses[i][j] = "not answered";

        for (i = 0; i < Phy_Questions; i++) {
            for (j = 0; j < 4; j++) {
                //ans = j;
                if (Phy_Opt[i][j].isChecked()) {

                    Responses[0][i] = Phy_Opt[i][j].getText().toString();
                    answer = Phy_Opt[i][4].getText().toString().substring(7);

                    if (Integer.parseInt(answer) == j + 1)
                        Result[0] += 1;
                }
            }
        }


        for (i = 0; i < Che_Questions; i++) {
            for (j = 0; j < 4; j++) {
                if (Che_Opt[i][j].isChecked()) {

                    Responses[1][i] = Che_Opt[i][j].getText().toString();
                    answer = Che_Opt[i][4].getText().toString().substring(7);

                    if (Integer.parseInt(answer) == j + 1)
                        Result[1] += 1;
                }
            }
        }


        for (i = 0; i < Mat_Questions; i++) {
            for (j = 0; j < 4; j++) {
                if (Mat_Opt[i][j].isChecked()) {

                    Responses[2][i] = Mat_Opt[i][j].getText().toString();
                    answer = Mat_Opt[i][4].getText().toString().substring(7);

                    if (Integer.parseInt(answer) == j + 1)
                        Result[2] += 1;
                }
            }
        }

        result();
    }

    private void login() throws ParseException {

        login = findViewById(R.id.login);
        login.setVisibility(View.VISIBLE);


        final ProgressBar l_progress = findViewById(R.id.l_progress);
        Button go = findViewById(R.id.go);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                l_phone = findViewById(R.id.l_phone);
                l_password = findViewById(R.id.l_password);

                int valid = 1;

                s_phone = l_phone.getText().toString();
                final String s_password = l_password.getText().toString();

                if (s_phone.isEmpty()) {
                    valid = 0;
                    l_phone.setError("No empty fields are allowed");
                } else
                    l_phone.setError(null);

                if (s_password.isEmpty()) {
                    valid = 0;
                    l_password.setError("No empty fields are allowed");
                } else
                    l_password.setError(null);


                if (valid == 1) {

                    db.collection("Users").document(s_phone).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {

                                    if (document.get("Password").toString().equals(s_password)) {

                                        l_progress.setVisibility(View.VISIBLE);
                                        login.setVisibility(View.GONE);

                                        l_phone.setText("");
                                        l_password.setText("");
                                        l_progress.setVisibility(View.GONE);


                                        final LinearLayout start_quiz = findViewById(R.id.start_quiz);
                                        start_quiz.setVisibility(View.VISIBLE);
                                        Toast.makeText(quiz.this, "Welcome", Toast.LENGTH_SHORT).show();

                                        TextView rules = findViewById(R.id.rules);
                                        rules.setText(Html.fromHtml("<u><b><strong>INSTRUCTIONS:</strong></b></u> <br> &nbsp &nbsp 1. Once you press START QUIZ the quiz starts with timer of time remaining to end quiz<br>" +
                                                "&nbsp &nbsp 2. Once your Quiz starts you SHOULD NOT PRESS HOME, BACK, MENU, POWER BUTTONS <br>" +
                                                "&nbsp &nbsp 3. If you press any of the above mentioned buttons or if you come out of the app QUIZ SUBMITS AUTOMATICALLY <br>" +
                                                "&nbsp &nbsp 4. Press SUBMIT button after attending ALL SUBJECTS <br>" +
                                                "&nbsp &nbsp 5. Check all the questions, If any of the questions not loaded, please sign in again"));

                                        CardView SQuiz = findViewById(R.id.SQuiz);
                                        SQuiz.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                try {
                                                    if (Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME) ==1 && Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME_ZONE) == 1) {
                                                        // Enabled
                                                        final Calendar calendar = Calendar.getInstance();
                                                        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");


                                                        db.collection("Timestamp").document("time").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                                if (task.isSuccessful()) {
                                                                    DocumentSnapshot doc = task.getResult();
                                                                    if (doc.exists()) {
                                                                        String startt = doc.get("start").toString();
                                                                        stop = doc.get("stop").toString();
                                                                        try {
                                                                            if (calendar.getTime().compareTo(simpleDateFormat.parse(startt)) >= 0 && calendar.getTime().compareTo(simpleDateFormat.parse(stop)) < 0) {
                                                                                mTimeLeftInMillis = simpleDateFormat.parse(stop).getTime() - simpleDateFormat.parse(simpleDateFormat.format(calendar.getTime())).getTime() + 60000;
                                                                                start_quiz.setVisibility(View.GONE);
                                                                                LinearLayout quiz = findViewById(R.id.quiz);
                                                                                quiz.setVisibility(View.VISIBLE);
                                                                                startTimer();
                                                                                start = true;

                                                                            } else {
                                                                                Toast.makeText(quiz.this, "you came before schedulded time", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        } catch (ParseException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        });

                                                    } else {
                                                        // Disabed
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(quiz.this);
                                                        builder.setMessage("Set to Automatic date, time and time zone");
                                                        builder.setCancelable(false)
                                                                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                                        startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
                                                                    }
                                                                });
                                                        final AlertDialog dialog = builder.create();

                                                        //show dialog box
                                                        dialog.show();
                                                    }
                                                } catch (Settings.SettingNotFoundException e) {
                                                    e.printStackTrace();
                                                }


                                            }
                                        });
                                    } else
                                        Toast.makeText(quiz.this, "Phone number or Password is incorrect", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(quiz.this, "No such user", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(quiz.this, "" + task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }

        });

        Button b_signup = findViewById(R.id.b_signup);
        b_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                login.setVisibility(View.GONE);
                L_sign_up = findViewById(R.id.L_sign_up);
                L_sign_up.setVisibility(View.VISIBLE);

                Name = findViewById(R.id.name);
                Email = findViewById(R.id.email);
                Sem = findViewById(R.id.sem);
                Phone = findViewById(R.id.phone);
                College = findViewById(R.id.college);
                Place = findViewById(R.id.place);
                Password = findViewById(R.id.password);
                C_Password = findViewById(R.id.c_password);

                Name.setEnabled(true);
                Email.setEnabled(true);
                Sem.setEnabled(true);
                Phone.setEnabled(true);
                College.setEnabled(true);
                Place.setEnabled(true);
                Password.setEnabled(true);
                C_Password.setEnabled(true);

                s_progress = findViewById(R.id.s_progress);
                s_progress.setVisibility(View.GONE);
                in_signup = true;

                reg = findViewById(R.id.register);
                reg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (reg.getText().toString().equals("VERIFY")) {
                            Name = findViewById(R.id.name);
                            Email = findViewById(R.id.email);
                            String name = Name.getText().toString().trim();
                            final String email = Email.getText().toString().trim();
                            Sem = findViewById(R.id.sem);
                            String sem = Sem.getText().toString().trim();
                            Phone = findViewById(R.id.phone);
                            phone = Phone.getText().toString().trim();
                            College = findViewById(R.id.college);
                            String college = College.getText().toString().trim();
                            Place = findViewById(R.id.place);
                            String place = Place.getText().toString().trim();
                            Password = findViewById(R.id.password);
                            String password = Password.getText().toString().trim();
                            C_Password = findViewById(R.id.c_password);
                            String c_password = C_Password.getText().toString().trim();

                            int valid = 1;

                            if (name.isEmpty()) {
                                valid = 0;
                                Name.setError("No empty fields are allowed");
                            } else
                                Name.setError(null);

                            if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                valid = 0;
                                Email.setError("Enter valid email");
                            } else
                                Email.setError(null);

                            if (sem.isEmpty()) {
                                valid = 0;
                                Sem.setError("No empty fields are allowed");
                            } else
                                Sem.setError(null);

                            if (phone.isEmpty()) {
                                valid = 0;
                                Phone.setError("No empty fields are allowed");
                            } else if (phone.length() != 10) {
                                valid = 0;
                                Phone.setError("Invalid Phone number");
                            } else
                                Phone.setError(null);

                            if (college.isEmpty()) {
                                valid = 0;
                                College.setError("No empty fields are allowed");
                            } else
                                College.setError(null);

                            if (place.isEmpty()) {
                                valid = 0;
                                Place.setError("No empty fields are allowed");
                            } else
                                Place.setError(null);

                            if (password.isEmpty()) {
                                valid = 0;
                                Password.setError("No empty fields are allowed");
                            } else if (password.length() < 6) {
                                valid = 0;
                                Password.setError("Minimum password length must be 6 charcaters");
                            } else if (true) {
                                for (i = 0; i < password.length(); i++) {
                                    if (password.charAt(i) == ' ') {
                                        valid = 0;
                                        Password.setError("White spaces are not allowed");
                                        break;
                                    }
                                }
                            } else
                                Password.setError(null);


                            if (c_password.isEmpty()) {
                                valid = 0;
                                C_Password.setError("No empty fields are allowed");
                            } else {
                                C_Password.setError(null);

                                if (password.equals(c_password)) {
                                    if (valid == 1) {
                                        s_progress.setVisibility(View.VISIBLE);


                                        user = new HashMap<>();
                                        user.put("Name", name);
                                        user.put("Email", email);
                                        user.put("Sem", sem);
                                        user.put("Phone", phone);
                                        user.put("College", college);
                                        user.put("Place", place);
                                        user.put("Password", password);
                                        
                                        // function call for phone authentication
                                        sendVerificationCodeToUser(phone);
                                    }


                                } else
                                    C_Password.setError("Enter Correct Password");
                            }
                        } else {
                            s_progress.setVisibility(View.VISIBLE);
                            Phone_Otp = findViewById(R.id.phone_otp);
                            String otp = Phone_Otp.getText().toString();
                            if(!otp.isEmpty()) {
                                verifyCode(otp);
                            }
                            else{
                                Phone_Otp.setError("Enter otp");
                                s_progress.setVisibility(View.GONE);
                            }
                        }
                    }
                });

                TextView have_acc = findViewById(R.id.have_acc);
                have_acc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        L_sign_up.setVisibility(View.GONE);
                        in_signup = false;
                        login.setVisibility(View.VISIBLE);

                        l_phone = findViewById(R.id.l_phone);
                        l_password = findViewById(R.id.l_password);

                        Name = findViewById(R.id.name);
                        Email = findViewById(R.id.email);
                        Sem = findViewById(R.id.sem);
                        Phone = findViewById(R.id.phone);
                        College = findViewById(R.id.college);
                        Place = findViewById(R.id.place);
                        Phone_Otp = findViewById(R.id.phone_otp);
                        Password = findViewById(R.id.password);
                        C_Password = findViewById(R.id.c_password);

                        Name.setText("");
                        Name.setError(null);
                        College.setText("");
                        College.setError(null);
                        Place.setText("");
                        Place.setError(null);
                        Email.setText("");
                        Email.setError(null);
                        Sem.setText("");
                        Sem.setError(null);
                        Phone.setText("");
                        Phone.setError(null);
                        Phone_Otp.setError(null);
                        Phone_Otp.setText("");
                        Phone_Otp.setVisibility(View.GONE);
                        reg.setText("VERIFY");
                        Password.setText("");
                        Password.setError(null);
                        C_Password.setText("");
                        C_Password.setError(null);

                        l_phone.setText("");
                        l_phone.setError(null);
                        l_password.setText("");
                        l_password.setError(null);
                    }
                });

            }
        });
    }

    // phone number authentication starts
    private void sendVerificationCodeToUser(String phone) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + phone,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,   // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    codeByFirbase = s;

                    Name = findViewById(R.id.name);
                    College = findViewById(R.id.college);
                    Place = findViewById(R.id.place);
                    Email = findViewById(R.id.email);
                    Sem = findViewById(R.id.sem);
                    Phone = findViewById(R.id.phone);
                    Password = findViewById(R.id.password);
                    C_Password = findViewById(R.id.c_password);

                    Phone_Otp = findViewById(R.id.phone_otp);
                    Phone_Otp.setVisibility(View.VISIBLE);
                    s_progress.setVisibility(View.GONE);
                    reg.setText("REGISTER");

                    Name.setEnabled(false);
                    College.setEnabled(false);
                    Place.setEnabled(false);
                    Email.setEnabled(false);
                    Sem.setEnabled(false);
                    Phone.setEnabled(false);
                    Password.setEnabled(false);
                    C_Password.setEnabled(false);


                }

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    String code = phoneAuthCredential.getSmsCode();
                    /*if (code != null) {
                        TextInputEditText phone_otp = findViewById(R.id.phone_otp);
                        phone_otp.setVisibility(View.VISIBLE);
                        phone_otp.setText(code);
                    }*/
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(quiz.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    s_progress.setVisibility(View.GONE);
                }
            };

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeByFirbase, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            db.collection("Users").document(phone).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(quiz.this, "User Created", Toast.LENGTH_SHORT).show();
                                    s_progress.setVisibility(View.GONE);
                                    L_sign_up.setVisibility(View.GONE);
                                    in_signup = false;
                                    login.setVisibility(View.VISIBLE);

                                    Name.setText("");
                                    College.setText("");
                                    Place.setText("");
                                    Email.setText("");
                                    Sem.setText("");
                                    Phone_Otp.setText("");
                                    Phone_Otp.setVisibility(View.GONE);
                                    reg.setText("VERIFY");
                                    Phone.setText("");
                                    Password.setText("");
                                    C_Password.setText("");

                                    l_phone = findViewById(R.id.l_phone);
                                    l_password = findViewById(R.id.l_password);
                                    l_phone.setText("");
                                    l_phone.setError(null);
                                    l_password.setText("");
                                    l_password.setError(null);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(quiz.this, "User not Created " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    s_progress.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(quiz.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                                s_progress.setVisibility(View.GONE);
                            }
                        }
                    }
                });
    }
    // phone number authentication ends


    private void startTimer() throws ParseException {
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
            }
        }.start();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (calendar.getTime().compareTo(simpleDateFormat.parse(stop)) == 0) {
            Toast.makeText(quiz.this, "your quiz is submitted", Toast.LENGTH_SHORT).show();
            validate_quiz();
        }
    }

    private void updateCountDownText() {

        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;
        int minutes = (int) ((mTimeLeftInMillis / (1000 * 60)) % 60);
        int hours = (int) ((mTimeLeftInMillis / (1000 * 60 * 60)) % 24);


        if (hours == 0 && minutes <= 4 && seconds <= 60) {
            mTextViewCountDown.setTextColor(Color.RED);
        }
        if (hours == 0 && minutes == 0 && seconds == 0) {
            validate_quiz();
        }
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        mTextViewCountDown.setText(timeLeftFormatted);
    }


    private void result() {


        Map<String, Object> resultt = new HashMap<>();
        resultt.put("PHYSICS", Result[0]);
        resultt.put("CHEMISTRY", Result[1]);
        resultt.put("MATHEMATICS", Result[2]);
        resultt.put("TOTAL", Result[0] + Result[2] + Result[2]);
        db.collection("Result").document(s_phone).set(resultt);

        createPDF();

        startActivity(new Intent(quiz.this, Result.class));
        finish();
    }

    private void createPDF() {

        PdfDocument myPdfDocument = new PdfDocument();
        Paint myPaint = new Paint();

        PdfDocument.PageInfo myPageInfo1 = new PdfDocument.PageInfo.Builder(400, 3 * 20 + (10 + quiz.Phy_Questions + quiz.Che_Questions + quiz.Mat_Questions) * 20, 1).create();
        PdfDocument.Page myPage1 = myPdfDocument.startPage(myPageInfo1);
        Canvas canvas = myPage1.getCanvas();

        int y_axis = 50;
        int questions = 0;

        //canvas.drawText("Responses",200,10,myPaint);
        for (int i = 0; i < 3; i++) {
            y_axis += 10;
            if (i == 0) {
                canvas.drawText("Physics:", 20, y_axis, myPaint);
                questions = quiz.Phy_Questions;
            } else if (i == 1) {
                canvas.drawText("Chemistry:", 20, y_axis, myPaint);
                questions = quiz.Che_Questions;
            } else {
                canvas.drawText("Mathematics:", 20, y_axis, myPaint);
                questions = quiz.Mat_Questions;
            }
            y_axis += 20;

            for (int j = 0; j < questions; j++) {
                canvas.drawText(String.valueOf(j + 1) + ". " + quiz.Responses[i][j], 30, y_axis, myPaint);
                y_axis += 20;
            }
        }

        myPdfDocument.finishPage(myPage1);

        File file = new File(Environment.getExternalStorageDirectory(), "/Responses.pdf");

        try {
            myPdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(quiz.this, "Your quiz is submitted and response are stored in Responses.pdf", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        myPdfDocument.close();
    }

    @Override
    public void onBackPressed() {

        if (in_signup) {
            L_sign_up.setVisibility(View.GONE);
            in_signup = false;
            login.setVisibility(View.VISIBLE);

            l_phone = findViewById(R.id.l_phone);
            l_password = findViewById(R.id.l_password);

            Name = findViewById(R.id.name);
            College = findViewById(R.id.college);
            Place = findViewById(R.id.place);
            Email = findViewById(R.id.email);
            Sem = findViewById(R.id.sem);
            Phone = findViewById(R.id.phone);
            Phone_Otp = findViewById(R.id.phone_otp);
            Password = findViewById(R.id.password);
            C_Password = findViewById(R.id.c_password);

            Name.setText("");
            Name.setError(null);
            College.setText("");
            College.setError(null);
            Place.setText("");
            Place.setError(null);
            Email.setText("");
            Email.setError(null);
            Sem.setText("");
            Sem.setError(null);
            Phone.setText("");
            Phone.setError(null);
            Phone_Otp.setError(null);
            Phone_Otp.setText("");
            Phone_Otp.setVisibility(View.GONE);
            reg.setText("VERIFY");
            Password.setText("");
            Password.setError(null);
            C_Password.setText("");
            C_Password.setError(null);

            l_phone.setText("");
            l_phone.setError(null);
            l_password.setText("");
            l_password.setError(null);
        } else if (!start) {
            if (back)
                super.onBackPressed();
            else
                showDialog();
        } else {
        }

    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(quiz.this);
        builder.setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        back = true;
                        onBackPressed();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        final AlertDialog dialog = builder.create();

        //show dialog box
        dialog.show();

    }


    @Override
    protected void onPause() {
        super.onPause();
        if (start && call) {
            if (!storage_permission)
                Toast.makeText(this, "your quiz is submitted", Toast.LENGTH_LONG).show();
            validate_quiz();
            /*Toast.makeText(quiz.this,"comback to quiz within 5 seconds",Toast.LENGTH_SHORT).show();
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    // Do what ever you want
                    if (!storage_permission)
                        Toast.makeText(quiz.this, "your quiz is submitted", Toast.LENGTH_LONG).show();
                    validate_quiz();
                }
            };
            handler.postDelayed(runnable, 5000);*/
        }

        call = true;


    }


    @Override
    protected void onStart() {
        super.onStart();
        /*if(start)
            handler.removeCallbacks(runnable);*/


        if (start) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // to hide navigation bar during quiz
                    View quizPage = findViewById(R.id.quiz);
                    quizPage.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
            }, 5000);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull final int[] grantResults) {

        // not checked request code because there only one permission requested in this activity(referenced)
        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED)
                Toast.makeText(quiz.this, "make sure no phone call comes during test", Toast.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (grantResults[1] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(quiz.this, "your test responses will not be downloded", Toast.LENGTH_LONG).show();
                        storage_permission = false;
                    }
                }
            }, 3500);

        }
    }
}

