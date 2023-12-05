package com.example.project3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

public class AddNewEntry extends AppCompatActivity {
    private EditText editTextJobNumber, editTextInsuranceName, editTextCustomerName;
    private Button buttonSubmit;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_entry);
        editTextJobNumber = findViewById(R.id.editTextJobNumber);
        editTextInsuranceName = findViewById(R.id.editTextInsuranceName);
        editTextCustomerName = findViewById(R.id.editTextCustomerName);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("jobNumbers");
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Assuming submitJobInfo is an asynchronous operation
                submitJobInfo(new OnDatabaseOperationCompleteListener() {
                    @Override
                    public void onDatabaseOperationComplete() {
                        // The database operation is complete
                        // Now, start the new activity

                        Intent intent = new Intent(AddNewEntry.this, MainActivity.class);
                        // Add any additional flags or data to the Intent if needed

                        // Start the main activity
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
    }
    private void submitJobInfo(final OnDatabaseOperationCompleteListener listener) {
        // Assume this method involves an asynchronous operation, like adding an entry to the database

        // Simulate an asynchronous operation with a delay
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Perform the asynchronous operation
                // ...
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                String jobNumber = editTextJobNumber.getText().toString().trim();
                String insuranceName = editTextInsuranceName.getText().toString().trim();
                String customerName = editTextCustomerName.getText().toString().trim();

                // Create a JSON object with the entered data
                JSONObject jobInfoJson = new JSONObject();
                try {
                    jobInfoJson.put("jobNumber", jobNumber);
                    jobInfoJson.put("insuranceName", insuranceName);
                    jobInfoJson.put("customerName", customerName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Upload the JSON object to Firebase Storage
                uploadJsonToStorage(user.getUid(),jobNumber + ".json", jobInfoJson.toString());


                // Simulate completion
                try {
                    Thread.sleep(2000); // Simulating a 2-second delay
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Notify the listener that the database operation is complete
                if (listener != null) {
                    listener.onDatabaseOperationComplete();
                }
            }
        }).start();
    }

    // Callback interface
    interface OnDatabaseOperationCompleteListener {
        void onDatabaseOperationComplete();
    }
    @Override
    public void onBackPressed() {
        // Create an Intent to go back to the main activity
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        // Add any additional flags or data to the Intent if needed

        // Start the main activity
        startActivity(intent);

        // Finish the current activity (optional, depending on your use case)
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private void uploadJsonToStorage(String userId,String fileName, String jsonData) {
        StorageReference userFolderRef = storageRef.child(userId).child(fileName.substring(0,fileName.length()-5));
        StorageReference jsonFileRef = userFolderRef.child(fileName);

        byte[] dataBytes = jsonData.getBytes();
        UploadTask uploadTask = jsonFileRef.putBytes(dataBytes);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // JSON file uploaded successfully
            showToast("Upload successful");
            Intent resultIntent = new Intent();
            resultIntent.putExtra("newFileName", fileName);
            setResult(RESULT_OK, resultIntent);
            finish();


        }).addOnFailureListener(exception -> {
            // Handle any errors
            showToast("Upload failed: " + exception.getMessage());
        });
    }
}