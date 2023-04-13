package com.shenma.printtest.report;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.print.PrintHelper;

import com.shenma.printtest.R;
import com.shenma.printtest.adapter.PdfDocumentAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2023/3/15 8:58
 * desc：网络打印机-----打印图片    比较模糊(报告质量的好坏,取决于图片的分辨率和像素)
 */
public class PrintPictureReportActivity extends AppCompatActivity {

    private TextView mTV, mTVName;
    private View view, mPrintLayout;
    private PrintHelper photoPrinter;
    private LayoutInflater layoutInflater;
    private LinearLayout linear_main;
    private ImageView iv_bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_picture);
        iv_bmp = findViewById(R.id.iv_bmp);
        mTV = findViewById(R.id.tv_click);
        mTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoPrinter = new PrintHelper(PrintPictureReportActivity.this);
                iv_bmp.setImageResource(R.drawable.qqq);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.qqq);

                mTV.setVisibility(View.INVISIBLE);
                mTV.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mTV.setVisibility(View.VISIBLE);

                    }
                }, 1500);
                //打印图片报告
                photoPrinter.printBitmap("droids.bmp - test print", bitmap);
            }
        });


    }


}