package com.shenma.printtest.util;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2023/3/17 14:31
 * desc：
 */

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileUtils {

    /**
     * 获取包含文件的应用程序路径
     *
     * @return String 根目录路径
     */
    public static String getAppPath() {
        File dir = new File(android.os.Environment.getExternalStorageDirectory()
                + File.separator
                + "PDF"
                + File.separator);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getPath() + File.separator;
    }

    /**
     * 打开PDF文件
     * @param context
     * @param url
     * @throws ActivityNotFoundException
     * @throws IOException
     */
    public static void openFile(Context context, File url) throws ActivityNotFoundException {
        if (url.exists()) {
            Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", url);

            String urlString = url.toString().toLowerCase();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            /**
             * Security
             */
            List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            // 通过比较url和扩展名，检查您要打开的文件类型。
            // 当if条件匹配时，插件设置正确的意图(mime)类型
            // 所以Android知道用什么程序打开文件
            if (urlString.toLowerCase().contains(".doc")
                    || urlString.toLowerCase().contains(".docx")) {
                // Word document
                intent.setDataAndType(uri, "application/msword");
            } else if (urlString.toLowerCase().contains(".pdf")) {
                // PDF file
                intent.setDataAndType(uri, "application/pdf");
            } else if (urlString.toLowerCase().contains(".ppt")
                    || urlString.toLowerCase().contains(".pptx")) {
                // Powerpoint file
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            } else if (urlString.toLowerCase().contains(".xls")
                    || urlString.toLowerCase().contains(".xlsx")) {
                // Excel file
                intent.setDataAndType(uri, "application/vnd.ms-excel");
            } else if (urlString.toLowerCase().contains(".zip")
                    || urlString.toLowerCase().contains(".rar")) {
                // ZIP file
                intent.setDataAndType(uri, "application/trap");
            } else if (urlString.toLowerCase().contains(".rtf")) {
                // RTF file
                intent.setDataAndType(uri, "application/rtf");
            } else if (urlString.toLowerCase().contains(".wav")
                    || urlString.toLowerCase().contains(".mp3")) {
                // WAV/MP3 audio file
                intent.setDataAndType(uri, "audio/*");
            } else if (urlString.toLowerCase().contains(".gif")) {
                // GIF file
                intent.setDataAndType(uri, "image/gif");
            } else if (urlString.toLowerCase().contains(".jpg")
                    || urlString.toLowerCase().contains(".jpeg")
                    || urlString.toLowerCase().contains(".png")) {
                // JPG file
                intent.setDataAndType(uri, "image/jpeg");
            } else if (urlString.toLowerCase().contains(".txt")) {
                // Text file
                intent.setDataAndType(uri, "text/plain");
            } else if (urlString.toLowerCase().contains(".3gp")
                    || urlString.toLowerCase().contains(".mpg")
                    || urlString.toLowerCase().contains(".mpeg")
                    || urlString.toLowerCase().contains(".mpe")
                    || urlString.toLowerCase().contains(".mp4")
                    || urlString.toLowerCase().contains(".avi")) {
                // Video files
                intent.setDataAndType(uri, "video/*");
            } else {
                // 如果你愿意，你也可以为任何其他文件定义意图类型
                // 另外，使用下面的else子句来管理其他未知扩展
                // 在这种情况下，Android将显示设备上安装的所有应用程序
                // 因此您可以选择使用哪个应用程序
                intent.setDataAndType(uri, "*/*");
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "文件不存在", Toast.LENGTH_SHORT).show();
        }
    }

}