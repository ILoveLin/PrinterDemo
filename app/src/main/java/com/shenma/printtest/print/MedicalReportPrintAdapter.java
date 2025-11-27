package com.shenma.printtest.print;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import android.util.Log;

import com.shenma.printtest.widget.MedicalReportView;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 医用报告打印适配器
 * 用于将MedicalReportView渲染为PDF并打印
 */
public class MedicalReportPrintAdapter extends PrintDocumentAdapter {
    private static final String TAG = "MedicalReportPrintAdapter";
    
    private Context mContext;
    private MedicalReportView mReportView;
    private PrintedPdfDocument mPdfDocument;
    private int mTotalPages = 1;
    private int mPaperSize;
    
    public MedicalReportPrintAdapter(Context context, MedicalReportView reportView) {
        this.mContext = context;
        this.mReportView = reportView;
        this.mPaperSize = reportView.getPaperSize();
    }
    
    @Override
    public void onLayout(PrintAttributes oldAttributes,
                        PrintAttributes newAttributes,
                        CancellationSignal cancellationSignal,
                        LayoutResultCallback callback,
                        Bundle extras) {
        
        Log.d(TAG, "onLayout called");
        
        // 创建PDF文档
        mPdfDocument = new PrintedPdfDocument(mContext, newAttributes);
        
        // 检查是否取消
        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }
        
        // 创建文档信息
        PrintDocumentInfo.Builder builder = new PrintDocumentInfo
                .Builder("medical_report.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(mTotalPages);
        
        PrintDocumentInfo info = builder.build();
        
        // 布局完成
        callback.onLayoutFinished(info, true);
    }
    
    @Override
    public void onWrite(PageRange[] pages,
                       ParcelFileDescriptor destination,
                       CancellationSignal cancellationSignal,
                       WriteResultCallback callback) {
        
        Log.d(TAG, "onWrite called");
        
        try {
            // 创建页面
            PdfDocument.Page page = mPdfDocument.startPage(0);
            
            // 检查是否取消
            if (cancellationSignal.isCanceled()) {
                callback.onWriteCancelled();
                mPdfDocument.close();
                mPdfDocument = null;
                return;
            }
            
            // 获取Canvas并绘制报告
            Canvas canvas = page.getCanvas();
            
            // 获取页面尺寸
            int pageWidth = page.getInfo().getPageWidth();
            int pageHeight = page.getInfo().getPageHeight();
            
            String paperSizeName = (mPaperSize == MedicalReportView.PAPER_SIZE_A5) ? "A5" : "A4";
            Log.d(TAG, "打印纸张: " + paperSizeName + ", 页面尺寸: " + pageWidth + " x " + pageHeight);
            
            // 保存画布状态
            canvas.save();
            
            // 测量并布局ReportView
            int widthSpec = android.view.View.MeasureSpec.makeMeasureSpec(pageWidth, android.view.View.MeasureSpec.EXACTLY);
            int heightSpec = android.view.View.MeasureSpec.makeMeasureSpec(pageHeight, android.view.View.MeasureSpec.EXACTLY);
            mReportView.measure(widthSpec, heightSpec);
            mReportView.layout(0, 0, pageWidth, pageHeight);
            
            // 绘制ReportView到Canvas
            mReportView.draw(canvas);
            
            // 恢复画布状态
            canvas.restore();
            
            // 完成页面
            mPdfDocument.finishPage(page);
            
            // 检查是否取消
            if (cancellationSignal.isCanceled()) {
                callback.onWriteCancelled();
                mPdfDocument.close();
                mPdfDocument = null;
                return;
            }
            
            // 写入文件
            try {
                mPdfDocument.writeTo(new FileOutputStream(destination.getFileDescriptor()));
                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
            } catch (IOException e) {
                Log.e(TAG, "Error writing PDF", e);
                callback.onWriteFailed(e.toString());
            } finally {
                mPdfDocument.close();
                mPdfDocument = null;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onWrite", e);
            callback.onWriteFailed(e.toString());
        }
    }
    
    @Override
    public void onFinish() {
        super.onFinish();
        Log.d(TAG, "onFinish called");
        
        if (mPdfDocument != null) {
            mPdfDocument.close();
            mPdfDocument = null;
        }
    }
}
