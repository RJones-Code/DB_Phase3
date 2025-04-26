package com.example.Phase3;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

public class activity_sign_up extends AppCompatActivity {
    EditText editTextStudentID, editTextEmail, editTextPassword;
    Button buttonSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextStudentID = findViewById(R.id.editTextStudentID);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        TextView textViewBackToWelcome = findViewById(R.id.textViewBackToWelcome);
        textViewBackToWelcome.setOnClickListener(v -> {
            Intent intent = new Intent(activity_sign_up.this, activity_welcome.class);
            startActivity(intent);
        });

        buttonSubmit.setOnClickListener(v -> {
            String studentID = editTextStudentID.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            Response.Listener<String> responseListener = response -> {
                if (response.contains("Error")) {
                    Toast.makeText(activity_sign_up.this, response, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity_sign_up.this, "Account created successfully!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(activity_sign_up.this, activity_login.class);
                    startActivity(intent);
                }
            };

            SignUpRequest request = new SignUpRequest(studentID, email, password, getString(R.string.url) + "processsignup.php", responseListener);
            RequestQueue queue = Volley.newRequestQueue(activity_sign_up.this);
            queue.add(request);
        });
    }
}