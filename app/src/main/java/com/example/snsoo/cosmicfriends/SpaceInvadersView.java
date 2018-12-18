package com.example.snsoo.cosmicfriends;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Random;

public class SpaceInvadersView extends SurfaceView implements Runnable{

    private Context context;

    private Thread gameThread = null;

    private SurfaceHolder ourHolder;

    // 게임 플레이 상태
    private volatile boolean playing;

    // 게임 일시정지
    private boolean paused = true;

    private Canvas canvas;
    private Paint paint;

    // 프레임률
    private long fps;
    private long timeThisFrame;

    private int screenX;
    private int screenY;

    private PlayerShip playerShip;
    private Bullet bullet;

    private Bullet[] invadersBullets = new Bullet[100];
    private int nextBullet;
    private int maxInvaderBullets = 10;

    private Invader[] invaders = new Invader[30];
    private int numInvaders = 0;

    private SoundPool soundPool;
    private int playerExplodeID = -1;
    private int invaderExplodeID = -1;
    private int shootID = -1;
    private int damageShelterID = -1;
    private int uhID = -1;
    private int ohID = -1;

    private Bitmap backGround;

    private int score = 0;
    private int lives = 3;

    private long menaceInterval = 1000;

    private boolean uhOrOh;

    private long lastMenaceTime = System.currentTimeMillis();

    private Random rand = new Random();

    public SpaceInvadersView(Context context, int x, int y) {

        super(context);
        this.context = context;

        ourHolder = getHolder();
        paint = new Paint();

        screenX = x;
        screenY = y;

        Resources r = context.getResources();
        backGround = BitmapFactory.decodeResource(r, R.drawable.background);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);

        try{
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("shoot.ogg");
            shootID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("invaderexplode.ogg");
            invaderExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("playerexplode.ogg");
            playerExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("uh.ogg");
            uhID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("oh.ogg");
            ohID = soundPool.load(descriptor, 0);

        }catch(IOException e){
            Log.e("error", "failed to load sound files");
        }

        prepareLevel();
    }

    private void prepareLevel(){

        //플레이어 생성
        playerShip = new PlayerShip(context, screenX, screenY);

        //총알 생성 및 초기화
        bullet = new Bullet(screenY);
        for(int i = 0; i < invadersBullets.length; i++){
            invadersBullets[i] = new Bullet(screenY);
        }

        // 적 생성
        numInvaders = 0;
        for(int column = 0; column < 4; column ++ ){
            for(int row = 0; row < 2; row ++ ){
                if(rand.nextInt(3) < 2){
                    invaders[numInvaders] = new Invader(context, row, column, screenX, screenY);
                    numInvaders ++;
                }
            }
        }

        menaceInterval = 1000;

    }

    @Override
    public void run() {
        while (playing) {

            long startFrameTime = System.currentTimeMillis();

            if(!paused){
                update();
            }

            draw();

            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }

            if(!paused) {
                if ((startFrameTime - lastMenaceTime)> menaceInterval) {
                    if (uhOrOh) {
                        soundPool.play(uhID, 1, 1, 0, 0, 1);

                    } else {
                        soundPool.play(ohID, 1, 1, 0, 0, 1);
                    }

                    lastMenaceTime = System.currentTimeMillis();
                    uhOrOh = !uhOrOh;
                }
            }

        }



    }

    private void update(){

        boolean bumped = false;

        boolean lost = false;

        playerShip.update(fps);


        if(bullet.getStatus()){
            bullet.update(fps);
        }

        for(int i = 0; i < invadersBullets.length; i++){
            if(invadersBullets[i].getStatus()) {
                invadersBullets[i].update(fps);
            }
        }

        for(int i = 0; i < numInvaders; i++){
            if(invaders[i].getVisibility()) {

                invaders[i].update(fps);

                if(invaders[i].takeAim(playerShip.getX(), playerShip.getLength())){

                    if(invadersBullets[nextBullet].shoot(invaders[i].getX() + invaders[i].getLength() / 2, invaders[i].getY(), bullet.DOWN)) {

                        nextBullet++;

                        if (nextBullet == maxInvaderBullets) {
                            nextBullet = 0;
                        }
                    }
                }

                if (invaders[i].getX() > screenX - invaders[i].getLength()
                        || invaders[i].getX() < 0){

                    bumped = true;


                }
            }

        }

        if(bumped){
            for(int i = 0; i < numInvaders; i++){
                invaders[i].dropDownAndReverse();

                if(invaders[i].getY() > screenY - screenY / 10){
                    lost = true;
                }
            }
            menaceInterval = menaceInterval - 80;
        }

        if(lost){
            prepareLevel();
        }

        if(bullet.getImpactPointY() < 0){
            bullet.setInactive();
        }

        for(int i = 0; i < invadersBullets.length; i++){

            if(invadersBullets[i].getImpactPointY() > screenY){
                invadersBullets[i].setInactive();
            }
        }

        if(bullet.getStatus()) {
            for (int i = 0; i < numInvaders; i++) {
                if (invaders[i].getVisibility()) {
                    if (RectF.intersects(bullet.getRect(), invaders[i].getRect())) {
                        invaders[i].setInvisible();
                        soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                        bullet.setInactive();
                        score = score + 10;

                        // Has the player won
                        if(score == numInvaders * 10){
                            paused = true;
                            score = 0;
                            lives = 3;
                            try {
                                Intent intent = new Intent(context, MainActivity.class);
                                PendingIntent pi = PendingIntent.getActivity(context, 0, intent,
                                        PendingIntent.FLAG_ONE_SHOT);
                                pi.send();
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        for(int i = 0; i < invadersBullets.length; i++){
            if(invadersBullets[i].getStatus()){
                if(RectF.intersects(playerShip.getRect(), invadersBullets[i].getRect())){
                    invadersBullets[i].setInactive();
                    lives --;
                    soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);

                    if(lives == 0){
                        paused = true;
                        lives = 3;
                        score = 0;
                        prepareLevel();

                    }
                }
            }
        }


    }


    private void draw(){
        if (ourHolder.getSurface().isValid()) {
            canvas = ourHolder.lockCanvas();

            canvas.drawColor(Color.argb(255, 26, 128, 182));
            canvas.drawBitmap(backGround, 0, 0, null);

            paint.setColor(Color.argb(255,  255, 255, 255));

            canvas.drawBitmap(playerShip.getBitmap(), playerShip.getX(), screenY - 50, paint);

            for(int i = 0; i < numInvaders; i++){
                if(invaders[i].getVisibility()) {
                    if(uhOrOh) {
                        canvas.drawBitmap(invaders[i].getBitmap(), invaders[i].getX(), invaders[i].getY(), paint);
                    }else{
                        canvas.drawBitmap(invaders[i].getBitmap2(), invaders[i].getX(), invaders[i].getY(), paint);
                    }
                }
            }

            if(bullet.getStatus()){
                canvas.drawRect(bullet.getRect(), paint);
            }

            for(int i = 0; i < invadersBullets.length; i++){
                if(invadersBullets[i].getStatus()) {
                    canvas.drawRect(invadersBullets[i].getRect(), paint);
                }
            }

            paint.setColor(Color.argb(255,  249, 129, 0));
            paint.setTextSize(40);
            canvas.drawText("Score: " + score + "   Lives: " + lives, 10,50, paint);

            // Draw everything to the screen
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }

    }

    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:

                paused = false;

                if(motionEvent.getY() > screenY - screenY / 8) {
                    if (motionEvent.getX() > screenX / 2) {
                        playerShip.setMovementState(playerShip.RIGHT);
                    } else {
                        playerShip.setMovementState(playerShip.LEFT);
                    }

                }

                if(motionEvent.getY() < screenY - screenY / 8) {
                    if(bullet.shoot(playerShip.getX()+ playerShip.getLength()/2,screenY,bullet.UP)){
                        soundPool.play(shootID, 1, 1, 0, 0, 1);
                    }
                }

                break;

            case MotionEvent.ACTION_UP:

                if(motionEvent.getY() > screenY - screenY / 10) {
                    playerShip.setMovementState(playerShip.STOPPED);
                }
                break;
        }
        return true;
    }
}
