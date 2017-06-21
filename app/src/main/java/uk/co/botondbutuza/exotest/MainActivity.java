package uk.co.botondbutuza.exotest;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String FIRST_STREAM = "http://europe.minglvision.com/hls/cam1.m3u8";
    private static final String SECOND_STREAM = "http://europe.minglvision.com/hls/cam2.m3u8";

    @BindView(R.id.first_player)    TextureView firstPlayerView;
    @BindView(R.id.second_player)   TextureView secondPlayerView;

    private SimpleExoPlayer player1, player2;
    private int indexMain, indexPreview;

    private DataSource.Factory dataSourceFactory;
    private TrackSelector trackSelector;
    private LoadControl loadControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initPlayerVariables();
    }

    @Override
    protected void onResume() {
        super.onResume();

        preparePlayer(indexMain, FIRST_STREAM);
        getMainPlayer().setVideoTextureView(firstPlayerView);
        getMainPlayer().setPlayWhenReady(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer(indexMain);
    }


    @OnClick(R.id.switch_views)
    public void onSwitchViews() {
        if (getPreviewPlayer() != null) {
            releasePlayer(indexPreview);
        } else {

        }
    }


    private void preparePlayer(int index, String url) {
        long time = -1;

        if (getMainPlayer() != null) {
            log("preparing player with no time, grabbing main time="+getMainPlayer().getCurrentPosition());
            time = getMainPlayer().getCurrentPosition();
        }

        preparePlayer(index, createMediaSource(url), time);
    }

    private void preparePlayer(int index, MediaSource source, long time) {
        switch (index) {
            case 1:
                player1 = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
                log("preparing player, time="+time);
                if (time > -1) {
                    player1.seekTo(time);
                }
                player1.setVolume(0);
                player1.prepare(source);
                break;
            case 2:
                player2 = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
                log("preparing player, time="+time);
                if (time > -1) {
                    player2.seekTo(time);
                }
                player2.setVolume(0);
                player2.prepare(source);
                break;
        }
    }

    private void releasePlayer(int index) {
        if (getPlayer(index) == null) return;

        switch (index) {
            case 1:
                player1.stop();
                player1.release();
                player1 = null;
                break;
            case 2:
                player2.stop();
                player2.release();
                player2 = null;
                break;
        }
    }

    private MediaSource createMediaSource(String url) {
        log("creating media source, path="+url);
        return new HlsMediaSource(Uri.parse(url), dataSourceFactory, null, null);
    }

    public SimpleExoPlayer getMainPlayer() {
        return getPlayer(indexMain);
    }

    public SimpleExoPlayer getPreviewPlayer() {
        return getPlayer(indexPreview);
    }

    private SimpleExoPlayer getPlayer(int index) {
        switch (index) {
            case 1:
                return player1;
            case 2:
                return player2;
            default:
                return null;
        }
    }


    private void initPlayerVariables() {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, BuildConfig.APPLICATION_ID), bandwidthMeter);

        DefaultAllocator allocator = new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE);
        loadControl = new DefaultLoadControl(allocator);
        indexMain = 1; indexPreview = 2;
    }


    private void log(String msg) {
        Log.e(getClass().getSimpleName(), msg);
    }
}
