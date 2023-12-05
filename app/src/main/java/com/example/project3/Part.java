package com.example.project3;

import org.json.JSONException;
import org.json.JSONObject;

public class Part {
    String partNumber;
    String quantity;
    String partInfo;
    String price;
    String received;

    String RO; //jobNumber

    public Part(String partNumber, String quantity, String partInfo,String price) {
        this.partNumber = partNumber;
        this.quantity = quantity;
        this.partInfo = partInfo;
        this.price= price;
        this.received = "false";
        this.RO="";
    }
    public Part(JSONObject jsonObject) {
        try {
            // Assuming the keys in the JSON object match your field names
            this.partNumber = jsonObject.getString("partNumber");
            this.quantity = jsonObject.getString("quantity");
            this.price = jsonObject.getString("price");
            this.partInfo = jsonObject.getString("partInfo");
            this.received = jsonObject.getString("received");
            this.RO= jsonObject.getString("RO");

            // Add more lines for other fields if needed
        } catch (JSONException e) {
            e.printStackTrace();
            // Handle JSONException, such as logging or throwing a custom exception
        }
    }

    // Override toString() to define how each part should be displayed in the ListView
    @Override
    public String toString() {
        return "Part Number: " + this.partNumber + "\nQuantity: " + this.quantity + "\nPart Info: " + this.partInfo;
    }

    public String getPartNumber() {
        return this.partNumber;
    }

    public String getQuantity() {
        return this.quantity;
    }

    public String getPartInfo() {
        return this.partInfo;
    }

    public String getPrice() {
        return this.price;
    }

    public void setRO(String RO){
        this.RO = RO;
    }


    public String getRO(){
        return this.RO;
    }
    public String getReceived(){
        return this.received;
    }
    public boolean isChecked(){
        return "true".equals(this.received);

    }
    public boolean setCheck(boolean cur){
        if (cur){
            this.received = "true";
            return true;
        }
        else {
            this.received = "false";
            return true;
        }

    }
}
