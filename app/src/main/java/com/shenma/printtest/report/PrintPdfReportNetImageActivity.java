package com.shenma.printtest.report;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.print.PrintAttributes;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.TextAlignment;
import com.shenma.printtest.R;
import com.shenma.printtest.adapter.PdfDocumentAdapter;
import com.shenma.printtest.util.CommonUtils;
import com.shenma.printtest.util.LabelBean;
import com.shenma.printtest.util.LogUtils;
import com.shenma.printtest.util.SaxHelper2Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import es.voghdev.pdfviewpager.library.PDFViewPager;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2023/3/15 8:58
 * desc：
 * desc：网络打印机-----自定义pdf格式打印      高精度
 * <p>
 * 报告中的图片是网络图片url
 * <p>
 * <p>
 * 如果网络图片不存在  会闪退哦  记得使用网络可以访问到的图片url
 */
public class PrintPdfReportNetImageActivity extends AppCompatActivity {
    private Button create_pdf;
    private Button open_pdf, print_report;
    private String path;
    private Context context;
    private boolean isPermissions = false;
    private String absolutePath;
    private PDFView pdfView;
    private String mImagePath;
    private Document document;
    private PdfFont font;
    private DeviceRgb text_title_color;
    private DeviceRgb text_color;
    private ArrayList<LabelBean> mRepoerLabelList;
    private Paragraph mTitleLayout;
    private Paragraph mLayout;
    private ArrayList<LabelBean> mLogoList;
    private ArrayList<LabelBean> mImageList;
    private ArrayList<LabelBean> mLineList;
    private ArrayList<LabelBean> mTextList;
    private String mFontPath;
    private ArrayList<LabelBean> mImageAreaList;
    private String mLogoPath;
    private Button mSizeA3;
    private Button mSizeB5;
    private Button mSizeA4;
    private PageSize mPageSize;
    private DeviceRgb title_color;
    private String httpPictureUrl;
    private TextView mFontSetting;
    private PdfDocumentAdapter mPdfDocumentAdapter;
    private PrintJob printReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_pdf_network);
        requestPermission();
        initView();
        initData();
        onResponseListener();
    }

    private Button mBtnImage1, mBtnImage2, mBtnImage3, mBtnImage4, mBtnImage5;
    private Button mBtnImage6, mBtnImage7, mBtnImage8, mBtnImage9;
    private int mCurrentImageCount = 6; // 默认6张图

    private void initView() {
        create_pdf = findViewById(R.id.create_pdf);
        open_pdf = findViewById(R.id.open_pdf);
        pdfView = findViewById(R.id.pdfView);
        print_report = findViewById(R.id.print_report);
        mFontSetting = findViewById(R.id.set_font_song_ti);
        mSizeA3 = findViewById(R.id.btn_a3);
        mSizeA4 = findViewById(R.id.btn_a4);
        mSizeB5 = findViewById(R.id.btn_b5);
        
        // 初始化图片数量按钮
        mBtnImage1 = findViewById(R.id.btn_image_1);
        mBtnImage2 = findViewById(R.id.btn_image_2);
        mBtnImage3 = findViewById(R.id.btn_image_3);
        mBtnImage4 = findViewById(R.id.btn_image_4);
        mBtnImage5 = findViewById(R.id.btn_image_5);
        mBtnImage6 = findViewById(R.id.btn_image_6);
        mBtnImage7 = findViewById(R.id.btn_image_7);
        mBtnImage8 = findViewById(R.id.btn_image_8);
        mBtnImage9 = findViewById(R.id.btn_image_9);
        
        context = getApplicationContext();
        path = Environment.getExternalStorageDirectory() + "/report_network.pdf";
        mImagePath = Environment.getExternalStorageDirectory() + "/C_CME/001.jpg";

        String str = "请先点击(1-9图片数量按钮),然后点击(A3/A4/B5纸张类型),再点击(生成PDF文件),最后点击(打开PDF文件或打印报告)";
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
        byte[] bytes = readStream("STSONG.TTF");
        //读取自定义字体 宋体
        String fontPath = Environment.getExternalStorageDirectory() + "/CME_PDF/STSONG.TTF";
        File file = new File(fontPath);
        if (file.exists()) {
            file.delete();
        }
        file.getParentFile().mkdirs();

        boolean simsun = writeFile(bytes, "CME_PDF", "STSONG.TTF");
        Log.e("readXmlForSAX", "simsun==size==" + simsun);

        File file2 = new File(fontPath);
        Log.e("readXmlForSAX", "simsun==file.exists()==" + file2.exists());


    }

    private void initData() {
        try {
            mRepoerLabelList = readA4XmlForSAX();
        } catch (Exception e) {
            Log.e("readXmlForSAX", "readXmlForSAX解析出错");
            e.printStackTrace();
        }
    }

    //是否设置宋体字体  true=设置过了,false=未设置(默认选项)
    private boolean mTagSettingFont = false;

    private void onResponseListener() {
        findViewById(R.id.black).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mFontSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTagSettingFont = true;


            }
        });

        //生成PDF文件
        create_pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getListData();

                for (int i = 0; i < mRepoerLabelList.size(); i++) {
                    LabelBean labelBean = mRepoerLabelList.get(i);
                }

                createReportPDF(path, mRepoerLabelList, mPageSize);
            }
        });
        //打开PDF文件
        open_pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
                openPDF();
            }
        });
        mSizeA3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPageSize = PageSize.A3;

            }
        });
        mSizeA4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPageSize = PageSize.A4;

            }
        });
        mSizeB5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPageSize = PageSize.B5;


            }
        });
        //打印报告
        print_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打印PDF高精度报告
                onPrintPdf(path);
            }
        });
        
        // 设置图片数量按钮的点击事件
        View.OnClickListener imageCountClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int imageCount = 0;
                if (v.getId() == R.id.btn_image_1) imageCount = 1;
                else if (v.getId() == R.id.btn_image_2) imageCount = 2;
                else if (v.getId() == R.id.btn_image_3) imageCount = 3;
                else if (v.getId() == R.id.btn_image_4) imageCount = 4;
                else if (v.getId() == R.id.btn_image_5) imageCount = 5;
                else if (v.getId() == R.id.btn_image_6) imageCount = 6;
                else if (v.getId() == R.id.btn_image_7) imageCount = 7;
                else if (v.getId() == R.id.btn_image_8) imageCount = 8;
                else if (v.getId() == R.id.btn_image_9) imageCount = 9;
                
                mCurrentImageCount = imageCount;
                
                // 自动加载并渲染
                readReportChooseImageNum();
                
                Toast.makeText(PrintPdfReportNetImageActivity.this, 
                    "已选择" + imageCount + "张图片模板", Toast.LENGTH_SHORT).show();
            }
        };
        
        mBtnImage1.setOnClickListener(imageCountClickListener);
        mBtnImage2.setOnClickListener(imageCountClickListener);
        mBtnImage3.setOnClickListener(imageCountClickListener);
        mBtnImage4.setOnClickListener(imageCountClickListener);
        mBtnImage5.setOnClickListener(imageCountClickListener);
        mBtnImage6.setOnClickListener(imageCountClickListener);
        mBtnImage7.setOnClickListener(imageCountClickListener);
        mBtnImage8.setOnClickListener(imageCountClickListener);
        mBtnImage9.setOnClickListener(imageCountClickListener);
    }


    private void readReportChooseImageNum() {
        try {
            //解析 图像模板(比如5图,输入5)
            mImageAreaList = readAreaForSAX(String.valueOf(mCurrentImageCount));
            Toast.makeText(PrintPdfReportNetImageActivity.this, "读取图片模板-完毕", Toast.LENGTH_SHORT).show();

            Log.e("readXmlForSAX", "imageAreaList==size==" + mImageAreaList.size());
            //调整,新图像区域,镜检所见的位置
            adjustReportLayout();
            //安装不同类型创建集合
            getListData();


        } catch (Exception e) {
            Log.e("readXmlForSAX", "imageAreaList==Exception==");

            e.printStackTrace();
        }
    }

    /**
     * 调整报告布局
     */
    private void adjustReportLayout() {
        int nBottom = 1;
        int nLastBottom = 1;
        int nNum = 0;
        int vpimage1 = 0;  // 默认图片区域高度（一图报表.xml中的图像区域高度）
        int vpimage2 = 0;  // 选中模板的图片区域高度
        int mSeeResultTop = 0;   //镜检诊断: 的top值
        int mSeeContentOriginalTop = 0;  // 镜检所见内容原始Top值
        int mSeeContentOriginalBottom = 0;  // 镜检所见内容原始Bottom值
        int mSeeContentOriginalHeight = 0;  // 镜检所见内容原始高度
        
        try {
            if (null == mImageAreaList || mRepoerLabelList.size() == 0) {
                LogUtils.e("调整布局:===adjustReportLayout == 数据加载失败!==mReportLabelList.size() " + mRepoerLabelList.size());
                LogUtils.e("调整布局:===adjustReportLayout == 数据加载失败!==mImageAreaList.size() " + mImageAreaList.size());
            }
        } catch (Exception e) {
            LogUtils.e("调整布局:===adjustReportLayout == 数据加载失败!==e" + e);
        } finally {
            LogUtils.e("调整布局:===adjustReportLayout == 数据加载失败!==1210");
        }

        // 第一步：获取原始模板中的图像区域高度和镜检所见/诊断的位置信息
        for (int i = 0; i < mRepoerLabelList.size(); i++) {
            LabelBean mBean = mRepoerLabelList.get(i);
            LogUtils.e("调整布局:===镜检所见==mReportLabelList.get(i)===" + mRepoerLabelList.get(i).toString());

            if (mBean.getType().equals("图像区域")) {
                //当前 label 列表中(升序)   图像区域对应的角标
                nNum = i;
                vpimage1 = Integer.parseInt(mBean.getBottom()) - Integer.parseInt(mBean.getTop());
            }
            
            // 获取镜检所见内容的原始位置
            if (mBean.getContent().equals("镜检所见") && mBean.getType().equals("Edit")) {
                mSeeContentOriginalTop = Integer.parseInt(mBean.getTop());
                mSeeContentOriginalBottom = Integer.parseInt(mBean.getBottom());
                mSeeContentOriginalHeight = mSeeContentOriginalBottom - mSeeContentOriginalTop;
            }
            
            //这里是为了获取 镜检诊断: 标题的Top值
            if (mBean.getContent().equals("镜检诊断：") && mBean.getOrder().equals("47")) {
                //当前 label 列表中(升序)   图像区域对应的角标
                mSeeResultTop = Integer.parseInt(mBean.getTop()) - 2;
            }
        }

        // 第二步：计算选中模板的图像区域高度
        int maxImageBottom = 0;
        int minImageTop = Integer.MAX_VALUE;
        for (int i = 0; i < mImageAreaList.size(); i++) {
            LabelBean mBean = mImageAreaList.get(i);
            int mNewBottom = (Integer.parseInt(mBean.getBottom()) + nBottom);
            int mNewTop = (Integer.parseInt(mBean.getTop()) + nBottom);
            mBean.setTop(mNewTop + "");
            mBean.setBottom(mNewBottom + "");
            nLastBottom = mNewBottom;
            
            // 找到所有图像元素的最大Bottom和最小Top，计算实际图像区域高度
            if (mBean.getType().equals("Image") || mBean.getType().equals("imgRect") || 
                mBean.getType().equals("ImageDesc") || mBean.getType().equals("ImageSketch")) {
                if (mNewBottom > maxImageBottom) {
                    maxImageBottom = mNewBottom;
                }
                if (mNewTop < minImageTop) {
                    minImageTop = mNewTop;
                }
            }
        }
        
        // 计算选中模板的实际图像区域高度
        vpimage2 = maxImageBottom - minImageTop;
        
        LogUtils.e("调整布局:===vpimage1(默认图像高度)=" + vpimage1);
        LogUtils.e("调整布局:===vpimage2(选中模板图像高度)=" + vpimage2);
        LogUtils.e("调整布局:===镜检所见原始高度=" + mSeeContentOriginalHeight);

        // 第三步：调整图像区域以下的项目
        int t = 0;
        int tF = 0;
        int tz = 10;
        int mBottom = 0;
        
        // 图像高度差值
        int heightDiff = vpimage2 - vpimage1;
        
        ////nNum 代表图片区域的:角标
        for (int i = nNum + 1; i < mRepoerLabelList.size(); i++) {
            LabelBean mBean = mRepoerLabelList.get(i);
            int top = Integer.parseInt(mBean.getTop());
            int bottom = Integer.parseInt(mBean.getBottom());
            String order = mBean.getOrder();
            String type = mBean.getType();
            String content = mBean.getContent();
            
            if (i == nNum + 1) {
                //vpimage2 	加载图片模板之后:图片区域高度	vpimage1默认图片区域高度
                t = vpimage2 - vpimage1;// t其实是差值
                nBottom = (nLastBottom - top) - t;
            }
            
            //调整分界线的位置
            if (order.equals("7") && tF == 0) {
                Log.e("调整layout布局", "mBean====" + mBean.toString());
                tF = 1;
                mBean.setTop((top + nBottom + t + tz) + "");
                mBean.setBottom((bottom + nBottom + t + tz) + "");
                Log.e("调整layout布局", "mBean==后==" + mBean.toString());
            }

            //调整分界线（图像区域下方的第一条分界线）
            if (order.equals("7") && content.equals("分界线") && top > 400 && tF == 0) {
                tF = 1;
                // 这条分界线已经在前面调整过了，跳过
                continue;
            }
            
            //调整镜检所见标题的位置
            if (order.equals("46") && type.equals("Caption")) {
                mBean.setTop((top + nBottom + t + tz) + "");
                mBean.setBottom((bottom + nBottom + t + tz) + "");
                mBottom = Integer.parseInt((bottom + nBottom + t + tz) + "") + 1;
                LogUtils.e("调整布局:===镜检所见标题 newTop=" + mBean.getTop() + ", newBottom=" + mBean.getBottom());
            }
            //调整镜检所见--内容的位置和高度
            else if (order.equals("46") && content.equals("镜检所见")) {
                // 新的Top位置
                int newTop = mBottom;
                // 新的Bottom位置 = 镜检诊断标题的Top - 2
                int newBottom = mSeeResultTop + heightDiff;
                
                mBean.setTop(newTop + "");
                mBean.setBottom(newBottom + "");
                
                LogUtils.e("调整布局:===镜检所见内容 newTop=" + newTop + ", newBottom=" + newBottom + ", 新高度=" + (newBottom - newTop));
            }
            //调整镜检诊断及之后的所有元素（根据Top位置判断，而不是order）
            // 镜检诊断的原始Top是832，所有Top >= 832的元素都需要调整
            else if (top >= 832) {
                // 对于镜检诊断及之后的所有元素，统一按照高度差值进行调整
                int newTop = top + heightDiff;
                int newBottom = bottom + heightDiff;
                mBean.setTop(newTop + "");
                mBean.setBottom(newBottom + "");
                LogUtils.e("调整布局:===调整元素[" + content + "] order=" + order + " type=" + type + " originalTop=" + top + " newTop=" + newTop + ", newBottom=" + newBottom);
            }
        }
    }

    private void createReportPDF(String path, ArrayList<LabelBean> labelBeansList, PageSize mPageSize) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        file.getParentFile().mkdirs();

        // 创建Document
        PdfWriter writer = null;
        try {
            writer = new PdfWriter(new FileOutputStream(path));
        } catch (FileNotFoundException e) {
            Log.e("FileNotFoundException", e.toString());
        }

        PdfDocument pdf_document = new PdfDocument(writer);
        // 生成的PDF文档信息
        PdfDocumentInfo info = pdf_document.getDocumentInfo();
        // 标题
        info.setTitle("First pdf file");
        // 作者
        info.setAuthor("Quinto");
        // 科目
        info.setSubject("test");
        // 关键词
        info.setKeywords("pdf");
        // 创建日期
        info.setCreator("2022-10-20");
        document = new Document(pdf_document, mPageSize, false);
        document.setMargins(0, 0, 0, 0);

        // 文字字体（显示中文）、大小、颜色
        font = null;
        try {
            //未设置的时候使用默认字体
            if (mTagSettingFont) {
                String s = Environment.getExternalStorageDirectory() + "/CME_PDF/STSONG.TTF";
                font = PdfFontFactory.createFont(s, PdfEncodings.IDENTITY_H, true);
                String str = "设置pdf字体格式为:宋体(window版本)";
                Toast.makeText(PrintPdfReportNetImageActivity.this, str, Toast.LENGTH_SHORT).show();

            } else {
                font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");

            }


        } catch (IOException e) {
            Log.e("IOException--字体问题", e.toString());
            Log.e("IOException", e.toString());
        }
//        STSONG.TTF


        title_color = CommonUtils.getColorRgba(32768);
        text_title_color = new DeviceRgb(65, 136, 160);
        text_color = new DeviceRgb(43, 43, 43);
//       mLogoList,mLineList,mImageList,mTextList

//        1,画logo
//        for (int i = 0; i < mLogoList.size(); i++) {
//            LabelBean mBean = mLogoList.get(i);
//            absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
//            Paragraph imageLogo = null;
//            mLogoPath = absolutePath + "/C_CME/image_01.jpg";
//            mImagePath = absolutePath + "/C_CME/image_03.png";
//
//            float left = CommonUtils.getScaleLeft2Right(Float.parseFloat(mBean.getLeft()), mPageSize);
//            float right = CommonUtils.getScaleLeft2Right(Float.parseFloat(mBean.getRight()), mPageSize);
//            float top = CommonUtils.getScaleTop2Bottom(Float.parseFloat(mBean.getTop()), mPageSize);
//            float bottom = CommonUtils.getScaleTop2Bottom(Float.parseFloat(mBean.getBottom()), mPageSize);
//            float mFixBottom = CommonUtils.getScaleFixTopNum(bottom, mPageSize);
//            float mWidth = right - left;
//            float mHeight = bottom - top;
//            //添加logo
//            try {
//
//                Image image5 = new Image(ImageDataFactory.create("http://rongcloud-web.qiniudn.com/docs_demo_rongcloud_logo.png"));
//                image5.setWidth(mWidth);
//                image5.setHeight(mHeight);
//                imageLogo = new Paragraph().add(image5);
//                //left 设置20  默认右移动了20
//                //top 设置20  默认下移动了20
//                //right 设置20  默认左移动了20
//                //bottom 设置20  默认上移动了20
//                imageLogo.setFixedPosition(1, left, mFixBottom, mWidth);
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//                Log.e("PrintPdfReportActivity", "画logo的时候出现异常:362行,图片不存在,请在手机本地路径下添加image_01.jpg的图片");
//                Log.e("PrintPdfReportActivity", "图片不存在,mLogoPath=" + mLogoPath);
//            }
////            document.add(imageLogo);
//            document.add(imageLogo);
//
//        }

        //2,画Line
        for (int i = 0; i < mLineList.size(); i++) {
            LabelBean mBean = mLineList.get(i);

            float left = CommonUtils.getScaleLeft2Right(Float.parseFloat(mBean.getLeft()), mPageSize);
            float right = CommonUtils.getScaleLeft2Right(Float.parseFloat(mBean.getRight()), mPageSize);
            float top = CommonUtils.getScaleTop2Bottom(Float.parseFloat(mBean.getTop()), mPageSize);
            float bottom = CommonUtils.getScaleTop2Bottom(Float.parseFloat(mBean.getBottom()), mPageSize);
            float mWidth = right - left;
            float mHeight = bottom - top;
            float mFontSize = CommonUtils.getScaleFontSizeNum(Float.parseFloat(mBean.getFontSize()), mPageSize);
            //添加line
            LineSeparator mLine = new LineSeparator(new SolidLine());
            //1:加粗,0:正常
            if (mBean.getBold().equals("1")) {
                mLine.setBold();
            }
            mLine.setRelativePosition(left, top - 2, right, 0);
//            mLine.setRelativePosition(left, top - 2, right, 0);
            mLine.setWidth(mWidth);
            document.add(mLine);

        }
        LogUtils.e("Task:===Task == 开始前=mImageAreaList.size()=" + mImageAreaList.size());

        //3,A:画报告选中图片的标注,文字说明  mImageAreaList
        for (int i = 0; i < mImageAreaList.size(); i++) {
            LabelBean mBean = mImageAreaList.get(i);
            float left = CommonUtils.getScaleLeft2Right(Float.parseFloat(mBean.getLeft()), mPageSize);
            float right = CommonUtils.getScaleLeft2Right(Float.parseFloat(mBean.getRight()), mPageSize);
            float top = CommonUtils.getScaleTop2Bottom(Float.parseFloat(mBean.getTop()), mPageSize);
            float bottom = CommonUtils.getScaleTop2Bottom(Float.parseFloat(mBean.getBottom()), mPageSize);
            float mWidth = right - left;
            float mHeight = bottom - top;
            float mFontSize = CommonUtils.getScaleFontSizeNum(Float.parseFloat(mBean.getBottom()), mPageSize);
            String type = mBean.getType();
            switch (type) {
                case "Image"://图像:添加图片

                    break;
                case "ImageDesc"://图像说明:添加text,更具数据Bean设置
                    DrawTextLayout(mBean, "ImageAreaLayout");
                    break;

                case "ImageSketch": //图像标注:添加图片
//                    try {
//                        Image image5 = new Image(ImageDataFactory.create("http://rongcloud-web.qiniudn.com/docs_demo_rongcloud_logo.png"));
//                        image5.setWidth(mWidth);
//                        image5.setHeight(mHeight);
//                        Paragraph mImageLayoutSketch = new Paragraph().add(image5);
//                        //left 设置20  默认右移动了20
//                        //top 设置20  默认下移动了20
//                        //right 设置20  默认左移动了20
//                        //bottom 设置20  默认上移动了20
//                        mImageLayoutSketch.setFixedPosition(1, left, CommonUtils.getScaleFixTopNum(Float.parseFloat(mBean.getBottom()), mPageSize), mWidth);
//                        document.add(mImageLayoutSketch);
//                    } catch (MalformedURLException e) {
//                        Log.e("PrintPdfReportActivity", "图像标注:添加图片:444行,图片不存在,请在手机本地路径下添加image_01.jpg的图片");
//                        Log.e("PrintPdfReportActivity", "图片不存在,mLogoPath=" + mLogoPath);
//                        e.printStackTrace();
//                    }
                    break;

            }

        }
        //4,画Text文字布局
        for (int i = 0; i < mTextList.size(); i++) {
            LabelBean mBean = mTextList.get(i);
            DrawTextLayout(mBean, "TextLayout");
        }
        //3,B:画报告选中图片
        //开启线程去加载网络图片,并且画如PDF之中
        startThreadLoadingNetworkImage();


    }

    /**
     * //开启线程去加载网络图片,并且画如PDF之中
     */
    private void startThreadLoadingNetworkImage() {
        //画报告中的图片,开启线程去画 result是线程返回的结果,result.isDone() 表示线程执行完毕
        ExecutorService executor = Executors.newCachedThreadPool();
        TaskLogo tasklogo = new TaskLogo();
        ImageTask task = new ImageTask();
        TaskImageSketch taskSketch = new TaskImageSketch();
        //在线访问,logo图片
        Future<String> resultLogo = executor.submit(tasklogo);
        //在线访问,报告图片右下角小图片(示意图)
        Future<String> resultSketch = executor.submit(taskSketch);
        //在线访问,报告图片
        Future<String> result = executor.submit(task);


        try {
            LogUtils.e("Task:===Task == result=get=" + result.get(18, TimeUnit.SECONDS));
            LogUtils.e("Task:===Task == result=resultLogo.isDone()=" + resultLogo.isDone());
            LogUtils.e("Task:===Task == result=resultSketch.isDone()=" + resultSketch.isDone());
            LogUtils.e("Task:===Task == result=result.isDone()=" + result.isDone());


            if (result.isDone()) {
                document.close();
                // 关闭
                Toast.makeText(PrintPdfReportNetImageActivity.this, "PDF,创建完毕", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(PrintPdfReportNetImageActivity.this, "PDF,创建error", Toast.LENGTH_SHORT).show();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 有返回值的线程(如果不动请百度或者访问下面链接查看使用示例)
     * https://github.com/ILoveLin/toBeBetterJavaer/blob/master/docs/thread/callable-future-futuretask.md
     */
    private class ImageTask implements Callable<String> {
        @Override
        public String call() throws Exception {
            ArrayList<LabelBean> mTempList = new ArrayList<>();
            for (int i = 0; i < mImageAreaList.size(); i++) {
                String type = mImageAreaList.get(i).getType();
                if (type.equals("Image")) {
                    mTempList.add(mImageAreaList.get(i));
                }
            }

            LabelBean mBean = null;
            int sum = 0;
            int size = mTempList.size() - 1;
            LogUtils.e("Task:===Task == mImageAreaList.size()==" + mTempList.size());

            for (int i = 0; i < mTempList.size(); i++) {
                mBean = mTempList.get(i);
                LogUtils.e("Task:===Task == mBean==" + mBean.toString());
                LogUtils.e("Task:===Task == sum==" + sum);
                sum = i;
                LogUtils.e("Task:===Task == sum==" + sum);


                float left = CommonUtils.getScaleLeft2Right(Float.parseFloat(mBean.getLeft()), mPageSize);
                float right = CommonUtils.getScaleLeft2Right(Float.parseFloat(mBean.getRight()), mPageSize);
                float top = CommonUtils.getScaleTop2Bottom(Float.parseFloat(mBean.getTop()), mPageSize);
                float bottom = CommonUtils.getScaleTop2Bottom(Float.parseFloat(mBean.getBottom()), mPageSize);
                float mWidth = right - left;
                float mHeight = bottom - top;
                Image image2 = null;

                String type = mBean.getType();
                if ("Image".equals(type)) {
                    try {
                        //网络图片url
                        if (mBean.getOrder().equals("1")) { //报告中,第一张图
//                            httpPictureUrl = "http://192.168.67.66:7001/124/002.jpg";
                            httpPictureUrl = "https://www.baidu.com/img/bdlogo.png";
                        }
                        if (mBean.getOrder().equals("2")) { //报告中,第一张图
                            httpPictureUrl = "http://rongcloud-web.qiniudn.com/docs_demo_rongcloud_logo.png";
                        }
                        if (mBean.getOrder().equals("3")) { //报告中,第一张图
                            httpPictureUrl = "https://www.baidu.com/img/bdlogo.png";
                        }
                        if (mBean.getOrder().equals("4")) { //报告中,第一张图
                            httpPictureUrl = "https://www.baidu.com/img/bdlogo.png";
                        }
                        if (mBean.getOrder().equals("5")) { //报告中,第一张图
                            httpPictureUrl = "https://www.baidu.com/img/bdlogo.png";
                        }
                        if (mBean.getOrder().equals("6")) { //报告中,第一张图
                            httpPictureUrl = "https://www.baidu.com/img/bdlogo.png";
                        }
                        if (mBean.getOrder().equals("7")) { //报告中,第一张图
                            httpPictureUrl = "https://www.baidu.com/img/bdlogo.png";
                        }
                        if (mBean.getOrder().equals("8")) { //报告中,第一张图
                            httpPictureUrl = "https://www.baidu.com/img/bdlogo.png";
                        }
                        if (mBean.getOrder().equals("9")) { //报告中,第一张图
                            httpPictureUrl = "https://www.baidu.com/img/bdlogo.png";
                        }
//                        httpPictureUrl = "https://www.baidu.com/img/bdlogo.png";
                        image2 = new Image(ImageDataFactory.create(httpPictureUrl));
                    } catch (MalformedURLException e) {
                        Log.e("PrintPdfReportActivity", "图像标注:添加图片:413行,图片不存在,请在手机本地路径下添加001.jpg的图片");
                        Log.e("PrintPdfReportActivity", "图片不存在,mImagePath=" + mImagePath);
                        e.printStackTrace();
                    }
                    image2.setWidth(mWidth);
                    image2.setHeight(mHeight);
                    Paragraph mImageLayout = new Paragraph().add(image2);
                    //left 设置20  默认右移动了20
                    //top 设置20  默认下移动了20
                    //right 设置20  默认左移动了20
                    //bottom 设置20  默认上移动了20
                    mImageLayout.setFixedPosition(1, left, CommonUtils.getScaleFixTopNum(Float.parseFloat(mBean.getBottom()), mPageSize), mWidth);
                    document.add(mImageLayout);
                    LogUtils.e("Task:===Task =current= sum==" + sum);

                    if (size == sum) {
                        return sum + "";

                    } else {

                    }
                } else {
                    return null;
                }
            }
            return null;


        }
    }

    /**
     * 开启线程下载报告选中图片的 示意图
     */
    private class TaskImageSketch implements Callable<String> {
        @Override
        public String call() throws Exception {
            ArrayList<LabelBean> mTempList = new ArrayList<>();
            for (int i = 0; i < mImageAreaList.size(); i++) {
                String type = mImageAreaList.get(i).getType();
                if (type.equals("ImageSketch")) {
                    mTempList.add(mImageAreaList.get(i));
                }
            }

            LabelBean mBean = null;
            int sum = 0;
            int size = mTempList.size() - 1;
            LogUtils.e("Task:===Task == mImageAreaList.size()==" + mTempList.size());

            for (int i = 0; i < mTempList.size(); i++) {
                mBean = mTempList.get(i);
                LogUtils.e("Task:===Task == mBean==" + mBean.toString());
                LogUtils.e("Task:===Task == sum==" + sum);
                sum = i;
                LogUtils.e("Task:===Task == sum==" + sum);
                float left = CommonUtils.getScaleLeft2Right(Float.parseFloat(mBean.getLeft()), mPageSize);
                float right = CommonUtils.getScaleLeft2Right(Float.parseFloat(mBean.getRight()), mPageSize);
                float top = CommonUtils.getScaleTop2Bottom(Float.parseFloat(mBean.getTop()), mPageSize);
                float bottom = CommonUtils.getScaleTop2Bottom(Float.parseFloat(mBean.getBottom()), mPageSize);
                float mWidth = right - left;
                float mHeight = bottom - top;
                Image image2 = null;

                String type = mBean.getType();
                if ("ImageSketch".equals(type)) {
                    //网络图片url
                    String url = "http://rongcloud-web.qiniudn.com/docs_demo_rongcloud_logo.png";
                    try {
                        Image image5 = new Image(ImageDataFactory.create(url));
                        image5.setWidth(mWidth);
                        image5.setHeight(mHeight);
                        Paragraph mImageLayoutSketch = new Paragraph().add(image5);
                        //left 设置20  默认右移动了20
                        //top 设置20  默认下移动了20
                        //right 设置20  默认左移动了20
                        //bottom 设置20  默认上移动了20
                        mImageLayoutSketch.setFixedPosition(1, left, CommonUtils.getScaleFixTopNum(Float.parseFloat(mBean.getBottom()), mPageSize), mWidth);
                        document.add(mImageLayoutSketch);
                    } catch (MalformedURLException e) {
                        Log.e("PrintPdfReportActivity", "图像标注:添加图片:676行,图片不存在,请能正常访问的网络图片");
                        Log.e("PrintPdfReportActivity", "图片不存在,url=" + url);
                        e.printStackTrace();
                    }

                    if (size == sum) {
                        return sum + "";

                    } else {

                    }
                } else {
                    return null;
                }
            }
            return null;


        }
    }

    /**
     * 开启线程下载报告的logo
     */
    private class TaskLogo implements Callable<String> {
        @Override
        public String call() throws Exception {
            LabelBean mBean = null;
            int sum = 0;
            int size = mLogoList.size() - 1;
            LogUtils.e("Task:===Tasklogo == mImageAreaList.size()==" + mLogoList.size());

            for (int i = 0; i < mLogoList.size(); i++) {
                mBean = mLogoList.get(i);
                LogUtils.e("Task:===Tasklogo == mBean==" + mBean.toString());
                LogUtils.e("Task:===Tasklogo == sum==" + sum);
                sum = i;
                LogUtils.e("Task:===Tasklogo == sum==" + sum);
                float left = CommonUtils.getScaleLeft2Right(Float.parseFloat(mBean.getLeft()), mPageSize);
                float right = CommonUtils.getScaleLeft2Right(Float.parseFloat(mBean.getRight()), mPageSize);
                float top = CommonUtils.getScaleTop2Bottom(Float.parseFloat(mBean.getTop()), mPageSize);
                float bottom = CommonUtils.getScaleTop2Bottom(Float.parseFloat(mBean.getBottom()), mPageSize);
                float mWidth = right - left;
                float mHeight = bottom - top;
                //网络图片url
                String url = "http://www.baidu.com/img/bdlogo.png";
                try {
                    Image image5 = new Image(ImageDataFactory.create(url));
                    image5.setWidth(mWidth);
                    image5.setHeight(mHeight);
                    Paragraph mImageLayoutSketch = new Paragraph().add(image5);
                    //left 设置20  默认右移动了20
                    //top 设置20  默认下移动了20
                    //right 设置20  默认左移动了20
                    //bottom 设置20  默认上移动了20
                    mImageLayoutSketch.setFixedPosition(1, left, CommonUtils.getScaleFixTopNum(Float.parseFloat(mBean.getBottom()), mPageSize), mWidth);
                    document.add(mImageLayoutSketch);
                } catch (MalformedURLException e) {
                    Log.e("PrintPdfReportActivity", "添加logo:添加图片:747行,图片不存在,请能正常访问的网络图片");
                    Log.e("PrintPdfReportActivity", "图片不存在,url=" + url);
                    e.printStackTrace();
                }
                if (size == sum) {
                    return sum + "";
                } else {

                }

            }
            return null;


        }
    }

    private void DrawTextLayout(LabelBean mBean, String type) {
        // 过滤掉"年龄单位"类型的元素，因为它和"岁"在同一个位置
        if (mBean.getType().equals("年龄单位") || mBean.getContent().equals("年龄单位")) {
            Log.e("mBean", "跳过年龄单位元素: " + mBean.toString());
            return;
        }
        
        Log.e("mBean", "mBean=" + mBean.toString());
        float left = CommonUtils.getScaleLeft2Right(Float.parseFloat(mBean.getLeft()), mPageSize);
        float right = CommonUtils.getScaleLeft2Right(Float.parseFloat(mBean.getRight()), mPageSize);
        float top = CommonUtils.getScaleTop2Bottom(Float.parseFloat(mBean.getTop()), mPageSize);
        float bottom = CommonUtils.getScaleTop2Bottom(Float.parseFloat(mBean.getBottom()), mPageSize);
        float mWidth = right - left;
        float mHeight = bottom - top;
        float mFontSize = CommonUtils.getScaleFontSizeNum(Float.parseFloat(mBean.getFontSize()), mPageSize);
        float mFixBottom = CommonUtils.getScaleFixTopNum(Float.parseFloat(mBean.getBottom()), mPageSize);
        //添加Text  并且根据order,设置病例caseBean的对应字段数据

        //设置文字  getType == Caption   表示不用替换Content   不然根据order 替换Content
        Text mTextView = null;
        if (type.equals("TextLayout")) {//病例模板
            if (mBean.getType().equals("Edit")) {
                //设置字体  1:加粗,0:正常
                if (mBean.getBold().equals("1")) {
                    if (mBean.getContent().equals("岁")) {
                        mTextView = new Text("岁").setFont(font).setFontSize(mFontSize).setFontColor(text_color).setBold();
                    }

                } else {
                    if (mBean.getContent().equals("镜检所见")) {
                        mTextView = new Text("我们都知道，在过去几十年的认知当中，欧盟作为一个政治实体，一直被誉为是多极世界的重要一端，无论是中国还是美国，在商讨多边合作时，都对此深信不疑。然而这个惯性共识，我们现在或许要做出改变了。\n" +
                                "\n" +
                                "根据今日俄罗斯网站在3月27日的报道称，现如今的中俄正在重塑国际秩序，美国则主导着大西洋彼岸的合作伙伴，让欧洲国家成为过客。" + "我们都知道，在过去几十年的认知当中，欧盟作为一个政治实体，一直被誉为是多极世界的重要一端，无论是中国还是美国，在商讨多边合作时，都对此深信不疑。然而这个惯性共识，我们现在或许要做出改变了。\n" +
                                "\n" + "根据今日俄罗斯网站在3月27日的报道称，现如今的中俄正在重塑国际秩序，美国则主导着大西洋彼岸的合作伙伴，让欧洲国家成为过客。" + "我们都知道，在过去几十年的认知当中，欧盟作为一个政治实体，一直被誉为是多极世界的重要一端，无论是中国还是美国，在商讨多边合作时，都对此深信不疑。然而这个惯性共识，我们现在或许要做出改变了。\n" +
                                "\n" + "根据今日俄罗斯网站在3月27日的报道称，现如今的中俄正在重塑国际秩序，美国则主导着大西洋彼岸的合作伙伴，让欧洲国家成为过客。" + "我们都知道，在过去几十年的认知当中，欧盟作为一个政治实体，一直被誉为是多极世界的重要一端，无论是中国还是美国，在商讨多边合作时，都对此深信不疑。然而这个惯性共识，我们现在或许要做出改变了。\n" +
                                "\n" + "根据今日俄罗斯网站在3月27日的报道称，现如今的中俄正在重塑国际秩序，美国则主导着大西洋彼岸的合作伙伴，让欧洲国家成为过客。" + "我们都知道，在过去几十年的认知当中，欧盟作为一个政治实体，一直被誉为是多极世界的重要一端，无论是中国还是美国，在商讨多边合作时，都对此深信不疑。然而这个惯性共识，我们现在或许要做出改变了。\n" +
                                "\n" + "根据今日俄罗斯网站在3月27日的报道称，现如今的中俄正在重塑国际秩序，美国则主导着大西洋彼岸的合作伙伴，让欧洲国家成为过客。" + "我们都知道，在过去几十年的认知当中，欧盟作为一个政治实体，一直被誉为是多极世界的重要一端，无论是中国还是美国，在商讨多边合作时，都对此深信不疑。然而这个惯性共识，我们现在或许要做出改变了。\n" +
                                "\n" +

                                "根据今日俄罗斯网站在3月27日的报道称，现如今的中俄正在重塑国际秩序，美国则主导着大西洋彼岸的合作伙伴，让欧洲国家成为过客。").setFont(font).setFontSize(mFontSize).setFontColor(text_color).setBold();


                    } else {
                        mTextView = new Text(mBean.getContent() + "").setFont(font).setFontSize(mFontSize).setFontColor(text_color).setBold();

                    }
//                    mTextView = new Text("123456").setFont(font).setFontSize(mFontSize).setFontColor(text_color);

                }

            } else {
                //设置字体  1:加粗,0:正常
                if (mBean.getBold().equals("1")) {
                    mTextView = new Text(mBean.getContent()).setFont(font).setFontSize(mFontSize).setFontColor(text_color).setBold();
                } else {
                    mTextView = new Text(mBean.getContent()).setFont(font).setFontSize(mFontSize).setFontColor(text_color);
                }

            }
        } else {//ImageAreaLayout  图片区域中的文字
            mTextView = new Text(mBean.getContent()).setFont(font).setFontSize(mFontSize).setFontColor(text_color).setBold();

        }


        //设置字体  1:加粗,0:正常
        if (mBean.getType().equals("1")) {
            mTextView.setBold();
        }
        //设置文字,颜色
        mTextView.setFontColor(text_color);
        //设置文字,大小
        mTextView.setFontSize(mFontSize);
        //设置文字,显示位置
        Paragraph mLayout = new Paragraph(mTextView);
//            mLayout.setBackgroundColor(title_color);
        //        mLayout.setHeight(mHeight);

//        float mHeight = bottom - top;


        switch (mBean.getAlignment()) {
            case "0"://0:左,1:中,2:右边
                mLayout.setTextAlignment(TextAlignment.LEFT);
                break;
            case "1":
                mLayout.setTextAlignment(TextAlignment.CENTER);
                break;
            case "2":
                mLayout.setTextAlignment(TextAlignment.RIGHT);
                break;
            default:
                mLayout.setTextAlignment(TextAlignment.CENTER);
                break;
        }

        //设置字体是否斜体   0:常规,1:斜体
        if (!(mBean.getTilt().equals("0"))) {
        }

        mLayout.setFixedPosition(1, left, mFixBottom, mWidth);


        if (mBean.getContent().equals("镜检所见")) { //镜检所见 需要填写内容的区域
            DeviceRgb mColor = CommonUtils.getRGBColor(16711680);

            mLayout.setBackgroundColor(mColor);
            mLayout.setHeight(mHeight);
        } else if (mBean.getContent().equals("镜检诊断")) {//镜检诊断 需要填写内容的区域
            DeviceRgb mBGColor = CommonUtils.getRGBColor(255);
            mLayout.setBackgroundColor(mBGColor);
            mLayout.setHeight(mHeight);
        }

        document.add(mLayout);
    }


    /**
     * 创建PDF文件
     */
    private void createPDF(String path) {
        if (isPermissions) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            file.getParentFile().mkdirs();

            // 创建Document
            PdfWriter writer = null;
            try {
                writer = new PdfWriter(new FileOutputStream(path));
            } catch (FileNotFoundException e) {
                Log.e("FileNotFoundException", e.toString());
            }

            PdfDocument pdf_document = new PdfDocument(writer);
            // 生成的PDF文档信息
            PdfDocumentInfo info = pdf_document.getDocumentInfo();
            // 标题
            info.setTitle("First pdf file");
            // 作者
            info.setAuthor("Quinto");
            // 科目
            info.setSubject("test");
            // 关键词
            info.setKeywords("pdf");
            // 创建日期
            info.setCreator("2022-10-20");
            document = new Document(pdf_document, PageSize.A4, true);
            document.setMargins(0, 0, 0, 0);
            float width = PageSize.A4.getWidth();
            float mPageHeigth = PageSize.A4.getHeight();
            //和实际的A4值打印相差2.38倍
            Log.e("A4纸张:", "width==" + width);
            Log.e("A4纸张:", "mPageHeigth==" + mPageHeigth);

            // 文字字体（显示中文）、大小、颜色
            font = null;
            try {
                font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
            } catch (IOException e) {
                Log.e("IOException", e.toString());
            }

//            DeviceRgb title_color = new DeviceRgb(0, 0, 0);
//            DeviceRgb title_color = CommonUtils.getRGBColor(32768);、、16711680
            DeviceRgb title_color = CommonUtils.getColorRgba(32768);
//            DeviceRgb title_color = CommonUtils.getColorRgba(16711680);
//            DeviceRgb title_color = new DeviceRgb(0, 0, 0);
            text_title_color = new DeviceRgb(65, 136, 160);
            text_color = new DeviceRgb(43, 43, 43);

            // 行分隔符
            // 实线：SolidLine()  点线：DottedLine()  仪表盘线：DashedLine()
//            LineSeparator separator = new LineSeparator(new SolidLine());
//            separator.setStrokeColor(new DeviceRgb(0, 0, 68));

            /**
             * itextpdf 7
             *      以左下方为原点坐标轴来确定
             */


            //设置姓名一些信息
//            setInfo();


            // 添加可换行空间
            document.add(new Paragraph(""));
            // 添加水平线
            // 添加可换行空间
            document.add(new Paragraph(""));


            // 关闭
            document.close();
            Toast.makeText(this, "PDF文件已生成", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }
    }


    /**
     * 打开PDF文件
     */
    private void openPDF() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                File file = new File(path);

                pdfView.fromFile(file)
                        //启用滑动
                        .enableSwipe(true)
                        //禁用双击
                        .enableDoubletap(false)
                        .swipeHorizontal(true)
                        .load();
                pdfView.setVisibility(View.VISIBLE);
            }
        }.start();
    }


    /**
     * 动态申请权限
     */
    private void requestPermission() {

        XXPermissions.with(this)
                // 适配 Android 11 需要这样写，这里无需再写 Permission.Group.STORAGE
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (all) {


                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(getApplicationContext(), permissions);
                        } else {

                        }
                        Toast.makeText(context, "存储权限获取失败", Toast.LENGTH_SHORT).show();

                    }
                });


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private ArrayList<LabelBean> readA4XmlForSAX() throws Exception {
        //获取文件资源建立输入流对象
//        String s = Environment.getExternalStorageDirectory() + "/CMEReport";
//
//        File file = new File(s, "模板一.xml");
//        InputStream is = new FileInputStream(file);

        InputStream is = getAssets().open("一图报表.xml");
        //①创建XML解析处理器
        SaxHelper2Text ss = new SaxHelper2Text();
        //②得到SAX解析工厂
        SAXParserFactory factory = SAXParserFactory.newInstance();
        //③创建SAX解析器
        SAXParser parser = factory.newSAXParser();
        //④将xml解析处理器分配给解析器,对文档进行解析,将事件发送给处理器
        parser.parse(is, ss);
        is.close();

        ArrayList<LabelBean> dataList = ss.getDataList();
        /**
         * 根据指定比较器产生的顺序对指定列表进行排序
         * 因为动态调整镜检所见的坐标,所以需要对数据Bean .getTop 进行排序
         */
        ArrayList<LabelBean> sortList = getSortList(dataList);
        return sortList;
    }

    private void getListData() {

        if (null == mRepoerLabelList || 0 == mRepoerLabelList.size()) {
            return;
        }
        mLogoList = new ArrayList<>();
        mImageList = new ArrayList<>();
        mLineList = new ArrayList<>();
        mTextList = new ArrayList<>();
        for (int i = 0; i < mRepoerLabelList.size(); i++) {
            LabelBean mBean = mRepoerLabelList.get(i);
            if (mBean.getContent().equals("分界线")) {
                mLineList.add(mBean);
            } else if (mBean.getContent().equals("徽标")) {
                mLogoList.add(mBean);
            } else if (mBean.getContent().equals("图像区域")) {
                mImageList.add(mBean);
            } else if (mBean.getContent().equals("年龄单位")) {   //年龄单位  和  岁  这两个label是同一个位置
            } else {
                mTextList.add(mBean);
            }
        }


        mLogoList = getSortList(mLogoList);
        mImageList = getSortList(mImageList);
        mLineList = getSortList(mLineList);
        mTextList = getSortList(mTextList);


        Log.e("解析后的数据", "mLogoList.size()==" + mLogoList.size());
        Log.e("解析后的数据", "mImageList.size()==" + mImageList.size());
        Log.e("解析后的数据", "mLineList.size()==" + mLineList.size());
        Log.e("解析后的数据", "mTextList.size()==" + mTextList.size());


    }

    private Disposable mDisposable3s;

    /**
     * 开始打印报告
     *
     * @param path 生成的pd f文件路径
     */
    private void onPrintPdf(String path) {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setColorMode(PrintAttributes.COLOR_MODE_COLOR);
//        printManager.print("test pdf print", new MyPrintAdapter(this, this.path), builder.build());
        mPdfDocumentAdapter = new PdfDocumentAdapter(this, this.path);
        printReport = printManager.print("PrintReport", mPdfDocumentAdapter, builder.build());
        mPdfDocumentAdapter.setOnPrintStatue(new PdfDocumentAdapter.OnPrintStatueListener() {
            @Override
            public void onPrintStatue(boolean statue) {
                if (printReport.isCancelled()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("打印报告回调======", "====打印被取消了");
                            Toast.makeText(PrintPdfReportNetImageActivity.this, "打印被取消了", Toast.LENGTH_SHORT).show();

                        }
                    });
                }
                if (printReport.isBlocked()) {
//                    Toast.makeText(this, "打印被取消了", Toast.LENGTH_LONG).show();
                    Log.e("打印报告回调======", "====打印机器被锁定了(卡了或者没有纸张了)");
                    Toast.makeText(PrintPdfReportNetImageActivity.this, "打印机器被锁定了(卡了或者没有纸张了)", Toast.LENGTH_SHORT).show();


                }

                create_pdf.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (printReport.isStarted()) {
                            Toast.makeText(PrintPdfReportNetImageActivity.this, "开始打印", Toast.LENGTH_SHORT).show();
                            mDisposable3s = Observable
                                    .interval(3, TimeUnit.SECONDS)//定时器操作符，这里三秒打印一个log
                                    //取消任务时取消定时唤醒
                                    .doOnDispose(() -> {

                                    })
                                    .subscribe(count -> {
                                        boolean completed = printReport.isCompleted();//完成的回调
                                        boolean failed = printReport.isFailed();      //失败的回调
                                        if (completed) {
                                            Log.e("打印报告回调======", "====打印完成");
                                            Toast.makeText(PrintPdfReportNetImageActivity.this, "打印完成", Toast.LENGTH_SHORT).show();

                                            if (null != mDisposable3s) {
                                                mDisposable3s.dispose();
                                                mDisposable3s = null;
                                            }

                                        }
                                        if (failed) {
                                            Log.e("打印报告回调======", "====打印失败");
                                            Toast.makeText(PrintPdfReportNetImageActivity.this, "打印失败", Toast.LENGTH_SHORT).show();

                                            if (null != mDisposable3s) {
                                                mDisposable3s.dispose();
                                                mDisposable3s = null;
                                            }

                                        }
                                        if (!completed && count == 40) {  //2分钟还未打印成功,默认打印失败
                                            Log.e("打印报告回调======", "====打印失败");
                                            Toast.makeText(PrintPdfReportNetImageActivity.this, "打印失败", Toast.LENGTH_SHORT).show();
                                            if (null != mDisposable3s) {
                                                mDisposable3s.dispose();
                                                mDisposable3s = null;
                                            }

                                        }

                                    });

                        }

                    }
                },1000);

            }

        });


    }

    /**
     * A,读取图片数量模板
     * B,画图片和标注
     * C,调整第二根线,和镜检所见布局
     * D,画调整后的线和镜检所见
     */
    private ArrayList<LabelBean> readAreaForSAX(String data) throws Exception {
        //获取文件资源建立输入流对象
        InputStream is = null;
        switch (data) {
            case "1":
                is = getAssets().open("检查01图.xml");
                break;
            case "2":
                is = getAssets().open("检查02图.xml");
                break;
            case "3":
                is = getAssets().open("检查03图.xml");
                break;
            case "4":
                is = getAssets().open("检查04图.xml");
                break;
            case "5":
                is = getAssets().open("检查05图.xml");
                break;
            case "6":
                is = getAssets().open("检查06图.xml");
                break;
            case "7":
                is = getAssets().open("检查07图.xml");
                break;
            case "8":
                is = getAssets().open("检查08图.xml");
                break;
            case "9":
                is = getAssets().open("检查09图.xml");
                break;


        }
        //①创建XML解析处理器
        SaxHelper2Text ss = new SaxHelper2Text();
        //②得到SAX解析工厂
        SAXParserFactory factory = SAXParserFactory.newInstance();
        //③创建SAX解析器
        SAXParser parser = factory.newSAXParser();
        //④将xml解析处理器分配给解析器,对文档进行解析,将事件发送给处理器
        parser.parse(is, ss);
        is.close();

        ArrayList<LabelBean> dataList = ss.getDataList();
        getSortList(dataList);
        return dataList;
    }


    /**
     * 排序  升序
     *
     * @param dataList
     * @return
     */
    public ArrayList<LabelBean> getSortList(ArrayList<LabelBean> dataList) {
        Collections.sort(dataList, new Comparator<LabelBean>() {
            public int compare(LabelBean o1, LabelBean o2) {
                if (Float.parseFloat(o1.getTop()) - Float.parseFloat(o2.getTop()) > 0) {
                    return 1;
                } else if (Float.parseFloat(o1.getTop()) - Float.parseFloat(o2.getTop()) < 0) {
                    return -1;
                } else {
                    return 1;//如果年龄相同则通过名字排序，通过调用compareTo按名字字典排序
                }
            }
        });
        return dataList;
    }

    public byte[] readStream(String fileName) {
        try {
            InputStream inStream = getResources().getAssets().open(fileName);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            outStream.close();
            inStream.close();
            return outStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //把文件写到存储目录
    public static boolean writeFile(byte[] buffer, String folder,
                                    String fileName) {
        boolean writeSucc = false;

        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);

        String folderPath = "";
        if (sdCardExist) {
            folderPath = Environment.getExternalStorageDirectory()
                    + File.separator + folder + File.separator;
        } else {
            writeSucc = false;
        }

        File fileDir = new File(folderPath);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }

        File file = new File(folderPath + fileName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file, true);
            out.write(buffer);
            writeSucc = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return writeSucc;
    }
}
