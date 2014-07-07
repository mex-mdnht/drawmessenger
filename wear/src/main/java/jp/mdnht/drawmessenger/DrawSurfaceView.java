package jp.mdnht.drawmessenger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;



public class DrawSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private  static  final String TAG ="DrawSurfaceView";
    private SurfaceHolder holder;
    private float touchX = 0F;
    private float touchY = 0F;
    private  Paint paint;
    private Path path;
    private boolean isDrawing = false;
    //private Thread looper;

    public DrawSurfaceView(Context context) {
        super(context);
        init();
    }

    public DrawSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /*public DrawSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }*/

   /* public DrawSurfaceView(Context context) {
        super(context);
        init();
    }*/

    private void init(){
        holder = getHolder();
        holder.addCallback(this);
        setFocusable(true);
        requestFocus();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Canvas canvas = surfaceHolder.lockCanvas();
        canvas.drawColor(Color.WHITE);
        path = new Path();
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(3);
        surfaceHolder.unlockCanvasAndPost(canvas);
        // スレッドの作成と実行
       // looper = new Thread(this);
       // looper.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
       //Log.d(TAG, "aaaaaa" + event.toString());
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
               // path.reset();
                path.moveTo(x, y);
                touchX = x;
                touchY = y;
                isDrawing = true;
                paint();
                return true;
            }
               // paint();
            case MotionEvent.ACTION_MOVE:
                path.quadTo(touchX, touchY, (x + touchX)/2, (y + touchY)/2);
                touchX = x;
                touchY = y;
                //break;
                paint();
                return true;

            case MotionEvent.ACTION_UP: {
                path.lineTo(touchX, touchY);
                isDrawing = false;
                //break;
               // path.reset();
                paint();
               // path.reset();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    public void paint() {

        Canvas canvas = holder.lockCanvas();
        //canvas.drawColor(Color.WHITE);
       // canvas.drawCircle(touchX, touchY, 3F, paint);
        canvas.drawPath(path,paint);
        holder.unlockCanvasAndPost(canvas);
    }

    /*@Override
    public void run() {

        while(looper != null)
        {
           // Log.d(TAG, "aaaaaa" + isDrawing);
            if(isDrawing) {
                paint();
            }
        }
    }*/
}