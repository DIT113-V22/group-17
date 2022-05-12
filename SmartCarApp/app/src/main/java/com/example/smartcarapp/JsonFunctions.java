package com.example.smartcarapp;

import android.util.JsonWriter;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class JsonFunctions {

    public static void objectToJson(Object user){
        System.out.println("The JSON representation of Object mobilePhone is ");

        Gson gson = new Gson();
        try {
            gson.toJson(user, new FileWriter("C:\\Users\\Ergiman\\Documents\\GitHub\\group-17\\SmartCarApp\\app\\src\\main\\assets\\UserData.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void writeJsonStream(OutputStream out, UserData user) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        writer.setIndent("  ");
        writeUser(writer, user);
        writer.close();
    }



    public void writeUser(JsonWriter writer, UserData user) throws IOException {
        writer.beginObject();
        writer.name("travelledDistance").value(user.getTravelledDistance());
        writer.endObject();
    }


}
