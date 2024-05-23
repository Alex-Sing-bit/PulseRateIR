package com.polar.polarsdkecghrdemo.helpers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.google.mlkit.vision.face.Face;
import com.polar.polarsdkecghrdemo.HRActivity;

import java.util.ArrayList;

public class DrawDetection {
    static int COLOR = Color.GREEN;
    private static final HRActivity hr = new HRActivity();

    public static Bitmap drawDetection(Bitmap bitmap, Person p, Face face) {
        if (face == null) {
            return bitmap;
        }

        return drawDetection(bitmap, p, face.getBoundingBox());
    }

    private static Bitmap drawDetection(Bitmap bitmap, Person p, Rect faceBounds) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        Paint fillPaint = new Paint();
        fillPaint.setColor(Color.WHITE);
        fillPaint.setStyle(Paint.Style.FILL);



        assert faceBounds != null;
        canvas.drawRect(faceBounds, paint);

        if (p == null) {
            return bitmap;
        }
        paint.setTextSize(50);
        if (p.getMood() == Mood.STRESSED) {
            COLOR = Color.RED;
        } else if (p.getMood() == Mood.TIRED) {
            COLOR = Color.BLUE;
        } else {
            COLOR = Color.GREEN;
        }

        int u = canvas.getHeight() - 450;
        int d = canvas.getHeight() - 150;
        int l = 400;

        canvas.drawText(p.getName(),
                faceBounds.right + 10, faceBounds.top + 20, paint);
        canvas.drawText(p.getMood().toString(),
                faceBounds.right + 10, faceBounds.top + 80, paint);
        canvas.drawText(p.getPulseRate() + "",
                faceBounds.right + 10, faceBounds.top + 140, paint);

        int len = (d - u) / 2;
        int sdvig = 25;
        Rect sq = new Rect(l - len * 2 - sdvig, d,
                l - sdvig, u);

        //canvas.drawRect(sq, fillPaint);

        canvas.drawLine(l, d, l - len * 2,
                d, paint);
        canvas.drawLine(l - len * 2, d,
                l - len * 2, u, paint);
        ArrayList<Integer> values = p.getyHrValues();
        int size = values.size();
        if (size > 0) {
            int xDel = (len * 2 / size);
            int yDel = len / 100 + 1;

            for (int i = 1; i < size; i++) {
                canvas.drawLine(l - len * 2 + xDel * (i - 1),
                        d - values.get(i - 1) * yDel,
                        l - len * 2 + xDel * i,
                        d - values.get(i) * yDel, paint);
            }
        }
        //drawValues(p.getyHrValues(), canvas, paint);
        return bitmap;
    }

    private static void drawValues(ArrayList<Integer> values, Canvas canvas, Paint paint) {
        int len = 100;
        int sdvig = 100;
        canvas.drawLine(sdvig, canvas.getHeight() - sdvig, len * 4 + sdvig, canvas.getHeight() - sdvig, paint);
        canvas.drawLine(sdvig, canvas.getHeight() - sdvig, sdvig, canvas.getHeight() - len - sdvig, paint);
        int size = values.size();
        if (size <= 0) {
            return;
        }
        int xDel = (len * 4 / size);
        for (int i = 0; i < size; i++) {
            canvas.drawLine(sdvig + xDel * i, canvas.getHeight() - sdvig - values.get(i),
                    sdvig + 20 + xDel * i, canvas.getHeight() - sdvig - values.get(i), paint);
        }
    }
}
