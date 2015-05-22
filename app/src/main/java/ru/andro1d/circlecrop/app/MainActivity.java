package ru.andro1d.circlecrop.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.graphics.Point;
import android.content.Context;
import android.util.DisplayMetrics;

import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int SELECT_PICTURE_ACTIVITY_REQUEST_CODE = 0;
    private static final int TAKE_PHOTO = 1;
    private static final String CAMERA_FILE_PREFIX = "IMG_";
    private static final String CAMERA_FILE_EXTENSION = ".jpg";
    private static String photoPath = null;
    private String filePath = "";
    private static Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.context = getApplicationContext();
        setContentView(R.layout.activity_main);

        Fragment aapFrag = new SplashScreen();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.animator.show_frame, R.animator.hide_frame);
        ft.add(R.id.container, aapFrag);
        //ft.addToBackStack("app_move");
        ft.commit();

        ActionBar actionBar = getActionBar();
        actionBar.hide();

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
                    Toast.makeText(this, "CAMERA", Toast.LENGTH_LONG).show();
                    break;
                }
                case R.id.action_folder: {
                    Toast.makeText(this, "Gallery", Toast.LENGTH_LONG).show();
                    break;
                }
                case R.id.action_save: {
                    Toast.makeText(this, "Save", Toast.LENGTH_LONG).show();
                    break;
                }
                case R.id.action_settings: {
                    Toast.makeText(this, "Settings", Toast.LENGTH_LONG).show();
                    break;
                }

            }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String str) {
        filePath=str;
    }

    public static Context getAppContext() {return MainActivity.context; }

}