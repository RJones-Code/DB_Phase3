package com.example.Phase3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class activity_enroll_section extends AppCompatActivity {
    private static final String TAG = "EnrollSection";
    private String studentId;
    private String courseId;
    private LinearLayout sectionContainer;
    private Button logout;
    private Button btnBack;
    private String studentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_section);
        sectionContainer = findViewById(R.id.sectionListContainer);
        courseId = getIntent().getStringExtra("course_id");
        studentId = getIntent().getStringExtra("student_id");
        studentEmail = getIntent().getStringExtra("email");
        Log.d(TAG, "courseId: " + courseId + ", studentId: " + studentId);
        btnBack = findViewById(R.id.buttonBack);
        logout = findViewById(R.id.buttonLogout);

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(activity_enroll_section.this, activity_enroll_courses.class);
            intent.putExtra("email", studentEmail);
            startActivity(intent);
            finish();
        });

        logout.setOnClickListener(v ->
                startActivity(new Intent(this, activity_welcome.class)));

        fetchSections();
    }

    private void fetchSections() {
        new Thread(() -> {
            try {
                JSONObject postData = new JSONObject();
                postData.put("course_id", courseId);
                postData.put("student_id", studentId);

                HttpURLConnection conn = (HttpURLConnection) new URL(getString(R.string.url) + "enrollsection.php").openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postData.toString().getBytes());
                }

                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }

                runOnUiThread(() -> handleSectionsResponse(response.toString()));

            } catch (Exception e) {
                Log.e(TAG, "Fetch error: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to load sections", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void handleSectionsResponse(String response) {
        try {
            JSONObject json = new JSONObject(response);

            if (!json.has("success")) {
                throw new JSONException("Invalid response format");
            }

            if (!json.getBoolean("success")) {
                String error = json.optString("error", "Unknown error");
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                return;
            }

            if (!json.has("sections")) {
                throw new JSONException("Missing sections array");
            }

            JSONArray sections = json.getJSONArray("sections");
            for (int i = 0; i < sections.length(); i++) {
                JSONObject section = sections.getJSONObject(i);
                if (validateSection(section)) {
                    createSectionButton(section);
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSON error: " + e.getMessage());
            Toast.makeText(this, "Data format error", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateSection(JSONObject section) {
        try {
            return section.has("section_id") &&
                    section.has("semester") &&
                    section.has("year") &&
                    section.has("current_enrollment") &&
                    section.has("capacity");
        } catch (Exception e) {
            Log.e(TAG, "Invalid section: " + section.toString());
            return false;
        }
    }

    private void createSectionButton(JSONObject section) {
        try {
            Button btn = new Button(this);
            String text = String.format("Section %s\n%s %s (%d/%d)",
                    section.optString("section_id", "N/A"),
                    section.optString("semester", "Unknown"),
                    section.optString("year", "Unknown"),
                    section.optInt("current_enrollment", 0),
                    section.optInt("capacity", 0));

            btn.setText(text);
            btn.setOnClickListener(v -> attemptEnrollment(section));
            sectionContainer.addView(btn);
        } catch (Exception e) {
            Log.e(TAG, "Button creation failed: " + e.getMessage());
        }
    }

    private void attemptEnrollment(JSONObject section) {
        new Thread(() -> {
            try {
                JSONObject postData = new JSONObject();
                postData.put("course_id", courseId);
                postData.put("student_id", studentId);
                postData.put("section_id", section.optString("section_id"));
                postData.put("semester", section.optString("semester"));
                postData.put("year", section.optString("year"));

                HttpURLConnection conn = (HttpURLConnection) new URL(getString(R.string.url) + "enrollsection.php").openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postData.toString().getBytes());
                }

                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }

                processEnrollmentResult(response.toString());

            } catch (Exception e) {
                Log.e(TAG, "Enrollment error: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(this, "Enrollment failed", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void processEnrollmentResult(String response) {
        runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(response);
                boolean success = json.optBoolean("success", false);
                String message = json.optString("message", "Success");
                String error = json.optString("error", "Unknown error");

                if (success) {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Log.e(TAG, "Bad enrollment response: " + response);
                Toast.makeText(this, "Invalid server response", Toast.LENGTH_LONG).show();
            }
        });
    }
}