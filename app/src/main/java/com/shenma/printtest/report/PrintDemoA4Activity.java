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

import com.shenma.printtest.R;
import com.shenma.printtest.util.LabelBean;
import com.shenma.printtest.util.SaxHelper2Text;
import com.shenma.printtest.widget.MedicalReportView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * A4医用报告打印演示Activity
 * 使用自定义View渲染医用报告
 */
public class PrintDemoA4Activity extends AppCompatActivity {
    private static final String TAG = "PrintDemoA4Activity";
    
    private MedicalReportView mReportView;
    private Button mBtnPrint;
    private Button mBtnBack;
    private Button mBtnImage1, mBtnImage2, mBtnImage3, mBtnImage4, mBtnImage5;
    private Button mBtnImage6, mBtnImage7, mBtnImage8, mBtnImage9;
    private Button mBtnPaperA4, mBtnPaperA5;  // 纸张大小选择按钮
    private int mCurrentImageCount = 6; // 当前选择的图片数量
    private int mCurrentPaperSize = MedicalReportView.PAPER_SIZE_A4; // 当前纸张大小，默认A4
    
    private ArrayList<LabelBean> mReportLabels;
    private ArrayList<LabelBean> mImageAreaLabels;
    
    // 示例图片URL列表（使用指定的图片地址）
    private String[] mImageUrls = {
        "https://www.szcme.com/assets/image/与KMT合照.jpg",
        "https://www.szcme.com/assets/image/与客户合照.jpg",
        "https://www.szcme.com/assets/image/与KMT合照.jpg",
        "https://www.szcme.com/assets/image/与客户合照2.jpg",
        "https://www.szcme.com/assets/image/与KMT合照.jpg",
        "https://www.szcme.com/assets/image/2.png",
        "https://www.szcme.com/assets/image/与KMT合照.jpg",
        "https://www.szcme.com/assets/image/20241122_5.jpg",
        "https://www.szcme.com/assets/image/与KMT合照.jpg"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_demo_a4);
        
        initViews();
        initListeners();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // 从打印预览返回后，强制ReportView重新测量和布局
        // 这样可以恢复到正确的显示尺寸
        if (mReportView != null) {
            mReportView.post(() -> {
                mReportView.requestLayout();
                mReportView.invalidate();
            });
        }
    }

    private void initViews() {
        mReportView = findViewById(R.id.medical_report_view);
        mBtnPrint = findViewById(R.id.btn_print);
        mBtnBack = findViewById(R.id.btn_back);
        
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
        
        // 初始化纸张大小按钮
        mBtnPaperA4 = findViewById(R.id.btn_paper_a4);
        mBtnPaperA5 = findViewById(R.id.btn_paper_a5);
    }

    private void initListeners() {
        mBtnBack.setOnClickListener(v -> finish());
        
        mBtnPrint.setOnClickListener(v -> printReport());
        
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
                
                Log.d(TAG, "按钮点击: 选择了 " + imageCount + " 张图片");
                mCurrentImageCount = imageCount;
                
                // 自动加载并渲染
                loadReportWithImageCount(imageCount);
                
                Toast.makeText(PrintDemoA4Activity.this, 
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
        
        // 设置纸张大小按钮的点击事件
        mBtnPaperA4.setOnClickListener(v -> {
            mCurrentPaperSize = MedicalReportView.PAPER_SIZE_A4;
            mReportView.setPaperSize(mCurrentPaperSize);
            updatePaperSizeButtonState();
            
            // 如果已经加载了报告，重新渲染
            if (mReportLabels != null && mImageAreaLabels != null) {
                loadReportWithImageCount(mCurrentImageCount);
            }
            
            Toast.makeText(PrintDemoA4Activity.this, "已切换到A4纸张", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "切换到A4纸张");
        });
        
        mBtnPaperA5.setOnClickListener(v -> {
            mCurrentPaperSize = MedicalReportView.PAPER_SIZE_A5;
            mReportView.setPaperSize(mCurrentPaperSize);
            updatePaperSizeButtonState();
            
            // 如果已经加载了报告，重新渲染
            if (mReportLabels != null && mImageAreaLabels != null) {
                loadReportWithImageCount(mCurrentImageCount);
            }
            
            Toast.makeText(PrintDemoA4Activity.this, "已切换到A5纸张", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "切换到A5纸张");
        });
        
        // 初始化按钮状态
        updatePaperSizeButtonState();
    }
    
    /**
     * 更新纸张大小按钮的状态
     */
    private void updatePaperSizeButtonState() {
        if (mCurrentPaperSize == MedicalReportView.PAPER_SIZE_A4) {
            mBtnPaperA4.setEnabled(false);
            mBtnPaperA5.setEnabled(true);
        } else {
            mBtnPaperA4.setEnabled(true);
            mBtnPaperA5.setEnabled(false);
        }
    }

    /**
     * 加载报告数据（使用指定的图片数量）
     */
    private void loadReportWithImageCount(int imageCount) {
        try {
            Log.d(TAG, "开始加载报告，图片数量: " + imageCount);
            
            // 1. 读取报告模板
            mReportLabels = readReportXml();
            
            // 2. 读取图片区域模板
            mImageAreaLabels = readImageAreaXml(String.valueOf(imageCount));
            
            // 3. 调整报告布局
            adjustReportLayout();
            
            // 4. 设置数据到View
            mReportView.setReportData(mReportLabels, mImageAreaLabels);
            
            // 5. 加载图片
            loadImages(imageCount);
            
            Toast.makeText(this, "报告加载完成", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e(TAG, "加载报告失败", e);
            Toast.makeText(this, "加载报告失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 读取报告XML模板
     */
    private ArrayList<LabelBean> readReportXml() throws Exception {
        InputStream is = getAssets().open("一图报表.xml");
        SaxHelper2Text saxHelper = new SaxHelper2Text();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.parse(is, saxHelper);
        is.close();
        
        ArrayList<LabelBean> dataList = saxHelper.getDataList();
        return getSortList(dataList);
    }

    /**
     * 读取图片区域XML模板
     */
    private ArrayList<LabelBean> readImageAreaXml(String imageCount) throws Exception {
        String fileName = "检查0" + imageCount + "图.xml";
        Log.d(TAG, "读取图片区域XML: " + fileName);
        
        InputStream is = getAssets().open(fileName);
        
        SaxHelper2Text saxHelper = new SaxHelper2Text();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        parser.parse(is, saxHelper);
        is.close();
        
        ArrayList<LabelBean> dataList = saxHelper.getDataList();
        Log.d(TAG, "读取到 " + dataList.size() + " 个图片区域元素");
        return getSortList(dataList);
    }

    /**
     * 调整报告布局
     * A区域（固定不变）：病理学、活检部位、建议、底部分界线、地址/医院电话/检查医生
     * 动态区域：镜检所见和镜检诊断，按2:1比例分配，确保不与A区域重合
     */
    private void adjustReportLayout() {
        if (mReportLabels == null || mImageAreaLabels == null) {
            return;
        }
        
        // ========== 固定位置常量 ==========
        // A区域（固定不变）的原始位置
        final int A_AREA_PATHOLOGY_TOP = 955;      // 病理学原始Top
        final int A_AREA_BIOPSY_TOP = 978;         // 活检部位原始Top
        final int A_AREA_SUGGESTION_TOP = 1000;    // 建议原始Top
        final int A_AREA_BOTTOM_LINE_TOP = 1021;   // 底部分界线原始Top
        final int A_AREA_BOTTOM_ELEMENTS_TOP = 1024; // 地址/医院电话/检查医生原始Top
        
        // A区域的总高度（从病理学到底部元素结束）
        final int A_AREA_HEIGHT = 87;  // 病理学(18) + 间距(3) + 活检部位(18) + 间距(3) + 建议(18) + 间距(3) + 分界线(3) + 底部元素(18) + 间距(3)
        
        int nBottom = 1;
        int nNum = 0;
        int vpimage1 = 0;  // 默认图片区域高度
        int vpimage2 = 0;  // 选中模板的图片区域高度
        
        // 第一步：获取原始模板中的图像区域高度
        for (int i = 0; i < mReportLabels.size(); i++) {
            LabelBean mBean = mReportLabels.get(i);
            
            if (mBean.getType().equals("图像区域")) {
                nNum = i;
                vpimage1 = Integer.parseInt(mBean.getBottom()) - Integer.parseInt(mBean.getTop());
            }
        }
        
        // 第二步：计算选中模板的图像区域高度
        int maxImageBottom = 0;
        int minImageTop = Integer.MAX_VALUE;
        for (int i = 0; i < mImageAreaLabels.size(); i++) {
            LabelBean mBean = mImageAreaLabels.get(i);
            int mNewBottom = (Integer.parseInt(mBean.getBottom()) + nBottom);
            int mNewTop = (Integer.parseInt(mBean.getTop()) + nBottom);
            mBean.setTop(mNewTop + "");
            mBean.setBottom(mNewBottom + "");
            
            // 找到所有图像元素的最大Bottom和最小Top
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
        
        vpimage2 = maxImageBottom - minImageTop;
        int heightDiff = vpimage2 - vpimage1;  // 图像高度差值
        
        Log.d(TAG, "========== 布局调整开始 ==========");
        Log.d(TAG, "调整布局: vpimage1(默认图像高度)=" + vpimage1);
        Log.d(TAG, "调整布局: vpimage2(选中模板图像高度)=" + vpimage2);
        Log.d(TAG, "调整布局: 图像高度差值=" + heightDiff);
        Log.d(TAG, "调整布局: 图像区域底部=" + maxImageBottom);
        
        // 第三步：计算A区域的新位置（保持原始布局不变）
        // A区域从原始位置开始，保持固定
        int areaPathologyTop = A_AREA_PATHOLOGY_TOP;
        int areaBiopsyTop = A_AREA_BIOPSY_TOP;
        int areaSuggestionTop = A_AREA_SUGGESTION_TOP;
        int areaBottomLineTop = A_AREA_BOTTOM_LINE_TOP;
        int areaBottomElementsTop = A_AREA_BOTTOM_ELEMENTS_TOP;
        
        Log.d(TAG, "调整布局: A区域起始位置(病理学Top)=" + areaPathologyTop);
        
        // 第四步：计算镜检所见和镜检诊断的可用空间
        // 镜检所见标题的新Top = 图像底部 + 间距
        int seeContentTitleTop = maxImageBottom + 10;
        int seeContentTitleBottom = seeContentTitleTop + 18;  // 标题高度18
        
        // 镜检所见内容的新Top
        int seeContentTop = seeContentTitleBottom + 2;
        
        // 可用空间 = A区域起始位置 - 镜检所见内容起始位置 - 镜检诊断标题高度(20) - 间距
        int availableSpace = areaPathologyTop - seeContentTop - 20 - 3;  // 减去镜检诊断标题(18+2)和间距(3)
        
        Log.d(TAG, "调整布局: 镜检所见内容起始Top=" + seeContentTop);
        Log.d(TAG, "调整布局: 可用空间=" + availableSpace + " (从" + seeContentTop + "到" + areaPathologyTop + ")");
        
        // 确保可用空间为正数
        if (availableSpace < 60) {
            Log.w(TAG, "警告: 可用空间不足，调整为最小值60");
            availableSpace = 60;
        }
        
        // 镜检所见和镜检诊断按2:1比例分配空间
        // 镜检所见占2/3，镜检诊断占1/3
        int seeContentHeight = (availableSpace * 2) / 3;
        int diagnosisContentHeight = availableSpace - seeContentHeight;
        
        int seeContentBottom = seeContentTop + seeContentHeight;
        
        // 镜检诊断标题
        int diagnosisTitleTop = seeContentBottom + 2;
        int diagnosisTitleBottom = diagnosisTitleTop + 18;
        
        // 镜检诊断内容
        int diagnosisContentTop = diagnosisTitleBottom + 2;
        int diagnosisContentBottom = diagnosisContentTop + diagnosisContentHeight;
        
        Log.d(TAG, "调整布局: 镜检所见内容 Top=" + seeContentTop + ", Bottom=" + seeContentBottom + ", 高度=" + seeContentHeight);
        Log.d(TAG, "调整布局: 镜检诊断内容 Top=" + diagnosisContentTop + ", Bottom=" + diagnosisContentBottom + ", 高度=" + diagnosisContentHeight);
        Log.d(TAG, "调整布局: 镜检诊断Bottom到A区域Top的间距=" + (areaPathologyTop - diagnosisContentBottom));
        Log.d(TAG, "========== 布局调整结束 ==========");
        
        // 第四步：应用调整到所有元素
        int t = 0;
        int tF = 0;
        int tz = 10;
        
        for (int i = nNum + 1; i < mReportLabels.size(); i++) {
            LabelBean mBean = mReportLabels.get(i);
            int top = Integer.parseInt(mBean.getTop());
            int bottom = Integer.parseInt(mBean.getBottom());
            String order = mBean.getOrder();
            String type = mBean.getType();
            String content = mBean.getContent();
            
            if (i == nNum + 1) {
                t = vpimage2 - vpimage1;
                int nLastBottom = maxImageBottom;
                nBottom = (nLastBottom - top) - t;
            }
            
            // 调整图像区域下方的第一条分界线
            if (order.equals("7") && tF == 0 && content.equals("分界线") && top > 400) {
                tF = 1;
                mBean.setTop((maxImageBottom + 5) + "");
                mBean.setBottom((maxImageBottom + 6) + "");
                Log.d(TAG, "调整布局: 第一条分界线 newTop=" + mBean.getTop());
            }
            // 镜检所见标题
            else if (order.equals("46") && type.equals("Caption")) {
                mBean.setTop(seeContentTitleTop + "");
                mBean.setBottom(seeContentTitleBottom + "");
                Log.d(TAG, "调整布局: 镜检所见标题 newTop=" + seeContentTitleTop);
            }
            // 镜检所见内容
            else if (order.equals("46") && content.equals("镜检所见") && type.equals("Edit")) {
                mBean.setTop(seeContentTop + "");
                mBean.setBottom(seeContentBottom + "");
                Log.d(TAG, "调整布局: 镜检所见内容 newTop=" + seeContentTop + ", newBottom=" + seeContentBottom);
            }
            // 镜检诊断标题
            else if (order.equals("47") && type.equals("Caption")) {
                mBean.setTop(diagnosisTitleTop + "");
                mBean.setBottom(diagnosisTitleBottom + "");
                Log.d(TAG, "调整布局: 镜检诊断标题 newTop=" + diagnosisTitleTop);
            }
            // 镜检诊断内容
            else if (order.equals("47") && content.equals("镜检诊断") && type.equals("Edit")) {
                mBean.setTop(diagnosisContentTop + "");
                mBean.setBottom(diagnosisContentBottom + "");
                Log.d(TAG, "调整布局: 镜检诊断内容 newTop=" + diagnosisContentTop + ", newBottom=" + diagnosisContentBottom);
            }
            // ========== A区域元素（保持原始位置不变） ==========
            // 病理学
            else if (order.equals("42") && type.equals("Caption")) {
                mBean.setTop(areaPathologyTop + "");
                mBean.setBottom((areaPathologyTop + 18) + "");
            }
            else if (order.equals("42") && content.equals("病理学") && type.equals("Edit")) {
                mBean.setTop(areaPathologyTop + "");
                mBean.setBottom((areaPathologyTop + 18) + "");
            }
            // 活检部位
            else if (order.equals("41") && type.equals("Caption")) {
                mBean.setTop(areaBiopsyTop + "");
                mBean.setBottom((areaBiopsyTop + 18) + "");
            }
            else if (order.equals("43") && content.equals("活检") && type.equals("Edit")) {
                mBean.setTop(areaBiopsyTop + "");
                mBean.setBottom((areaBiopsyTop + 18) + "");
            }
            // 试验
            else if (order.equals("61")) {
                mBean.setTop(areaBiopsyTop + "");
                mBean.setBottom((areaBiopsyTop + 18) + "");
            }
            // 建议标题
            else if (order.equals("36") && type.equals("Caption")) {
                mBean.setTop(areaSuggestionTop + "");
                mBean.setBottom((areaSuggestionTop + 18) + "");
            }
            // 建议内容
            else if (order.equals("36") && content.equals("建议") && type.equals("Edit")) {
                mBean.setTop(areaSuggestionTop + "");
                mBean.setBottom((areaSuggestionTop + 18) + "");
            }
            // 底部分界线
            else if (order.equals("7") && content.equals("分界线") && top >= 1020) {
                mBean.setTop(areaBottomLineTop + "");
                mBean.setBottom((areaBottomLineTop + 1) + "");
            }
            // 底部固定元素：地址、医院电话、检查医生
            else if (top >= areaBottomElementsTop) {
                // 保持原始位置不变
            }
        }
    }

    /**
     * 加载图片
     */
    private void loadImages(int imageCount) {
        for (int i = 0; i < imageCount && i < mImageUrls.length; i++) {
            String order = String.valueOf(i + 1);
            mReportView.setImageUrl(order, mImageUrls[i]);
        }
        
        // 加载Logo
        mReportView.setImageUrl("logo", "https://www.szcme.com/assets/image/与KMT合照.jpg");
    }

    /**
     * 打印报告
     */
    private void printReport() {
        if (mReportLabels == null || mImageAreaLabels == null) {
            Toast.makeText(this, "请先选择图片数量加载报告", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 检查图片是否加载完成
        if (!checkImagesLoaded()) {
            Toast.makeText(this, "图片正在加载中，请稍后再试", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // 获取打印管理器
            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
            
            // 配置打印属性
            PrintAttributes.Builder builder = new PrintAttributes.Builder();
            builder.setColorMode(PrintAttributes.COLOR_MODE_COLOR);  // 彩色打印
            
            // 根据当前纸张大小设置
            if (mCurrentPaperSize == MedicalReportView.PAPER_SIZE_A5) {
                builder.setMediaSize(PrintAttributes.MediaSize.ISO_A5);  // A5纸张
                Log.d(TAG, "打印纸张: A5");
            } else {
                builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4);  // A4纸张
                Log.d(TAG, "打印纸张: A4");
            }
            
            builder.setResolution(new PrintAttributes.Resolution("high_quality", "高质量", 300, 300));  // 300 DPI
            builder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);  // 无边距
            
            PrintAttributes printAttributes = builder.build();
            
            // 创建打印适配器
            com.shenma.printtest.print.MedicalReportPrintAdapter printAdapter = 
                new com.shenma.printtest.print.MedicalReportPrintAdapter(this, mReportView);
            
            // 开始打印任务
            String jobName = "医用报告_" + System.currentTimeMillis();
            PrintJob printJob = printManager.print(jobName, printAdapter, printAttributes);
            
            Log.d(TAG, "打印任务已创建: " + jobName);
            Toast.makeText(this, "正在准备打印...", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e(TAG, "打印失败", e);
            Toast.makeText(this, "打印失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * 检查图片是否加载完成
     */
    private boolean checkImagesLoaded() {
        // 这里可以添加更复杂的检查逻辑
        // 简单起见，假设图片已加载
        return true;
    }

    /**
     * 排序列表（升序）
     */
    private ArrayList<LabelBean> getSortList(ArrayList<LabelBean> dataList) {
        Collections.sort(dataList, new Comparator<LabelBean>() {
            public int compare(LabelBean o1, LabelBean o2) {
                float diff = Float.parseFloat(o1.getTop()) - Float.parseFloat(o2.getTop());
                if (diff > 0) {
                    return 1;
                } else if (diff < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        return dataList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReportView != null) {
            mReportView.clearImageCache();
        }
    }
}
