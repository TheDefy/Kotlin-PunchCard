package defy.com.punchcard.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by chenglei on 2017/9/26.
 */

public class MyView extends View {

    private Paint myPaint;

    public MyView(Context context) {
        this(context, null);
    }

    public MyView(Context context, @Nullable AttributeSet attrs) {
        this(context, null, 0);
    }

    public MyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        myPaint = new Paint();
        myPaint.setAntiAlias(true);
        myPaint.setColor(Color.RED);
        myPaint.setStyle(Paint.Style.STROKE);
        myPaint.setStrokeWidth(5);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        testMoveTo(canvas);
        testQuadTo(canvas);
    }

    private void testMoveTo(Canvas canvas) {
        Path myPath = new Path();
        myPath.moveTo(10, 10);
        myPath.lineTo(100, 100);
        myPath.close();
        canvas.drawPath(myPath, myPaint);
    }

    private void testQuadTo(Canvas canvas) {
        canvas.restore();
        Path myPath = new Path();
        myPath.moveTo(100, 500);
        myPath.cubicTo(100, 500, 300, 100, 600, 500);
        myPath.close();
        canvas.drawPath(myPath, myPaint);
    }
}
