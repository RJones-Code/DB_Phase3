package com.example.Phase3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParentProfileActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AlertAdapter alertAdapter;
    private ArrayList<Alert> alertList;
    private String parentEmail;

    private Spinner spinnerStudents;
    private String selectedStudentEmail = "";


    Button btnLogout, btnViewTranscript;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_profile);

        recyclerView = findViewById(R.id.recyclerViewAlerts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        alertList = new ArrayList<>();
        alertAdapter = new AlertAdapter(alertList);
        recyclerView.setAdapter(alertAdapter);

        parentEmail = getIntent().getStringExtra("email");
        btnLogout = findViewById(R.id.btnLogout);
        btnViewTranscript = findViewById(R.id.btnViewTranscript);
        spinnerStudents = findViewById(R.id.spinnerStudents);

        fetchStudents();
        fetchAlerts();

        btnViewTranscript.setOnClickListener(v -> {
            if (!selectedStudentEmail.isEmpty()) {
                Intent intent = new Intent(ParentProfileActivity.this, TranscriptActivity.class);
                intent.putExtra("email", selectedStudentEmail);
                startActivity(intent);
            } else {
                Toast.makeText(ParentProfileActivity.this, "Please select a student first", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(ParentProfileActivity.this, activity_login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }


    private void fetchAlerts() {
        String url = getString(R.string.url) + "fetchParentAlerts.php?email=" + parentEmail;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject alertObject = response.getJSONObject(i);
                            String studentId = alertObject.getString("student_id");
                            String studentName = alertObject.getString("student_name");
                            String alertType = alertObject.getString("alert_type");
                            String alertMessage = alertObject.getString("alert");

                            Alert alert = new Alert(studentId, studentName, alertType, alertMessage);
                            alertList.add(alert);
                        }
                        alertAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    error.printStackTrace(); // ✅ ADD this
                    Toast.makeText(ParentProfileActivity.this, "Volley Error: " + error.toString(), Toast.LENGTH_LONG).show(); // ✅ updated Toast
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void fetchStudents() {
        String url = getString(R.string.url) + "fetchParentStudents.php?email=" + parentEmail;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    ArrayList<String> studentNames = new ArrayList<>();
                    ArrayList<String> studentEmails = new ArrayList<>();

                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject studentObject = response.getJSONObject(i);
                            String studentName = studentObject.getString("student_name");
                            String studentEmail = studentObject.getString("student_email");

                            studentNames.add(studentName);
                            studentEmails.add(studentEmail);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (!studentNames.isEmpty()) {  // ✅ important: check if list is not empty
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ParentProfileActivity.this,
                                android.R.layout.simple_spinner_item, studentNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerStudents.setAdapter(adapter);

                        spinnerStudents.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedStudentEmail = studentEmails.get(position);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                selectedStudentEmail = "";
                            }
                        });
                    } else {
                        Toast.makeText(ParentProfileActivity.this, "No students found", Toast.LENGTH_SHORT).show();
                    }

                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(ParentProfileActivity.this, "Error fetching students.", Toast.LENGTH_SHORT).show();
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }



    // 👉 Nested Alert class
    private static class Alert {
        private final String studentId;
        private final String studentName;
        private final String alertType;
        private final String alertMessage;

        public Alert(String studentId, String studentName, String alertType, String alertMessage) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.alertType = alertType;
            this.alertMessage = alertMessage;
        }

        public String getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public String getAlertType() { return alertType; }
        public String getAlertMessage() { return alertMessage; }
    }

    // 👉 Nested AlertAdapter class
    private static class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {
        private final ArrayList<Alert> alertList;

        public AlertAdapter(ArrayList<Alert> alertList) {
            this.alertList = alertList;
        }

        @Override
        public AlertViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
            return new AlertViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AlertViewHolder holder, int position) {
            Alert alert = alertList.get(position);
            holder.tvStudentId.setText(alert.getStudentId());
            holder.tvStudentName.setText(alert.getStudentName());
            holder.tvAlertType.setText(alert.getAlertType());
            holder.tvAlertMessage.setText(alert.getAlertMessage());
        }

        @Override
        public int getItemCount() {
            return alertList.size();
        }

        static class AlertViewHolder extends RecyclerView.ViewHolder {
            TextView tvStudentId, tvStudentName, tvAlertType, tvAlertMessage;

            AlertViewHolder(View itemView) {
                super(itemView);
                tvStudentId = itemView.findViewById(R.id.tvStudentId);
                tvStudentName = itemView.findViewById(R.id.tvStudentName);
                tvAlertType = itemView.findViewById(R.id.tvAlertType);
                tvAlertMessage = itemView.findViewById(R.id.tvAlertMessage);
            }
        }
    }
}
