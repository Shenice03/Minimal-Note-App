package ca.yorku.eecs.mack.notetakingapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap;
import android.os.Parcelable;

import java.util.ArrayList;

public class DrawingView extends View {

    private Paint currentPaint;
    private Path currentPath;
    private int currentColor = Color.BLACK;

    private boolean eraserMode = false;

    private Bitmap savedBitmap;

    private final ArrayList<Path> paths = new ArrayList<>();
    private final ArrayList<Paint> paints = new ArrayList<>();

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        initPaint();
    }

    private void initPaint() {
        currentPaint = new Paint();
        //currentPaint.setColor(currentColor);
        currentPaint.setAntiAlias(true);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
        //currentPaint.setStrokeWidth(8f);

        if (eraserMode) {
            currentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            currentPaint.setStrokeWidth(50f);
        } else {
            currentPaint.setColor(currentColor);
            currentPaint.setXfermode(null);
            currentPaint.setStrokeWidth(8f);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (savedBitmap != null) {
            canvas.drawBitmap(savedBitmap, 0, 0, null);
        }

        super.onDraw(canvas);

        for (int i = 0; i < paths.size(); i++) {
            canvas.drawPath(paths.get(i), paints.get(i));
        }

        if (currentPath != null) {
            canvas.drawPath(currentPath, currentPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath = new Path();
                currentPath.moveTo(x, y);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (currentPath != null) {
                    currentPath.lineTo(x, y);
                }
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                if (currentPath != null) {
                    paths.add(currentPath);
                    paints.add(new Paint(currentPaint)); // clone paint
                    currentPath = null;
                }
                invalidate();
                return true;
        }

        return false;
    }

    public void setPaintColor(int color) {
        currentColor = color;
        //initPaint();

        if (!eraserMode) {
            initPaint(); // Rebuild paint only if it is not an eraser
        }
    }

    /*public void clearCanvas() {
        paths.clear();
        paints.clear();
        invalidate();
    }*/
    public void setEraserMode(boolean enabled) {
        eraserMode = enabled;

        if (eraserMode) {
            currentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            currentPaint.setStrokeWidth(50f);
        } else {
            currentPaint.setXfermode(null);
            currentPaint.setStrokeWidth(8f);
        }

        invalidate();
    }


    public Bitmap getBitmapSnapshot() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas); // draw the current canvas onto this bitmap
        return bitmap;
    }

    public void loadFromBitmap(Bitmap bitmap) {
        savedBitmap = bitmap;
        invalidate();
    }

    public Bitmap exportDrawing() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }

    public void clearTempResources() {
        if (savedBitmap != null && !savedBitmap.isRecycled()) {
            savedBitmap.recycle();
            savedBitmap = null;
        }
        System.gc();
    }

}