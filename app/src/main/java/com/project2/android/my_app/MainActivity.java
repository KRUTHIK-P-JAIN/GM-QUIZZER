package com.project2.android.my_app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


    Button splashbtn = findViewById(R.id.splash);
    splashbtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        check_permissions();
      }
    });
  }

  private void check_permissions() {

    Intent intent = null;
    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED )
    {
      intent = new Intent(MainActivity.this, app_permission.class);
    }
    else intent = new Intent(MainActivity.this, quiz.class);

    startActivity(intent);
    finish();
  }


}
