package com.example.snsoo.cosmicfriends;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.view.Display;

public class PlayActivity extends Activity {

    SpaceInvadersView spaceInvadersView;
    private static MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        spaceInvadersView = new SpaceInvadersView(this, (int) (size.x*0.8), (int) (size.y*0.9));
        setContentView(spaceInvadersView);

        mp = MediaPlayer.create(this, R.raw.bg);
        mp.setLooping(true);
        mp.start();

    }

    @Override
    protected void onResume() {
        super.onResume();

        spaceInvadersView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mp.pause();

        spaceInvadersView.pause();
    }
}
