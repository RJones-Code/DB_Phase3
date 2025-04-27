package com.example.Phase3;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.content.Intent;


import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class activity_admin_create_alert extends AppCompatActivity {

    EditText etStudentID, etAlertMessage;
    Spinner spinnerAlertType;
    Button btnCreateAlert, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_create_alert);

        etStudentID = findViewById(R.id.etStudentID);
        etAlertMessage = findViewById(R.id.etAlertMessage);
        spinnerAlertType = findViewById(R.id.spinnerAlertType);
        btnCreateAlert = findViewById(R.id.btnCreateAlert);
        btnLogout = findViewById(R.id.btnLogout);

        // Set up spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.alert_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAlertType.setAdapter(adapter);

        btnCreateAlert.setOnClickListener(v -> {
            String studentId = etStudentID.getText().toString();
            String alertType = spinnerAlertType.getSelectedItem().toString();
            String alertMessage = etAlertMessage.getText().toString();

            Response.Listener<String> responseListener = response -> {
                if (response.contains("\"success\":true")) {
                    Toast.makeText(activity_admin_create_alert.this, "Alert Created Successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity_admin_create_alert.this, "Failed to Create Alert.", Toast.LENGTH_SHORT).show();
                }
            };

            CreateAlertRequest createAlertRequest = new CreateAlertRequest(
                    studentId, alertType, alertMessage, getString(R.string.url) + "createalert.php", responseListener);

            RequestQueue queue = Volley.newRequestQueue(activity_admin_create_alert.this);
            queue.add(createAlertRequest);
        });

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(activity_admin_create_alert.this, activity_login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

    }

    // 👉 Nested Inner Class: CreateAlertRequest
    private static class CreateAlertRequest extends StringRequest {
        private final Map<String, String> params;

        public CreateAlertRequest(String studentId, String alertType, String alertMessage, String url, Response.Listener<String> listener) {
            super(Request.Method.POST, url, listener, null);
            params = new HashMap<>();
            params.put("student_id", studentId);
            params.put("alert_type", alertType);
            params.put("alert", alertMessage);
        }

        @Override
        protected Map<String, String> getParams() {
            return params;
        }
    }
}
