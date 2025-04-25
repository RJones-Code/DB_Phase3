package com.example.Phase3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.Response;
import com.android.volley.Request;
import org.json.JSONException;
import org.json.JSONObject;
import android.view.View;

import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
public class instructor_profile extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);
    }
}
