package com.example.Phase3;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class activity_login extends AppCompatActivity {
    EditText etEmail, etPassword;
    Button login;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editTextPassword);
        login = findViewById(R.id.buttonLogin);

        login.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            Response.Listener<String> responseListener = response -> {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    String type = jsonResponse.getString("user_type");
                    if (success) {
                        Intent intent;
                        if(type.equals("student")) {
                            intent = new Intent(activity_login.this, student_profile.class);
                        }
                        else if(type.equals("instructor")){
                            intent = new Intent(activity_login.this, instructor_profile.class);
                        }
                        else{//admin
                            intent = new Intent(activity_login.this, admin_profile.class);
                        }
                        intent.putExtra("email", email);
                        activity_login.this.startActivity(intent);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity_login.this);
                        builder.setMessage("Login Failed").setNegativeButton("Retry", null).create().show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            };
            LoginRequest loginRequest = new LoginRequest(email, password, getString(R.string.url) + "processlogin.php", responseListener);
            RequestQueue queue = Volley.newRequestQueue(activity_login.this);
            queue.add(loginRequest);
        });
        TextView textViewSignUp = findViewById(R.id.textViewSignUp);
        textViewSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(activity_login.this, activity_sign_up.class);
            startActivity(intent);
        });
    }
}
