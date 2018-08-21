package com.bbtree.cardreader.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.bbtree.baselib.utils.ListUtils;
import com.bbtree.cardreader.entity.eventbus.PauseMusicEvent;
import com.bbtree.cardreader.entity.eventbus.ScreenSaverCircleEvent;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * Created by chenglei on 2017/8/16.
 */

public class MusicPlayService extends Service {

    private static final String TAG = MusicPlayService.class.getSimpleName();

    private static final String ACTION_START = "com.bbtree.music.start";

    private MediaPlayer mediaPlayer;

    private int playIndex = 0;

    public static void startMusicPlayService(Context context, ArrayList<String> urls) {
        Intent intent = new Intent(context, MusicPlayService.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("audio_url", urls);
        intent.putExtras(bundle);
        intent.setAction(ACTION_START);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
//        playMusic(lists, 0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int startCommandResult = START_STICKY;
        if (intent == null) {
            return startCommandResult;
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return startCommandResult;
        }
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        ArrayList<String> audioUrls = extras.getStringArrayList("audio_url");
        if (action.equals(ACTION_START)) {
            playMusic(audioUrls, 0);
        }
        return startCommandResult;
    }

    /**
     * 播放音乐
     *
     * @param lists
     * @param index
     */
    private void playMusic(final List<String> lists, int index) {
        if (ListUtils.isZero(lists)) return;
        if (lists.size() != index)
            playIndex = index;
        String path = lists.get(playIndex % lists.size());
        File file = new File(path);
        if (file.exists() && file.length() > 0) {
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(path);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                        }
                    }
                });

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        playIndex += 1;
                        playMusic(lists, playIndex);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Logger.t(TAG).i("onDestroy....");
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainPauseMusic(PauseMusicEvent mPauseMusic) {
        Logger.t(TAG).i("PauseMusicEvent::::" + mPauseMusic.toString());
        if (null == mPauseMusic) return;
        if (mPauseMusic.type == PauseMusicEvent.PauseMusicType.pause) {
            if (mPauseMusic.isPause() && mPauseMusic.getDelayTime() == Integer.MAX_VALUE) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            }
            if (mPauseMusic.isPause() && mPauseMusic.getDelayTime() != Integer.MAX_VALUE) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                Observable.timer(mPauseMusic.getDelayTime(), TimeUnit.MILLISECONDS).subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                            ScreenSaverCircleEvent event = new ScreenSaverCircleEvent();
                            event.setCircleDelayTime(0);
                            EventBus.getDefault().post(event);
                        }
                    }
                });

            }
            if (!mPauseMusic.isPause()) {
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }

        } else if (mPauseMusic.type == PauseMusicEvent.PauseMusicType.stopSelf) {
            if (mPauseMusic.isStopSelf()) {
                stopMusicPlayService();
            }
        }
    }


    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void stopMusicPlayService() {
        stop();
        stopSelf();
    }

}
