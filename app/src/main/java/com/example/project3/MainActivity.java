package com.example.project3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button buttonLogout;
    private Button buttonAdd;

    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        buttonLogout = findViewById(R.id.buttonLogout);
        buttonAdd = findViewById(R.id.add);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("jobNumbers");



        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, AddNewEntry.class);
                startActivity(intent);

            }
        });
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);

            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Fetch and display data
        fetchData();
    }
    private void fetchData() {
        // Get the current user's UID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            StorageReference userStorageRef = storageRef.child(user.getUid());

            userStorageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                @Override
                public void onSuccess(ListResult listResult) {
                    showToast("ListResult size:"+ String.valueOf(listResult.getPrefixes().size()));
                    // List of files obtained successfully
                    ArrayList<String> fileNames = new ArrayList<>();
                    for (StorageReference item : listResult.getPrefixes()) {


                        fileNames.add(item.getName());
                    }

                    // Now, download and parse each JSON file
                    for (String fileName : fileNames) {
                        downloadAndParseJson(user.getUid(), fileName);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Log.e("FirebaseStorage", "Error listing files", exception);
                    showToast("Error fetching data");
                }
            });
        }
    }
    private void downloadAndParseJson(String userId, String fileName) {
        showToast("fileName:"+ fileName);
        StorageReference jsonFileRef = storageRef.child(userId).child(fileName).child(fileName+".json");
        showToast("fileName:"+ fileName);

        jsonFileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // JSON file downloaded successfully
                String jsonData = new String(bytes);
                try {
                    JSONObject jsonObject = new JSONObject(jsonData);
                    // Parse the JSON object and add it to the adapter
                    adapter.addItem(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("MainActivity", "Error parsing JSON", e);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.e("FirebaseStorage", "Error downloading JSON file", exception);
                showToast("Error downloading JSON file");
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Adapter for the RecyclerView
    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private final ArrayList<JSONObject> data = new ArrayList<>();

        public void addItem(JSONObject item) {
            data.add(item);
            notifyDataSetChanged();
            Log.d("MyAdapter", "Data size: " + data.size());
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            try {
                JSONObject item = data.get(position);
                String jobNumber = item.getString("jobNumber");
                String insuranceName = item.getString("insuranceName");
                String customerName = item.getString("customerName");

                holder.textViewJobNumber.setText("Job Number: " + jobNumber);
                holder.textViewInsuranceName.setText("Insurance Name: " + insuranceName);
                holder.textViewCustomerName.setText("Customer Name: " + customerName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView textViewJobNumber, textViewInsuranceName, textViewCustomerName;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewJobNumber = itemView.findViewById(R.id.textViewJobNumber);
                textViewInsuranceName = itemView.findViewById(R.id.textViewInsuranceName);
                textViewCustomerName = itemView.findViewById(R.id.textViewCustomerName);
            }
        }
    }

}