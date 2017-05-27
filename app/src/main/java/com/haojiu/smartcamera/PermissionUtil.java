package com.haojiu.smartcamera;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

public class PermissionUtil {
    private Activity activity;

    public PermissionUtil(Activity activity) {
        this.activity = activity;
    }

    /**
     * 判断是否拥有指定权限
     * @param permissions  要检查的权限
     * @return  若有一个权限没有，就返回false，全部权限都拥有，返回true
     */
    public boolean hasPermissionGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否向用户显示权限请求的依据（向用户解释为什么要申请该权限）
     * @param permissions  要申请的权限
     * @return  若有一个权限应该显示依据，返回true，若全部权限都不应该显示依据，返回false
     */
    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 请求所需所有的权限
     * @param permissions  要请求的权限
     * @param message  权限请求的依据
     * @param requestCode  权限请求码
     */
    public void requestRequiredPermissions(final String[] permissions, int message, final int requestCode) {
        if (shouldShowRequestPermissionRationale(permissions)) {
            //弹出自定义的对话框提示申请权限
            new AlertDialog.Builder(activity)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activity, permissions, requestCode);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.finish();
                        }
                    })
                    .show();
        } else {
            //弹出系统的权限提示框
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }
}
