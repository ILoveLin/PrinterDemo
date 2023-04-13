package com.shenma.printtest;

import android.content.Context;
import android.content.Intent;
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

import com.shenma.printtest.adapter.PdfDocumentAdapter;
import com.shenma.printtest.report.PrintPdfReportActivity;
import com.shenma.printtest.report.PrintPictureReportActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2023/3/15 8:58
 * desc：主界面
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_pdf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PrintPdfReportActivity.class);
                startActivity(intent);

            }
        });
        findViewById(R.id.tv_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PrintPictureReportActivity.class);
                startActivity(intent);

            }
        });
    }

}