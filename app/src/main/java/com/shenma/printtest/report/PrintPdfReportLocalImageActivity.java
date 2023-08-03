package com.shenma.printtest.report;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
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
import com.shenma.printtest.util.SaxHelper2Text;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
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
 * desc：网络打印机-----自定义pdf格式打印      高精度.
 * <p>
 * 报告中选中的图片是手机SD卡本地图片
 */
public class PrintPdfReportLocalImageActivity extends AppCompatActivity {
    private Button create_pdf;
    private Button open_pdf, read_image_num, print_report;
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
    private Paragraph mLayout;
    private ArrayList<LabelBean> mLogoList;
    private ArrayList<LabelBean> mImageList;
    private ArrayList<LabelBean> mLineList;
    private ArrayList<LabelBean> mTextList;
    private String mFontPath;
    private EditText edit_num;
    private ArrayList<LabelBean> mImageAreaList;
    private String mLogoPath;
    private Button mSizeA3;
    private Button mSizeB5;
    private Button mSizeA4;
    private PageSize mPageSize;
    private DeviceRgb title_color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_pdf_local);
        requestPermission();
        initView();
        initData();
        onResponseListener();
    }

    private void initView() {
        create_pdf = findViewById(R.id.create_pdf);
        open_pdf = findViewById(R.id.open_pdf);
        read_image_num = findViewById(R.id.read_image_num);
        pdfView = findViewById(R.id.pdfView);
        print_report = findViewById(R.id.print_report);
        edit_num = findViewById(R.id.edit_num);
        mSizeA3 = findViewById(R.id.btn_a3);
        mSizeA4 = findViewById(R.id.btn_a4);
        mSizeB5 = findViewById(R.id.btn_b5);
        context = getApplicationContext();
        path = Environment.getExternalStorageDirectory() + "/report_local.pdf";
        mImagePath = Environment.getExternalStorageDirectory() + "/C_CME/001.jpg";

        String str = "请先点击(1:读取图片模板)," +
                "然后点击(2:A3 A4 B5,纸张类型中的一种)," +
                "再点击(3:生成PDF文件)," +
                "最后点击(5:打开PDF文件,或者点击打印报告)";
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();

    }

    private void initData() {
        try {
            mRepoerLabelList = readA4XmlForSAX();
        } catch (Exception e) {
            Log.e("readXmlForSAX", "readXmlForSAX解析出错");
            e.printStackTrace();
        }
    }

    private void onResponseListener() {
        findViewById(R.id.black).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
        //读取报告中,选中的图片模板   (报告中有几张图,然后会adjustReportLayout()调整--镜检所见: 镜检所见内容  这两个布局的大小)
        read_image_num.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readReportChooseImageNum();

            }
        });
    }


    private void readReportChooseImageNum() {
        try {
            //解析 图像模板(比如5图,输入5)
            mImageAreaList = readAreaForSAX(edit_num.getText().toString().trim());
            Toast.makeText(PrintPdfReportLocalImageActivity.this, "读取图片模板-完毕", Toast.LENGTH_SHORT).show();

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
        int vpimage1 = 0;
        int vpimage2 = 0;

        for (int i = 0; i < mRepoerLabelList.size(); i++) {
            LabelBean mBean = mRepoerLabelList.get(i);
            if (mBean.getType().equals("图像区域")) {
                //当前 label 列表中(升序)   图像区域对应的角标
                nNum = i;
                vpimage1 = Integer.parseInt(mBean.getBottom()) - Integer.parseInt(mBean.getTop());
                break;
            }

        }

        //接下来是图像区域
        for (int i = 0; i < mImageAreaList.size(); i++) {
            LabelBean mBean = mImageAreaList.get(i);
            int mNewBottom = (Integer.parseInt(mBean.getBottom()) + nBottom);
            int mNewTop = (Integer.parseInt(mBean.getTop()) + nBottom);
            mBean.setTop(mNewTop + "");
            mBean.setBottom(mNewBottom + "");
            nLastBottom = mNewBottom;
            if (mBean.getType().equals("Image")) {
                vpimage2 = mNewBottom - mNewTop;
            }

        }
        //图像区域以下的 项目
        int t = 0;
        int tF = 0;
        int tz = 10;
        ////nNum 代表图片区域的:角标
        for (int i = nNum + 1; i < mRepoerLabelList.size(); i++) {
            LabelBean mBean = mRepoerLabelList.get(i);
            int top = Integer.parseInt(mBean.getTop());
            int bottom = Integer.parseInt(mBean.getBottom());
            String order = mBean.getOrder();
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
            //调整镜检所见的位置
            if (order.equals("46")) {
                Log.e("调整layout布局", "mBean====" + mBean.toString());
                mBean.setTop((top + nBottom + t + tz) + "");
                mBean.setBottom((bottom + nBottom + t + tz) + "");
                Log.e("调整layout布局", "mBean==后==" + mBean.toString());

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
        document = new Document(pdf_document, mPageSize, true);
        document.setMargins(0, 0, 0, 0);

        // 文字字体（显示中文）、大小、颜色
        font = null;
        try {
            font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
        } catch (IOException e) {
            Log.e("IOException", e.toString());
        }
//        simsun.ttc


        title_color = CommonUtils.getColorRgba(32768);
        text_title_color = new DeviceRgb(65, 136, 160);
        text_color = new DeviceRgb(43, 43, 43);
//       mLogoList,mLineList,mImageList,mTextList

//        1,画logo
        for (int i = 0; i < mLogoList.size(); i++) {
            LabelBean mBean = mLogoList.get(i);
            absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            Paragraph imageLogo = null;
            /**
             * /C_CME/image_01.jpg   这个是我自己本地图片路径,请替换成你自己的,不然会闪退
             * 记得替换成自己的手机本地图片路径
             */
            mLogoPath = absolutePath + "/C_CME/logo.jpg";
            mImagePath = absolutePath + "/C_CME/001.jpg";
            Log.e("本地图片地址path===", "absolutePath==" + absolutePath);
            Log.e("本地图片地址path===", "mLogoPath==" + mLogoPath);
            Log.e("本地图片地址path===", "mImagePath==" + mImagePath);

            float left = CommonUtils.getScaleLeft2Right(Float.parseFloat(mBean.getLeft()), mPageSize);
            float right = CommonUtils.getScaleLeft2Right(Float.parseFloat(mBean.getRight()), mPageSize);
            float top = CommonUtils.getScaleTop2Bottom(Float.parseFloat(mBean.getTop()), mPageSize);
            float bottom = CommonUtils.getScaleTop2Bottom(Float.parseFloat(mBean.getBottom()), mPageSize);
            float mFixBottom = CommonUtils.getScaleFixTopNum(bottom, mPageSize);
            float mWidth = right - left;
            float mHeight = bottom - top;
            //添加logo
            try {
                Image image5 = new Image(ImageDataFactory.create(mLogoPath));
                image5.setWidth(mWidth);
                image5.setHeight(mHeight);
                imageLogo = new Paragraph().add(image5);
                //left 设置20  默认右移动了20
                //top 设置20  默认下移动了20
                //right 设置20  默认左移动了20
                //bottom 设置20  默认上移动了20
                imageLogo.setFixedPosition(1, left, mFixBottom, mWidth);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e("PrintPdfReportActivity", "画logo的时候出现异常:362行,图片不存在,请在手机本地路径下添加image_01.jpg的图片");
                Log.e("PrintPdfReportActivity", "图片不存在,mLogoPath=" + mLogoPath);
            }
//            document.add(imageLogo);
            document.add(imageLogo);

        }

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
        //3,画图片   mImageAreaList
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
                    Image image2 = null;
                    try {
                        //手机路径的图片,如果没有自己随便给张图片即可
                        if (mBean.getOrder().equals("1")) { //报告中,第一张图
                            mImagePath = absolutePath + "/C_CME/001.jpg";
                        }

//                        image2 = new Image(ImageDataFactory.create("http://192.168.67.166:7001/99/001.jpg"));
                        image2 = new Image(ImageDataFactory.create(mImagePath));
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


                    break;
                case "ImageDesc"://图像说明:添加text,更具数据Bean设置
                    DrawTextLayout(mBean, "ImageAreaLayout");
                    break;

                case "ImageSketch": //图像标注:添加图片
                    try {
                        Image image5 = new Image(ImageDataFactory.create(mLogoPath));
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
                        Log.e("PrintPdfReportActivity", "图像标注:添加图片:444行,图片不存在,请在手机本地路径下添加image_01.jpg的图片");
                        Log.e("PrintPdfReportActivity", "图片不存在,mLogoPath=" + mLogoPath);
                        e.printStackTrace();
                    }
                    break;

            }

        }

        //3,画Text
        for (int i = 0; i < mTextList.size(); i++) {
            LabelBean mBean = mTextList.get(i);
            DrawTextLayout(mBean, "TextLayout");


        }

        document.close();

        // 关闭
        Toast.makeText(PrintPdfReportLocalImageActivity.this, "pdf创建完毕", Toast.LENGTH_SHORT).show();


    }

    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    private void DrawTextLayout(LabelBean mBean, String type) {
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

    private void onPrintPdf(String path) {
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setColorMode(PrintAttributes.COLOR_MODE_COLOR);
        PdfDocumentAdapter mPdfDocumentAdapter = new PdfDocumentAdapter(this, this.path);
        PrintJob printReport = printManager.print("PrintReport", mPdfDocumentAdapter, builder.build());
        mPdfDocumentAdapter.setOnPrintStatue(new PdfDocumentAdapter.OnPrintStatueListener() {
            @Override
            public void onPrintStatue(boolean statue) {
                if (printReport.isCancelled()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("打印报告回调======", "====打印被取消了");
                            Toast.makeText(PrintPdfReportLocalImageActivity.this, "打印被取消了", Toast.LENGTH_SHORT).show();

                        }
                    });
                }
                if (printReport.isBlocked()) {
//                    Toast.makeText(this, "打印被取消了", Toast.LENGTH_LONG).show();
                    Log.e("打印报告回调======", "====打印机器被锁定了(卡了或者没有纸张了)");
                    Toast.makeText(PrintPdfReportLocalImageActivity.this, "打印机器被锁定了(卡了或者没有纸张了)", Toast.LENGTH_SHORT).show();


                }
                create_pdf.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (printReport.isStarted()) {
                            Toast.makeText(PrintPdfReportLocalImageActivity.this, "开始打印", Toast.LENGTH_SHORT).show();

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
                                            Toast.makeText(PrintPdfReportLocalImageActivity.this, "打印完成", Toast.LENGTH_SHORT).show();

                                            if (null != mDisposable3s) {
                                                mDisposable3s.dispose();
                                                mDisposable3s = null;
                                            }

                                        }
                                        if (failed) {
                                            Log.e("打印报告回调======", "====打印失败");
                                            Toast.makeText(PrintPdfReportLocalImageActivity.this, "打印失败", Toast.LENGTH_SHORT).show();

                                            if (null != mDisposable3s) {
                                                mDisposable3s.dispose();
                                                mDisposable3s = null;
                                            }

                                        }
                                        if (!completed && count == 40) {  //2分钟还未打印成功,默认打印失败
                                            Log.e("打印报告回调======", "====打印失败");
                                            Toast.makeText(PrintPdfReportLocalImageActivity.this, "打印失败", Toast.LENGTH_SHORT).show();
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
}
