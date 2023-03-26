package com.example.vfence.ar;

public class Vector2d {
    double a;
    double b;
    Axis axis;

    public Vector2d(Vector3d vector, Axis axis) {
        this.axis = axis;
        switch (axis){
            case X :{
                a = vector.getY();
                b = vector.getZ();
                break;
            }
            case Y :{
                a = vector.getX();
                b = vector.getZ();
                break;
            }
            case Z :{
                a = vector.getX();
                b = vector.getY();
                break;
            }
        }
    }

    public static double getAngleCos(Vector2d v1, Vector2d v2){
        double scalar = v1.a * v2.a + v1.b * v2.b;
        double len1 = Math.sqrt(v1.a*v1.a + v1.b*v1.b);
        double len2 = Math.sqrt(v2.a*v2.a + v2.b*v2.b);
        return scalar/(len1*len2);
    }

    public static double cosToGrad(double cosA){
        return Math.acos(cosA)*180/Math.PI;
    }
    public static double cosToRad(double cosA){
        return Math.acos(cosA);
    }

    public Vector2d(double a, double b, Axis axis) {
        this.a = a;
        this.b = b;
        this.axis = axis;
    }

    public double getX(){
        switch (axis){
            case Z:
            case Y : {
                return a;
            }
            default : {
                return 0d;
            }
        }
    }
    public double getY(){
        switch (axis){
            case X : {
                return a;
            }
            case Z : {
                return b;
            }
            default : {
                return 0d;
            }
        }
    }
    public double getZ(){
        switch (axis){
            case X:
            case Y : {
                return b;
            }
            default : {
                return 0d;
            }
        }
    }

    public void setX(double x){
        this.a = x;
    }

    public void setY(double y){
        if(axis==Axis.X){
            this.a = y;
        }else{
            this.b = y;
        }
    }

    public void setZ(double z){
        this.b = z;
    }

    @Override
    public String toString() {
        return "Vector2d{" +
                "a=" + a +
                ", b=" + b +
                '}';
    }
}

enum Axis{
    X,
    Y,
    Z;
}
