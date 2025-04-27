package com.example.Phase3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class activity_student_list extends AppCompatActivity {

    private LinearLayout studentListLayout;
    private String courseId, sectionId, semester, year;
    private String studentListUrl;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        studentListLayout = findViewById(R.id.studentListLayout);
        backButton = findViewById(R.id.backButton);

        // Get course details passed from previous activity
        courseId = getIntent().getStringExtra("course_id");
        sectionId = getIntent().getStringExtra("section_id");
        semester = getIntent().getStringExtra("semester");
        year = getIntent().getStringExtra("year");

        if (courseId == null || sectionId == null || semester == null || year == null) {
            Toast.makeText(this, "Missing course information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backButton.setOnClickListener(v -> {
            finish(); // Go back to previous activity
        });

        // Build the URL
        studentListUrl = getString(R.string.url) + "showStudents.php";

        // Start fetching student list
        fetchStudentList();
    }

    private void fetchStudentList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = getStudentListFromServer();
                runOnUiThread(() -> {
                    if (result == null) {
                        Toast.makeText(activity_student_list.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    handleStudentListResponse(result);
                });
            }
        }).start();
    }

    private String getStudentListFromServer() {
        try {
            URL url = new URL(studentListUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Send course info as headers
            conn.setRequestProperty("Course-ID", courseId);
            conn.setRequestProperty("Section-ID", sectionId);
            conn.setRequestProperty("Semester", semester);
            conn.setRequestProperty("Year", year);

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
                Log.e("FetchStudentList", "Server returned error: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            Log.e("FetchStudentList", "Error: " + e.getMessage());
            return null;
        }
    }

    private void handleStudentListResponse(String result) {
        try {
            JSONArray studentsArray = new JSONArray(result);

            if (studentsArray.length() == 0) {
                Toast.makeText(this, "No students enrolled.", Toast.LENGTH_SHORT).show();
                return;
            }

            for (int i = 0; i < studentsArray.length(); i++) {
                JSONObject student = studentsArray.getJSONObject(i);

                String studentId = student.getString("student_id");
                String studentName = student.getString("name");
                String grade = student.optString("grade", "N/A");

                addStudentEntry(studentId, studentName, grade);
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Error parsing student data", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void addStudentEntry(String studentId, String studentName, String grade) {
        LinearLayout entryLayout = new LinearLayout(this);
        entryLayout.setOrientation(LinearLayout.VERTICAL);
        entryLayout.setPadding(32, 32, 32, 32);

        TextView studentInfo = new TextView(this);
        studentInfo.setText("Student ID: " + studentId + "\nName: " + studentName + "\nGrade: " + grade);
        studentInfo.setTextSize(18);

        entryLayout.addView(studentInfo);
        studentListLayout.addView(entryLayout);
    }
}