package ru.andro1d.circlecrop.app;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import java.io.File;
import android.view.ViewGroup.LayoutParams;


import android.view.View.OnClickListener;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.Button;
import android.content.res.Resources;
import android.util.TypedValue;
import android.content.Context;
import android.widget.ImageView;
import android.content.ContentResolver;
import android.os.Environment;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.security.MessageDigest;

import android.net.Uri;

public class AppView extends Fragment {
    enum Mode {FULL_IMAGE, FULL_WIDTH, FULL_HEIGHT};
    static Mode mode = Mode.FULL_HEIGHT;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, null);
        MainActivity act = (MainActivity) getActivity();
        Bitmap bmp = getResizedBitmap(act.getFilePath());
        final CardView mainLayout = (CardView) v.findViewById(R.id.card_view);
        final CheckBox checkbox = (CheckBox) v.findViewById(R.id.checkBox);
        final ColorPicker colorPicker = (ColorPicker) v.findViewById(R.id.color_picker);
        final SeekBar skBar = (SeekBar) v.findViewById(R.id.radius_seekBar);
                      skBar.setProgress(skBar.getMax());
        final ImageProcessing imageView = new ImageProcessing(MainActivity.getAppContext());
        Point s = getDisplaySize();
        float scaleImage=getScaleIndex(bmp.getWidth(), bmp.getHeight(), s);
        int x =  s.x;
        int y =  s.y;
        imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp,
                                 (int) (bmp.getWidth()/scaleImage),
                                 (int) (bmp.getHeight()/scaleImage), false));

        if (mode!=Mode.FULL_IMAGE) {
            TransformParameters.fullHeight=(x>y);
            if (x>y) x=y; else y=x;
            s.set(x,y);
        }
        imageView.setParameters(s, colorPicker.getColor(),colorPicker.getOpacity());

        LayoutParams imageViewLayoutParams;
        //imageViewLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        imageViewLayoutParams = new LayoutParams(x, y);
        imageView.setLayoutParams(imageViewLayoutParams);
        imageView.setId(R.id.user_image_id);
        mainLayout.addView(imageView);

        colorPicker.setMonitor(imageView);

        ActionBar actionBar = act.getActionBar();
        //actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.show();

        checkbox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkbox.isChecked())
                    mode = Mode.FULL_IMAGE;
                else mode = Mode.FULL_HEIGHT;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Vibrator vibrator =
                                (Vibrator) MainActivity.getAppContext().getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(40);
                    }
                }).start();
                updateView();
            }

            ;
        });

        skBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

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
                    imageView.setClipPathRadius((float)seekBar.getProgress()/seekBar.getMax());
                }
        });

        return v;
    }

    public void updateView(){
        MainActivity act = (MainActivity) getActivity();
        Bitmap bmp = getResizedBitmap(act.getFilePath());
        Point s = getDisplaySize();
        float scaleImage=getScaleIndex(bmp.getWidth(), bmp.getHeight(), s);
        int x =  s.x;
        int y =  s.y;
        if (mode!=Mode.FULL_IMAGE) {
            if (x>y) x=y; else y=x;
            s.set(x,y);
        } else {
            x = (int) (bmp.getWidth()/scaleImage);
            y = (int) (bmp.getHeight()/scaleImage);
        }
        ImageProcessing imageView =(ImageProcessing) getView().findViewById(R.id.user_image_id);
        imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp,
                (int) (bmp.getWidth() / scaleImage),
                (int) (bmp.getHeight() / scaleImage), false));
        ViewGroup.LayoutParams prm = imageView.getLayoutParams();
        prm.height=y; prm.width=x;
        final ColorPicker colorPicker = (ColorPicker) getView().findViewById(R.id.color_picker);
        imageView.setParameters(s, colorPicker.getColor(), colorPicker.getOpacity());
        imageView.requestLayout();

        CardView cv = (CardView) getView().findViewById(R.id.card_view);
        prm = cv.getLayoutParams();
        prm.height=y+10;
        prm.width=x+10;
        cv.requestLayout();
            //Toast.makeText( MainActivity.getAppContext(),"W: "+Float.toString(x),Toast.LENGTH_SHORT).show();
    }

    private float getScaleIndex(int width, int height, Point s){
        float scaleImage=1;
        switch (mode) {
            case FULL_HEIGHT:
            case FULL_WIDTH: {
                scaleImage = (float) Math.min((float) (width) / s.x, (float) (height) / s.y);
                break;
            }
            case FULL_IMAGE: {
                scaleImage = (float) Math.max((float)(width)/s.x,(float)(height)/s.y);
                break;
            }
        }
        return  scaleImage;
    }

    private Bitmap getResizedBitmap(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            Point screen = getDisplaySize();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fileName, options);
            options.inSampleSize = calculateInSampleSize(options,screen.x,screen.y);
            options.inJustDecodeBounds = false;
            Bitmap b = BitmapFactory.decodeFile(fileName, options);
            return b;
        }
        return null;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public Point getDisplaySize() {
        if (getActivity() != null) {
            MainActivity act = (MainActivity) getActivity();
            DisplayMetrics displayMetrics = act.getApplicationContext().getResources().getDisplayMetrics();

            float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
            float dpWidth  = displayMetrics.widthPixels  / displayMetrics.density;

            Point res = new Point();
            res.set((int) ((dpWidth - (isPortrait()?17:123)) * displayMetrics.density),
                    (int) ((dpHeight -(isPortrait()?217:76)) * displayMetrics.density));
            return res;
        } else return null;
    }

    private boolean isPortrait(){
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            return true;
        else
            return false;
    }

    @Override
    public void onPause(){ super.onPause();    }

    @Override
    public void onResume(){super.onResume();   }
}