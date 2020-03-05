package com.example.instaspam.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instaspam.Profile.AccountSettingsActivity;
import com.example.instaspam.R;
import com.example.instaspam.Utils.Permissions;

//import android.support.v4.app.Fragment;

public class PhotoFragment extends Fragment {
    private static final String TAG = "PhotoFragment";

    private static final int PHOTO_FRAGMENT_NUM = 1;
    private static final int GALLERY_FRAGMENT_NUM = 2;
    private static final int CAMERA_REQUEST_CODE = 5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        Log.d(TAG, "onCreateView: started.");

        Button btnLaunchCamera = (Button)view.findViewById(R.id.btnLaunchCamera);

        btnLaunchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: launching camera.");

               if( ((ShareActivity)getActivity()).getCurrentTabNumber() == PHOTO_FRAGMENT_NUM) {
                   if(((ShareActivity)getActivity()).checkPermissions(Permissions.CAMERA_PERMISSION[0])){
                       Log.d(TAG, "onClick: starting camera");
                       Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                       startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);



                   } else {
                       Log.d(TAG, "onClick: go back to shareActivity so it can ask permission again.");
                       Intent intent = new Intent(getActivity(), ShareActivity.class);
                       intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  //clear activity stack
                       startActivity(intent);


                   }
               }
            }
        });


        return view;
    }


    private boolean isRootTask(){
        if (((ShareActivity)getActivity()).getTask() == 0) return true;
        else return false;

    }
    /**
     * Start taking pictures.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAMERA_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: done taking a photo.");
            Log.d(TAG, "onActivityResult: attempting to navigate to final share screen.");
            //navigate to the final share screen to publish photo


            Bitmap bitmap;
            bitmap = (Bitmap)data.getExtras().get("data");  //data is a key word

            if(isRootTask()) {
                try {
                    Log.d(TAG, "onActivityResult: received new bitmap from camera: " + bitmap);
                    Intent intent = new Intent(getActivity(), NextActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                    startActivity(intent);


                } catch(NullPointerException e) {
                    Log.d(TAG, "onActivityResult: NullPointerExceptionL " + e.getMessage());
                }
            } else {
                try {
                    Log.d(TAG, "onActivityResult: received new  bitmapfrom camera and to accountsettingactivity: " + bitmap);
                    Intent intent = new Intent(getActivity(), AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();


                } catch(NullPointerException e) {
                    Log.d(TAG, "onActivityResult: NullPointerExceptionL " + e.getMessage());
                }

            }



        }
    }
}
