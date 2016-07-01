package com.app.Utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/**
 * Created by brent on 2016-06-16.
 */
public class CandidUtils {

    /**
     * Variables
     */
    public static String TAG = "CANDID_UTILS";

    /**
     * Permissions
     */
    private final static String PERMISSIONS_KEY = "permissions";
    private final static String REQUEST_CODE_KEY = "request";

    @TargetApi(Build.VERSION_CODES.M)
    public static void requestPermission(@NonNull final AppCompatActivity activity, final String[] permissions, final int requestCode) {
        if (activity.shouldShowRequestPermissionRationale(permissions[0])) {
            PermissionRequestDialog permissionDialog = PermissionRequestDialog.newInstance(permissions, requestCode);
            permissionDialog.show(activity.getSupportFragmentManager(), "dialog");
        } else {
            activity.requestPermissions(permissions, requestCode);
        }
    }

    public static void requestPermission(@NonNull final Fragment fragment, final String[] permissions, final int requestCode) {
        if (fragment.shouldShowRequestPermissionRationale(permissions[0])) {
            PermissionRequestDialog permissionDialog = PermissionRequestDialog.newInstance(permissions, requestCode);
            permissionDialog.show(fragment.getChildFragmentManager(), "dialog");
        } else {
            fragment.requestPermissions(permissions, requestCode);
        }
    }

    public static class PermissionRequestDialog extends DialogFragment {
        private String[] mPermissions;
        private int mRequestCode;

        public static PermissionRequestDialog newInstance(final String[] permissions, final int requestCode) {
            Bundle bundle = new Bundle();
            bundle.putStringArray(PERMISSIONS_KEY, permissions);
            bundle.putInt(REQUEST_CODE_KEY, requestCode);
            PermissionRequestDialog dialog = new PermissionRequestDialog();
            dialog.setArguments(bundle);
            return dialog;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Bundle args = getArguments();
            mPermissions = args.getStringArray(PERMISSIONS_KEY);
            mRequestCode = args.getInt(REQUEST_CODE_KEY);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage("Request Camera Permissions")
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (parent == null) {
                                        ActivityCompat.requestPermissions(getActivity(), mPermissions, mRequestCode);
                                    } else {
                                        parent.requestPermissions(mPermissions, mRequestCode);
                                    }
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Do nothing
                                }
                            })
                    .create();
        }
    }

    /**
     * UI Dialogs, Popups and Toasts
     */
    public static void showToast(@NonNull final Activity activity, final String text) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Do nothing
                                }
                            })
                    .create();
        }
    }

    /**
     * Bitmaps
     */
    private static LruCache<String, Bitmap> mAppCache;

    public static void setupCache() {
        // Setup cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        Log.d(TAG, "Cache Size: " + cacheSize);
        mAppCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
            }
        };
    }

    public static void loadBitmap(final int imageResource, ImageView imageView, Context context) {
        final String imageKey = String.valueOf(imageResource);
        final Bitmap bitmap = getBitmapFromCache(imageKey);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else if (shouldCancelTask(imageResource, imageView)) {
            final BitmapLoaderTask task = new BitmapLoaderTask(imageView, mAppCache, context);
            final AsyncBitmapDrawable asyncBitmapDrawable = new AsyncBitmapDrawable(null, task, context);
            imageView.setImageDrawable(asyncBitmapDrawable);
            task.execute(imageResource);
        }
    }

    public static Bitmap getBitmapFromCache(String key) {
        if (mAppCache != null) {
            return mAppCache.get(key);
        } else {
            return null;
        }
    }

    public static void addBitmapToCache(String key, Bitmap bitmap) {
        if (getBitmapFromCache(key) == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Log.d(TAG, "Saving bitmap of size: " + bitmap.getAllocationByteCount());
            }
            mAppCache.put(key, bitmap);
        }
    }

    public static boolean shouldCancelTask(final int imageResource, ImageView imageView) {
        final BitmapLoaderTask task = getBitmapLoaderTask(imageView);

        if (task != null) {
            final int bitmapResource = task.mBitmapResource;
            if (bitmapResource == 0 || bitmapResource != imageResource) {
                task.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    public static class BitmapLoaderTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageView> mImageViewReference;
        private final WeakReference<Context> mContextReference;
        private final WeakReference<LruCache<String, Bitmap>> mCacheReference;
        private int mBitmapResource;

        public BitmapLoaderTask(ImageView imageView, LruCache cache, Context context) {
            mImageViewReference = new WeakReference<>(imageView);
            mContextReference = new WeakReference<>(context);
            mCacheReference = new WeakReference<LruCache<String, Bitmap>>(cache);
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            mBitmapResource = params[0];
            Bitmap bitmap = scaleBitmap(mBitmapResource, 100, 175, mContextReference.get());
            addBitmapToCache(String.valueOf(mBitmapResource), bitmap);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) bitmap = null;

            if (bitmap != null) {
                final ImageView imageView = mImageViewReference.get();
                final BitmapLoaderTask task = getBitmapLoaderTask(imageView);
                if (this == task) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    public static Bitmap scaleBitmap(final int imageResource, final int targetWidth, final int targetHeight, Context context) {
        // Get bitmap size
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), imageResource, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;

        // Calculate smallest in sample size
        int inSampleSize = 1;
        if (imageHeight > targetHeight || imageWidth > targetWidth) {
            final int halfHeight = imageHeight / 2;
            final int halfWidth = imageWidth / 2;
            while ((halfHeight / inSampleSize) > targetHeight && (halfWidth / inSampleSize) > targetWidth) inSampleSize *= 2;
        }
        Log.d(TAG, "Image: " + imageWidth + " x " + imageHeight + "     Target: " + targetWidth + " x " + targetHeight + "     inSampleSize: " + inSampleSize);
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeResource(context.getResources(), imageResource, options);
    }

    public static class AsyncBitmapDrawable extends BitmapDrawable {
        private final WeakReference<BitmapLoaderTask> mBitmapTaskReference;

        public AsyncBitmapDrawable(Bitmap bitmap, BitmapLoaderTask task, Context context) {
            super(context.getResources(), bitmap);
            mBitmapTaskReference = new WeakReference<>(task);
        }

        public BitmapLoaderTask getBitmapTask() {
            return mBitmapTaskReference.get();
        }
    }

    public static BitmapLoaderTask getBitmapLoaderTask(@NonNull ImageView imageView) {
        final Drawable drawable = imageView.getDrawable();
        if (drawable instanceof AsyncBitmapDrawable) {
            final AsyncBitmapDrawable bitmap = (AsyncBitmapDrawable) drawable;
            return bitmap.getBitmapTask();
        }
        return null;
    }
}
