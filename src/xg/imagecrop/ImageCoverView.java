package xg.imagecrop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

public class ImageCoverView extends View {
	private int mCoverLeft = 0;
	private int mCoverTop = 0;
	private int mCoverRight = 0;
	private int mCoverBottom = 0;
	
	private int mHideColor = 0xAF000000;
	private int mCoverColor = 0xFF808080;
	
	private Paint mPaint = new Paint();
	
	private int mCoverWidth = 200;
	private float mStrokWidth = 3.0f;
	
	private PointF mCoverMidPoint = new PointF();
	
	public ImageCoverView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ImageCoverView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ImageCoverView(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		DisplayMetrics dm = getResources().getDisplayMetrics();
		mCoverWidth = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, mCoverWidth, dm);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		initDraw();
		
		mPaint.setColor(mCoverColor);
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeWidth(mStrokWidth);
		canvas.drawRect(mCoverLeft, mCoverTop, mCoverRight, mCoverBottom, mPaint);	
		mPaint.setStyle(Style.FILL);
		mPaint.setColor(mHideColor);
		canvas.drawRect(getLeft(), getTop(), getRight(), mCoverTop, mPaint);	
		canvas.drawRect(getLeft(), mCoverTop, mCoverLeft, 
				mCoverBottom + mStrokWidth / 2, mPaint);	
		canvas.drawRect(mCoverRight + mStrokWidth / 2, mCoverTop,
				getRight(), mCoverBottom + mStrokWidth / 2, mPaint);	
		canvas.drawRect(getLeft(), mCoverBottom + mStrokWidth / 2, 
				getRight(), getBottom(), mPaint);
	}
	
	private void initDraw() {
		mCoverMidPoint.set((getRight() - getLeft()) / 2, (getBottom() - getTop()) / 2);
		mCoverLeft = (int) (mCoverMidPoint.x - mCoverWidth / 2);
		mCoverTop = (int) (mCoverMidPoint.y - mCoverWidth / 2);
		mCoverRight = (int) (mCoverMidPoint.x + mCoverWidth / 2);
		mCoverBottom = (int) (mCoverMidPoint.y + mCoverWidth / 2);
	}

	public int getCoverLeft() {
		return mCoverLeft;
	}

	public int getCoverTop() {
		return mCoverTop;
	}

	public int getCoverRight() {
		return mCoverRight;
	}

	public int getCoverBottom() {
		return mCoverBottom;
	}

	public PointF getCoverMidPoint() {
		return mCoverMidPoint;
	}

	public int getCoverWidth() {
		return mCoverWidth;
	}
	
	public void setCoverWidth(int width) {
		this.mCoverWidth = width;
		postInvalidate();
	}

	public int getHideColor() {
		return mHideColor;
	}
	
	public void setHidColor(int color) {
		this.mHideColor = color;
		postInvalidate();
	}

	public int getCoverColor() {
		return mCoverColor;
	}
	
	public void setCoverColor(int color) {
		this.mCoverColor = color;
		postInvalidate();
	}

	public float getStrokWidth() {
		return mStrokWidth;
	}
	
	public void setStrokWidth(float width) {
		this.mStrokWidth = width;
		postInvalidate();
	}
	
}
