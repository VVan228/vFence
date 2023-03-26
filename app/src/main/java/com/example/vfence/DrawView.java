package com.example.vfence;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.vfence.ar.AvgPoint;

import java.util.List;

public class DrawView extends View {

    private List<AvgPoint> points;

    public void setPoints(List<AvgPoint> points){
        this.points = points;
    }

    public DrawView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawCircle(canvas);
    }

    public void drawCircle(Canvas canvas){
        canvas.drawColor(Color.TRANSPARENT);
        Paint paint = new Paint();
        paint.setColor(Color.GREEN); // установим белый цвет
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.FILL); // заливаем
        paint.setAntiAlias(true);

        for(AvgPoint p: points){
            if(p.isVisible()){
                canvas.drawCircle((float) (canvas.getWidth()*p.getPoint().getX()),
                        (float) (canvas.getHeight()*p.getPoint().getY()),
                        20, paint);
            }
        }

    }

}
