package com.example.Phase3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class instructor_profile extends AppCompatActivity {
    private TextView welcomeTextView;
    private Button courseHistoryButton;
    private String instructorEmail;
    private String instructorId;  // Store instructor ID to pass later

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_dashboard);

        // Initialize views
        welcomeTextView = findViewById(R.id.welcomeTextView);
        courseHistoryButton = findViewById(R.id.courseHistoryButton);

        // Get email from previous screen (login)
        instructorEmail = getIntent().getStringExtra("email");

        if (instructorEmail == null || instructorEmail.isEmpty()) {
            Toast.makeText(this, "No instructor email provided", Toast.LENGTH_SHORT).show();
            finish();  // Close activity if no email
            return;
        }

        // Fetch instructor details
        fetchInstructorData();

        // Set click listener to go to course history
        courseHistoryButton.setOnClickListener(view -> {
            Intent intent = new Intent(instructor_profile.this, activity_course_history.class);
            intent.putExtra("email", instructorEmail);
            intent.putExtra("instructor_id", instructorId);  // Pass instructor ID also
            startActivity(intent);
        });
    }

    private void fetchInstructorData() {
        Response.Listener<String> responseListener = response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                if (jsonResponse.getBoolean("success")) {
                    // Get instructor's name and ID
                    String name = jsonResponse.getString("name");
                    instructorId = jsonResponse.getString("instructor_id"); // Store instructor ID
                    welcomeTextView.setText("Welcome Instructor " + name);
                } else {
                    Toast.makeText(this, "Error: " + jsonResponse.optString("error", "Unknown error"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error parsing server response", Toast.LENGTH_SHORT).show();
            }
        };

        String url = getString(R.string.url) + "instructorprofile.php";

        StringRequest instructorDataRequest = new StringRequest(Request.Method.POST, url,
                responseListener, error -> Toast.makeText(this, "Request failed", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", instructorEmail);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(instructorDataRequest);
    }
}
