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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.Response;
import com.android.volley.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.view.View;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;

public class student_profile extends AppCompatActivity {
    private String studentEmail;
    private LinearLayout alertsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // Get email from login
        studentEmail = getIntent().getStringExtra("email");

        // Initialize UI elements
        TextView welcomeText = findViewById(R.id.textViewWelcome);
        Button btnAddParent = findViewById(R.id.buttonAddParent);
        Button btnRegisterCourses = findViewById(R.id.buttonRegisterCourses);
        Button btnViewTranscript = findViewById(R.id.buttonViewTranscript);
        Button btnLogout = findViewById(R.id.buttonLogout);
        alertsContainer = findViewById(R.id.alertsContainer);

        // Fetch student data
        fetchStudentData();
        fetchAlerts(); // ✅ Fetch alerts separately

        // Button click handlers
        btnAddParent.setOnClickListener(v -> {
            Intent intent = new Intent(student_profile.this, add_parent_activity.class);
            intent.putExtra("email", studentEmail);
            startActivity(intent);
        });
        btnRegisterCourses.setOnClickListener(v -> {
            Intent intent = new Intent(student_profile.this, activity_enroll_courses.class);
            intent.putExtra("email", studentEmail);
            startActivity(intent);
        });
        btnViewTranscript.setOnClickListener(v -> {
            Intent intent = new Intent(student_profile.this, TranscriptActivity.class);
            intent.putExtra("email", studentEmail);
            startActivity(intent);
        });
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(student_profile.this, activity_welcome.class);
            startActivity(intent);
            finish();
        });
    }

    private void fetchStudentData() {
        Response.Listener<String> responseListener = response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                if (jsonResponse.getBoolean("success")) {
                    TextView welcomeText = findViewById(R.id.textViewWelcome);
                    welcomeText.setText("Welcome, " + jsonResponse.getString("name"));
                } else {
                    Toast.makeText(this, "Error: " + jsonResponse.getString("error"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        };

        StringRequest studentDataRequest = new StringRequest(Request.Method.POST, getString(R.string.url) + "studentprofile.php",
                responseListener, error -> Toast.makeText(this, "Request failed", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", studentEmail);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(studentDataRequest);
    }

    private void fetchAlerts() {
        String url = getString(R.string.url) + "fetchStudentAlerts.php?email=" + studentEmail;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    alertsContainer.removeAllViews();
                    try {
                        if (response.length() > 0) {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject alertObject = response.getJSONObject(i);

                                View alertView = getLayoutInflater().inflate(
                                        R.layout.alert_item, alertsContainer, false);

                                TextView alertType = alertView.findViewById(R.id.alertType);
                                TextView alertMessage = alertView.findViewById(R.id.alertMessage);

                                alertType.setText(alertObject.getString("alert_type"));
                                alertMessage.setText(alertObject.getString("alert"));

                                alertsContainer.addView(alertView);
                            }
                        } else {
                            TextView noAlerts = new TextView(this);
                            noAlerts.setText("No alerts found");
                            alertsContainer.addView(noAlerts);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(student_profile.this, "Error fetching alerts", Toast.LENGTH_SHORT).show();
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
