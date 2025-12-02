package com.shenma.printtest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.shenma.printtest.report.PrintDemoA3_A4_A5_B5Activity;

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
        //打印照片，打印自定义PDF文档，等等方式。虽然能满足需求，但是我已经不推荐了，因为有更好的推荐，请使用MedicalReportView，自定义View打印即可，高效，高清晰度，健壮性更强
        //打印照片，打印自定义PDF文档，等等方式。虽然能满足需求，但是我已经不推荐了，因为有更好的推荐，请使用MedicalReportView，自定义View打印即可，高效，高清晰度，健壮性更强
        //打印照片，打印自定义PDF文档，等等方式。虽然能满足需求，但是我已经不推荐了，因为有更好的推荐，请使用MedicalReportView，自定义View打印即可，高效，高清晰度，健壮性更强
        //打印照片，打印自定义PDF文档，等等方式。虽然能满足需求，但是我已经不推荐了，因为有更好的推荐，请使用MedicalReportView，自定义View打印即可，高效，高清晰度，健壮性更强
//        findViewById(R.id.tv_pdf).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, PrintPdfReportLocalImageActivity.class);
//                startActivity(intent);
//
//            }
//        });
//        findViewById(R.id.tv_pdf_online).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, PrintPdfReportNetImageActivity.class);
//                startActivity(intent);
//
//            }
//        });
//        findViewById(R.id.tv_picture).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, PrintPictureReportActivity.class);
//                startActivity(intent);
//
//            }
//        });
//        findViewById(R.id.tv_test).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Intent intent = new Intent(MainActivity.this, TestReportActivity.class);
////                startActivity(intent);
//
//            }
//        });
        // 退出按钮
        findViewById(R.id.btn_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 跳转到医用报告打印界面（CardView点击）
        findViewById(R.id.card_demo_a4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PrintDemoA3_A4_A5_B5Activity.class);
                startActivity(intent);
            }
        });
    }

}