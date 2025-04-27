package com.example.Phase3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class activity_enroll_courses extends AppCompatActivity {

    private String studentId;
    private String studentEmail;
    private LinearLayout coursesContainer;
    private String courseUrl;
    private Button logout;
    private Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enroll_courses);

        studentEmail = getIntent().getStringExtra("email");
        coursesContainer = findViewById(R.id.courseListContainer);
        courseUrl = getString(R.string.url) + "enrollcourse.php";
        back = findViewById(R.id.buttonBack);
        logout = findViewById(R.id.buttonLogout);

        back.setOnClickListener(v -> {
            Intent intent = new Intent(activity_enroll_courses.this, student_profile.class);
            intent.putExtra("email", studentEmail);
            startActivity(intent);
            finish();
        });

        logout.setOnClickListener(v ->
                startActivity(new Intent(this, activity_welcome.class)));

        listCourses();
    }

    private void listCourses() {
        new Thread(() -> {
            String result = getCoursesFromServer();
            runOnUiThread(() -> {
                if (result == null) {
                    Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    return;
                }
                handleCourseResponse(result);
            });
        }).start();
    }

    private String getCoursesFromServer() {
        HttpURLConnection conn = null;
        OutputStream os = null;
        BufferedReader br = null;
        try {
            URL url = new URL(courseUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);


            JSONObject postData = new JSONObject();
            try {
                postData.put("email", studentEmail);
            } catch (JSONException e) {
                Log.e("JSON", "Error creating post data", e);
            }

            conn.setRequestProperty("Content-Type", "application/json");
            os = conn.getOutputStream();
            os.write(postData.toString().getBytes());
            os.flush();

            InputStream is = conn.getInputStream();
            try {
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Log.e("ENCODING", "UTF-8 not supported", e);
                return null;
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();

        } catch (Exception e) {
            Log.e("NETWORK", "Error fetching courses", e);
            return null;
        } finally {
            try {
                if (os != null) os.close();
                if (br != null) br.close();
                if (conn != null) conn.disconnect();
            } catch (Exception e) {
                Log.e("STREAM", "Error closing resources", e);
            }
        }
    }

    private void handleCourseResponse(String result) {
        try {
            JSONObject json = new JSONObject(result);

            if (!json.getBoolean("success")) {
                String error = json.optString("error", "Unknown error");
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                return;
            }
            studentId = json.getString("student_id");
            JSONArray courses = json.getJSONArray("courses");
            for (int i = 0; i < courses.length(); i++) {
                JSONObject course = courses.getJSONObject(i);
                final String courseId = course.getString("course_id");
                final String courseName = course.getString("course_name");
                String credits = course.getString("credits");


                Button btnCourse = new Button(this);
                btnCourse.setText(String.format("%s (%s) - %s credits", courseName, courseId, credits));
                btnCourse.setPadding(0, 16, 0, 16);

                btnCourse.setOnClickListener(v -> {
                    Intent intent = new Intent(activity_enroll_courses.this, activity_enroll_section.class);
                    intent.putExtra("course_id", courseId);
                    intent.putExtra("course_name", courseName);
                    intent.putExtra("student_id", studentId);
                    intent.putExtra("email", studentEmail);
                    startActivity(intent);
                });

                coursesContainer.addView(btnCourse);
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Data format error", Toast.LENGTH_SHORT).show();
            Log.e("JSON_PARSE", "Response: " + result, e);
        }
    }
}