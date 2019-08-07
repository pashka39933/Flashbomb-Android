package com.koszelew.flashbomb.Listeners;

import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.io.IOException;


public class CameraVideoListener implements TextureView.SurfaceTextureListener {

    /* Variables */
    private FirebaseAnalytics analytics;
    TextureView video;
    private final MediaPlayer mediaPlayer;
    private final File videoFile;

    /* Help flag */
    public boolean surfaceAvailable = false;

    /* Contructor */
    public CameraVideoListener(TextureView video, File output, FirebaseAnalytics analytics) {

        this.video = video;
        this.mediaPlayer = new MediaPlayer();
        this.videoFile = output;
        this.analytics = analytics;

    }

    /* Texture ready callback */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

        /* Playing video and marking info flag */
        PlayVideo();
        surfaceAvailable = true;

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {

        mediaPlayer.release();
        return false;

    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {}

    /* Method resetting mediaPlayer */
    public void Reset() {

        mediaPlayer.reset();

    }

    /* Method playing video */
    public void PlayVideo() {

        mediaPlayer.setLooping(true);

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(final MediaPlayer mediaPlayer) {

                /* Analytics report */
                Bundle bundle = new Bundle();
                bundle.putString("output_video_dimensions", (mediaPlayer.getVideoWidth() + " x " + mediaPlayer.getVideoHeight()));
                analytics.logEvent("VideoFile", bundle);

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

                mediaPlayer.start();

            }

        });

        mediaPlayer.setSurface(new Surface(video.getSurfaceTexture()));

        try {
            mediaPlayer.setDataSource(videoFile.getAbsolutePath());
        } catch (IOException e) {
            Crashlytics.logException(e);
        }

        /* Analytics report */
        Bundle bundle = new Bundle();
        bundle.putString("output_video_size", ((float) (videoFile.length()) / 1024 / 1024) + " MB");
        analytics.logEvent("VideoFileSize", bundle);

        /* Preparing media player */
        mediaPlayer.prepareAsync();

    }

}
