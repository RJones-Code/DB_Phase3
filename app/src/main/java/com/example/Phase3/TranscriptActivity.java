package com.example.Phase3;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.Response;
import com.android.volley.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;

public class TranscriptActivity extends AppCompatActivity {
    private String studentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcript);

        // Get student email passed from previous activity
        studentEmail = getIntent().getStringExtra("email");

        // Initialize UI elements
        LinearLayout transcriptContainer = findViewById(R.id.transcriptContainer);

        // Fetch student transcript data
        fetchTranscriptData(transcriptContainer);
    }

    private void fetchTranscriptData(LinearLayout transcriptContainer) {
        Response.Listener<String> responseListener = response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                if (jsonResponse.getBoolean("success")) {
                    // Update the UI with transcript details
                    TextView nameText = new TextView(this);
                    nameText.setText("Transcript for " + jsonResponse.getString("name"));
                    transcriptContainer.addView(nameText);

                    // Display completed courses
                    JSONArray completedCourses = jsonResponse.getJSONArray("completed_courses");
                    if (completedCourses.length() > 0) {
                        TextView completedTitle = new TextView(this);
                        completedTitle.setText("Completed Courses:");
                        transcriptContainer.addView(completedTitle);
                        for (int i = 0; i < completedCourses.length(); i++) {
                            JSONObject course = completedCourses.getJSONObject(i);
                            TextView courseText = new TextView(this);
                            String courseInfo = "Course ID: " + course.getString("course_id") + "\n" +
                                    "Section ID: " + course.getString("section_id") + "\n" +
                                    "Year: " + course.getString("year") + "\n" +
                                    "Semester: " + course.getString("semester") + "\n" +
                                    "Grade: " + course.getString("grade");
                            courseText.setText(courseInfo);
                            transcriptContainer.addView(courseText);
                        }
                    }

                    // Display current courses
                    JSONArray currentCourses = jsonResponse.getJSONArray("current_courses");
                    if (currentCourses.length() > 0) {
                        TextView currentTitle = new TextView(this);
                        currentTitle.setText("Current Courses:");
                        transcriptContainer.addView(currentTitle);
                        for (int i = 0; i < currentCourses.length(); i++) {
                            JSONObject course = currentCourses.getJSONObject(i);
                            TextView courseText = new TextView(this);
                            String courseInfo = "Course ID: " + course.getString("course_id") + "\n" +
                                    "Section ID: " + course.getString("section_id") + "\n" +
                                    "Year: " + course.getString("year") + "\n" +
                                    "Semester: " + course.getString("semester");
                            courseText.setText(courseInfo);
                            transcriptContainer.addView(courseText);
                        }
                    }

                    // Display GPA
                    TextView gpaText = new TextView(this);
                    gpaText.setText("GPA: " + jsonResponse.getString("gpa"));
                    transcriptContainer.addView(gpaText);

                    // Display PhD details if applicable
                    if (jsonResponse.has("phd_details")) {
                        JSONObject phdDetails = jsonResponse.getJSONObject("phd_details");
                        TextView phdText = new TextView(this);
                        phdText.setText("PhD Details:\n" +
                                "Proposal Date: " + phdDetails.optString("proposal_date", "Not Available") + "\n" +
                                "Dissertation Date: " + phdDetails.optString("dissertation_date", "Not Available"));
                        transcriptContainer.addView(phdText);
                    }
                } else {
                    Toast.makeText(this, "Error: " + jsonResponse.getString("error"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        };

        StringRequest transcriptDataRequest = new StringRequest(Request.Method.POST, getString(R.string.url) + "studentTranscript.php",
                responseListener, error -> Toast.makeText(this, "Request failed", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", studentEmail);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(transcriptDataRequest);
    }
}
