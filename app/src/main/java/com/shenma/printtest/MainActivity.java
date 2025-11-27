package com.shenma.printtest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.shenma.printtest.report.PrintPdfReportLocalImageActivity;
import com.shenma.printtest.report.PrintPdfReportNetImageActivity;
import com.shenma.printtest.report.PrintPictureReportActivity;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2023/3/15 8:58
 * desc：主界面
 */
public class MainActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_pdf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PrintPdfReportLocalImageActivity.class);
                startActivity(intent);

            }
        });
        findViewById(R.id.tv_pdf_online).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PrintPdfReportNetImageActivity.class);
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
        findViewById(R.id.tv_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, TestReportActivity.class);
//                startActivity(intent);

            }
        });
        findViewById(R.id.tv_demo_a4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, com.shenma.printtest.report.PrintDemoA4Activity.class);
                startActivity(intent);

            }
        });
    }

}