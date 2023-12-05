package com.example.project3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button buttonLogout;
    private static final int ADD_NEW_ENTRY_REQUEST_CODE = 1001;

    private Button buttonAdd;

    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private ArrayList<String> fileNames;
    private ArrayList<String> newfileNames;

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


        adapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Handle item click

                String fileName = newfileNames.get(position);
                Toast.makeText(MainActivity.this, "fileName " + fileName, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, PartsInfo.class);
                intent.putExtra("fileName", fileName);
                startActivity(intent);


            }

        });


        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, AddNewEntry.class);
                startActivityForResult(intent, ADD_NEW_ENTRY_REQUEST_CODE);

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_NEW_ENTRY_REQUEST_CODE && resultCode == RESULT_OK) {
            // The new entry was added successfully

            // Extract the new file name from the result Intent
            String newFileName = data.getStringExtra("newFileName");
            newfileNames.add(newFileName);
            adapter.notifyDataSetChanged();

            // Now, update the adapter with the new entry
            downloadAndParseJson(FirebaseAuth.getInstance().getCurrentUser().getUid(), newFileName);




        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Fetch and display data
        adapter.clear();
        fetchData();



    }

    private void fetchData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            StorageReference userStorageRef = storageRef.child(user.getUid());

            userStorageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                @Override
                public void onSuccess(ListResult listResult) {
                    fileNames = new ArrayList<>();
                    newfileNames= new ArrayList<>();
                    for (StorageReference item : listResult.getPrefixes()) {
                        // Extract the actual file name from the StorageReference
                        String fileName = item.getName();
                        fileNames.add(fileName);
                    }

                    // Now, download and parse each JSON file
                    for (String fileName : fileNames) {
                        downloadAndParseJson(user.getUid(), fileName);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("FirebaseStorage", "Error listing files", exception);
                    showToast("Error fetching data");
                }
            });
        }
    }


    private void downloadAndParseJson(String userId, String fileName) {
        StorageReference jsonFileRef = storageRef.child(userId).child(fileName).child(fileName + ".json");

        jsonFileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                String jsonData = new String(bytes);
                try {
                    JSONObject jsonObject = new JSONObject(jsonData);
                    adapter.addItem(jsonObject);
                    newfileNames.add(fileName);
                    adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("MainActivity", "Error parsing JSON", e);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("FirebaseStorage", "Error downloading JSON file", exception);
                showToast("Error downloading JSON file");
            }
        });
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Adapter for the RecyclerView




    }
