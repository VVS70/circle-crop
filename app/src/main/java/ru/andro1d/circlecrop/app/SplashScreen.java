package ru.andro1d.circlecrop.app;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.*;
import android.os.Environment;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SplashScreen extends Fragment {

    private static final int SELECT_PICTURE_ACTIVITY_REQUEST_CODE = 0;
    private static final int TAKE_PHOTO = 1;
    private static final String CAMERA_FILE_PREFIX = "IMG_";
    private static final String CAMERA_FILE_EXTENSION = ".jpg";
    public static String photoPath = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.splash_screen, null);

        ImageButton button_gallery = (ImageButton) v.findViewById(R.id.gallery);
        button_gallery.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_PICTURE_ACTIVITY_REQUEST_CODE);
            }
        });
        ImageButton button_camera = (ImageButton) v.findViewById(R.id.camera);
        button_camera.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File photo = null;
                    try {
                        photo = createImageFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        photo = null;
                        photoPath = null;
                    }
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
                    startActivityForResult(captureIntent, TAKE_PHOTO);
                } catch (Exception e) {
                    String errorMessage = "Camera don't work:(";
                    Toast.makeText(MainActivity.getAppContext(), errorMessage, Toast.LENGTH_SHORT)
                    .show();
                }

            }
        });
    return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PICTURE_ACTIVITY_REQUEST_CODE:
                if (resultCode == getActivity().RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                     Cursor cursor = MainActivity.getAppContext().getContentResolver()
                             .query(selectedImage, filePathColumn, null, null, null);
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        setResultImage(cursor.getString(columnIndex));
                    }
                    cursor.close();
                }
                break;
            case TAKE_PHOTO:
                if (resultCode == getActivity().RESULT_OK) {
                    if (photoPath != null) {
                        setResultImage(photoPath);
                    }
                }
                break;
        }
    }

    private void setResultImage(String s){
        MainActivity act = (MainActivity) getActivity();
        act.setFilePath(s);

        Fragment fr = getFragmentManager().findFragmentById(R.id.container);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.animator.show_frame, R.animator.hide_frame);
        ft.hide(fr);
        ft.detach(fr);
        ft.commit();

        fr = new AppView();
        ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.animator.show_frame, R.animator.hide_frame);
        ft.add(R.id.container, fr);
        //ft.addToBackStack("app_move");
        ft.commit();
    }

    public static void galleryAddPic(final String pathToPhoto) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File f = new File(pathToPhoto);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                MainActivity.getAppContext().sendBroadcast(mediaScanIntent);
            }
        }).start();
    }

    public static File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = CAMERA_FILE_PREFIX + timeStamp + "_";
        File image = File.createTempFile(
                imageFileName,
                CAMERA_FILE_EXTENSION,
                getAlbumDir()
        );
        photoPath  = image.getAbsolutePath();
        return image;
    }

    public static File getAlbumDir() {
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Toast.makeText(MainActivity.getAppContext(),
                                "Failed to create directory", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                }
            }
        } else {

            Toast.makeText(MainActivity.getAppContext(),
                    "External storage is not \n mounted for READ/WRITE.",
                    Toast.LENGTH_SHORT).show();
        }
        return storageDir;
    }
}
