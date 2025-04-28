package com.example.Phase3;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class add_parent_activity extends AppCompatActivity {

    private String studentEmail;
    EditText etParentEmail, etParentPhone, etParentPassword;
    Button btnSubmitParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_parent);

        etParentEmail = findViewById(R.id.etParentEmail);
        etParentPhone = findViewById(R.id.etParentPhone);
        etParentPassword = findViewById(R.id.etParentPassword);
        btnSubmitParent = findViewById(R.id.btnSubmitParent);

        // Get email from login
        studentEmail = getIntent().getStringExtra("email");

        btnSubmitParent.setOnClickListener(v -> {
            String parentEmail = etParentEmail.getText().toString();
            String parentPhone = etParentPhone.getText().toString();
            String parentPassword = etParentPassword.getText().toString();

            if (parentEmail.isEmpty() || parentPhone.isEmpty() || parentPassword.isEmpty()) {
                Toast.makeText(add_parent_activity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                Response.Listener<String> responseListener = response -> {
                    if (response.contains("\"success\":true")) {
                        Toast.makeText(add_parent_activity.this, "Parent added successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // Close form after success
                    } else {
                        Toast.makeText(add_parent_activity.this, "Failed to add parent.", Toast.LENGTH_SHORT).show();
                    }
                };

                AddParentRequest addParentRequest = new AddParentRequest(
                        parentEmail, parentPhone, parentPassword, studentEmail,
                        getString(R.string.url) + "addparent.php", responseListener);

                RequestQueue queue = Volley.newRequestQueue(add_parent_activity.this);
                queue.add(addParentRequest);
            }
        });
    }

    private static class AddParentRequest extends StringRequest {
        private final Map<String, String> params;

        public AddParentRequest(String parentEmail, String parentPhone, String parentPassword, String studentEmail, String url, Response.Listener<String> listener) {
            super(Request.Method.POST, url, listener, error -> {
                error.printStackTrace();
            });
            params = new HashMap<>();
            params.put("parent_email", parentEmail);
            params.put("parent_phone", parentPhone);
            params.put("parent_password", parentPassword);
            params.put("student_email", studentEmail);
        }

        @Override
        protected Map<String, String> getParams() {
            return params;
        }
    }
}
