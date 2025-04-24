package com.example.Phase3;

import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class activity_sign_up extends AppCompatActivity {
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        TextView textViewBackToWelcome = findViewById(R.id.textViewBackToWelcome);
        textViewBackToWelcome.setOnClickListener(v -> {
            Intent intent = new Intent(activity_sign_up.this, activity_welcome.class);
            startActivity(intent);
        });
    }
}