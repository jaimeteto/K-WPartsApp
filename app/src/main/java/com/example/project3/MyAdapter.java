package com.example.project3;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    public  ArrayList<JSONObject> data = new ArrayList<>();
    private OnItemClickListener onItemClickListener;



    public void clear(){

        data.clear();
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void addItem(JSONObject item) {

        data.add(item);
        notifyDataSetChanged();

    }
    public String getItemName(int index){

        return "";

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
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onItemClickListener.onItemClick(position);
                        }
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View view){



                    return true;
                }
            });
        }
    }
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
