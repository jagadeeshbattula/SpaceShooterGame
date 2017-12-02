package ubknights.com.spaceshooter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    Button playAgain;
    static int playedNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        playAgain = (Button)findViewById(R.id.playagain);

        //PLAY AGAIN BUTTON:-
        playAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (playedNow){
                    case 1:
                        Intent t1 = new Intent(MainActivity.this, TheGameLevel1X.class);
                        startActivity(t1);
                        break;
                    case 2:
                        Intent t2 = new Intent(MainActivity.this, TheGameLevel2.class);
                        startActivity(t2);
                        break;
                    default:
                        break;
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //create a menu with two options: play round 1 or play round two
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE,0,Menu.NONE,"First Level");
        menu.add(Menu.NONE, 1,Menu.NONE,"Second Level");
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case  0:
                //STARTS GAME LEVEL 1:-
                playedNow = 1;
               Intent t1 = new Intent(MainActivity.this, TheGameLevel1X.class);
                startActivity(t1);
                return true;
            case 1:
                //STARTS GAME LEVEL 2:-
                playedNow = 2;
                Intent t2 = new Intent(MainActivity.this, TheGameLevel2.class);
                startActivity(t2);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

