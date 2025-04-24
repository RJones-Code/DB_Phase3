package com.example.Phase3;

//import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
import android.widget.Button;
//import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.toolbox.Volley;

//import org.json.JSONException;
//import org.json.JSONObject;

public class activity_welcome extends AppCompatActivity {
    Button login;
    Button signUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome); // or whatever layout you're using
        login = (Button) findViewById(R.id.buttonLogin);
        signUp = (Button) findViewById(R.id.buttonSignUp);
        login.setOnClickListener(view -> {
            Intent intent = new Intent(activity_welcome.this, activity_login.class);
            startActivity(intent);
        });
        signUp.setOnClickListener(view -> {
            Intent intent = new Intent(activity_welcome.this, activity_sign_up.class);
            startActivity(intent);
        });
    }
}
