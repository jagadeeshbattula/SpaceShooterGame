package ubknights.com.spaceshooter;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class TheGameLevel2 extends Activity implements View.OnTouchListener {

    TheGameLevel2.TheView mySurfaceView;
    TheSprites2 allsprites;     //ALL SPRITES LOCATIONS AND VALUES
    float xTouch,yTouch=0;    //ONTOUCH METHOD VARIABLES
    int s_width,s_height;   //WIDTH AND HEIGHT OF SURFACEVIEW
    int loc = 0,eLoc=0,e1Loc=3,e2Loc=0,m1Loc=0,m2Loc=1,m3Loc=2; ////FOR LOOPING SPRITES IS UPDATED
    int shipx, shipy;   //SHIP LOCATION VARIABLES
    //METEORS MOVEMENT UPDATION VARIBALES
    int m1catchshipx = 0, m1catchshipy = 0, m2catchshipx=0, m2catchshipy=0, m3catchshipx=0, m3catchshipy=0;
    int m1x = 100, m2x = 125, m3x = 150;
    int m1y1 =100, m1y2 = 100, m2y1 = 125, m2y2 = 125, m3y1 = 150, m3y2 = 150;
    float m1xdifference=0, m1ydifference=0, m2xdifference=0, m2ydifference=0, m3xdifference=0, m3ydifference=0;
    float m1xdiff=0, m1ydiff=0, m2xdiff=0, m2ydiff=0, m3xdiff=0, m3ydiff=0;
    //METEOR HITS BY BULLET COUNTERS
    int meteor1counter = 0, meteor2counter=0, meteor3counter=0;
    int meteorhit=0;
    Point randommeteor1, randommeteor2, randommeteor3;      //METEORS RANDOM SPAWINING VARIABLES
    Point explosion;
    Point shipbullet;
    //15 frames per seconds
    float skipTime =1000.0f/30.0f; //setting 30fps
    long lastUpdate;
    float dt;
    Canvas c;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //SETTING BITMAP LOCATIONS FROM CLASS allsprites
        allsprites = new TheSprites2(getResources());
        //make sure there is only ONE copy of the image and that the image
        //is in the drawable-nodpi. if it is not unwanted scaling might occur
        shipbullet = new Point();//used for canvas drawing location
        randommeteor1 = new Point();
        randommeteor2 = new Point();
        randommeteor3 = new Point();
        explosion = new Point();
        explosion.x = 2147483647;
        explosion.y = 2147483647;       //INITIALISING VARIABLES
        lastUpdate = 0;
        //SET FULL SCREEN
        hideAndFull();
        mySurfaceView = new TheView(this);
        mySurfaceView.setOnTouchListener(this);
        setContentView(mySurfaceView);

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                //START THE GAME THREAD
                mySurfaceView.startGame();
            }
        },2000);
    }

    //METHOD FOR SETTING FULL SCREEN:-
    public void hideAndFull()
    {
        ActionBar bar = getActionBar();
        bar.hide();
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

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
                view.performClick();//to get rid of the message, mimicking a click
                break;
            case MotionEvent.ACTION_MOVE:
                xTouch = motionEvent.getX();
                yTouch = motionEvent.getY();
                break;
        }
        return true;
    }

    public class TheView extends SurfaceView implements SurfaceHolder.Callback {
        SurfaceHolder holder;
        Boolean change = true;
        Thread gameThread;
        Rect place;
        Bitmap background;
        MediaPlayer mainsong;
        MediaPlayer gameover;
        MediaPlayer success;
        Vibrator vibrator;
        SoundPool soundPool;
        HashMap<Integer,Integer>soundMap_H;     //SOUNDS, MUSIC AND BACKGROUND SPACE IMAGE

        public TheView(Context context) {
            super(context);
            holder = getHolder();
            background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
            mainsong = MediaPlayer.create(context, R.raw.mainsong);
            success = MediaPlayer.create(context, R.raw.success);
            gameover = MediaPlayer.create(context, R.raw.gameover);
            vibrator = (Vibrator)getSystemService(context.VIBRATOR_SERVICE);
            soundPool = new SoundPool(4,AudioManager.STREAM_MUSIC,0);

            //SOUNDMAP SOUNDS
            soundMap_H = new HashMap<Integer, Integer>();
            soundMap_H.put(1, soundPool.load(context, R.raw.shipbullet,1));
            soundMap_H.put(2, soundPool.load(context, R.raw.shipboom,1));
            soundMap_H.put(3, soundPool.load(context, R.raw.meteorboom,1));

            holder.addCallback(this);
            gameThread = new Thread(runn);

        }

        Runnable runn = new Runnable() {
            @Override
            public void run() {

                while (change == true) {
                    if (!holder.getSurface().isValid()) {
                        continue;
                    }

                    dt = System.currentTimeMillis() - lastUpdate;
                    if (dt >= skipTime) {

                        //HOLD THE CANVAS TO PAINT
                        c = holder.lockCanvas();

                        //SETTING BACKGROUND IMAGE:-
                        c.drawBitmap(background, 0, 0, null);
                        mainsong.start();

                        //SHIP:-
                        if(yTouch!=0) {     //TO SHOW SHIP ONLY WHEN TOUCHED
                            shipx = s_width - allsprites.shipSize.width() * 40;
                            shipy = (int) yTouch;
                            place = new Rect(shipx, shipy, (int) shipx + (allsprites.shipSize.width()) * 4, shipy + allsprites.shipSize.width() * 2);
                            c.drawBitmap(allsprites.space, (allsprites.shipSprites[eLoc]), place, null);

                            //SHIP BULLET:-
                            place = new Rect((int) shipbullet.x, shipbullet.y, (int) shipbullet.x + allsprites.shipBulletSprite.width() * 4,
                                    shipbullet.y + allsprites.shipBulletSprite.height() * 4);
                            c.drawBitmap(allsprites.space, allsprites.shipBulletSprite, place, null);
                        }
                        //EXPLOSION:-
                        place = new Rect((int)explosion.x, (int) explosion.y,
                                (int) explosion.x+allsprites.boomSize[loc].width() * 12, explosion.y+allsprites.boomSize[loc].height() * 12 );
                        c.drawBitmap(allsprites.space, allsprites.boomsprites[loc], place, null);

                        //METEOR1:-
                        place = new Rect(randommeteor1.x, randommeteor1.y, randommeteor1.x+allsprites.meteorSize.width()*4, randommeteor1.y+allsprites.meteorSize.height()*4);
                        c.drawBitmap(allsprites.space, allsprites.meteorsprites[m1Loc], place, null);

                        //METEOR2:-
                        place = new Rect(randommeteor2.x, randommeteor2.y, randommeteor2.x+allsprites.meteorSize.width()*4, randommeteor2.y+allsprites.meteorSize.height()*4);
                        c.drawBitmap(allsprites.space, allsprites.meteorsprites[m2Loc], place, null);

                        //METEOR3:-
                        place = new Rect(randommeteor3.x, randommeteor3.y, randommeteor3.x+allsprites.meteorSize.width()*4, randommeteor3.y+allsprites.meteorSize.height()*4);
                        c.drawBitmap(allsprites.space, allsprites.meteorsprites[m3Loc], place, null);


                        holder.unlockCanvasAndPost(c);

                        //SHIP BULLET UPDATE:-
                        shipbullet.x = shipbullet.x + s_width/20;

                        //METEOR RESPAWING AND MOVEMENT TOWARDS THE SHIP BASED ON THE SHIP POSITION
                        //METEORS X-AXIS MOVEMENT UPDATE:-
                        m1x = (int)(m1x-0.25);
                        m1xdifference = randommeteor1.x - m1catchshipx;
                        m1xdiff = m1xdifference/m1x;
                        m2x = (int)(m2x-0.25);
                        m2xdifference = randommeteor2.x - m2catchshipx;
                        m2xdiff = m2xdifference/m2x;
                        m3x = (int)(m3x-0.25);
                        m3xdifference = randommeteor3.x - m3catchshipx;
                        m3xdiff = m3xdifference/m3x;
                        //METEOR1 Y AXIS MOVEMENT UPDATE:-
                        if(randommeteor1.y < m1catchshipy){
                            m1y1 = (int) (m1y1-0.25);
                            m1ydifference = m1catchshipy - randommeteor1.y;
                            m1ydiff = m1ydifference/m1y1;
                            randommeteor1.x = randommeteor1.x - (int) m1xdiff;
                            randommeteor1.y = randommeteor1.y + (int) m1ydiff;
                        }
                        if(randommeteor1.y > m1catchshipy){
                            m1y2 = (int) (m1y2 - 0.25);
                            m1ydifference = randommeteor1.y - m1catchshipy;
                            m1ydiff = m1ydifference /m1y2;
                            randommeteor1.x = randommeteor1.x - (int) m1xdiff;
                            randommeteor1.y = randommeteor1.y - (int) m1ydiff;
                        }
                        if(randommeteor1.y == m1catchshipy){
                            randommeteor1.x = randommeteor1.x - (int) m1xdiff;
                        }
                        //METEOR2 Y AXIS MOVEMENT:-
                        if(randommeteor2.y < m2catchshipy){
                            m2y1 = (int) (m2y1-0.25);
                            m2ydifference = m2catchshipy - randommeteor2.y;
                            m2ydiff = m2ydifference/m2y1;
                            randommeteor2.x = randommeteor2.x - (int) m2xdiff;
                            randommeteor2.y = randommeteor2.y + (int) m2ydiff;
                        }
                        if(randommeteor2.y > m2catchshipy){
                            m2y2 = (int) (m2y2 - 0.25);
                            m2ydifference = randommeteor2.y - m2catchshipy;
                            m2ydiff = m2ydifference /m2y2;
                            randommeteor2.x = randommeteor2.x - (int) m2xdiff;
                            randommeteor2.y = randommeteor2.y - (int) m2ydiff;
                        }
                        if(randommeteor2.y == m2catchshipy){
                            randommeteor2.x = randommeteor2.x - (int) m2xdiff;
                        }
                        //METEOR3 Y AXIS MOVEMENT:-
                        if(randommeteor3.y < m3catchshipy){
                            m3y1 = (int) (m3y1-0.25);
                            m3ydifference = m3catchshipy - randommeteor3.y;
                            m3ydiff = m3ydifference/m3y1;
                            randommeteor3.x = randommeteor3.x - (int) m3xdiff;
                            randommeteor3.y = randommeteor3.y + (int) m3ydiff;
                        }
                        if(randommeteor3.y > m3catchshipy){
                            m3y2 = (int) (m3y2 - 0.25);
                            m3ydifference = randommeteor3.y - m3catchshipy;
                            m3ydiff = m3ydifference /m3y2;
                            randommeteor3.x = randommeteor3.x - (int) m3xdiff;
                            randommeteor3.y = randommeteor3.y - (int) m3ydiff;
                        }
                        if(randommeteor3.y == m3catchshipy){
                            randommeteor3.x = randommeteor3.x - (int) m3xdiff;
                        }

                        //CHECK IF METEOR HIT BORDER OR HIT THE SHIP:-
                        if(randommeteor1.x<=0 || randommeteor2.x<=0 || randommeteor3.x<=0){
                            vibrator.vibrate(200);
                            mainsong.stop();
                            gameover.start();
                            gameDone();
                        }

                        //CHECK IF SHIP BULLET IS OUT OF SCREEN:-
                        if (shipbullet.x > s_width) {
                            resetShipBullet();
                        }

                        //CHECK IF BULLETS ARE OUT OF SCREEN:-
                        loc = ((loc + 1) % 6);
                        eLoc = (eLoc + 1) % 4;
                        e1Loc = (e1Loc + 1) % 6;
                        e2Loc = (e2Loc + 1) % 6;
                        m1Loc = (m1Loc + 1) % 4;
                        m2Loc = (m2Loc + 1) % 4;
                        m3Loc = (m3Loc + 1) % 4;

                        //CHECK IF SHIP BULLET HIT METEORS:-
                        checkHitMeteor1();
                        checkHitMeteor2();
                        checkHitMeteor3();

                        lastUpdate = System.currentTimeMillis();
                    }
                }
            }
        };

        //METHOD TO CHECK METEORS HIT BY BULLET:-
        public void checkHitMeteor1()
        {
            if(shipbullet.x > randommeteor1.x && shipbullet.x < randommeteor1.x+allsprites.meteorSize.width()*4 &&
                    shipbullet.y > randommeteor1.y && shipbullet.y < randommeteor1.y+allsprites.meteorSize.height()*4 )
            {
                meteor1counter = meteor1counter + 1;    //INCREASE METEOR1 HIT COUNT FOR RESPAWINING METEOR
                if(meteor1counter >= 3){
                    soundPool.play(3,  1,1,1,0,1f);     //METEOR1 EXPLOSION SOUND
                    showExplosionMeteor1();
                    moveMeteor1();
                    meteorhit = meteorhit + 1;
                }
                if(meteorhit == 10){        //CHECK IF SHIP HIT 10 METEORS AND GAME OVER
                    mainsong.stop();
                    success.start();
                    gameDone();
                }
                resetShipBullet();
            }
        }
        public void checkHitMeteor2()
        {
            if(shipbullet.x > randommeteor2.x && shipbullet.x < randommeteor2.x+allsprites.meteorSize.width()*4 &&
                    shipbullet.y > randommeteor2.y && shipbullet.y < randommeteor2.y+allsprites.meteorSize.height()*4 )
            {
                meteor2counter = meteor2counter + 1;    //INCREASE METEOR2 HIT COUNT FOR RESPAWINING METEOR
                if(meteor2counter >= 3){
                    soundPool.play(3,  1,1,1,0,1f);     //METEOR2 EXPLOSION SOUND
                    showExplosionMeteor2();
                    moveMeteor2();
                    meteorhit = meteorhit + 1;
                }
                if(meteorhit >=10){         //CHECK IF SHIP HIT 10 METEORS AND GAME OVER
                    mainsong.stop();
                    success.start();
                    gameDone();
                }
                resetShipBullet();
            }
        }
        public void checkHitMeteor3()
        {
            if(shipbullet.x > randommeteor3.x && shipbullet.x < randommeteor3.x+allsprites.meteorSize.width()*4 &&
                    shipbullet.y > randommeteor3.y && shipbullet.y < randommeteor3.y+allsprites.meteorSize.height()*4 )
            {
                meteor3counter = meteor3counter + 1;    //INCREASE METEOR3 HIT COUNT FOR RESPAWINING METEOR
                if(meteor3counter >= 3){
                    soundPool.play(3,  1,1,1,0,1f);     //METEOR3 EXPLOSION SOUND
                    showExplosionMeteor3();
                    moveMeteor3();
                    meteorhit = meteorhit + 1;
                }
                if(meteorhit >= 10){        //CHECK IF SHIP HIT 10 METEORS AND GAME OVER
                    mainsong.stop();
                    success.start();
                    gameDone();
                }
                resetShipBullet();
            }
        }

        //RANDOM FUNCTIONS FOR METEORS RESPWANING:-
        public void randomFunctionMeteor1()
        {
            Random r1 = new Random();
            randommeteor1.x = r1.nextInt(s_width - 9*s_width/10) + 9*s_width/10;
            Random r2 = new Random();
            randommeteor1.y = r2.nextInt(s_height);
            //TO KNOW WHERE THE SHIP IS WHEN RESPWANING, FOR THE METEOR TO MOVE TOWARDS THE SHIP
            m1catchshipx = shipx;
            m1catchshipy = shipy;
            m1y1 = 100;
            m1y2 = 100;
            m1x = 100;
        }
        public void randomFunctionMeteor2()
        {
            Random r1 = new Random();
            randommeteor2.x = r1.nextInt(s_width - 9*s_width/10) + 9*s_width/10;
            Random r2 = new Random();
            randommeteor2.y = r2.nextInt(s_height);
            //TO KNOW WHERE THE SHIP IS WHEN RESPWANING, FOR THE METEOR TO MOVE TOWARDS THE SHIP
            m2catchshipx = shipx;
            m2catchshipy = shipy;
            m2y1 = 125;
            m2y2 = 125;
            m2x = 125;
        }
        public void randomFunctionMeteor3()
        {
            Random r1 = new Random();
            randommeteor3.x = r1.nextInt(s_width - 9*s_width/10) + 9*s_width/10;
            Random r2 = new Random();
            randommeteor3.y = r2.nextInt(s_height);
            //TO KNOW WHERE THE SHIP IS WHEN RESPWANING, FOR THE METEOR TO MOVE TOWARDS THE SHIP
            m3catchshipx = shipx;
            m3catchshipy = shipy;
            m3y1 = 150;
            m3y2 = 150;
            m3x = 150;
        }

        //RESPWAN METEORS AT RANDOM LOCATION
        public void moveMeteor1(){meteor1counter=0;randomFunctionMeteor1();}
        public void moveMeteor2(){meteor2counter=0;randomFunctionMeteor2();}
        public void moveMeteor3(){meteor3counter=0;randomFunctionMeteor3();}

        //METEORS EXPLOSION WHEN HIT BY BULLET
        public void showExplosionMeteor1()
        {
            vibrator.vibrate(300);
            explosion.x = randommeteor1.x;
            explosion.y = randommeteor1.y;
            //MAKE EXPLOSION NULL AFTER BULLET HIT
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    // this code will be executed after 100 milli seconds
                    makeExplosionNull();
                }
            }, 100);
        }
        public void showExplosionMeteor2()
        {
            vibrator.vibrate(300);
            explosion.x = randommeteor2.x;
            explosion.y = randommeteor2.y;
            //MAKE EXPLOSION NULL AFTER BULLET HIT
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    // this code will be executed after 100 milli seconds
                    makeExplosionNull();
                }
            }, 100);
        }
        public void showExplosionMeteor3()
        {
            vibrator.vibrate(300);
            explosion.x = randommeteor3.x;
            explosion.y = randommeteor3.y;
            //MAKE EXPLOSION NULL AFTER BULLET HIT
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    // this code will be executed after 100 milli seconds
                    makeExplosionNull();
                }
            }, 100);
        }

        //MAKE EXPLOSION NULL AFTER SHOWING ONCE
        public void makeExplosionNull()
        {
            explosion.x = 2147483647;
            explosion.y = 2147483647;
        }

        //RESET SHIP BULLET AFTER HITTING METEOR
        public void resetShipBullet()
        {
            soundPool.play(1,  1,1,1,0,1f);
            shipbullet.x = (allsprites.shipSize.width() + allsprites.shipSize.width()*4);
            shipbullet.y = (int) yTouch + (allsprites.shipSize.height()-allsprites.shipBulletSprite.height());
        }

        public void startGame()
        {
            gameThread.start();
        }
        public void gameDone(){
            vibrator.cancel();
            mainsong.stop();
            change = false;
            Intent i = new Intent(TheGameLevel2.this, MainActivity.class);
            startActivity(i);
            //CLEAN THE SURFACE AND SHOW THE MENU BY REMOVING FULL SCREEN
        }

        //SURFACEVIEW MENTODS
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {

        }
        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int pixelFormat, int width, int height) {
            s_width = width-(int)0.1 * width;
            s_height = height-(int)0.1 * height;
            randomFunctionMeteor1();
            randomFunctionMeteor2();
            randomFunctionMeteor3();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        }
    }
}
