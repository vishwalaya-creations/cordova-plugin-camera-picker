package com.vc.plugins;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraAccessException;
import android.widget.Toast;

import java.io.File;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

public class CameraPicker extends CordovaPlugin {

    private CallbackContext callbackContext;

    private static final int CAMERA_REQUEST_CODE = 1001;
    private static final int IMAGE_PICKER_REQUEST_CODE = 1002;

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        if (action.equals("captureImage")) {
            captureImage(callbackContext);
            return true;
        } else if (action.equals("pickImage")) {
            pickImage(callbackContext);
            return true;
        }
        return false;
    }

    private void captureImage(final CallbackContext callbackContext) {
        // Get the front camera ID (typically 1, but check the available cameras)
        CameraManager cameraManager = (CameraManager) cordova.getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = null;

            // Loop through available cameras and find the front camera
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                int lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                    cameraId = id;
                    break;
                }
            }

            // Create a camera intent to capture an image using the front camera
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(cordova.getActivity().getExternalCacheDir(), "photo.jpg")));
            intent.putExtra("android.intent.extras.CAMERA_FACING", CameraCharacteristics.LENS_FACING_FRONT); // Front camera

            if (intent.resolveActivity(cordova.getActivity().getPackageManager()) != null) {
                cordova.startActivityForResult(this, intent, CAMERA_REQUEST_CODE);
            } else {
                callbackContext.error("No camera app found");
            }

        } catch (Exception e) {
            callbackContext.error("Error accessing camera: " + e.getMessage());
        }
    }

    private void pickImage(final CallbackContext callbackContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            cordova.startActivityForResult(this, intent, IMAGE_PICKER_REQUEST_CODE);
        } else {
            callbackContext.error("This feature requires Android 13 or higher");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE && intent != null) {
                Uri imageUri = intent.getData();
                this.callbackContext.success(imageUri.toString());
            } else if (requestCode == IMAGE_PICKER_REQUEST_CODE && intent != null) {
                Uri selectedImage = intent.getData();
                this.callbackContext.success(selectedImage.toString());
            }
        } else {
            this.callbackContext.error("Operation failed or canceled");
        }
    }
}