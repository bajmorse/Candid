package com.app.Camera;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.DngCreator;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.R;
import com.app.Utils.AutoResizeTextureView;
import com.app.Utils.CandidUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CameraTestFragment extends Fragment implements
        TextureView.SurfaceTextureListener,
        FragmentCompat.OnRequestPermissionsResultCallback {

    // Logger Tag
    public static final String TAG = "CAMERA_LEFT";
    // Permissions
    private static final int CAMERA_PERMISSIONS = 1;
    private static final int READ_PERMISSIONS = 2;
    private static final int WRITE_PERMISSIONS = 3;
    private static final int READ_WRITE_PERMISSIONS = 4;
    private static final int READ_WRITE_CAMERA_PERMISSIONS = 5;
    // Fragment Listener
    private OnFragmentInteractionListener mListener;
    // Camera
    private AutoResizeTextureView mCameraTextureView;
    private Surface mRawCaptureSurface, mJpegCaptureSurface, mPreviewSurface;
    private Size mPreviewSize;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;
    private CaptureResult mCaptureResult;
    private CameraCharacteristics mCameraCharacteristics;
    private Integer mCameraFacing = CameraCharacteristics.LENS_FACING_BACK;
    private File mPhotoDir;
    private int mImageFormat;

    public static CameraTestFragment newInstance() {
        return new CameraTestFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set immersive mode
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN);

        // Check permissions
        boolean hasCameraPermissions = (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
        boolean hasStoragePermissions = (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasCameraPermissions && !hasStoragePermissions) {
            CandidUtils.requestPermission(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_WRITE_CAMERA_PERMISSIONS);
        } else if (!hasCameraPermissions) {
            CandidUtils.requestPermission(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSIONS);
        } else if (!hasStoragePermissions) {
            CandidUtils.requestPermission(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_WRITE_PERMISSIONS);
        }

        // Setup capture directory
        Log.d(TAG, "Getting photo directory.");
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mPhotoDir = new File(picturesDir.getAbsolutePath() + '/' + getString(R.string.app_name));
        if (!mPhotoDir.exists()) {
            Log.d(TAG, "Candid photo directory doesn't exist. Creating " + mPhotoDir.getAbsolutePath() + " now.");
            mPhotoDir.mkdirs();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate view
        return inflater.inflate(R.layout.fragment_camera_test, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup camera view
        mCameraTextureView = (AutoResizeTextureView) view.findViewById(R.id.camera_test);
        mCameraTextureView.setSurfaceTextureListener(this);

        // Setup photo button
        ImageView takePhotoButton = (ImageView) view.findViewById(R.id.take_photo_button);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capture();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Camera functions
     */

    private void initCamera() {
        try {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                CandidUtils.requestPermission(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSIONS);
                return;
            }

            Log.d(TAG, "Initializing camera");
            CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);

            // Get rear camera
            String[] cameraIds = cameraManager.getCameraIdList();
            String cameraId = null;
            for (String id : cameraIds) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(id);
                if (Objects.equals(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING), mCameraFacing)) {
                    cameraId = id;
                    mCameraCharacteristics = cameraCharacteristics;
                }
            }
            if (cameraId == null)
                throw new CameraAccessException(CameraAccessException.CAMERA_ERROR, "Couldn't find a valid camera");

            // Get camera configuration map
            StreamConfigurationMap configurationMap = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (configurationMap == null)
                throw new CameraAccessException(CameraAccessException.CAMERA_ERROR, "Couldn't find stream configuration map");

            // Find a supported image type
            boolean supportsRaw = false, supportsJpeg = false;
            for (int format : configurationMap.getOutputFormats()) {
                if (format == ImageFormat.RAW_SENSOR) {
                    Log.d(TAG, "Raw Supported");
                    supportsRaw = true;
                } else if (format == ImageFormat.JPEG) {
                    Log.d(TAG, "Jpeg Supported");
                    supportsJpeg = true;
                }
            }
            if (supportsJpeg) {
                mImageFormat = ImageFormat.JPEG;
            } else if (supportsRaw) {
                mImageFormat = ImageFormat.RAW_SENSOR;
            } else
                throw new CameraAccessException(CameraAccessException.CAMERA_ERROR, "Couldn't find a supported image type");

            // Get image sizes
            Size rawSize = configurationMap.getOutputSizes(ImageFormat.RAW_SENSOR)[0];
            Size jpegSize = configurationMap.getOutputSizes(ImageFormat.JPEG)[0];

            // Find preview size
            Size[] previewSizes = configurationMap.getOutputSizes(SurfaceTexture.class);
            mPreviewSize = findOptimalPreviewSize(previewSizes, rawSize);
            if (mPreviewSize == null) throw new CameraAccessException(CameraAccessException.CAMERA_ERROR, "Couldn't find a suitable preview size");

            // Setup image readers
            ImageReader rawReader = ImageReader.newInstance(rawSize.getWidth(), rawSize.getHeight(), ImageFormat.RAW_SENSOR, 1);
            rawReader.setOnImageAvailableListener(new RawImageAvailableListener(), null);
            mRawCaptureSurface = rawReader.getSurface();
            ImageReader jpegReader = ImageReader.newInstance(jpegSize.getWidth(), jpegSize.getHeight(), ImageFormat.JPEG, 1);
            jpegReader.setOnImageAvailableListener(new JpegImageAvailableListener(), null);
            mJpegCaptureSurface = jpegReader.getSurface();

            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {

                @Override
                public void onOpened(CameraDevice camera) {
                    mCameraDevice = camera;
                    initPreview();
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size findOptimalPreviewSize(Size[] sizes, Size targetSize) {
        float targetRatio = ((float) targetSize.getWidth()) / targetSize.getHeight();
        float tolerance = 0.1f;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int maxPixels = screenWidth * Math.round(screenWidth * targetRatio);

        for (Size size : sizes) {
            int width = size.getWidth();
            int height = size.getHeight();
            if (width * height <= maxPixels) {
                float ratio = ((float) width) / height;
                if (Math.abs(ratio - targetRatio) < tolerance) {
                    return size;
                }
            }
        }
        return  null;
    }

    private void initPreview() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        float previewRatio = mPreviewSize.getWidth() / (float) mPreviewSize.getHeight();
        int previewHeight = Math.round(screenWidth * previewRatio);

        ViewGroup.LayoutParams params = mCameraTextureView.getLayoutParams();
        params.width = screenWidth;
        params.height = previewHeight;

        List<Surface> surfaces = Arrays.asList(mPreviewSurface, mRawCaptureSurface, mJpegCaptureSurface);
        try {
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    mCameraCaptureSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {}
            }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to init preview");
        }
    }

    private void updatePreview() {
        if (mCameraDevice == null || mCameraCaptureSession == null) {
            Log.e(TAG, "Failed to update preview");
        }

        try {
            CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(mPreviewSurface);

            builder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            mCameraCaptureSession.setRepeatingRequest(builder.build(), new CameraCaptureSession.CaptureCallback() {
            }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to start preview");
        }
    }

    private void capture() {
        Log.d(TAG, "Attempting to capture photo");
        try {
            CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            builder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF);
            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
            builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON);

            if (mImageFormat == ImageFormat.JPEG) {
                builder.addTarget(mJpegCaptureSurface);
                builder.set(CaptureRequest.JPEG_QUALITY, (byte) 100);
            } else {
                builder.addTarget(mRawCaptureSurface);
                builder.set(CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE, CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE_ON);
            }

            mCameraCaptureSession.capture(builder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    Log.d(TAG, "Successfully captured photo");
                    mCaptureResult = result;
                }

                @Override
                public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
                    super.onCaptureFailed(session, request, failure);
                    Log.e(TAG, "Failed to capture photo");
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to start capturing photo");
        }
    }

    /**
     * Permissions
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "Permission request result");
        switch (requestCode) {
            case CAMERA_PERMISSIONS:
            case READ_WRITE_CAMERA_PERMISSIONS: {
                boolean cameraPermissionGranted = false;
                for (int idx = 0; idx < permissions.length; idx++) {
                    if (Objects.equals(permissions[idx], Manifest.permission.CAMERA) && grantResults[idx] == PackageManager.PERMISSION_GRANTED) {
                        cameraPermissionGranted = true;
                        break;
                    }
                }

                if (!cameraPermissionGranted) {
                    CandidUtils.ErrorDialog.newInstance("Did not receive camera permissions").show(getChildFragmentManager(), "dialog");
                } else {
                    initCamera();
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * SurfaceTextureListener functions
     */

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mPreviewSurface = new Surface(surface);
        initCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        mCameraCaptureSession = null;
        return true;
    }

    /**
     * Created classes and interfaces
     */

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private class RawImageAvailableListener implements ImageReader.OnImageAvailableListener {

        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "Saving raw image to " + mPhotoDir);
            new SaveRawTask(getActivity(), mPhotoDir, reader.acquireLatestImage(), mCameraCharacteristics, mCaptureResult).execute();
        }
    }

    private static class SaveRawTask extends AsyncTask<Void, Void, Boolean> {
        WeakReference<Activity> mActivityReference;
        private File mFile;
        private Image mImage;
        private DngCreator mDngCreator;

        public SaveRawTask(Activity activity, File dir, Image image, CameraCharacteristics cameraCharacteristics, CaptureResult captureResult) {
            mActivityReference = new WeakReference<Activity>(activity);
            mFile = new File(dir, System.currentTimeMillis() + ".dng");
            mImage = image;
            mDngCreator = new DngCreator(cameraCharacteristics, captureResult);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                mDngCreator.writeImage(new FileOutputStream(mFile), mImage);
                mDngCreator.close();
                mImage.close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            CandidUtils.showToast(mActivityReference.get(), "Saved photo: " + aBoolean);
        }
    }

    private class JpegImageAvailableListener implements ImageReader.OnImageAvailableListener {

        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "Saving jpeg image to " + mPhotoDir);
            new SaveJpegTask(getActivity(), mPhotoDir, reader.acquireLatestImage()).execute();
        }
    }

    private static class SaveJpegTask extends AsyncTask<Void, Void, Boolean> {
        private WeakReference<Activity> mActivityReference;
        private File mFile;
        private Image mImage;

        public SaveJpegTask(Activity activity, File dir, Image image) {
            mActivityReference = new WeakReference<Activity>(activity);
            mFile = new File(dir, System.currentTimeMillis() + ".jpg");
            mImage = image;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes);
            mImage.close();

            try {
                new FileOutputStream(mFile).write(bytes);
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            CandidUtils.showToast(mActivityReference.get(), "Saved " + mFile + ": " + aBoolean);
        }
    }
}
