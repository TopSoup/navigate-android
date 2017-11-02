package com.topsoup.navigate.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class CompassView extends View {
	private Paint circlePaint, tickPaint, mPaint;
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

		// 对字的画笔进行初始化
		textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(15);

		mPaint = new Paint();
		mPaint.setStrokeWidth(5);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(Color.parseColor("#666666"));
		mPaint.setAntiAlias(true);
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
		canvas.drawCircle(vWidth / 2, vWidth / 2, 10, mPaint);
	}

	public void setDegrees(float degrees, float distance) {
		this.mDegrees = degrees;
		this.distance = distance;
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
		canvas.drawText(strd, vWidth / 2 - strdW / 2, vWidth / 4 * 3, mPaint);
		canvas.drawText(str, vWidth / 2 - strW / 2, vWidth / 4, mPaint);
	}

	private void abc(Canvas canvas) {
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(Color.BLUE);
		canvas.save();
		canvas.rotate(mDegrees, vWidth / 2, vWidth / 2);
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

	@Override
	protected void onDraw(Canvas canvas) {
		// canvas.drawColor(Color.CYAN);
		// 黑色圆盘
		canvas.drawCircle(compassRadiu, compassRadiu, compassRadiu, circlePaint);
		// 画红色的刻度
		int degress;
		float textWidth;
		abc(canvas);
		drawStr(canvas);
		canvasCenterCircle(canvas);
		for (int i = 0; i < 24; i++) {
			canvas.save();
			canvas.translate(compassRadiu, compassRadiu);
			// 当前canvas旋转角度
			degress = i * 15;
			canvas.rotate(15 * i);
			canvas.drawLine(0, -compassRadiu, 0, -compassRadiu + tickHeight,
					tickPaint);
			switch (degress) {
			case 0:
				textWidth = textPaint.measureText("北");
				drawText(canvas, "北", textWidth);
				break;
			case 45:
				textWidth = textPaint.measureText("45");
				drawText(canvas, "45", textWidth);
				break;
			case 90:
				textWidth = textPaint.measureText("东");
				drawText(canvas, "东", textWidth);
				break;
			case 135:
				textWidth = textPaint.measureText("135");
				drawText(canvas, "135", textWidth);
				break;
			case 180:
				textWidth = textPaint.measureText("南");
				drawText(canvas, "南", textWidth);
				break;
			case 225:
				textWidth = textPaint.measureText("225");
				drawText(canvas, "225", textWidth);
				break;
			case 270:
				textWidth = textPaint.measureText("西");
				drawText(canvas, "西", textWidth);
				break;
			case 315:
				textWidth = textPaint.measureText("315");
				drawText(canvas, "315", textWidth);
				break;
			case 310:
				textWidth = textPaint.measureText("310");
				drawText(canvas, "310", textWidth);
				// canvas.drawLine(0,
				// -compassRadiu + tickHeight + textHeight + 10,
				// -textWidth / 3, -compassRadiu + tickHeight + textHeight
				// + 30, tickPaint);
				// canvas.drawLine(0,
				// -compassRadiu + tickHeight + textHeight + 10,
				// textWidth / 3, -compassRadiu + tickHeight + textHeight
				// + 30, tickPaint);

				break;
			default:
				break;
			}
			canvas.restore();
		}
	}

	private void drawText(Canvas canvas, String text, float textWidth) {
		canvas.drawText(text, -(textWidth / 2), -compassRadiu + tickHeight
				+ textHeight, textPaint);
	}
}
