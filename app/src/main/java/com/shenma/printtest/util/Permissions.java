package com.shenma.printtest.util;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2023/3/17 14:31
 * desc：
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

public class Permissions {

    /**
     * 弹出权限提示框
     */
    public static void showPermissionsSettingDialog(Context context, String permission) {
        String msg = "";
        if (permission.equals("android.permission.READ_EXTERNAL_STORAGE") ||
                permission.equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
            msg= "本App需要“允许储存空间”权限才能正常运行，请点击确定，进入设置界面进行授权处理";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showSettings(context);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    /**
     * 如果授权失败，就要进入App权限设置界面
     */
    public static void showSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}