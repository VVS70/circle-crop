package ru.andro1d.circlecrop.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.graphics.Point;
import android.content.Context;
import android.util.DisplayMetrics;

import android.util.Log;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends Activity {

    private static final int SELECT_PICTURE_ACTIVITY_REQUEST_CODE = 0;
    private static final int TAKE_PHOTO = 1;
    private static final String CAMERA_FILE_PREFIX = "IMG_";
    private static final String CAMERA_FILE_EXTENSION = ".jpg";
    public static Boolean needSplash = true;
    private String filePath = "";
    private static Context context;

    public interface OnBackPressedListener {
        public void onBackPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.context = getApplicationContext();
        setContentView(R.layout.activity_main);

        if (needSplash) {
            Fragment aapFrag = new SplashScreen();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.animator.show_frame, R.animator.hide_frame);
            ft.add(R.id.container, aapFrag);
            ft.commit();
            ActionBar actionBar = getActionBar();
            actionBar.hide();
            needSplash = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
            switch (item.getItemId()) {
                case R.id.action_camera: {
                    try {
                        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File photo = null;
                        try {
                            photo = SplashScreen.createImageFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                            photo = null; SplashScreen.photoPath = null;
                        }
                        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
                        startActivityForResult(captureIntent, TAKE_PHOTO);
                    } catch (Exception e) {
                        String errorMessage = "Camera don't work:(";
                        Toast toast = Toast.makeText(MainActivity.getAppContext(),
                                        errorMessage, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    break;
                }
                case R.id.action_gallery: {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, SELECT_PICTURE_ACTIVITY_REQUEST_CODE);
                    break;
                }
                case R.id.action_save: {
                    Fragment fr = getFragmentManager().findFragmentById(R.id.container);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.addToBackStack("app_move");
                    ft.hide(fr);
                    fr = new SaveResult();
                    ft.setCustomAnimations(R.animator.show_frame, R.animator.hide_frame);
                    ft.add(R.id.container, fr);
                    ft.commit();
                    break;
                }
                case R.id.action_settings: {
                    Toast.makeText(this, "under construction", Toast.LENGTH_LONG).show();
                    break;
                }
            }
            return super.onOptionsItemSelected(item);
    }

    public String getFilePath() {
        if (filePath==""){
            SharedPreferences sPref;
            sPref = getPreferences(MODE_PRIVATE);
        return sPref.getString("#FileName", "");
        } else {
            return filePath;
        }
    }

    public void setFilePath(String str) {
        filePath=str;
        SharedPreferences sPref;
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString("#FileName", str);
        ed.commit();
    }

    public static Context getAppContext() {return MainActivity.context;    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PICTURE_ACTIVITY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = MainActivity.getAppContext().getContentResolver()
                              .query(selectedImage, filePathColumn, null, null, null);
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        setFilePath(cursor.getString(columnIndex));
                        AppView fr = (AppView) getFragmentManager().findFragmentById(R.id.container);
                        fr.updateView();
                    }
                    cursor.close();
                }
                break;
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (SplashScreen.photoPath != null) {
                        setFilePath(SplashScreen.photoPath);
                        AppView fr = (AppView) getFragmentManager().findFragmentById(R.id.container);
                        fr.updateView();
                        SplashScreen.galleryAddPic(SplashScreen.photoPath);
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fr = getFragmentManager().findFragmentById(R.id.container);
        OnBackPressedListener backPressedListener = null;
            if (fr instanceof  OnBackPressedListener) {
                backPressedListener = (OnBackPressedListener) fr;
            }
        if (backPressedListener != null) {
            backPressedListener.onBackPressed();
            super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}