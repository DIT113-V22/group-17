package com.example.smartcarapp;

public class UserData {
    private int travelledDistance;
    public UserData(int travelledDistance){
        this.travelledDistance = travelledDistance;
    }

    public int getTravelledDistance() {
        return travelledDistance;
    }

    public void setTravelledDistance(int travelledDistance) {
        this.travelledDistance = travelledDistance;
    }

    @Override
    public String toString() {
        return "UserData{" +
                "travelledDistance=" + travelledDistance +
                '}';
    }
}
