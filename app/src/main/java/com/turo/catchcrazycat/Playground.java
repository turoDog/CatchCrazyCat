package com.turo.catchcrazycat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Vector;

public class Playground extends SurfaceView implements View.OnTouchListener {

    private static int WIDTH = 40;//当前元素宽度
    private static final int ROW = 10;
    private static final int COL = 10;
    private static final int BLOCKS = 15;//默认的路障数量

    private Dot matrix[][];
    private Dot cat;

    public Playground(Context context) {
        super(context);
        //getHolder()获得SurfaceHolder对象，并添加回调函数
        getHolder().addCallback(callback);
        matrix = new Dot[ROW][COL];
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                matrix[i][j] = new Dot(j, i);
            }
        }
        setOnTouchListener(this);
        initGame();
    }
//获得当前点的坐标
    private Dot getDot(int x, int y) {
        return matrix[y][x];
    }

    /*
    * *判断当前点是否位于游戏边界
    * */
    private  boolean isAtEdge(Dot d){
        if (d.getX()*d.getY() == 0 || d.getX()+1 == COL || d.getY()+1 == ROW){
            return true;
        }
        return false;
    }

    //获得猫的六个方向上的下一个点的坐标
    private Dot getNeighbour (Dot one, int dir){
        switch(dir){
            case 1:
                return getDot(one.getX()-1, one.getY());
            case 2:
                if (one.getY()%2 == 0){
                    return getDot(one.getX()-1, one.getY()-1);
                }else{
                    return getDot(one.getX(), one.getY()-1);
                }
            case 3:
                if (one.getY()%2 == 0){
                    return getDot(one.getX(), one.getY()-1);
                }else{
                    return getDot(one.getX()+1, one.getY()-1);
                }
            case 4:
                return getDot(one.getX()+1, one.getY());
            case 5:
                if (one.getY()%2 == 0){
                    return getDot(one.getX(), one.getY()+1);
                }else{
                    return getDot(one.getX()+1, one.getY()+1);
                }
            case 6:
                if (one.getY()%2 == 0){
                    return getDot(one.getX()-1, one.getY()+1);
                }else{
                    return getDot(one.getX(),one.getY()+1);
                }
            default:
                break;
        }
        return null;
    }

    //红色点（猫）在六个方向上的可用距离
    private int getDistance(Dot one, int dir){
        int distance = 0;
        if (isAtEdge(one)){
            return 1;
        }
        Dot ori = one ,next;
        while(true){
            next = getNeighbour(ori,dir);
            if (next.getStatus() == Dot.STATUS_ON){
                return distance*-1;
            }
            if (isAtEdge(next)){
                distance++;
                return distance;
            }
            distance++;
            ori = next;
        }
    }

    //猫的移动
    private void MoveTo(Dot one){
        one.setStatus(Dot.STATUS_IN);
        getDot(cat.getX(), cat.getY()).setStatus(Dot.STATUS_OFF);
        cat.setXY(one.getX(), one.getY());
    }

    private void move(){
        if (isAtEdge(cat)){
            lose();
            return;
        }
        //Vector记录器，记录cat可用的邻居点
        Vector<Dot> avaliable = new Vector<>();
        //Vector记录器,记录猫可直接到达屏幕边缘的路径数及方向
        Vector<Dot> positive = new Vector<>();
        //HashMap存放
        HashMap<Dot, Integer> al = new HashMap<Dot, Integer>();
        for (int i = 1; i < 7; i++ ){
            Dot n = getNeighbour(cat,i);
            if (n.getStatus() == Dot.STATUS_OFF){
                //向Vector中添加元素
                //使用add方法直接添加元素
                avaliable.add(n);
                al.put(n,i);
                if (getDistance(n,i) > 0){
                    positive.add(n);
                }
            }
        }
        //可用状态邻居点为0，成功围住神经猫
        if (avaliable.size() == 0){
            win();
        }else if (avaliable.size() == 1){
            MoveTo(avaliable.get(0));
        }else{
            Dot best = null;
            if (positive.size() != 0){//存在可以直接到达屏幕边缘的走向
                System.out.println("向前进");
                int min = 999;
                for (int i = 0; i < positive.size(); i++){
                    int a = getDistance(positive.get(i), al.get(positive.get(i)));
                    if (a < min){
                        min = a;
                        best =  positive.get(i);
                    }
                }
                MoveTo(best);
            }else {//所有方向都存在路障
                System.out.println("躲路障");
                int max = 0;
                for (int i = 0; i < avaliable.size(); i++){
                    int k = getDistance(avaliable.get(i), al.get(avaliable.get(i)));
                    if (k <= max){
                        max = k;
                        best = avaliable.get(i);
                    }
                }
                MoveTo(best);
            }
        }

    }

    private void lose () {
        Toast.makeText(getContext(), "Lose", Toast.LENGTH_SHORT).show();
    }

    private void win () {
        Toast.makeText(getContext(), "You Win!", Toast.LENGTH_SHORT).show();
    }

    private void redraw() {
        Canvas c = getHolder().lockCanvas();
        //获得canvas（画布）对象
        c.drawColor(Color.LTGRAY);
        //将画布设置为浅灰色
        Paint paint = new Paint();
        //Paint（画笔）对象
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        //绘制时抗锯齿，让图形显得更圆滑
        for (int i = 0; i < ROW; i++) {
            //奇偶行的错位
            int offset = 0;
            if (i % 2 != 0) {
                offset = WIDTH / 2;
            }
            //根据三种不同的Dot状态设置不同颜色
            for (int j = 0; j < COL; j++) {
                Dot one = getDot(j, i);
                switch (one.getStatus()) {
                    case Dot.STATUS_OFF:
                        paint.setColor(0xFFEEEEEE);
                        break;
                    case Dot.STATUS_ON:
                        paint.setColor(0xFFFFAA00);
                        break;
                    case Dot.STATUS_IN:
                        paint.setColor(0xFFFF0000);
                        break;
                    default:
                        break;
                }
                //画一个椭圆，通过RectF对象来指定椭圆形的外切矩形，并依此来绘制椭圆
                c.drawOval(new RectF(one.getX() * WIDTH + offset, one.getY() * WIDTH,
                        (one.getX() + 1) * WIDTH + offset, (one.getY() + 1) * WIDTH), paint);
            }
        }
        getHolder().unlockCanvasAndPost(c);
    }

    SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            redraw();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            //适配屏幕
            WIDTH = width / (COL + 1);
            redraw();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    private void initGame() {
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                //将所有点的状态设置为STATUS_OFF(可用状态)
                matrix[i][j].setStatus(Dot.STATUS_OFF);
            }
        }
        cat = new Dot(4, 5);
        getDot(4, 5).setStatus(Dot.STATUS_IN);
        for (int i = 0; i < BLOCKS; ) {
            int x = (int) ((Math.random() * 1000) % COL);
            int y = (int) ((Math.random() * 1000) % ROW);
            //当选中的点为STATUS_OFF（可用状态时）再将其设置为路障
            if (getDot(x, y).getStatus() == Dot.STATUS_OFF) {
                getDot(x, y).setStatus(Dot.STATUS_ON);
                i++;
            }

        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //手指释放的瞬间响应
        if (event.getAction() == MotionEvent.ACTION_UP) {
            //Toast.makeText(getContext(), event.getX() + ":" + event.getY(), Toast.LENGTH_SHORT).show();
            int x, y;
            y = (int) (event.getY() / WIDTH);
            if (y % 2 == 0) {
                x = (int) (event.getX()/WIDTH);
            }else{
                x = (int) ((event.getX()-WIDTH/2)/WIDTH);
            }
            if (x+1 >COL || y+1>ROW){
                initGame();
            }else if(getDot(x,y).getStatus() == Dot.STATUS_OFF) {
                getDot(x, y).setStatus(Dot.STATUS_ON);
                move();
            }
            redraw();
        }
        return true;
    }
}
