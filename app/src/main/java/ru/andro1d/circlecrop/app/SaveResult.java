package ru.andro1d.circlecrop.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;

public class SaveResult extends Fragment implements MainActivity.OnBackPressedListener{
     private  BitmapFactory.Options options = null;
     private String filePath = "";
     private int finalScale;

    @Override
    public void onBackPressed() {
        MainActivity act = (MainActivity) getActivity();
        ActionBar actionBar = act.getActionBar();
         if ( actionBar!=null)  actionBar.show();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog, null);

        MainActivity act = (MainActivity) getActivity();

        ActionBar actionBar = act.getActionBar();
        if (actionBar!=null)  actionBar.hide();

        filePath = act.getFilePath();
        finalScale =  calculateMinScale();
        defineControls(v);

        return v;
    }

    private void defineControls(View v) {
        final View view = v;
        Button btn_ok = (Button)  v.findViewById(R.id.save_dlg_btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vBtn) {
                final Handler handler = new Handler();

                new Thread(new Runnable() {
                        @Override
                        public void run() {

                            SeekBar sk =(SeekBar) view.findViewById(R.id.save_dlg_set_size_seekBar);

                            int iX = (int) (options.outWidth / finalScale /(sk.getMax()/sk.getProgress()));
                            int iY = (int) (options.outHeight / finalScale /(sk.getMax()/sk.getProgress()));

                            if (AppView.mode!=AppView.Mode.FULL_IMAGE){
                                if (iX>iY) iX=iY; else iY=iX;
                            }

                            Bitmap b = prepareImage( (int) (iX*TransformParameters.ratioX),
                                                     (int) (iY*TransformParameters.ratioY));

                            float scaleX = iX / TransformParameters.maskWidth;
                            float scaleY = iY / TransformParameters.maskHeight;

                            float scaleXi = b.getWidth()  / TransformParameters.bmpWidth;
                            float scaleYi = b.getHeight() / TransformParameters.bmpHeight;

                            int dX = (int)((TransformParameters.startX)*scaleXi);
                            int dY = (int)((TransformParameters.startY)*scaleYi);

                            float[] values = new float[9];
                            TransformParameters.matrix.getValues(values);
                            values[2] = values [2] *scaleXi;
                            values[5] = values [5] *scaleYi;
                            TransformParameters.matrix.setValues(values );

                            Bitmap outputBitmap =
                                    Bitmap.createBitmap(iX,iY,Bitmap.Config.ARGB_8888);

                            Canvas canvas = new Canvas(outputBitmap);
                            Paint paint = new Paint();

                            paint.setColor(Color.WHITE);
                            canvas.drawPaint(paint);

                            canvas.save();
                            canvas.drawBitmap(b, TransformParameters.matrix, paint);

                            Path path = new Path();
                            path.addRect(0.0f, 0.0f, outputBitmap.getWidth(), outputBitmap.getHeight(), Path.Direction.CCW);
                            Path cPath = new Path();
                            cPath.reset();
                            cPath.addCircle(outputBitmap.getWidth() / 2,
                                    outputBitmap.getHeight() / 2,
                                    TransformParameters.maskRadius * (scaleX > scaleY ? scaleY : scaleX),
                                    Path.Direction.CCW);

                            paint.setColor(TransformParameters.maskColor);
                            paint.setAlpha(TransformParameters.maskOpacity);
                            paint.setAntiAlias(true);
                            paint.setFilterBitmap(true);
                            paint.setDither(true);
//                            canvas.drawARGB(0, 0, 0, 0);

                            canvas.restore();
                            canvas.clipPath(path);
                            canvas.clipPath(cPath, Region.Op.DIFFERENCE);
                            paint.setStyle(Paint.Style.FILL);
                            canvas.drawPaint(paint);

                            b.recycle();

                            String folderToSave =
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()+"/";

                            String fileName = folderToSave+"R_"+Long.toHexString(System.currentTimeMillis())+".jpg";
                            OutputStream fOut = null;

                            File file = new File(fileName);
                            try {
                                fOut = new FileOutputStream(file);
                                outputBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
                                fOut.flush();
                                fOut.close();
                            }
                            catch (Exception e) {
                                Toast.makeText( MainActivity.getAppContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                //e.getMessage();
                            }
                            finally {
                                outputBitmap.recycle();
                            }
                            SplashScreen.galleryAddPic(fileName);
                            handler.post(msg);
                        }
                    }).start();
                vBtn.getRootView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                vBtn.getRootView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
            }
        });

        Button btn_cancel = (Button)  v.findViewById(R.id.save_dlg_btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "cancel", Toast.LENGTH_SHORT).show();
                v.getRootView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                v.getRootView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
            }
        });
        final TextView txt = (TextView) v.findViewById(R.id.dlg_set_size_text);
        SeekBar sk =(SeekBar) v.findViewById(R.id.save_dlg_set_size_seekBar);
        sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 50) progress = 50;
                if (options.outWidth / finalScale /(seekBar.getMax()/progress) <10)
                     progress = (options.outWidth/finalScale)*seekBar.getMax()/10;
                int s = seekBar.getMax() / progress;
                txt.setText("Selected: " + Integer.toString(options.outWidth / finalScale / s) +
                        "x" + Integer.toString(options.outHeight / finalScale / s));
            }
        });
        int m = options.outWidth/finalScale;
        if (m<50) m =50;
        sk.setMax(m);
        sk.setProgress(m);
        int s = sk.getMax()/(sk.getProgress()>0?sk.getProgress():1);
        txt.setText("Selected: " + Integer.toString(options.outWidth / finalScale / s) +
                "x" + Integer.toString(options.outHeight / finalScale / s));
    }

    private int  calculateMinScale(){
        MainActivity act = (MainActivity) getActivity();
        options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        int imageHeight = options.outHeight;
        int imageWidth  = options.outWidth;

        long heapPad=(long) Math.max(4*1024*1024,Runtime.getRuntime().maxMemory()*0.1);
        long allocNativeHeap = Debug.getNativeHeapAllocatedSize();
        long maxMemory =  Runtime.getRuntime().maxMemory();
        int scale = 1;
        //4 bits per pixel in memory. We need 2 bitmap in memory
            while (((imageHeight/scale)*(imageWidth/scale)*4 + allocNativeHeap + heapPad)>(maxMemory/2)){
                scale *= 2;
            }
    return scale;
    }

    public Bitmap prepareImage(int scaleX, int scaleY){
        options.inSampleSize = finalScale;
        options.inJustDecodeBounds = false;
        Bitmap b = BitmapFactory.decodeFile(filePath, options);
        b = Bitmap.createScaledBitmap(b,scaleX,scaleY,true);
        return b;
    }

    public void saveImage(){

    }

    private Runnable msg = new Runnable() {
      public void run(){
          Context context = MainActivity.getAppContext();
          Toast.makeText(context, "File saved!", Toast.LENGTH_SHORT).show();
      }
    };

}
