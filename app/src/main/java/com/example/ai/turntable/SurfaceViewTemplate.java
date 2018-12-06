package com.example.ai.turntable;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * SurfaceView使用的一般模板，以后可以参照这个来写
 */
public class SurfaceViewTemplate extends SurfaceView implements SurfaceHolder.Callback,Runnable{

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

    public SurfaceViewTemplate(Context context) {
        this(context,null);
    }

    public SurfaceViewTemplate(Context context, AttributeSet attrs) {
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
    public void surfaceCreated(SurfaceHolder holder) {
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
        while(isRunning){
            draw();
        }
    }

    private void draw() {
        try {
            // 获得Canvas
            mCanvas = mHolder.lockCanvas();
            /**
             * 在主界面点击home键和back键surface都会被销毁，所以要判空
             */
            if (mCanvas != null){
                // draw something
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null){
                // 释放Canvas
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }

    }
}
