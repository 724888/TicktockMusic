package com.lauzy.freedom.librarys.widght.music;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.lauzy.freedom.librarys.R;

/**
 * Desc : PlayPauseView
 * Author : Lauzy
 * Date : 2017/8/11
 * Blog : http://www.jianshu.com/u/e76853f863a9
 * Email : freedompaladin@gmail.com
 */
@SuppressWarnings("unused")
public class PlayPauseView extends View {

    private int mWidth; //View宽度
    private int mHeight; //View高度
    private Paint mPaint;
    private Path mLeftPath; //暂停时左侧竖条Path
    private Path mRightPath; //暂停时右侧竖条Path
    private float mGapWidth; //两个暂停竖条中间的空隙,默认为两侧竖条的宽度
    private float mProgress; //动画Progress
    private Rect mRect;
    private boolean isPlaying;
    private float mRectWidth;  //圆内矩形宽度
    private float mRectHeight; //圆内矩形高度
    private int mRectLT;  //矩形左侧上侧坐标
    private int mRadius;  //圆的半径
    private int mBgColor = Color.WHITE;
    private int mBtnColor = Color.BLACK;
    private int mDirection = Direction.POSITIVE.value;
    private float mPadding;
    private int mAnimDuration = 200;//动画时间

    public PlayPauseView(Context context) {
        super(context);
    }

    public PlayPauseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PlayPauseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mLeftPath = new Path();
        mRightPath = new Path();
        mRect = new Rect();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PlayPauseView);
        mBgColor = ta.getColor(R.styleable.PlayPauseView_bg_color, Color.WHITE);
        mBtnColor = ta.getColor(R.styleable.PlayPauseView_btn_color, Color.BLACK);
        mGapWidth = ta.getFloat(R.styleable.PlayPauseView_gap_width, 0);
        mDirection = ta.getInt(R.styleable.PlayPauseView_anim_direction, Direction.POSITIVE.value);
        mPadding = ta.getFloat(R.styleable.PlayPauseView_space_padding, 0);
        mAnimDuration = ta.getInt(R.styleable.PlayPauseView_anim_duration, 200);
        ta.recycle();
    }

    @SuppressWarnings("unused")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                mWidth = mHeight = Math.min(mWidth, mHeight);
                setMeasuredDimension(mWidth, mHeight);
                break;
            case MeasureSpec.AT_MOST:
                float density = getResources().getDisplayMetrics().density;
                mWidth = mHeight = (int) (50 * density); //默认50dp
                setMeasuredDimension(mWidth, mHeight);
                break;
            case MeasureSpec.UNSPECIFIED:
                break;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = mHeight = w;
        initValue();
    }

    private void initValue() {
//        int rectLT = (int) (mWidth / 2 - radius / Math.sqrt(2));
//        int rectRB = (int) (mWidth / 2 + radius / Math.sqrt(2));
        mRadius = mWidth / 2;
       /* if (getPadding() > mRadius / Math.sqrt(2) || mPadding < 0) {
            *//*throw new IllegalArgumentException("The value of your padding is too large. " +
                    "The value must not be greater than " + (int) (mRadius / Math.sqrt(2)));*//*
        }*/
        mPadding = getSpacePadding() == 0 ? mRadius / 3f : getSpacePadding();
        if (getSpacePadding() > mRadius / Math.sqrt(2) || mPadding < 0) {
            mPadding = mRadius / 3f; //默认值
        }
        float space = (float) (mRadius / Math.sqrt(2) - mPadding); //矩形宽高的一半
        mRectLT = (int) (mRadius - space);
        int rectRB = (int) (mRadius + space);
        mRect.top = mRectLT;
        mRect.bottom = rectRB;
        mRect.left = mRectLT;
        mRect.right = rectRB;
//        mRectWidth = mRect.width();
//        mRectHeight = mRect.height();
        //不再使用int类型，改用float类型，并在原有值的基础上+1像素，确保合并成三角形时中间无缝隙
        // (若尺寸较大仍有缝隙)
        mRectWidth = space * 2 + 1f;
        mRectHeight = space * 2 + 1f;
        mGapWidth = getGapWidth() != 0 ? getGapWidth() : mRectWidth / 3;
        mProgress = isPlaying ? 0 : 1;
        mAnimDuration = getAnimDuration() < 0 ? 200 : getAnimDuration();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mLeftPath.rewind();
        mRightPath.rewind();

        float distance = mGapWidth * (1 - mProgress);  //暂停时左右两边矩形距离
        float barWidth = mRectWidth / 2 - distance / 2;     //一个矩形的宽度
        float leftLeftTop = barWidth * mProgress;       //左边矩形左上角

        float rightLeftTop = barWidth + distance;       //右边矩形左上角
        float rightRightTop = 2 * barWidth + distance;  //右边矩形右上角
        float rightRightBottom = rightRightTop - barWidth * mProgress; //右边矩形右下角

        if (mDirection == Direction.NEGATIVE.value) {
            mLeftPath.moveTo(mRectLT, mRectLT);
            mLeftPath.lineTo(leftLeftTop + mRectLT, mRectHeight + mRectLT);
            mLeftPath.lineTo(barWidth + mRectLT, mRectHeight + mRectLT);
            mLeftPath.lineTo(barWidth + mRectLT, mRectLT);
            mLeftPath.close();

            mRightPath.moveTo(rightLeftTop + mRectLT, mRectLT);
            mRightPath.lineTo(rightLeftTop + mRectLT, mRectHeight + mRectLT);
            mRightPath.lineTo(rightRightBottom + mRectLT, mRectHeight + mRectLT);
            mRightPath.lineTo(rightRightTop + mRectLT, mRectLT);
            mRightPath.close();
        } else {
            mLeftPath.moveTo(leftLeftTop + mRectLT, mRectLT);
            mLeftPath.lineTo(mRectLT, mRectHeight + mRectLT);
            mLeftPath.lineTo(barWidth + mRectLT, mRectHeight + mRectLT);
            mLeftPath.lineTo(barWidth + mRectLT, mRectLT);
            mLeftPath.close();

            mRightPath.moveTo(rightLeftTop + mRectLT, mRectLT);
            mRightPath.lineTo(rightLeftTop + mRectLT, mRectHeight + mRectLT);
            mRightPath.lineTo(rightLeftTop + mRectLT + barWidth, mRectHeight + mRectLT);
            mRightPath.lineTo(rightRightBottom + mRectLT, mRectLT);
            mRightPath.close();
        }
        canvas.save();
//        mPaint.setStrokeWidth(1);
//        mPaint.setStyle(Paint.Style.STROKE);
//        canvas.drawRect(mRect, mPaint);
        mPaint.setColor(mBgColor);
        canvas.drawCircle(mWidth / 2, mHeight / 2, mRadius, mPaint);

        canvas.translate(mRectHeight / 8f * mProgress, 0);
        float progress = isPlaying ? (1 - mProgress) : mProgress;
        int corner = mDirection == Direction.NEGATIVE.value ? -90 : 90;
        float rotation = isPlaying ? corner * (1 + progress) : corner * progress;
        canvas.rotate(rotation, mWidth / 2f, mHeight / 2f);

        mPaint.setColor(mBtnColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(mLeftPath, mPaint);
        canvas.drawPath(mRightPath, mPaint);
        canvas.restore();
    }

    private ValueAnimator getPlayPauseAnim() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(isPlaying ? 1 : 0, isPlaying ? 0 : 1);
        valueAnimator.setDuration(mAnimDuration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mProgress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        return valueAnimator;
    }

    public void play() {
        if (getPlayPauseAnim() != null) {
            getPlayPauseAnim().cancel();
        }
        setPlaying(true);
        getPlayPauseAnim().start();
    }

    public void playWithoutAnim() {
        if (getPlayPauseAnim() != null) {
            getPlayPauseAnim().cancel();
        }
        setPlaying(true);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(isPlaying ? 1 : 0, isPlaying ? 0 : 1);
        valueAnimator.setDuration(0);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mProgress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.start();
    }

    public void pause() {
        if (getPlayPauseAnim() != null) {
            getPlayPauseAnim().cancel();
        }
        setPlaying(false);
        getPlayPauseAnim().start();
    }

    private PlayPauseListener mPlayPauseListener;

    public void setPlayPauseListener(PlayPauseListener playPauseListener) {
        mPlayPauseListener = playPauseListener;
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying()) {
                    pause();
                    if (null != mPlayPauseListener) {
                        mPlayPauseListener.pause();
                    }
                } else {
                    play();
                    if (null != mPlayPauseListener) {
                        mPlayPauseListener.play();
                    }
                }
            }
        });
    }

    public interface PlayPauseListener {
        void play();

        void pause();
    }

    /* ------------下方是参数------------- */

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public void setGapWidth(int gapWidth) {
        mGapWidth = gapWidth;
    }

    public float getGapWidth() {
        return mGapWidth;
    }

    public int getBgColor() {
        return mBgColor;
    }

    public int getBtnColor() {
        return mBtnColor;
    }

    public int getDirection() {
        return mDirection;
    }

    public void setBgColor(int bgColor) {
        mBgColor = bgColor;
    }

    public void setBtnColor(int btnColor) {
        mBtnColor = btnColor;
    }

    public void setDirection(Direction direction) {
        mDirection = direction.value;
    }

    public float getSpacePadding() {
        return mPadding;
    }

    public void setSpacePadding(float padding) {
        mPadding = padding;
    }

    public int getAnimDuration() {
        return mAnimDuration;
    }

    public void setAnimDuration(int animDuration) {
        mAnimDuration = animDuration;
    }

    public enum Direction {
        POSITIVE(1),//顺时针
        NEGATIVE(2);//逆时针
        int value;

        Direction(int value) {
            this.value = value;
        }
    }
}
