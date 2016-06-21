package com.app.Utils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.Serializable;

/**
 * Created by brent on 2016-06-16.
 */
public class CandidUtils {

    /**
     * Permissions
     */

    private final static String PERMISSIONS_KEY = "permissions";
    private final static String REQUEST_CODE_KEY = "request";

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
}
