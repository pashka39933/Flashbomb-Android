package com.koszelew.flashbomb.Listeners;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ProgressBar;

import com.airbnb.lottie.LottieAnimationView;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.FlashbombEntity;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.FlashbombEntityLoader;


public class RecyclerViewVideoListener implements TextureView.SurfaceTextureListener {

    /* Variables */
    private final Context ctx;
    private final MediaPlayer mediaPlayer;
    private final FlashbombEntity entity;
    private final TextureView video;
    private final ProgressBar preloader;
    private final LottieAnimationView soundAnimation;

    /* Contructor */
    public RecyclerViewVideoListener(final Context ctx, MediaPlayer mp, FlashbombEntity entity, TextureView video, ProgressBar preloader, LottieAnimationView soundAnimation) {

        this.ctx = ctx;
        this.mediaPlayer = mp;
        this.entity = entity;
        this.video = video;
        this.preloader = preloader;
        this.soundAnimation = soundAnimation;

    }

    /* Texture ready callback */
    @Override
    public void onSurfaceTextureAvailable(final SurfaceTexture surfaceTexture, int i, int i1) {

        PrepareVideoPlayback(surfaceTexture);

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {}

    // Media player start method
    public void StartPlayback() {

        if(!mediaPlayer.isPlaying())
            mediaPlayer.start();

    }

    // Media player pause method
    public void PausePlayback() {

        if(mediaPlayer.isPlaying())
            mediaPlayer.pause();

    }


    private void PrepareVideoPlayback(SurfaceTexture surface) {

        /* When mediaPlayer data prepared */
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(final MediaPlayer mediaPlayer) {

                float videoAspect = (float)mediaPlayer.getVideoWidth() / ((float)mediaPlayer.getVideoHeight() + (float)mediaPlayer.getVideoWidth());
                float textureAspect = (float)video.getWidth() / ((float)video.getHeight() + (float)video.getWidth());
                float xScale = 1;
                float yScale = 1;
                if(videoAspect > textureAspect) {

                    float yScaleDone = (float)video.getHeight() / (float)mediaPlayer.getVideoHeight();
                    float targetVideoWidth = yScaleDone * (float)mediaPlayer.getVideoWidth();
                    xScale = targetVideoWidth / (float)video.getWidth();

                } else if(videoAspect < textureAspect) {

                    float xScaleDone = (float)video.getWidth() / (float)mediaPlayer.getVideoWidth();
                    float targetVideoHeight = xScaleDone * (float)mediaPlayer.getVideoHeight();
                    yScale = targetVideoHeight / (float)video.getHeight();

                }
                Matrix matrix = new Matrix();
                matrix.setScale(xScale, yScale, (float) video.getWidth() / 2, (float) video.getHeight() / 2);
                video.setTransform(matrix);

                /* Starting playback */
                StartPlayback();
                video.setAlpha(1);
                preloader.setVisibility(View.GONE);

            }

        });

        /* Showing preloader */
        preloader.setVisibility(View.VISIBLE);

        /* Setting sound animation progress */
        soundAnimation.setProgress(0);

        /* Resetting previous mediaPlayer */
        mediaPlayer.reset();
        mediaPlayer.setLooping(true);

        /* Setting mediaPlayer's display and looping */
        mediaPlayer.setSurface(new Surface(surface));

        /* Setting mediaPlayer's data source */
        FlashbombEntityLoader.getInstance(ctx).LoadVideo(entity.url, mediaPlayer);

        /* Preparing media player */
        mediaPlayer.prepareAsync();

    }

}
