package com.example.project3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PartsInfo extends AppCompatActivity {
    private static final int PICK_PDF_FILE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    ArrayList<Part> partsList;
    ListView mListView;
    TextView mTextView;
    String jobNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parts_info);
        Button uploadEstimateButton = findViewById(R.id.uploadEstimate);
        Intent intent = getIntent();
        jobNumber=intent.getStringExtra("fileName");

        mListView = findViewById(R.id.partsList);
        mTextView = findViewById(R.id.emptyList);


        //if a json file exists for this job, fetch it and display it
        FirebaseUtils.fetchAndDisplayParts(this,jobNumber,mListView,mTextView);


        // Set an onClickListener for the button
        uploadEstimateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Implement the functionality you want when the button is clicked
                if (ContextCompat.checkSelfPermission(PartsInfo.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted, request it
                    ActivityCompat.requestPermissions(PartsInfo.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                } else {
                    // Permission already granted, proceed with file access
                    pickPdfFile();
                }
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with file access
                pickPdfFile();
            } else {
                // Permission denied, handle accordingly (e.g., show a message to the user)
                Toast.makeText(this, "Permission denied. Cannot access PDF file.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pickPdfFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf"); // Specify the MIME type of PDF files
        startActivityForResult(intent, PICK_PDF_FILE);
    }


    public static String getFilePathFromUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }

        String scheme = uri.getScheme();
        if (scheme != null) {
            if (scheme.equals("file")) {
                return uri.getPath();
            } else if (scheme.equals("content")) {
                return copyFileFromUri(context, uri);
            }
        }

        return null;
    }

    private static String copyFileFromUri(Context context, Uri uri) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }

            File tempFile = createTempFile(context, "temp", ".pdf");
            outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static File createTempFile(Context context, String prefix, String suffix) {
        File cacheDir = context.getCacheDir();
        try {
            return File.createTempFile(prefix, suffix, cacheDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_FILE && resultCode == RESULT_OK && data != null) {
            // Handle the selected file URI
            Uri selectedFileUri = data.getData();
            // Convert content URI to file path
            String filePath = getFilePathFromUri(PartsInfo.this, selectedFileUri);

            new PDFreader().execute(filePath);
        }
    }
    private class PDFreader extends AsyncTask<String, Void, String> {
        private ArrayList<Part> processEstimate(String input) {
            //StringBuilder result = new StringBuilder();

            ArrayList partsList2 = new ArrayList<>();
            String[] lines = input.split("\\r?\\n"); // Split the input into an array of lines
            boolean inEstimateSection = false;

            for (String line : lines) {
                if (line.startsWith("Line Oper Description")) {
                    inEstimateSection = true;
                } else if (line.startsWith("SUBTOTALS")) {
                    inEstimateSection = false;
                } else if (inEstimateSection) {
                    // Process each line after "Line Oper Description" and before "SUBTOTALS"
                    //result.append(processEstimateLine(line)).append("\n");
                    Part newLine = processEstimateLine(line);

                    if (newLine != null) {
                        partsList2.add(newLine);
                        newLine.setRO(jobNumber);
                        //Log.d("inProcessEstimate", newLine.toString());



                    }

                }


            }
            //add to firebase if it does not exist

            FirebaseUtils.uploadJsonToStorage(PartsInfo.this,jobNumber,partsList2);
            return partsList2;
        }

        private Part processEstimateLine(String line) {
            String regex = "(.*)\\s([a-zA-Z0-9]{9,})\\s(\\d+)\\s(\\d+\\.\\d+) "; // Ensure part number has length > 8

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(line);
            Part partEntry;

            // Check if the pattern is found in the line
            if (matcher.find()) {
                String partInfo = matcher.group(1);
                String partNumber = matcher.group(2);
                String quantity = matcher.group(3);
                String price = matcher.group(4);
                partEntry = new Part(partNumber, quantity, partInfo, price);

                // Modify this line according to your needs
                // return "Part Number: " + partNumber + ", Quantity: " + quantity +", Part Info: "+ partInfo ;
                return partEntry;

            }


            return null; // Return null if no match is found

        }

        @Override
        protected String doInBackground(String... params) {

            try {
                // creating a string for
                // storing our extracted text.
                String extractedText = "";

                // creating a variable for pdf reader
                // and passing our PDF file in it.

                PdfReader reader = new PdfReader(params[0]);

                // below line is for getting number
                // of pages of PDF file.
                int n = reader.getNumberOfPages();

                // running a for loop to get the data from PDF
                // we are storing that data inside our string.
                for (int i = 0; i < n; i++) {
                    extractedText = extractedText + PdfTextExtractor.getTextFromPage(reader, i + 1).trim() + "\n";
                    // to extract the PDF content from the different pages
                }
                reader.close();
                // after extracting all the data we are
                // setting that string value to our text view.
                return extractedText;

                // below line is used for closing reader.

            } catch (Exception e) {
                // for handling error while extracting the text file.
                e.printStackTrace();
                return ("Error:" + params[0]);
            }
        }


        @Override
        protected void onPostExecute(String result) {
            // Display the extracted text in a TextView or perform other actions
            //TextView textView = findViewById(R.id.pdf);


            partsList = processEstimate(result);

            ArrayAdapter<Part> adapter2  = new PartsAdapter(PartsInfo.this,partsList);

            //textView.setText("PDF Contents:\n" + lines);
            if (partsList != null) {
                //textView.setText("PDF Contents:\n" + lines);
                ListView partsListView = findViewById(R.id.partsList);
                partsListView.setAdapter(adapter2);


            }
        }
    }


}