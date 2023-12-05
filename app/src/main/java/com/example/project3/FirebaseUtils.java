package com.example.project3;
import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class FirebaseUtils {

    private FirebaseAuth mAuth;
    public static void fetchAndDisplayParts(Context context, String jobNumber, ListView listView, TextView emptyTextView) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Check if the user is authenticated
        if (user == null) {
            // Handle the case where the user is not authenticated
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("jobNumbers");

        StorageReference userStorageRef = storageRef.child(user.getUid());
        // Create a reference to the specific path
        StorageReference jobNumberRef = userStorageRef.child(jobNumber);

        // Create a reference to the JSON file
        StorageReference jsonRef = jobNumberRef.child("partsData.json");

        jsonRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            String jsonContent = new String(bytes);
            if (!jsonContent.isEmpty()) {
                // Parse the JSON content into an ArrayList of Part objects
                ArrayList<Part> partsList = parseJsonToPartsList(jsonContent);

                // Update the adapter with the new data

//                ArrayAdapter<Part> adap = new ArrayAdapter<>(
//                        context,
//                        R.layout.list_item_parts, // Create a custom layout file (list_item.xml)
//                        R.id.partInfo,      // ID of the TextView in the custom layout for part information
//                        partsList
//                );
                ArrayAdapter<Part> adap = new PartsAdapter(context,partsList);
                listView.setAdapter(adap);

            } else {
                // If the JSON file is empty, show a message in the TextView
                emptyTextView.setVisibility(View.INVISIBLE);
            }
        }).addOnFailureListener(exception -> {
            // Handle failure when retrieving the JSON content
            Toast.makeText(context, "Error fetching parts data", Toast.LENGTH_SHORT).show();
        });
    }

    // Helper method to parse JSON content into an ArrayList of Part objects
    private static ArrayList<Part> parseJsonToPartsList(String jsonContent) {
        ArrayList<Part> partsList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonContent);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                // Assuming Part class has a constructor that takes a JSONObject
                Part part = new Part(jsonObject);
                partsList.add(part);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return partsList;
    }

    public static String partObjectToJson(Part part) {
        try {
            JSONObject jsonPart = new JSONObject();
            jsonPart.put("partNumber", part.getPartNumber());
            jsonPart.put("quantity", part.getQuantity());
            jsonPart.put("partInfo", part.getPartInfo());
            jsonPart.put("price",part.getPrice());
            jsonPart.put("RO",part.getRO());
            jsonPart.put("received",part.getReceived());


            return jsonPart.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null; // Handle the exception based on your requirements
        }
    }



    // Upload JSON string to Firebase Storage
    public static void uploadJsonToStorage(Context context, String jobNumber, ArrayList<Part> partsList) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Check if the user is authenticated
        if (user == null) {
            // Handle the case where the user is not authenticated
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("jobNumbers");

        StorageReference userStorageRef = storageRef.child(user.getUid());
        // Create a reference to the specific path
        StorageReference jobNumberRef = userStorageRef.child(jobNumber);

        // Create a reference to the JSON file
        StorageReference jsonRef = jobNumberRef.child("partsData.json");

        // Retrieve the existing content of the JSON file
        jsonRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            String existingJson = new String(bytes);

            try {
                // Parse the existing JSON array or create a new array if it doesn't exist
                JSONArray jsonArray;
                if (existingJson.isEmpty()) {
                    jsonArray = new JSONArray();
                } else {
                    jsonArray = new JSONArray(existingJson);
                }

                // Convert the ArrayList of Part objects to JSON objects and update or add to the array
                for (Part part : partsList) {
                    JSONObject partObject = new JSONObject(partObjectToJson(part));
                    if (containsPartNumber(jsonArray, part.getPartNumber())) {
                        // Update the existing entry
                        updatePartInArray(jsonArray, partObject);
                    } else {
                        // Add a new entry
                        jsonArray.put(partObject);
                    }
                }

                // Convert the updated array back to a JSON string
                String updatedJson = jsonArray.toString();

                // Upload the updated JSON content
                byte[] data = updatedJson.getBytes("UTF-8");
                UploadTask uploadTask = jsonRef.putBytes(data);

                // Handle success or failure
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    // Handle success
                    Toast.makeText(context, "Success uploading data", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(exception -> {
                    // Handle failure
                    Toast.makeText(context, "Error uploading data", Toast.LENGTH_SHORT).show();
                });
            } catch (JSONException | UnsupportedEncodingException e) {
                // Handle JSON parsing or encoding error
                Toast.makeText(context, "Error parsing or encoding JSON", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(exception -> {
            // Handle failure when retrieving existing JSON content
            Toast.makeText(context, "Error fetching existing data. Creating a new array.", Toast.LENGTH_SHORT).show();

            try {
                // Convert the ArrayList of Part objects to JSON objects
                JSONArray jsonArray = new JSONArray();
                for (Part part : partsList) {
                    JSONObject partObject = new JSONObject(partObjectToJson(part));
                    jsonArray.put(partObject);
                }

                // Convert the new array to a JSON string
                String newJson = jsonArray.toString();

                // Upload the new JSON content
                byte[] data = newJson.getBytes(StandardCharsets.UTF_8);
                UploadTask uploadTask = jsonRef.putBytes(data);

                // Handle success or failure
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    // Handle success
                    Toast.makeText(context, "Success uploading data", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(uploadException -> {
                    // Handle failure
                    Toast.makeText(context, "Error uploading data", Toast.LENGTH_SHORT).show();
                });
            } catch (JSONException e) {
                // Handle JSON parsing error for the new JSON
                Toast.makeText(context, "Error parsing JSON for new array", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Update the "received" key in the JSON array for a specific part number
    public static void updateReceivedStatus(Context context, String jobNumber, String partNumber, boolean newReceivedStatus) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Check if the user is authenticated
        if (user == null) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("jobNumbers");

        StorageReference userStorageRef = storageRef.child(user.getUid());
        StorageReference jobNumberRef = userStorageRef.child(jobNumber);
        StorageReference jsonRef = jobNumberRef.child("partsData.json");

        jsonRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            try {
                String existingJson = new String(bytes);
                JSONArray jsonArray;

                if (existingJson.isEmpty()) {
                    jsonArray = new JSONArray();
                } else {
                    jsonArray = new JSONArray(existingJson);
                }

                // Update the "received" key for the specified part number
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    if (jsonObject.has("partNumber") && jsonObject.getString("partNumber").equals(partNumber)) {
                        jsonObject.put("received", newReceivedStatus);
                        jsonArray.put(i, jsonObject);

                        // Convert the updated array back to a JSON string
                        String updatedJson = jsonArray.toString();

                        // Upload the updated JSON content
                        byte[] data = updatedJson.getBytes("UTF-8");
                        UploadTask uploadTask = jsonRef.putBytes(data);

                        // Handle success or failure
                        uploadTask.addOnSuccessListener(taskSnapshot -> {
                            Toast.makeText(context, "Success updating received status", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(exception -> {
                            Toast.makeText(context, "Error updating received status", Toast.LENGTH_SHORT).show();
                        });

                        return; // Break out of the loop once the part is updated
                    }
                }

                // If the part number is not found, you may want to handle this case
                Toast.makeText(context, "Part number not found in the data", Toast.LENGTH_SHORT).show();
            } catch (JSONException | UnsupportedEncodingException e) {
                Toast.makeText(context, "Error parsing or encoding JSON", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(exception -> {
            Toast.makeText(context, "Error fetching data", Toast.LENGTH_SHORT).show();
        });
    }


    // Helper method to check if a part with the given part number exists in the JSON array
    private static boolean containsPartNumber(JSONArray jsonArray, String partNumber) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.has("partNumber") && jsonObject.getString("partNumber").equals(partNumber)) {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // Helper method to update a part in the JSON array based on its part number
    private static void updatePartInArray(JSONArray jsonArray, JSONObject updatedPart) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject existingPart = jsonArray.getJSONObject(i);
                if (existingPart.has("partNumber") && existingPart.getString("partNumber").equals(updatedPart.getString("partNumber"))) {
                    // Update the existing entry
                    jsonArray.put(i, updatedPart);
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }





}
