package com.example.ai.turntable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * SurfaceView使用的一般模板，以后可以参照这个来写
 */
public class LuckyPan extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder mHolder;
    private Canvas mCanvas;

    /**
     * 用于绘制的线程
     */
    private Thread t;
    /**
     * 线程的控制开关
     */
    private boolean isRunning;
    /**
     * 盘块的奖项
     */
    private String[] mStrs = new String[]{"单反相机", "IPAD", "恭喜发财",
            "IPHONE", "服装一套", "恭喜发财"};
    /**
     * 奖项的图标
     */
    private int[] mImgs = new int[]{R.drawable.p_danfan,
            R.drawable.p_ipad,
            R.drawable.p_xiaolian,
            R.drawable.p_iphone,
            R.drawable.p_meizi,
            R.drawable.p_xiaolian};
    /**
     * 与图标对应的bitmap数组
     */
    private Bitmap[] mImgsBitmap;

    private Bitmap mBgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg2);

    /**
     * 盘块的颜色
     */
    private int[] mColors = new int[]{0xFFFFC300, 0xFFF17E01, 0xFFFFC300, 0xFFF17E01, 0xFFFFC300, 0xFFF17E01};

    private int mItemCount = 6;
    /**
     * 整个盘块的范围
     */
    private RectF mRange = new RectF();

    /**
     * 整个盘块的直径
     */
    private int mRadius;
    /**
     * 绘制盘块的画笔
     */
    private Paint mArcPaint;

    /**
     * 绘制文本的画笔
     */
    private Paint mTextPaint;

    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
            20, getResources().getDisplayMetrics());
    /**
     * 盘块滚动的速度
     */
    private double mSpeed;

    /**
     * 绘制的角度
     */
    private volatile float mStartAngle = 0;
    /**
     * 是否点击了停止按钮
     */
    private boolean isShouldEnd;

    /**
     * 转盘的中心位置
     */
    private int mCenter;
    /**
     * 这里以paddingLeft为准
     */
    private int mPadding;


    public LuckyPan(Context context) {
        this(context, null);
    }

    public LuckyPan(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);

        // 可获得焦点
        setFocusable(true);
        setFocusableInTouchMode(true);
        // 设置常亮
        setKeepScreenOn(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = Math.min(getMeasuredWidth(), getMeasuredHeight());

        mPadding = getPaddingLeft();
        // 圆盘直径
        mRadius = width - mPadding * 2;
        // 圆盘中心点
        mCenter = width / 2;
        // 设置成正方形
        setMeasuredDimension(width, width);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        /**
         * 初始化绘制盘块的画笔
         */
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);

        // 初始化绘制文字的画笔
        mTextPaint = new Paint();
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setTextSize(mTextSize);
        // 初始化盘块绘制的范围
        mRange = new RectF(mPadding, mPadding, mPadding + mRadius, mPadding + mRadius);

        // 初始化图片
        mImgsBitmap = new Bitmap[mItemCount];
        for (int i = 0; i < mItemCount; i++) {
            mImgsBitmap[i] = BitmapFactory.decodeResource(getResources(), mImgs[i]);
        }

        // 开启线程绘制
        isRunning = true;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // 关闭线程
        isRunning = false;
    }

    @Override
    public void run() {
        // 不断进行绘制
        while (isRunning) {
            long start = System.currentTimeMillis();
            draw();
            long end = System.currentTimeMillis();

            if (end - start < 50) {
                try {
                    Thread.sleep(50 - (end - start));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void draw() {
        try {
            // 获得Canvas
            mCanvas = mHolder.lockCanvas();
            /**
             * 在主界面点击home键和back键surface都会被销毁，所以要判空
             */
            if (mCanvas != null) {
                // draw something
                // 绘制背景
                drawBg();

                // 绘制盘块
                float tmpAngle = mStartAngle;

                float sweepAngle = 360 / mItemCount;

                for (int i = 0; i < mItemCount; i++) {
                    mArcPaint.setColor(mColors[i]);
                    /**
                     * 绘制盘块
                     */
                    mCanvas.drawArc(mRange, tmpAngle, sweepAngle, true, mArcPaint);

                    /**
                     * 绘制文本
                     */
                    drawText(tmpAngle, sweepAngle, mStrs[i]);
                    /**
                     * 绘制每个盘块的图标
                     */
                    drawIcon(tmpAngle, mImgsBitmap[i]);

                    tmpAngle += sweepAngle;
                }
                /**
                 * 只要mStartAngle改变，转盘就会转动
                 */
                mStartAngle += mSpeed;
                /**
                 * 如果点击了停止按钮，就缓慢停止
                 */
                if (isShouldEnd) {
                    mSpeed -= 1;
                }
                if (mSpeed <= 0) {
                    mSpeed = 0;
                    isShouldEnd = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null) {
                // 释放Canvas
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }

    }

    /**
     * 绘制图标
     *
     * @param tmpAngle
     * @param bitmap
     */
    private void drawIcon(float tmpAngle, Bitmap bitmap) {
        // 设置图片的宽度为直径1/8
        int imgWidth = mRadius / 8;
        // Math.PI/8
        float angle = (float) ((tmpAngle + 360 / mItemCount / 2) * Math.PI / 180);
        /**
         * x,y是图片中心点坐标
         */
        int x = (int) (mCenter + mRadius / 2 / 2 * Math.cos(angle));
        int y = (int) (mCenter + mRadius / 2 / 2 * Math.sin(angle));

        // 确定图片位置
        Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth / 2, y + imgWidth / 2);
        mCanvas.drawBitmap(bitmap, null, rect, null);
    }


    /**
     * 绘制每个盘块的文本
     *
     * @param tmpAngle
     * @param sweepAngle
     * @param mStr
     */
    private void drawText(float tmpAngle, float sweepAngle, String mStr) {

        Path path = new Path();
        path.addArc(mRange, tmpAngle, sweepAngle);
        // 利用水平偏移量让文字居中
        // 文本的宽度
        float textWidth = mTextPaint.measureText(mStr);
        int hOffset = (int) (mRadius * Math.PI / mItemCount / 2 - textWidth / 2);
        int vOffset = mRadius / 2 / 6;//垂直偏移量

        /**
         * hOffset是水平偏移量，即就是圆弧的方向，
         * vOffset是垂直偏移量，即就是半径指向圆心的方向
         */
        mCanvas.drawTextOnPath(mStr, path, hOffset, vOffset, mTextPaint);

    }

    /**
     * 绘制背景
     */
    private void drawBg() {
        mCanvas.drawColor(0xFFFFFFFF);
        mCanvas.drawBitmap(mBgBitmap,
                null,
                new RectF(mPadding / 2, mPadding / 2,
                        getMeasuredWidth() - mPadding / 2,
                        getMeasuredHeight() - mPadding / 2),
                null);
    }

    /**
     * 点击启动旋转,index可以控制停下来的位置
     */
    public void luckyStart(int index) {

        int angle = 360 / mItemCount;

        // 计算每项中奖的范围(当前的index)
        // 0-> 210 ~ 270
        // 1-> 150 ~ 210
        // 2-> 90 ~ 150
        // 3-> 30 ~ 90
        // 4-> -30 ~ 30
        // 5-> -90 ~ -30

        float from = 270 - (index + 1) * angle;
        float end = from + angle;

        // 设置停下来需要旋转的距离,停在一个区间
        float targetFrom = 4 * 360 + from;
        float targetEnd = 4 * 360 + end;

        /**
         * <pre>
         *     v1>0
         *     并且每次-1
         *     (v1+0)*(v1+1)/2=targetFrom;
         *     v1*v1+v1-2*targetFrom=0;
         *     v1=(-1+Math.sqrt(1+8*targetFrom))/2;
         *
         * </pre>
         */
        float v1 = (float) ((-1 + Math.sqrt(1 + 8 * targetFrom)) / 2);
        float v2 = (float) ((-1 + Math.sqrt(1 + 8 * targetEnd)) / 2);
        /**
         * v1 < mSpeed < v2
         */
        mSpeed = v1 + Math.random() * (v2 - v1);

        isShouldEnd = false;
    }

    /**
     * 点击启动转盘，控制概率
     */
    public void luckyStart(){
        /**
         * java产生随机数的方法
         * 1、Math.random()->得到0-1的随机数
         * 2、Random a = new Random();
         *    a.nextInt(10)->得到0-9之间的随机数
         */
        Random random = new Random();
        int randomNumber = random.nextInt(100);
        if (randomNumber<3){
            luckyStart(0);
        }else if (randomNumber<10 && randomNumber>=3){
            luckyStart(1);
        }else if (randomNumber<20 && randomNumber>=10){
            luckyStart(3);
        }else if (randomNumber<50 && randomNumber>=20){
            luckyStart(4);
        }else {
            if (randomNumber%2==0){
                luckyStart(2);
            }else {
                luckyStart(5);
            }

        }
    }

    /**
     * 点击停止
     */
    public void luckyEnd() {
        mStartAngle = 0;
        isShouldEnd = true;
    }

    /**
     * 判断是否在转动
     *
     * @return
     */
    public boolean isStart() {
        return mSpeed != 0;
    }

    /**
     * 判断是否结束
     *
     * @return
     */
    public boolean isShouldEnd() {
        return isShouldEnd;
    }

}
