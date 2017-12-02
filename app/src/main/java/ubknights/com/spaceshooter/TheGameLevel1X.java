package ubknights.com.spaceshooter;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextPaint;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class TheGameLevel1X extends Activity implements View.OnTouchListener{

    TheView mySurfaceView; //SURFACEVIEW
    TheSprites2 allsprites; //ALL BITMAP LOCATIONS ARE STORED
    float xTouch=0,yTouch=0;    //ONTOUCH FUNCTION VARIABLES
    int s_width, s_height;  //WIDTH AND HEIGHT OF SURFACEVIEW
    int loc = 0,eLoc=0,e1Loc=3,e2Loc=0,m1Loc=0,m2Loc=1,m3Loc=2; //FOR LOOPING SPRITES IS UPDATED
    int enemycounter = 0, shipcounter = 0;  //UPDATES WHEN THE BULLETS HIT COUNTS
    Point randomenemy1, randomenemy2;   //RANDOMLY SELECT ENEMY POSITION
    Point shipbullet, enemy1bullet, enemy2bullet;   //ENEMY AND SHIP BULLETS
    Point explosion, ship;    //EXPLOSION VARIABLE
    float skipTime =1000.0f/30.0f;
    long lastUpdate;
    float dt;
    Canvas c;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allsprites = new TheSprites2(getResources());
        shipbullet = new Point();
        randomenemy1 = new Point();
        randomenemy2 = new Point();
        enemy1bullet = new Point();
        enemy2bullet = new Point();
        explosion = new Point();    //INITIALISING VARIABLES
        explosion.x = 2147483647;
        explosion.y = 2147483647;
        lastUpdate = 0;

        //SET FULL SCREEN:-
        hideAndFull();

        //MAKE SURFACEVIEW ONTOUCH
        mySurfaceView = new TheView(this);
        mySurfaceView.setOnTouchListener(this);
        setContentView(mySurfaceView);

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
        mySurfaceView.startGame();
            }
       },2000);
    }

    //SET FULL SCREEN:-
    public void hideAndFull()
    {
        ActionBar bar = getActionBar();
        bar.hide();
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    //ONTOUCH METHOD:-
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch(motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                xTouch = motionEvent.getX();
                yTouch = motionEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                xTouch = motionEvent.getX();
                yTouch = motionEvent.getY();
                view.performClick();
                break;
            case MotionEvent.ACTION_MOVE:
                xTouch = motionEvent.getX();
                yTouch = motionEvent.getY();
                break;
        }
        return true;
    }

    //THEVIEW CLASS IS SURFACEVIEW:-
    public class TheView extends SurfaceView implements SurfaceHolder.Callback {
        SurfaceHolder holder;
        Boolean change = true;
        Thread gameThread;
        Rect place;
        Bitmap bkground;    //BACKGROUND SPACE IMAGE
        //MUSIC AND SOUNDS
        MediaPlayer mainsong;
        MediaPlayer success;
        MediaPlayer gameover;
        Vibrator vibrator;
        SoundPool soundPool;
        HashMap<Integer,Integer>soundMap_H;

        //CONSTRUCTOR FOR TheView CLASS:-
        public TheView(Context context) {
            super(context);
            //HOLDS THE SURFACE
            holder = getHolder();
            //SOUNDS AND MUSIC
            bkground = BitmapFactory.decodeResource(getResources(), R.drawable.background);
            mainsong = MediaPlayer.create(context, R.raw.mainsong);
            success  = MediaPlayer.create(context, R.raw.success);
            gameover = MediaPlayer.create(context, R.raw.gameover);
            vibrator = (Vibrator)getSystemService(context.VIBRATOR_SERVICE);
            //SOUND POOL
            soundPool = new SoundPool(4,AudioManager.STREAM_MUSIC,0);
            soundMap_H = new HashMap<Integer, Integer>();
            soundMap_H.put(1, soundPool.load(context, R.raw.shipbullet,1));
            soundMap_H.put(2, soundPool.load(context, R.raw.shipboom,1));
            soundMap_H.put(3, soundPool.load(context, R.raw.enemyboom,1));

            holder.addCallback(this);
            gameThread = new Thread(runn);
        }

        Runnable runn = new Runnable() {
            @Override
            public void run() {

                while (change == true) {
                    //CHECKS IF THE SURFACE IS HOLDED
                    if (!holder.getSurface().isValid()) {
                        continue;
                    }
                    dt = System.currentTimeMillis() - lastUpdate;
                    if (dt >= skipTime) {
                        //HOLD CANVAS TO PAINT ON IT
                        c = holder.lockCanvas();
                        //ADD BACKGROUND IMAGE TO CANAVS:-
                        c.drawBitmap(bkground, 0,0,null);
                        mainsong.start();

                        if(yTouch!=0) {     //TO SHOW SHIP ONLY WHEN TOUCHED
                            //SHIP:-
                            place = new Rect((int) s_width - allsprites.shipSize.width() * 40, (int) yTouch,
                                    (int) (s_width - allsprites.shipSize.width() * 40) + (allsprites.shipSize.width()) * 4, (int) yTouch + allsprites.shipSize.width() * 2);
                            c.drawBitmap(allsprites.space, (allsprites.shipSprites[eLoc]), place, null);

                            //SHIP BULLET:-
                            place = new Rect((int) shipbullet.x, shipbullet.y, (int) shipbullet.x + allsprites.shipBulletSprite.width() * 4,
                                    shipbullet.y + allsprites.shipBulletSprite.height() * 4);
                            c.drawBitmap(allsprites.space, allsprites.shipBulletSprite, place, null);
                        }
                        //EXPLOSION:-
                        place = new Rect((int)explosion.x, (int) explosion.y,
                                (int) explosion.x+allsprites.boomSize[loc].width() * 8, explosion.y+allsprites.boomSize[loc].height() * 8 );
                        c.drawBitmap(allsprites.space, allsprites.boomsprites[loc], place, null);

                        //ENEMY SHIP1:-
                        place = new Rect(randomenemy1.x, randomenemy1.y, randomenemy1.x+allsprites.enemySize.width()*4, randomenemy1.y+allsprites.enemySize.height()*4);
                        c.drawBitmap(allsprites.space, allsprites.enemy1sprites[e1Loc], place, null);

                        //ENEMY SHIP2:-
                        place = new Rect(randomenemy2.x, randomenemy2.y, randomenemy2.x+allsprites.enemySize.width()*4, randomenemy2.y+allsprites.enemySize.height()*4);
                        c.drawBitmap(allsprites.space, allsprites.enemy2sprites[e2Loc], place, null);


                        //ENEMY1 BULLET:-
                        place = new Rect((int) enemy1bullet.x, (int) enemy1bullet.y, (int) enemy1bullet.x + allsprites.enemybulletSprite.width() * 4,
                                (int) enemy1bullet.y + allsprites.enemybulletSprite.height() * 4);
                        c.drawBitmap(allsprites.space, allsprites.enemybulletSprite, place, null);

                        //ENEMY2 BULLET:-
                        place = new Rect((int) enemy2bullet.x, (int) enemy2bullet.y, (int) enemy2bullet.x + allsprites.enemybulletSprite.width() * 4,
                                (int) enemy2bullet.y + allsprites.enemybulletSprite.height() * 4);
                        c.drawBitmap(allsprites.space, allsprites.enemybulletSprite, place, null);

                        //UNLOCK CANAVS AND POST CHANGES
                        holder.unlockCanvasAndPost(c);

                        //SHIP BULLET UPDATE:-
                        shipbullet.x = shipbullet.x + s_width / 20;

                        //ENEMY BULLETS UPDATE:-
                        enemy1bullet.x = enemy1bullet.x-50;
                        enemy2bullet.x = enemy2bullet.x-50;

                        //SHIPSPRITES SELECTION IN INFINITE LOOP
                        loc = ((loc + 1) % 6);
                        eLoc = (eLoc + 1) % 4;
                        e1Loc = (e1Loc + 1) % 6;
                        e2Loc = (e2Loc + 1) % 6;
                        m1Loc = (m1Loc + 1) % 4;
                        m2Loc = (m2Loc + 1) % 4;
                        m3Loc = (m3Loc + 1) % 4;

                        //CHECK IF SHIP BULLETS HIT ENEMYS:-
                        checkHitEnemy1();
                        checkHitEnemy2();
                        //CHECK IF ENEMY BULLET HIT SHIP:-
                        checkHitShip();

                        //CHECK IF BULLETS ARE OUT OF SCREEN:-
                        if (shipbullet.x > s_width) {
                            resetShipBullet();
                        }
                        if(enemy1bullet.x <= 0){
                            resetEnemy1Bullet();
                        }
                        if(enemy2bullet.x <= 0){
                            resetEnemy2Bullet();
                        }
                        lastUpdate = System.currentTimeMillis();
                    }
                }
            }
        };

        //METHODS TO CHECK IF BULLET HIT ENEMY:-
        public void checkHitEnemy1()
        {
            if(shipbullet.x > randomenemy1.x && shipbullet.x < randomenemy1.x+allsprites.enemySize.width()*4 &&
                    shipbullet.y > randomenemy1.y && shipbullet.y < randomenemy1.y+allsprites.enemySize.height()*4 )
            {
                enemycounter = enemycounter + 1;    //ENEMY COUNTER INCREMENT
                if(enemycounter == 10)
                {
                    mainsong.stop();
                    success.start();    //MUSICS START AND STOP
                    gameDone();
                }
                //CALLING ENEMY EXPLOSION AND RESPAWN ENEMY AND SHIP BULLET
                showExplosionEnemy1();
                moveEnemy1();
                resetShipBullet();
            }
        }
        public void checkHitEnemy2()
        {
            if(shipbullet.x > randomenemy2.x && shipbullet.x < randomenemy2.x+allsprites.enemySize.width()*4 &&
                    shipbullet.y > randomenemy2.y && shipbullet.y < randomenemy2.y+allsprites.enemySize.height()*4 )
            {
                enemycounter = enemycounter + 1;    //ENEMY COUNTER
                if(enemycounter == 10)
                {
                    mainsong.stop();
                    success.start();        //MUSIC
                    gameDone();
                }
                //CALLING ENEMY EXPLOSION AND RESPAWN ENEMY AND SHIP BULLET
                showExplosionEnemy2();
                moveEnemy2();
                resetShipBullet();
            }
        }
        //SHIP HIT CHECK METHOD:-
        public void checkHitShip()
        {
            if(enemy1bullet.x > (int) s_width-allsprites.shipSize.width() * 40 && enemy1bullet.x < (int) (s_width-allsprites.shipSize.width()*40)+(allsprites.shipSize.width())*4 &&
                    enemy1bullet.y > (int) yTouch && enemy1bullet.y < (int) yTouch + allsprites.shipSize.width()*2 )
            {
                shipcounter = shipcounter + 1;      //SHIP HIT COUNTER INCREMENT
                if(shipcounter == 5)
                {
                    mainsong.stop();
                    gameover.start();     //MUSIC START AND STOP
                    gameDone();
                }
                //CALLING SHIP EXPLOSION AND RESPAWN SHIP BULLET
                showExplosionShip();
                resetEnemy1Bullet();
            }
            else if(enemy2bullet.x > (int) s_width-allsprites.shipSize.width() * 40 &&  enemy2bullet.x < (int) (s_width-allsprites.shipSize.width()*40)+(allsprites.shipSize.width())*4 &&
                    enemy2bullet.y > (int) yTouch && enemy2bullet.y < (int) yTouch + allsprites.shipSize.width()*2)
            {
                shipcounter = shipcounter + 1;      //SHIP COUNTER
                if(shipcounter == 5)
                {
                    mainsong.stop();
                    gameover.start();     //MUSIC START AND STOP
                    gameDone();
                }
                //CALLING SHIP EXPLOSION AND RESPAWN SHIP BULLET
                showExplosionShip();
                resetEnemy2Bullet();
            }
        }

        //RANDOM FUNCTIONS TO RESPAWN ENEMYS:-
        public void randomFunctionEnemy1()
        {

            Random r1 = new Random();
            randomenemy1.x = r1.nextInt(9*s_width/10 - 3*s_width/5) + 3*s_width/5; //WIDTH RANGE OF ENEMY RESPAWN FOR ENEMY 1
            Random r2 = new Random();
            randomenemy1.y = r2.nextInt(9*s_height/10-0)+0;     //HEIGHT RANGE OF ENEMY RESPAWN
        }
        public void randomFunctionEnemy2()
        {
            Random r1 = new Random();
            randomenemy2.x = r1.nextInt(9*s_width/10 - 3*s_width/5) + 3*s_width/5;      //RESPWAN RANGE ENEMY 2
            Random r2 = new Random();
            randomenemy2.y = r2.nextInt(9*s_height/10-0)+0;     //RESPWAN RANGE ENEMY 2
        }
        //MOVE ENEMY TO NEW PLACE WHEN HIT:-
        public void moveEnemy1(){randomFunctionEnemy1();}
        public void moveEnemy2(){randomFunctionEnemy2();}

        //EXPLOSION FUNCTION WHEN BULLET HITS:-
        public void showExplosionEnemy1()
        {
            vibrator.vibrate(100);
            soundPool.play(3,  1,1,1,0,1f);     //EXPLOSION SOUND
            explosion.x = randomenemy1.x;       //EXPLOSION SHOWING LOCATION ENEMY 1
            explosion.y = randomenemy1.y;
            //MAKES EXPLOSION NULL AFTER ENEMY HIT
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    makeExplosionNull();
                }
            }, 100);
        }
        public void showExplosionEnemy2()
        {
            vibrator.vibrate(100);
            soundPool.play(3,  1,1,1,0,1f);     //EXPLOSION SOUND
            explosion.x = randomenemy2.x;
            explosion.y = randomenemy2.y;       //EXPLOSION LOCATIONS ENEMY 2
            //MAKES EXPLOSION NULL AFTER ENEMY HIT
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    makeExplosionNull();
                }
            }, 100);
        }
        public void showExplosionShip()
        {
            vibrator.vibrate(150);
            soundPool.play(2,  1,1,1,0,1f);     //SHIP EXPLOSION SOUND
            explosion.x = (int) s_width-allsprites.shipSize.width() * 40;
            explosion.y = (int) yTouch;
            //MAKES EXPLOSION NULL AFTER SHIP HIT
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    makeExplosionNull();
                }
            }, 100);
        }
        //MAKE EXPLOSION NULL AFTER ONCE SHOWN:-
        public void makeExplosionNull()
        {
            explosion.x = 2147483647;
            explosion.y = 2147483647;
        }

        //RESET BULLETS WHEN HIT OCCURES:-
        public void resetShipBullet()
        {
            soundPool.play(1,  1,1,1,0,1f);     //SHIP BULLET SOUND
            shipbullet.x = (allsprites.shipSize.width() + allsprites.shipSize.width()*4);
            shipbullet.y = (int) yTouch + (allsprites.shipSize.height()-allsprites.shipBulletSprite.height());
        }
        public void resetEnemy1Bullet()
        {
            enemy1bullet.x = randomenemy1.x;
            enemy1bullet.y = randomenemy1.y;
        }
        public void resetEnemy2Bullet()
        {
            enemy2bullet.x = randomenemy2.x;
            enemy2bullet.y = randomenemy2.y;
        }

        //START GAMETHREAD:-
        public void startGame()
        {
            gameThread.start();
        }

        //STOP GAMETHREAD
        public void gameDone(){
            mainsong.stop();
            vibrator.cancel();
            change = false;
            Intent i = new Intent(TheGameLevel1X.this, MainActivity.class);
            startActivity(i);
        }

        //SURFACEVIEW METHODS
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {

        }
        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int pixelFormat, int width, int height) {
            s_width = width-(int) 0.1*width;
            s_height = height-(int)0.1 * height;
            randomFunctionEnemy1();
            randomFunctionEnemy2();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        }
    }
}