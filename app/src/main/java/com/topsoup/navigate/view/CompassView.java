package com.topsoup.navigate.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.graphics.Rect;

public class CompassView extends View {
	private static final String TAG = "CompassView";

	private Paint circlePaint, tickPaint, mPaint, mPaintRun;
	private Paint mKeyPaint;//每30度绘制一个关键线

	private TextPaint textPaint;
	// 指定控件宽和高，用于自适应
	private float vWidth;
	// 圆盘的半径
	private float compassRadiu;
	// 刻度线段的长度
	private float tickHeight;
	// 字体高度和宽度
	private float textHeight;

	private float mDegrees, distance;
	private float mDegreesRun;
	private float speed_j;
	private static final double CONVERSION_ANGLE_CONST = Math.PI / 180;  //转换角所用的常量
	private static final int DIVIDE_COUNT = 24; //将圆划分为24等份
	private double lineRateSize = 1 / 15d;
	private int offset = 90;
	private int rotate = 0; //顺时针旋转角度
	private float _in_rotate = 0; //顺时针旋转角度
	private int width, height;
	private boolean isDebug = false;

	//外置接口
	public void setRotate(float rotate) {
		this.rotate = (int) -rotate + offset;
		_in_rotate = -rotate;

		invalidate();

		Log.i(TAG, "========setRotate: " + rotate);
	}

	public CompassView(Context context) {
		super(context);
		initPaint(context);
	}

	public CompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint(context);
	}

	private void initPaint(Context context) {
		// 对画圆盘画初始化
		circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		circlePaint.setColor(Color.BLACK);
		circlePaint.setStyle(Paint.Style.FILL);

		// 对刻度画笔进行初始化
		tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		tickPaint.setColor(Color.RED);
		tickPaint.setStrokeWidth(2);

		mKeyPaint = new Paint();
		mKeyPaint.setStrokeWidth(3);
		mKeyPaint.setColor(Color.RED);
		mKeyPaint.setAntiAlias(true);

		// 对字的画笔进行初始化
		textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(15);

		mPaint = new Paint();
		mPaint.setStrokeWidth(5);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(Color.parseColor("#666666"));
		mPaint.setAntiAlias(true);

		mPaintRun = new Paint();
		mPaintRun.setStrokeWidth(2);
		mPaintRun.setStyle(Paint.Style.STROKE);
		mPaintRun.setColor(Color.parseColor("#666666"));
		mPaintRun.setAntiAlias(true);
	}

	// 自适应在这里做的
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// 获取控件的宽和高
		vWidth = w;
		compassRadiu = Math.min(w, h) / 2;
		tickHeight = (1 / 12F) * compassRadiu;
		textHeight = textPaint.descent() - textPaint.ascent();
	}

	/**
	 * 在 圆中心绘制一个点
	 * 
	 * @param canvas
	 */
	private void canvasCenterCircle(Canvas canvas) {
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(Color.GRAY);
		canvas.drawCircle(vWidth / 2, vWidth / 2, 12, mPaint);
	}

	public void setDegrees(float degrees, float distance, float degreesRun, float speed_j) {
		this.mDegrees = degrees;
		this.distance = distance;
		this.mDegreesRun = degreesRun;
		this.speed_j = speed_j;
		invalidate();
	}

	/**
	 * 绘制文字
	 * 
	 * @param canvas
	 */
	private void drawStr(Canvas canvas) {
		mPaint.setTextSize(16);
		mPaint.setColor(Color.GREEN);
		String strd = String.format("距离%.1f米\n", distance);
		int strdW = (int) mPaint.measureText(strd);
		String str = String.format("方位角:%.1f°\n", mDegrees);
		int strW = (int) mPaint.measureText(str);
		String strspeed = String.format("速度:%.1f节\n", speed_j);
		int strspeedW = (int) mPaint.measureText(strspeed);

		canvas.drawText(strspeed, vWidth / 2 - strspeedW / 2, vWidth / 4 * 3 , mPaint);
		canvas.drawText(strd, vWidth / 2 - strdW / 2, vWidth / 4 * 3 - 16, mPaint);
		canvas.drawText(str, vWidth / 2 - strW / 2, vWidth / 4, mPaint);
	}

	private void drawArrow(Canvas canvas) {
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(Color.RED);
		canvas.save();
		float d = (mDegrees + _in_rotate + 360) % 360;
		canvas.rotate(d, vWidth / 2, vWidth / 2);
		float fromX = vWidth / 2, fromY = vWidth / 2, toX = vWidth / 2, toY = vWidth / 6, heigth = 13, bottom = 8;
		canvas.drawLine(fromX, vWidth / 3 * 2, toX, toY, mPaint);
		float juli = (float) Math.sqrt((toX - fromX) * (toX - fromX)
				+ (toY - fromY) * (toY - fromY));// 获取线段距离
		float juliX = toX - fromX;// 有正负，不要取绝对值
		float juliY = toY - fromY;// 有正负，不要取绝对值
		float dianX = toX - (heigth / juli * juliX);
		float dianY = toY - (heigth / juli * juliY);
		// float dian2X = fromX + (heigth / juli * juliX);
		// float dian2Y = fromY + (heigth / juli * juliY);
		// 终点的箭头
		Path path = new Path();
		path.moveTo(toX, toY - 10);// 此点为三边形的起点
		path.lineTo(dianX + (bottom / juli * juliY), dianY
				- (bottom / juli * juliX));
		path.lineTo(dianX - (bottom / juli * juliY), dianY
				+ (bottom / juli * juliX));
		path.close(); // 使这些点构成封闭的三边形
		canvas.drawPath(path, mPaint);
		// canvas.drawLine(vWidth / 2, vWidth / 2 + 100, vWidth / 2,
		// -vWidth / 5 * 4, mPaint);
		canvas.restore();
	}

	private void drawRun(Canvas canvas) {
		if (mDegreesRun == 0) {
			return;
		}
		mPaintRun.setAlpha(0);
		mPaintRun.setStyle(Paint.Style.FILL);
		mPaintRun.setColor(Color.WHITE);
		canvas.save();
		float d = (mDegreesRun + _in_rotate + 360) % 360;
		canvas.rotate(d, vWidth / 2, vWidth / 2);
		float fromX = vWidth / 2, fromY = vWidth / 2, toX = vWidth / 2, toY = fromY - 22, heigth = 16, bottom = 10;
		canvas.drawLine(fromX, fromY, toX, toY, mPaintRun);
		float juli = (float) Math.sqrt((toX - fromX) * (toX - fromX)
				+ (toY - fromY) * (toY - fromY));// 获取线段距离
		float juliX = toX - fromX;// 有正负，不要取绝对值
		float juliY = toY - fromY;// 有正负，不要取绝对值
		float dianX = toX - (heigth / juli * juliX);
		float dianY = toY - (heigth / juli * juliY);
		// float dian2X = fromX + (heigth / juli * juliX);
		// float dian2Y = fromY + (heigth / juli * juliY);
		// 终点的箭头
		Path path = new Path();
		path.moveTo(toX, toY - 15);// 此点为三边形的起点
		path.lineTo(dianX + (bottom / juli * juliY), dianY
				- (bottom / juli * juliX));
		path.lineTo(dianX - (bottom / juli * juliY), dianY
				+ (bottom / juli * juliX));
		path.close(); // 使这些点构成封闭的三边形
		canvas.drawPath(path, mPaintRun);
		// canvas.drawLine(vWidth / 2, vWidth / 2 + 100, vWidth / 2,
		// -vWidth / 5 * 4, mPaint);
		canvas.restore();
	}

	//坐标旋转公式
	private float getRotatePointX(float a, float x, float y) {
		return (float) ((x - width / 2) * Math.cos(CONVERSION_ANGLE_CONST * a) + (y - height / 2) * Math.sin(CONVERSION_ANGLE_CONST * a)) + width / 2;
	}

	private float getRotatePointY(float a, float x, float y) {
		return (float) ((y - height / 2) * Math.cos(CONVERSION_ANGLE_CONST * a) - (x - width / 2) * Math.sin(CONVERSION_ANGLE_CONST * a)) + height / 2;
	}

	private void drawOriText(Canvas canvas, String strOri, float rotateAngle) {

		float x1 = compassRadiu, x2 = compassRadiu;
		float y1 = compassRadiu*2, y2 = (float)(compassRadiu*2 - compassRadiu * lineRateSize - 8);
		int textWidth = 0;
		int textHeight = 0;

		Rect rect = new Rect();
		textPaint.getTextBounds(strOri, 0, strOri.length(), rect);
		textWidth = rect.width();//文字宽
		textHeight = rect.height();//文字高

		canvas.drawText(strOri, getRotatePointX(rotateAngle, x2, y2) - textWidth / 2, getRotatePointY(rotateAngle, x2, y2) + textHeight / 2, textPaint);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		width = getMeasuredWidth();
		height = getMeasuredHeight();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// canvas.drawColor(Color.CYAN);
		// 黑色圆盘
		canvas.drawCircle(compassRadiu, compassRadiu, compassRadiu, circlePaint);
		// 画红色的刻度
		float degress;
		float textWidth;
		// 绘制箭头
		drawArrow(canvas);
		// 绘制行走方向
		drawRun(canvas);
		// 绘制文字
		drawStr(canvas);
		// 绘制中心点
		canvasCenterCircle(canvas);
		int ax = 360 / DIVIDE_COUNT;
		for (int i = 0; i < DIVIDE_COUNT; i++) {
			float rotateAngle = (ax * i - rotate + 360) % 360;
			degress = rotateAngle;
			//绘制文字
			float genAngle = (((270 - rotateAngle < 0 ? (270 - rotateAngle + 360) : (270 - rotateAngle)) - rotate) + 360) % 360;//转换起始角度，以最上方为0度

			//绘制普通线
			float x1 = compassRadiu, x2 = compassRadiu;//(float)(compassRadiu * lineRateSize + compassRadiu);
			float y1 = compassRadiu*2, y2 = (float)(compassRadiu*2 - compassRadiu * lineRateSize);//compassRadiu*2;

//			Log.i(TAG, "=====> r: " + compassRadiu+" degress: " + degress + " width: "+width + " height:" + height);
//			Log.i(TAG, "=====> onDraw x1: " + getRotatePointX(degress, x1, y1) + "  x2:" + getRotatePointX(degress, x2, y2));
//			Log.i(TAG, "=====> onDraw y1: " + getRotatePointY(degress, x1, y1) + "  y2:" + getRotatePointY(degress, x2, y2));

			canvas.drawLine(getRotatePointX(degress, x1, y1), getRotatePointY(degress, x1, y1),
					getRotatePointX(degress, x2, y2), getRotatePointY(degress, x2, y2), tickPaint);


			if ((rotateAngle + rotate) % 45 == 0) {
				//绘制关键线
				x1 = compassRadiu;
				x2 = compassRadiu;
				y1 = compassRadiu*2;
				y2 = (float)(compassRadiu*2 - compassRadiu * lineRateSize - 6);

				canvas.drawLine(getRotatePointX(rotateAngle, x1, y1), getRotatePointY(rotateAngle, x1, y1),
						getRotatePointX(rotateAngle, x2, y2), getRotatePointY(rotateAngle, x2, y2), mKeyPaint);

				//绘制文字
				String strA;//显示的角度
				if (genAngle < 10) {
					strA = " " + (int) genAngle + "°";
				} else if (genAngle < 100) {
					strA = " " + (int) genAngle + "°";
				} else {
					strA = "" + (int) genAngle + "°";

				}

				//绘制东西南北文字
				String strOri;//显示的方向
				if ((int) genAngle == 0) {
					strOri = "北";
					drawOriText(canvas, strOri, rotateAngle);
				} else if (genAngle == 90) {
					strOri = "东";
					drawOriText(canvas, strOri, rotateAngle);
				} else if (genAngle == 180) {
					strOri = "南";
					drawOriText(canvas, strOri, rotateAngle);
				} else if (genAngle == 270) {
					strOri = "西";
					drawOriText(canvas, strOri, rotateAngle);
				} else {
					drawOriText(canvas, strA, rotateAngle);
				}
			}
		}
	}

	private void drawText(Canvas canvas, String text, float textWidth) {
		canvas.drawText(text, -(textWidth / 2), -compassRadiu + tickHeight
				+ textHeight, textPaint);
	}
}
