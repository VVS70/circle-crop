package ru.andro1d.circlecrop.app;

import android.graphics.drawable.BitmapDrawable;
import android.os.Vibrator;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.graphics.Path;
import android.graphics.*;

import android.os.Handler;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import java.io.File;
import java.io.IOException;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import java.util.ArrayList;
import android.app.Application;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;



public class ImageProcessing extends ImageView{

    final String LOG_TAG = "ImgProc: ";

    public static String imageFileName;
    private Bitmap bitmap, mask;
    private Paint paint;
    //public static float density;
    private Path path,cPath;
    static Point screenSize = new Point();

    Vibrator vibrator;

    private float rot = 0;

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private float[] lastEvent = null;
    public static float rds = 1;
    public ImageProcessing(Context context,AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init();
    }

    public ImageProcessing(Context context,AttributeSet attrs){
        super(context, attrs);init();
    }

    public ImageProcessing(Context context){
        super(context);
        init();
    }

    private void init(){
        //density = getResources().getDisplayMetrics().density;

        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(3.0f);
        paint.setAntiAlias(true);

        path = new Path();
        cPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap!=null) {
            canvas.save();
            canvas.drawBitmap(bitmap,matrix, paint);
            canvas.restore();
            canvas.clipPath(path);
            canvas.clipPath(cPath, Region.Op.DIFFERENCE);
            canvas.drawBitmap(mask, 0, 0, paint);
        }
}

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    float dx = event.getX() - start.x;
                    float dy = event.getY() - start.y;
                    matrix.postTranslate(dx, dy);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = (newDist / oldDist);
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                    if (lastEvent != null && event.getPointerCount() == 2) {
                        newRot = rotation(event);
                        float r = newRot - d;
                        float[] values = new float[9];
                        matrix.getValues(values);
                        float tx = values[2];
                        float ty = values[5];
                        float sx = values[0];
                        float xc = (this.getWidth() / 2) * sx;
                        float yc = (this.getHeight() / 2) * sx;
                        matrix.postRotate(r, tx + xc, ty + yc);
                    }
                }
                break;
        }
        TransformParameters.matrix.set(matrix);
        this.invalidate();
        return true;
    }

    public static void setImageFileName(String f) {
    imageFileName = f;
    }

    private void setClipPath() {
        int maskWidth;
        int maskHeight;
        if (AppView.mode==AppView.Mode.FULL_IMAGE){
            maskHeight = bitmap.getHeight();
            maskWidth= bitmap.getWidth();
        } else {
            maskHeight = screenSize.x;
            maskWidth  = screenSize.y;
        }
        path.reset();
        path.addRect(0.0f, 0.0f, maskWidth, maskHeight, Path.Direction.CCW);
        TransformParameters.maskWidth = maskWidth;
        TransformParameters.maskHeight = maskHeight;
        cPath.reset();
        float x = (float)(maskWidth)/2-10;
        float y = (float)(maskHeight)/2-10;
        float r = (x<y?x:y)*rds;
        cPath.addCircle(maskWidth / 2,
                maskHeight / 2, r, Path.Direction.CCW);
        TransformParameters.maskRadius =r;
     }

    public void setClipPathRadius(Float r) {
        rds = r;
        setClipPath();
        this.invalidate();
    }

    private Bitmap prepareMask(int color, int opacity){
        int maskWidth  = 0, maskHeight = 0;
        if (AppView.mode==AppView.Mode.FULL_IMAGE){
            maskHeight = bitmap.getHeight();
            maskWidth= bitmap.getWidth();
        } else if (AppView.mode==AppView.Mode.FULL_HEIGHT){
            maskHeight = screenSize.y;
            maskWidth  = screenSize.x;
        } else {
            maskHeight = screenSize.y;
            maskWidth  = screenSize.x;
        }

        Bitmap output = Bitmap.createBitmap(
                        maskWidth,
                        maskHeight,
                        Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint pnt = new Paint();
        pnt.setColor(color);
        pnt.setStyle(Paint.Style.FILL);
        pnt.setAntiAlias(true);
        pnt.setAlpha(opacity);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawPaint(pnt);
        setClipPath();
         TransformParameters.ratioX=(float)bitmap.getWidth()/maskWidth;
         TransformParameters.ratioY=(float)bitmap.getHeight()/maskHeight;
        return output;
    }

    public void setParameters(Point p, int color, int opacity){
        screenSize.set(p.x, p.y);
        bitmap =((BitmapDrawable) getDrawable()).getBitmap();
        mask = prepareMask(color, opacity);
        matrix.set(this.getImageMatrix());
        matrix.reset();
        float dx = (bitmap.getWidth() - screenSize.x)>0?bitmap.getWidth() - screenSize.x:0;
        float dy = (bitmap.getHeight()- screenSize.y)>0?bitmap.getHeight()- screenSize.y:0;
        matrix.postTranslate(-dx / 2, -dy / 2);
        TransformParameters.startX=dx;  TransformParameters.startY=dy;
        TransformParameters.bmpHeight = bitmap.getHeight();
        TransformParameters.bmpWidth = bitmap.getWidth();
        TransformParameters.matrix.set(matrix);
        this.invalidate();
    }
    public void setMaskColor (int maskColor, int maskOpacity){
        mask = prepareMask(maskColor, maskOpacity);
        this.invalidate();
    }

    public void changeDrawMode(Boolean mode){
        if (mode) {

        } else {

        }
    }

    public static float getDistance(Point p1, Point p2){
        return (float) (Math.sqrt((p1.x - p2.x)*(p1.x - p2.x)+(p1.y - p2.y)*(p1.y - p2.y)));
    }

    public static float getDistance(float x1,float y1,float x2,float y2){
        return (float) (Math.sqrt((x1 - x2)*(x1 - x2)+(y1 - y2)*(y1 - y2)));
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private float rotation(MotionEvent event) {
        double dX = (event.getX(0) - event.getX(1));
        double dY = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(dY, dX);
        return (float) Math.toDegrees(radians);
    }
}
