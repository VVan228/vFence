package com.example.vfence.ar;

public class AvgPoint {
    private final int MOVING_AVERAGE_SIZE = 20;

    Vector2d[] pointsArray = new Vector2d[MOVING_AVERAGE_SIZE];
    int pointsArrayLen = 0;
    boolean visible = false;

    public boolean isVisible(){
        return visible;
    }

    public void setPoint(Vector2d vector){
        if(vector != null){
            visible = true;
        } else{
            visible = false;
            return;
        }
        if(pointsArrayLen<pointsArray.length){
            pointsArray[pointsArrayLen] = vector;
            pointsArrayLen++;
            return;
        }
        for(int i = 0; i<pointsArray.length-1; i++){
            pointsArray[i] = pointsArray[i+1];
        }
        pointsArray[pointsArray.length-1] = vector;
    }

    public Vector2d getPoint(){
        if(pointsArrayLen>0 && pointsArrayLen<pointsArray.length){
            return pointsArray[pointsArrayLen-1];
        } else if(pointsArrayLen == 0){
            return null;
        }
        float avgX = 0f;
        float avgY = 0f;
        for(Vector2d v: pointsArray){
            avgX += v.getX();
            avgY += v.getY();
        }
        avgX /= (float)pointsArray.length;
        avgY /= (float)pointsArray.length;
        return new Vector2d(avgX, avgY, Axis.Z);
    }
}
