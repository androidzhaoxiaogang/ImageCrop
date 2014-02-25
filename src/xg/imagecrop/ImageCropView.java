package xg.imagecrop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

public class ImageCropView extends FrameLayout {

	private static final int STATE_INIT = 0;
	private static final int STATE_DRAG = 1;
	private static final int STATE_ZOOM = 2;

	private int mState = STATE_INIT;

	private ImageView mImageView;
	private ImageCoverView mImageCoverView;
	private Bitmap mBitmap;

	private Matrix mMatrix = new Matrix();
	private Matrix mSavedMatrix = new Matrix();

	private PointF mStartPoint = new PointF();
	private PointF mZoomPoint = new PointF();
	
	private float mOldDist = 1f;
	private float [] mMatrixValues = new float[9];
	private float mMiniScale = 1f;
	
	public ImageCropView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	public ImageCropView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public ImageCropView(Context context) {
		super(context);
		initView(context);
	}

	public void setImageBitmap(Bitmap bitmap) {
		this.mBitmap = bitmap;
		mImageView.setScaleType(ScaleType.FIT_CENTER);
		mImageView.setImageBitmap(bitmap);
	}

	private void initView(Context context) {
		mImageView = new ImageView(context);
		addView(mImageView, new FrameLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT));
		mImageCoverView = new ImageCoverView(context);
		addView(mImageCoverView, new FrameLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT));
		mImageView.setOnTouchListener(mOntouchListener);
	}
	
	View.OnTouchListener mOntouchListener = new View.OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				onActionDown(event);
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				onActionPointerDown(event);
				break;
			case MotionEvent.ACTION_MOVE:
				onActionMove(event);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				mState = STATE_INIT;
				mMatrix.getValues(mMatrixValues);
				break;
			}
			mImageView.setImageMatrix(mMatrix);
			return true;
		}
	};
	
	public Bitmap cropImageBitmap(String path) throws FileNotFoundException {
		if(mBitmap != null) {
			int left = (int)((mImageCoverView.getCoverLeft() - mMatrixValues[2]) / mMatrixValues[0]);
			int top = (int)((mImageCoverView.getCoverTop() - mMatrixValues[5]) / mMatrixValues[4]);
			int right = (int) ((mImageCoverView.getCoverRight() - mMatrixValues[2]) / mMatrixValues[0]);
			int bottom = (int) ((mImageCoverView.getCoverBottom() - mMatrixValues[5]) / mMatrixValues[4]);
			trimSize(left, top, right, bottom);
			Bitmap bitmap = Bitmap.createBitmap(mBitmap, left, top, right - left, bottom - top);
			bitmap.compress(CompressFormat.PNG, 100, new FileOutputStream(new File(path)));
			return bitmap;
		}
		return null;
	}
	
	private void trimSize(int left, int top, int right, int bottom) {
		mMatrix.getValues(mMatrixValues);
		int bitmapLeft = (int) mMatrixValues[2];
		int bitmapTop = (int) mMatrixValues[5];
		int bitmapRight = (int) (mBitmap.getWidth() * mMatrixValues[0] - mMatrixValues[2]);
		int bitmapBottom = (int) (mBitmap.getHeight() * mMatrixValues[4] - mMatrixValues[5]);
		if(bitmapLeft > mImageCoverView.getCoverLeft()) {
			left += (bitmapLeft - mImageCoverView.getCoverLeft()) / mMatrixValues[0];
		}
		if(bitmapTop > mImageCoverView.getCoverTop()) {
			top += (bitmapTop - mImageCoverView.getCoverTop()) / mMatrixValues[4];
		}
		if(bitmapRight < mImageCoverView.getCoverRight()) {
			right -= (mImageCoverView.getCoverRight() - bitmapRight) / mMatrixValues[0];
		}
		if(bitmapBottom < mImageCoverView.getCoverBottom()) {
			bottom -= (mImageCoverView.getCoverBottom() - bitmapBottom) / mMatrixValues[4];
		}
	}
	
	private void onActionDown( MotionEvent event) {
		mImageView.setScaleType(ScaleType.MATRIX);
		mMatrix.set(mImageView.getImageMatrix());
		mSavedMatrix.set(mMatrix);
		mStartPoint.set(event.getX(), event.getY());
		mState = STATE_DRAG;
	}
	
	private void onActionPointerDown(MotionEvent event) {
		mOldDist = spacing(event);
		if (mOldDist > 10f) {
			mSavedMatrix.set(mMatrix);
			onCenterPoint(mZoomPoint, event);
			mMiniScale = (float) mImageCoverView.getCoverWidth() / 
					Math.min(mBitmap.getWidth(), mBitmap.getHeight());
			mState = STATE_ZOOM;
		}
	}
	
	private void onActionMove(MotionEvent event) {
		if (mState == STATE_DRAG) {
			mMatrix.set(mSavedMatrix);
			float transX = event.getX() - mStartPoint.x;
			float transY = event.getY() - mStartPoint.y;
			mMatrix.getValues(mMatrixValues);
			float leftLimit = mImageCoverView.getCoverLeft() - mMatrixValues[2];
			float topLimit = mImageCoverView.getCoverTop() - mMatrixValues[5];
			float rightLimit = mImageCoverView.getCoverRight() - 
					(mBitmap.getWidth() * mMatrixValues[0] + mMatrixValues[2]);
			float bottomLimit = mImageCoverView.getCoverBottom() - 
					(mBitmap.getHeight() * mMatrixValues[0] + mMatrixValues[5]);
			if(transX > 0 && transX > leftLimit) {
				transX = leftLimit;
			}
			if(transY > 0 && transY > topLimit) {
				transY = topLimit;
			}
			if(transX < 0 && transX < rightLimit) {
				transX = rightLimit;
			}
			if(transY < 0 && transY < bottomLimit) {
				transY = bottomLimit;
			}
			mMatrix.postTranslate(transX, transY);
		} else if (mState == STATE_ZOOM) {
			float newDist = spacing(event);
			if (newDist > 10f) {
				mMatrix.set(mSavedMatrix);
				mMatrix.getValues(mMatrixValues);
				float scale = newDist / mOldDist;
				if(mMatrixValues[0] * scale < mMiniScale) {
					scale = mMiniScale / mMatrixValues[0];
				}
				mMatrix.postScale(scale, scale, mZoomPoint.x, mZoomPoint.y);
			}
		}
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	private void onCenterPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
}