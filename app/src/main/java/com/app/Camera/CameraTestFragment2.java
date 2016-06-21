package com.app.Camera;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CameraTestFragment2 extends Fragment implements
        FragmentCompat.OnRequestPermissionsResultCallback,
        TextureView.SurfaceTextureListener {

    // Logger Tag
    public static final String TAG = "CAMERA_RIGHT";
    // Fragment Listener
    private OnFragmentInteractionListener mListener;
    // Permissions
    private static final int CAMERA_PERMISSIONS = 1;
    private static final int READ_PERMISSIONS = 2;
    private static final int WRITE_PERMISSIONS = 3;
    private static final int READ_WRITE_PERMISSIONS = 4;
    private static final int READ_WRITE_CAMERA_PERMISSIONS = 5;
    // Camera Orientations
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    // Camera States
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRECAPTURE = 2;
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;
    private static final int STATE_PICTURE_TAKEN = 4;
    private int mState = STATE_PREVIEW;
    // Camera Constants
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    // Camera
    private AutoResizeTextureView mCameraTextureView;
    private Surface mRawCaptureSurface, mJpegCaptureSurface, mPreviewSurface;
    private Size mPreviewSize;
    private String mCameraId;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;
    private CaptureResult mCaptureResult;
    private CaptureRequest mPreviewRequest;
    private CameraCharacteristics mCameraCharacteristics;
    private Integer mCameraFacing = CameraCharacteristics.LENS_FACING_BACK;
    private File mPhotoDir;
    private int mImageFormat;
    private int mSensorOrientation;
    private ImageReader mImageReader;
    private CaptureRequest.Builder mCaptureBuilder, mPreviewBuilder;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    // Camera callbacks and listeners
    private final CameraDeviceStateCallback mStateCallback = new CameraDeviceStateCallback();
    private final CameraCaptureStateCallback mCaptureStateCallback  = new CameraCaptureStateCallback();
    private final CameraCaptureCallback mCaptureCallback = new CameraCaptureCallback();
    private final CameraCaptureRequestCallback mCaptureRequestCallback = new CameraCaptureRequestCallback();
    private final CameraImageAvailableListener mOnImageAvailableListener = new CameraImageAvailableListener();
    // Threading
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    /**
     * Lifecycle functions
     */

    public static CameraTestFragment2 newInstance() {
        CameraTestFragment2 fragment = new CameraTestFragment2();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera_test_2, container, false);
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
    public void onResume() {
        super.onResume();
        startBackgroundThread();
//        if (mPreviewSurface != null) {
//            openCamera(mCameraTextureView.getWidth(), mCameraTextureView.getHeight());
//        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Camera initialization functions
     */

    private void initCamera(int width, int height) {
        Activity activity = getActivity();
        CameraManager cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            // Get rear camera
            String[] cameraIds = cameraManager.getCameraIdList();
            for (String id : cameraIds) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(id);
                if (Objects.equals(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING), mCameraFacing)) {
                    mCameraId = id;
                    mCameraCharacteristics = cameraCharacteristics;
                }
            }
            if (mCameraId == null || mCameraCharacteristics == null)
                throw new CameraAccessException(CameraAccessException.CAMERA_ERROR, "Couldn't find a valid camera");

            // Get camera configuration map
            StreamConfigurationMap configurationMap = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (configurationMap == null)
                throw new CameraAccessException(CameraAccessException.CAMERA_ERROR, "Couldn't find stream configuration map");

            // Find a supported image type
            boolean supportsJpeg = false;
            for (int format : configurationMap.getOutputFormats()) {
                if (format == ImageFormat.JPEG) {
                    Log.d(TAG, "Jpeg Supported");
                    supportsJpeg = true;
                }
            }
            if (supportsJpeg) {
                mImageFormat = ImageFormat.JPEG;
            } else
                throw new CameraAccessException(CameraAccessException.CAMERA_ERROR, "Couldn't find a supported image type");

            // Find preview size
            Size rawSize = configurationMap.getOutputSizes(ImageFormat.RAW_SENSOR)[0];
            Size jpegSize = configurationMap.getOutputSizes(ImageFormat.JPEG)[0];
            Size[] previewSizes = configurationMap.getOutputSizes(SurfaceTexture.class);
            mPreviewSize = findOptimalPreviewSize(previewSizes, rawSize);
            if (mPreviewSize == null)
                throw new CameraAccessException(CameraAccessException.CAMERA_ERROR, "Couldn't find a suitable preview size");

            // Setup image readers
            mSensorOrientation = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            mImageReader = ImageReader.newInstance(jpegSize.getWidth(), jpegSize.getHeight(), ImageFormat.JPEG, 2);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
            mJpegCaptureSurface = mImageReader.getSurface();
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error while initializing camera");
            e.printStackTrace();
        }
    }

    private void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            CandidUtils.requestPermission(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSIONS);
            return;
        }

        initCamera(width, height);
        configureTransform(width, height);

        Activity activity = getActivity();
        CameraManager cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera");
            }
            cameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while waiting to lock camera");
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (mCameraCaptureSession != null) {
                mCameraCaptureSession.close();
                mCameraCaptureSession = null;
            }
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (mImageReader != null) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while closing camera");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private void initPreview() {
        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewBuilder.addTarget(mPreviewSurface);

            List<Surface> surfaces = Arrays.asList(mPreviewSurface, mJpegCaptureSurface);
            mCameraDevice.createCaptureSession(surfaces, mCaptureStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void configureTransform(final int viewWidth, final int viewHeight) {
        Activity activity = getActivity();
        if (mCameraTextureView == null || mPreviewSize == null || activity == null) {
            Log.e(TAG, "NullPointer when trying to configure transform");
            return;
        }

        // Get center coordinates
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();

        // Rotate if necessary
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) viewHeight / mPreviewSize.getHeight(), (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation -2), centerX, centerY);
        } else if (rotation == Surface.ROTATION_180) {
            matrix.postRotate(180, centerX, centerY);
        }

        // Transform texture
        mCameraTextureView.setTransform(matrix);
    }

    /**
     * Picture taking functions
     */

    private void takePicture() {
        lockFocus();
    }

    private void lockFocus() {
        try {
            // Tell camera to lock focus
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

            // Wait for the lock and capture
            mState = STATE_WAITING_LOCK;
            mCameraCaptureSession.capture(mPreviewBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlockFocus() {
        try {
            // Reset auto-focus trigger
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);

            // Reset capture request
            mState = STATE_PREVIEW;
            mCameraCaptureSession.capture(mPreviewBuilder.build(), mCaptureCallback, mBackgroundHandler);
            mCameraCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void preCapture() {
        try {
            // Trigger camera
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);

            // Wait for pre-capture and capture
            mState = STATE_WAITING_PRECAPTURE;
            mCameraCaptureSession.capture(mPreviewBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void capture() {
        try {
            final Activity activity = getActivity();
            if (mCameraDevice == null || activity == null) {
                Log.e(TAG, "Null pointer when attempting to capture picture.");
                return;
            }

            // Build capture request
            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mJpegCaptureSurface);

            // Set auto focus
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            // Fix orientation
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            // Capture photo
            mCameraCaptureSession.stopRepeating();
            mCameraCaptureSession.capture(captureBuilder.build(), mCaptureRequestCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Threading
     */

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper functions
     */

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

    private int getOrientation(int rotation) {
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    /**
     * Request permissions listener
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
                    openCamera(mCameraTextureView.getWidth(), mCameraTextureView.getHeight());
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Surface texture listener functions
    */

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "Surface Texture Available");

        mPreviewSurface = new Surface(surface);
        openCamera(width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        configureTransform(width, height);
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        closeCamera();
        return true;
    }

    /**
     * Created classes
     */

    private class CameraDeviceStateCallback extends CameraDevice.StateCallback {

        @Override
        public void onOpened(CameraDevice camera) {
            mCameraOpenCloseLock .release();
            mCameraDevice = camera;
            initPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
            Log.e(TAG, "Camera Device Error");
        }
    }

    private class CameraCaptureStateCallback extends CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(CameraCaptureSession session) {
            if (mCameraDevice == null) return;

            mCameraCaptureSession = session;
            try {
                // Set auto focus
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                // Set capture as repeating to continually get preview
                mPreviewRequest = mPreviewBuilder.build();
                mCameraCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            CandidUtils.showToast(getActivity(), "Failed");
        }
    }

    private class CameraCaptureRequestCallback extends CameraCaptureSession.CaptureCallback {

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            CandidUtils.showToast(getActivity(), "Saved: " + mPhotoDir);
            unlockFocus();
        }
    }

    private class CameraCaptureCallback extends CameraCaptureSession.CaptureCallback {

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            process(result);
        }

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        capture();
                    } else if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            capture();
                        } else {
                            preCapture();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        capture();
                    }
                }
            }
        }
    }

    private class CameraImageAvailableListener implements ImageReader.OnImageAvailableListener {

        @Override
        public void onImageAvailable(ImageReader reader) {
            File saveFile = new File(mPhotoDir, "newest_candid.jpg");
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), saveFile));
        }
    }

    private static class ImageSaver implements Runnable {
        private final Image mImage;
        private final File mFile;

        public ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            // Get image buffer
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            // Save picture
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(mFile);
                outputStream.write(bytes);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
