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

public class student_profile extends AppCompatActivity {
    private String studentEmail;

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
        LinearLayout alertsContainer = findViewById(R.id.alertsContainer);

        // Fetch student data
        fetchStudentData();

        // Button click handlers
        btnAddParent.setOnClickListener(v ->
                startActivity(new Intent(this, activity_welcome.class)));

        btnRegisterCourses.setOnClickListener(v ->
                startActivity(new Intent(this, activity_welcome.class)));

        btnViewTranscript.setOnClickListener(v ->
                startActivity(new Intent(this, activity_welcome.class)));
    }

    private void fetchStudentData() {
        Response.Listener<String> responseListener = response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                if (jsonResponse.getBoolean("success")) {
                    // Update welcome message
                    TextView welcomeText = findViewById(R.id.textViewWelcome);
                    welcomeText.setText("Welcome, " + jsonResponse.getString("name"));

                    // Populate alerts
                    LinearLayout alertsContainer = findViewById(R.id.alertsContainer);
                    alertsContainer.removeAllViews();
                    /*
                    if (jsonResponse.getJSONArray("alerts").length() > 0) {
                        for (int i = 0; i < jsonResponse.getJSONArray("alerts").length(); i++) {
                            JSONObject alert = jsonResponse.getJSONArray("alerts").getJSONObject(i);
                            View alertView = getLayoutInflater().inflate(
                                    R.layout.alert_item, alertsContainer, false);

                            TextView alertType = alertView.findViewById(R.id.alertType);
                            TextView alertMessage = alertView.findViewById(R.id.alertMessage);

                            alertType.setText(alert.getString("alert_type"));
                            alertMessage.setText(alert.getString("alert"));

                            alertsContainer.addView(alertView);
                        }
                    } else {
                        TextView noAlerts = new TextView(this);
                        noAlerts.setText("No alerts found");
                        alertsContainer.addView(noAlerts);
                    }
                     */
                } else {
                    Toast.makeText(this, "Error: " + jsonResponse.getString("error"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        };

        // Create custom request to send email
        StringRequest studentDataRequest = new StringRequest(Request.Method.POST, getString(R.string.url) + "studentprofile.php",
                responseListener, error -> Toast.makeText(this, "Request failed", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("mobile", "true");
                params.put("email", studentEmail);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(studentDataRequest);
    }
}