package com.example.Phase3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class activity_course_history extends AppCompatActivity {

    private LinearLayout courseListLayout;
    private String instructorId;
    private String instructorEmail;
    private String courseHistoryUrl;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_history);

        courseListLayout = findViewById(R.id.courseListLayout);
        backButton = findViewById(R.id.backButton);

        // Get instructor_id passed from previous activity
        instructorId = getIntent().getStringExtra("instructor_id");
        instructorEmail = getIntent().getStringExtra("email");

        if (instructorId == null || instructorId.isEmpty()) {
            Toast.makeText(this, "Missing instructor ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(activity_course_history.this, instructor_profile.class);
            intent.putExtra("instructor_id", instructorId); // Pass instructor_id back if needed
            intent.putExtra("email", instructorEmail);
            startActivity(intent);
            finish(); // Finish current activity so it doesn't stay in the backstack
        });

        // Build the full URL
        courseHistoryUrl = getString(R.string.url) + "courseHistory.php";

        // Start fetching course history in background
        fetchCourseHistory();
    }

    private void fetchCourseHistory() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = getCourseHistoryFromServer();
                runOnUiThread(() -> {
                    if (result == null) {
                        Toast.makeText(activity_course_history.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Handle JSON parsing and UI updates on the main thread
                    handleCourseHistoryResponse(result);
                });
            }
        }).start();
    }

    private String getCourseHistoryFromServer() {
        try {
            URL url = new URL(courseHistoryUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Add instructor_id to the HTTP request header
            conn.setRequestProperty("Instructor-ID", instructorId);

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                return response.toString();
            } else {
                Log.e("FetchCourseHistory", "Server returned error: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            Log.e("FetchCourseHistory", "Error: " + e.getMessage());
            return null;
        }
    }

    private void handleCourseHistoryResponse(String result) {
        try {
            // Parse the response string into a JSONArray
            JSONArray coursesArray = new JSONArray(result);

            if (coursesArray.length() == 0) {
                Toast.makeText(activity_course_history.this, "No courses found.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Iterate over each course and display it
            for (int i = 0; i < coursesArray.length(); i++) {
                // Get each course as a JSONObject
                JSONObject course = coursesArray.getJSONObject(i);

                // Extract course details
                String courseId = course.getString("course_id");
                String sectionId = course.getString("section_id");
                String semester = course.getString("semester");
                String year = course.getString("year");

                // Add the course entry
                addCourseEntry(courseId, sectionId, semester, year);
            }
        } catch (JSONException e) {
            Toast.makeText(activity_course_history.this, "Error parsing data", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void addCourseEntry(String courseId, String sectionId, String semester, String year) {
        LinearLayout entryLayout = new LinearLayout(this);
        entryLayout.setOrientation(LinearLayout.VERTICAL);
        entryLayout.setPadding(32, 32, 32, 32);

        TextView courseInfo = new TextView(this);
        courseInfo.setText("Course ID: " + courseId + "\nSection ID: " + sectionId + "\nSemester: " + semester + "\nYear: " + year);
        courseInfo.setTextSize(18);

        Button showStudentsButton = new Button(this);
        showStudentsButton.setText("Show Students");

        showStudentsButton.setOnClickListener(v -> {
            Intent intent = new Intent(activity_course_history.this, activity_student_list.class);
            intent.putExtra("course_id", courseId);
            intent.putExtra("section_id", sectionId);
            intent.putExtra("semester", semester);
            intent.putExtra("year", year);
            startActivity(intent);
        });

        entryLayout.addView(courseInfo);
        entryLayout.addView(showStudentsButton);

        courseListLayout.addView(entryLayout);
    }
}