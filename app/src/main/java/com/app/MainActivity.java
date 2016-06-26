package com.app;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
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
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;

import com.app.Chats.ChatsFragment;
import com.app.Connect.ConnectFragment;
import com.app.NewsFeed.NewsFeedFragment;
import com.app.Profile.ProfileFragment;
import com.app.Utils.CandidUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity implements
        NewsFeedFragment.OnFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener,
        ConnectFragment.OnFragmentInteractionListener,
        ChatsFragment.OnFragmentInteractionListener,
        TextureView.SurfaceTextureListener {

    /**
     * Activity variables
     */
    // Logger tag
    private final static String TAG = "MainActivity";
    // Adapter for the tab sections at the top
    private CandidTabsPagerAdapter mCandidTabsPagerAdapter;
    // The view pager that will hold the tabs
    private CandidViewPager mViewPager;
    // The tab layout to display the sections
    private TabLayout mTabLayout;
    private AppBarLayout mAppBar;
    private int mViewPagerPadding = 0, mStatusBarHeight = 0;

    /**
     * Camera variables
     */
    // Permissions
    private static final int CAMERA_PERMISSIONS = 1;
    private static final int READ_WRITE_PERMISSIONS = 2;
    private static final int LOCATION_PERMISSIONS = 3;
    private static final int PERMISSIONS = 9;
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
    private boolean mCameraOpen = false;
    // Camera Constants
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    // Camera
    private TextureView mCameraTextureView;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set content view for activity
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(getResources().getColor(R.color.transparent));

        // Get the app bar
        mAppBar = (AppBarLayout) findViewById(R.id.appbar);

        // Create the adapter that will return the proper fragment for each of the three
        // tabs in the pager
        mCandidTabsPagerAdapter = new CandidTabsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter
        mViewPager = (CandidViewPager) findViewById(R.id.container);
        if (mViewPager != null) {
            mViewPager.setAdapter(mCandidTabsPagerAdapter);
        }

        // Set up the TabLayout with the view pager
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        if (mTabLayout != null) {
            mTabLayout.setupWithViewPager(mViewPager);
        }
        setupTabLayout();

        // Get view sizes
        mTabLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                mViewPagerPadding = mTabLayout.getHeight() + mAppBar.getPaddingTop();
                mViewPager.setPadding(0, mViewPagerPadding, 0, 0);
                mTabLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        // Setup camera view
        mCameraTextureView = (TextureView) findViewById(R.id.camera_background);
        if (mCameraTextureView != null) {
            mCameraTextureView.setSurfaceTextureListener(this);
        } else {
            Log.e(TAG, "Can't find preview texture view.");
        }

        // Setup photo button
        ImageView takePhotoButton = (ImageView) findViewById(R.id.take_photo_button);
//        if (takePhotoButton != null) {
//            takePhotoButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    capture();
//                }
//            });
//        } else {
//            Log.e(TAG, "Can't find take photo button.");
//        }

        // Check permissions
        boolean hasCameraPermissions = (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
        boolean hasStoragePermissions = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        boolean hasLocationPermissions = (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

        ArrayList<String> permissions = new ArrayList<>();
        if (!hasCameraPermissions) permissions.add(Manifest.permission.CAMERA);
        if (!hasStoragePermissions) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!hasLocationPermissions) permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (!permissions.isEmpty()) {
            String[] permissionsList = permissions.toArray(new String[permissions.size()]);
            CandidUtils.requestPermission(this, permissionsList, !hasCameraPermissions ? CAMERA_PERMISSIONS : PERMISSIONS);
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

    private void setupTabLayout() {
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mTabLayout.setSelectedTabIndicatorHeight(0);
        mTabLayout.setPadding(0, getStatusBarHeight(), 0, 0);

        TabLayout.Tab leftTab = mTabLayout.getTabAt(0);
        if (leftTab != null) {
            View view = getLayoutInflater().inflate(R.layout.tab_profile, null);
            ImageView icon = (ImageView) view.findViewById(R.id.profile_icon);
            icon.setImageResource(R.drawable.profile_icon);
            leftTab.setCustomView(view);
        }

        TabLayout.Tab centerTab = mTabLayout.getTabAt(1);
        if (centerTab != null) {
            View view = getLayoutInflater().inflate(R.layout.tab_candid, null);
            ImageView title = (ImageView) view.findViewById(R.id.candid_title);
            title.setImageResource(R.drawable.title);
            centerTab.setCustomView(view);
            centerTab.select();
        }

        TabLayout.Tab rightTab = mTabLayout.getTabAt(2);
        if (rightTab != null) {
            View view = getLayoutInflater().inflate(R.layout.tab_connect, null);
            ImageView icon = (ImageView) view.findViewById(R.id.connect_icon);
            icon.setImageResource(R.drawable.connect_icon);
            rightTab.setCustomView(view);
        }
    }

    private int getStatusBarHeight() {
        if (mStatusBarHeight == 0) {
            int statusBarHeight = 0;
            int statusBarHeightResource = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (statusBarHeightResource > 0) statusBarHeight = getResources().getDimensionPixelSize(statusBarHeightResource);
            mStatusBarHeight = statusBarHeight;
        }
        return mStatusBarHeight;
    }

    @Override
    public void onResume() {
        super.onResume();
//        startBackgroundThread();
    }

    @Override
    public void onPause() {
//        closeCamera();
//        stopBackgroundThread();
        super.onPause();
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

    /**
     * Camera initialization functions
     */

    private void initCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            CandidUtils.requestPermission(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSIONS);
            return;
        }

        initCamera(width, height);
        configureTransform(width, height);

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
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
        if (mCameraTextureView == null || mPreviewSize == null) {
            Log.e(TAG, "NullPointer when trying to configure transform");
            return;
        }

        // Get center coordinates
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
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
     * Camera picture taking functions
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
            if (mCameraDevice == null) {
                Log.e(TAG, "Null pointer when attempting to capture picture.");
                return;
            }

            // Build capture request
            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mJpegCaptureSurface);

            // Set auto focus
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            // Fix orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            // Capture photo
            mCameraCaptureSession.stopRepeating();
            mCameraCaptureSession.capture(captureBuilder.build(), mCaptureRequestCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Camera thread functions
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
     * Camera helper functions
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
            case CAMERA_PERMISSIONS: {
                boolean cameraPermissionGranted = false;
                for (int idx = 0; idx < permissions.length; idx++) {
                    if (Objects.equals(permissions[idx], Manifest.permission.CAMERA) && grantResults[idx] == PackageManager.PERMISSION_GRANTED) {
                        cameraPermissionGranted = true;
                        break;
                    }
                }

                if (!cameraPermissionGranted) {
                    CandidUtils.ErrorDialog.newInstance("Did not receive camera permissions").show(getSupportFragmentManager(), "dialog");
                } else {
//                    openCamera(mCameraTextureView.getWidth(), mCameraTextureView.getHeight());
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
//        openCamera(width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//        configureTransform(width, height);
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//        closeCamera();
        return true;
    }

    /**
     * Camera callback classes
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
            CandidUtils.showToast(getParent(), "Failed");
        }
    }

    private class CameraCaptureRequestCallback extends CameraCaptureSession.CaptureCallback {

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            CandidUtils.showToast(getParent(), "Saved: " + mPhotoDir);
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

    /**
     * CandidTabsPagerAdapter
     * Handles paging for the tab sections
     */
    private class CandidTabsPagerAdapter extends FragmentPagerAdapter {

        public final static int PROFILE_TAB = 0;
        public final static int MAIN_TAB = 1;
        public final static int CONNECT_TAB = 2;

        public CandidTabsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case PROFILE_TAB: {
                    return ProfileFragment.newInstance();
                }
                case MAIN_TAB: {
                    return NewsFeedFragment.newInstance();
                }
                case CONNECT_TAB: {
                    return ConnectFragment.newInstance();
                }
                default: return new Fragment();
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

    /**
     * Fragment interaction
     */
    @Override
    public void onFragmentInteraction(Uri uri) {}

    @Override
    public void onChatFragmentOpened() {
        mViewPager.setSwipeEnabled(false);
    }

    @Override
    public void onChatFragmentClosed() {
        mViewPager.setSwipeEnabled(true);
    }

    @Override
    public void showCamera() {
        // Hide app bar
        mAppBar.animate().y(-mAppBar.getHeight()).setStartDelay(0).setDuration(500).start();

        // Resize view pager
        ValueAnimator animator = ValueAnimator.ofInt(mViewPager.getPaddingTop(), -mViewPagerPadding);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mViewPager.setPadding(0, Integer.parseInt(animation.getAnimatedValue().toString()), 0, 0);
            }
        });
        animator.setDuration(770);
        animator.start();

        // Hide status bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Set camera as opened
        mCameraOpen = true;
    }

    private void hideCamera() {
        // Show app bar
        mAppBar.animate().y(0).setStartDelay(270).setDuration(500).start();

        // Resize view pager
        ValueAnimator animator = ValueAnimator.ofInt(mViewPager.getPaddingTop(), mViewPagerPadding);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mViewPager.setPadding(0, Integer.parseInt(animation.getAnimatedValue().toString()), 0, 0);
            }
        });
        animator.setDuration(770);
        animator.start();

        // Show status bar
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Set camera as closed
        mCameraOpen = false;
    }

    /**
     * Back button
     */
    @Override
    public void onBackPressed() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            String fragmentClassName = fragment.getClass().getName();
            if (Objects.equals(fragmentClassName, ConnectFragment.class.getName())) {
                FragmentManager fragmentManager = fragment.getChildFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
                    return;
                }
            } else if (Objects.equals(fragmentClassName, NewsFeedFragment.class.getName())) {
                if (mCameraOpen) {
                    hideCamera();
                    ((NewsFeedFragment) fragment).hideCamera();
                    return;
                }
            }
        }
        super.onBackPressed();
    }
}
