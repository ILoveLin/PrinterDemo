package com.shenma.printtest.adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2023/3/20 15:54
 * desc：自定义高精度报告适配器,传入PDF文件,打印
 */
public class PdfDocumentAdapter extends PrintDocumentAdapter {

    Context context = null;
    String pathName = "";

    public PdfDocumentAdapter(Context ctxt, String pathName) {
        context = ctxt;
        this.pathName = pathName;
    }

    @Override
    public void onLayout(PrintAttributes printAttributes, PrintAttributes printAttributes1, CancellationSignal cancellationSignal, LayoutResultCallback layoutResultCallback, Bundle bundle) {
        if (cancellationSignal.isCanceled()) {
            layoutResultCallback.onLayoutCancelled();
        } else {
            PrintDocumentInfo.Builder builder =
                    new PrintDocumentInfo.Builder(" file name");
            builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build();
            layoutResultCallback.onLayoutFinished(builder.build(),
                    !printAttributes1.equals(printAttributes));
        }
    }

    @Override
    public void onWrite(PageRange[] pageRanges, ParcelFileDescriptor parcelFileDescriptor, CancellationSignal cancellationSignal, WriteResultCallback writeResultCallback) {
        InputStream in = null;
        OutputStream out = null;
        try {
            File file = new File(pathName);
            in = new FileInputStream(file);
            out = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());

            byte[] buf = new byte[16384];
            int size;

            while ((size = in.read(buf)) >= 0
                    && !cancellationSignal.isCanceled()) {
                out.write(buf, 0, size);
            }

            if (cancellationSignal.isCanceled()) {
                writeResultCallback.onWriteCancelled();
            } else {
                writeResultCallback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
            }
        } catch (Exception e) {
            writeResultCallback.onWriteFailed(e.getMessage());
            Log.e("PdfDocumentAdapter ", e + "");
        } finally {
            try {
                in.close();
                out.close();
            } catch (Exception e) {
                Log.e("PdfDocumentAdapter ", e + "");

            }
        }
    }
}