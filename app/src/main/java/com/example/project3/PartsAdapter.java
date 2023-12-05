package com.example.project3;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PartsAdapter extends ArrayAdapter<Part> {
    Context context;
    ArrayList<Part> partList;
    public PartsAdapter(Context context, List<Part> partsList) {
        super(context, 0, partsList);
        this.context = context;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            itemView = inflater.inflate(R.layout.list_item_parts, parent, false);
        }

        CheckedTextView checkedTextView = itemView.findViewById(R.id.partInfo);

        // Get the part at the current position
        Part part = getItem(position);

        if (part != null) {
            // Set the text for the CheckedTextView based on the part information
            checkedTextView.setText("Part Number:"+part.getPartNumber()+"\n"+"QTY:"+part.getQuantity()+"\n"+"Part Info:"+ part.getPartInfo());
            if(part.isChecked()){
                checkedTextView.setCheckMarkDrawable(R.drawable.checked);
                checkedTextView.setChecked(true);
            }





            // Handle the click event for the CheckedTextView
            checkedTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Toggle the checked state of the part
                    //CheckedTextView checkedTextView1 = (CheckedTextView) view;
                    if (checkedTextView.isChecked()) {
                        checkedTextView.setCheckMarkDrawable(null);
                        FirebaseUtils.updateReceivedStatus(context,part.getRO(),part.getPartNumber(),!part.isChecked());
                        checkedTextView.setChecked(false);
                        part.setCheck(false);
                        //change the part received property in the database
                        String newJson = FirebaseUtils.partObjectToJson(part);
                        //replace the object with the partnumber in the file with part RO = filename

                        //create a function that takes the part
                        // Notify the adapter that the data set has changed
                        notifyDataSetChanged();
                    }
                    else{checkedTextView.setCheckMarkDrawable(R.drawable.checked);
                        checkedTextView.setChecked(true);
                        part.setCheck(true);
                        FirebaseUtils.updateReceivedStatus(context,part.getRO(),part.getPartNumber(),part.isChecked());
                        notifyDataSetChanged();

                    }

                }
            });
        }

        return itemView;
    }
}

