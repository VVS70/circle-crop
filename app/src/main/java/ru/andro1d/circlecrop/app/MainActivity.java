package ru.andro1d.circlecrop.app;

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
        /*
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        */
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

/*
    Resources resources = context.getResources();
    int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
    if (resourceId > 0) {
    return resources.getDimensionPixelSize(resourceId);
    }
*/
    public static Context getAppContext() {return MainActivity.context; }
    public Point getDisplaySize() {

        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth  = displayMetrics.widthPixels  / displayMetrics.density;

        /*
        float dpHeight = getWindow().getDecorView().getWidth();
        float dpWidth  = getWindow().getDecorView().getHeight();
        */

        Point res = new Point();
        res.set((int) ((dpWidth - 10) * displayMetrics.density), (int) ((dpHeight - (76+5+70+90)) * displayMetrics.density));


        return res;
    }
}