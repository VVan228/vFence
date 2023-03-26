package com.example.vfence.ar;

public class Vector3d {
    double x;
    double y;
    double z;

    public static Vector3d getXaxis(){
        return new Vector3d(1,0,0);
    }
    public static Vector3d getYaxis(){
        return new Vector3d(0,1,0);
    }
    public static Vector3d getZaxis(){
        return new Vector3d(0,0,1);
    }

    public Vector3d rotateInAxis(Vector3d axis, double theta){
        double _x, _y, _z;
        double u, v, w;
        _x = this.getX();_y=this.getY();_z=this.getZ();
        u = axis.getX();v=axis.getY();w=axis.getZ();
        double xPrime = u*(u*_x + v*_y + w*_z)*(1d - Math.cos(theta))
                + _x*Math.cos(theta)
                + (-w*_y + v*_z)*Math.sin(theta);
        double yPrime = v*(u*_x + v*_y + w*_z)*(1d - Math.cos(theta))
                + _y*Math.cos(theta)
                + (w*_x - u*_z)*Math.sin(theta);
        double zPrime = w*(u*_x + v*_y + w*_z)*(1d - Math.cos(theta))
                + _z*Math.cos(theta)
                + (-v*_x + u*_y)*Math.sin(theta);
        return new Vector3d(xPrime, yPrime, zPrime);
    }

    public Vector3d substractVector(Vector3d vector) {
        return new Vector3d(x-vector.getX(), y-vector.getY(), z-vector.getZ());
    }

    public Vector3d addVector(Vector3d vector) {
        return new Vector3d(x+vector.getX(), y+vector.getY(), z+vector.getZ());
    }

    public Vector3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getDistance(){
        return Math.sqrt(x*x + y*y + z*z);
    }

    @Override
    public String toString() {
        return "Vector3d{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
