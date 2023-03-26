package com.example.vfence.ar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ArCanvas {
    private static ArCanvas obj;


    List<Vector3d> importedPoints = new ArrayList<>();
    List<AvgPoint> points = new ArrayList<>();
    double horizontalView;
    double verticalView;

    public static ArCanvas getInstance(){
        if(obj == null){
            obj = new ArCanvas();
        }
        return obj;
    }

    public void init(List<Vector3d> points, double horizontalView, double verticalView) {
        importedPoints = new ArrayList<>(points);
        //this.points = Collections.nCopies(points.size(), new AvgPoint());
        for(int i = 0; i<importedPoints.size(); i++){
            this.points.add(new AvgPoint());
        }
        this.horizontalView = horizontalView;
        this.verticalView = verticalView;
    }

    public void addPoint(Vector3d vector){
        importedPoints.add(vector);
        points.add(new AvgPoint());
    }

    public List<AvgPoint> updateData(Vector3d position, double a1, double a2, double a3){
        for (int i = 0; i < importedPoints.size(); i++) {
            Vector3d coordinatedPoint = importedPoints.get(i).substractVector(position);
            Vector3d rotatedPoint = rotateVector(coordinatedPoint, a1,a2,a3);
            Vector2d positionedPoint = getPointCoord(this.horizontalView, this.verticalView, rotatedPoint);
            points.get(i).setPoint(positionedPoint);
            points.get(i).setDistance(rotatedPoint.getDistance());
        }
        return points;//points.stream().map(p->p.getPoint()).collect(Collectors.toList());
    }

    public Vector3d createNewPoint(Vector3d position, double a1, double a2, double a3, double distance){
        Vector3d newPoint = new Vector3d(distance, 0, 0);
        newPoint = rotateVector(newPoint, 0, -a2, -a3);
        newPoint = newPoint.rotateInAxis(Vector3d.getXaxis(), a1);
//        newPoint = rotateVector(newPoint, -a1, -a2, -a3);
        newPoint = newPoint.addVector(position);
        return newPoint;
    }

    public Vector2d getPointCoord(double /*rad*/horizontalView, double verticalView, Vector3d point){
        if(point.getX()<0){
            return null;
        }
        Vector2d res = new Vector2d(0,0, Axis.Z);

        //horizontal
        Vector2d pointZ = new Vector2d(point, Axis.Z);
        Vector2d a11 = new Vector2d(Math.cos(horizontalView/2), Math.sin(-horizontalView/2), Axis.Z);
        Vector2d a12 = new Vector2d(Math.cos(horizontalView/2), Math.sin(horizontalView/2), Axis.Z);
        double angle1 = Vector2d.cosToRad(Vector2d.getAngleCos(a11, pointZ));
        double angle2 = Vector2d.cosToRad(Vector2d.getAngleCos(pointZ, a12));

        boolean horizontal = Math.abs((angle1+angle2)-horizontalView)<0.0005;

        if(horizontal){
            res.a = angle1/horizontalView;
        }else{
            return null;
        }

        //vertical
        Vector2d pointY = new Vector2d(point, Axis.Y);
        Vector2d a21 = new Vector2d(Math.cos(verticalView/2), Math.sin(-verticalView/2), Axis.Y);
        Vector2d a22 = new Vector2d(Math.cos(verticalView/2), Math.sin(verticalView/2), Axis.Y);
        double angle3 = Vector2d.cosToRad(Vector2d.getAngleCos(a21, pointY));
        double angle4 = Vector2d.cosToRad(Vector2d.getAngleCos(pointY, a22));

        boolean vertical = Math.abs((angle3+angle4)-verticalView)<0.0005;

        if(vertical){
            res.b = angle3/verticalView;
        }else{
            return null;
        }

        return res;
    }

    public Vector3d rotateVector(Vector3d vector, double a1, double a2, double a3){
        Vector3d v = vector.rotateInAxis(Vector3d.getXaxis(), -a1);
        v = v.rotateInAxis(Vector3d.getYaxis(), a2);
        v = v.rotateInAxis(Vector3d.getZaxis(), -a3);
        return v;
    }
}
