package com.koszelew.flashbomb.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.crashlytics.android.Crashlytics;
import com.koszelew.flashbomb.Activities.MainActivity;
import com.koszelew.flashbomb.Listeners.RecyclerViewVideoListener;
import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.UIComponents.PanoramaImageView;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.FlashbombEntity;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.FlashbombEntityLoader;
import com.koszelew.flashbomb.Utils.Networking.SingletonNetwork;
import com.koszelew.flashbomb.Utils.Other.InitApp;

import java.io.IOException;
import java.util.ArrayList;

public class InfluencerGalleryRecyclerViewAdapter extends RecyclerView.Adapter<InfluencerGalleryRecyclerViewAdapter.ViewHolder> {

    /* Variables */
    private final MainActivity activity;
    private final Context context;
    private final ArrayList<FlashbombEntity> imageUrls;
    private int itemHeight = 0;
    public MediaPlayer mediaPlayer;
    private boolean mute;
    private MediaPlayer likeSound;
    public String influencerID = "";
    public boolean fullRefresh = true;

    /* Constructor */
    public InfluencerGalleryRecyclerViewAdapter(MainActivity activity, ArrayList<FlashbombEntity> imageUrls, int itemHeight, String influencerID) {

        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.imageUrls = imageUrls;
        this.itemHeight = itemHeight;
        this.mediaPlayer = new MediaPlayer();
        this.likeSound = MediaPlayer.create(context, R.raw.like_sound);
        this.influencerID = influencerID;

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextureView video;
        private PanoramaImageView image;
        private ProgressBar preloadProgressBar;
        final TextView nick;
        final ImageView emoji;
        final ImageView reportButton;
        final LottieAnimationView likeButton;
        private LottieAnimationView soundAnimation;

        public ViewHolder(View itemView) {

            super(itemView);

            /* Finding views */
            video = itemView.findViewById(R.id.influencerGalleryItemPreviewVideo);
            image = itemView.findViewById(R.id.influencerGalleryItemPreviewImage);
            preloadProgressBar = itemView.findViewById(R.id.influencerGalleryItemPreviewProgressBar);
            nick = itemView.findViewById(R.id.influencerGalleryItemNick);
            emoji = itemView.findViewById(R.id.influencerGalleryItemEmoji);
            reportButton = itemView.findViewById(R.id.influencerGalleryItemReport);
            likeButton = itemView.findViewById(R.id.influencerGalleryItemLike);
            soundAnimation = itemView.findViewById(R.id.influencerGalleryItemSound);

        }
    }

    @Override
    public InfluencerGalleryRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.influencer_gallery_listview_item, parent, false);
        return new InfluencerGalleryRecyclerViewAdapter.ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(final InfluencerGalleryRecyclerViewAdapter.ViewHolder holder, final int position) {

        /* Setting ListView item height */
        holder.itemView.getLayoutParams().height = itemHeight;

        /* Loading thumbnail, prefetching next one */
        holder.image.setGyroscopeObserver(activity.panoramaGyroscopeListener);
        FlashbombEntityLoader.getInstance(context).LoadImageFromUrl(imageUrls.get(position).thumbnail, holder.image);
        if ((position + 1) < imageUrls.size()) {
            FlashbombEntityLoader.getInstance(context).FetchImageFromUrl(imageUrls.get(position + 1).thumbnail);
        }
        if ((position + 1) < imageUrls.size() && imageUrls.get(position + 1).video.equals("1")) {
            FlashbombEntityLoader.getInstance(context).FetchVideo(imageUrls.get(position + 1).url);
        }

        /* Setting nick */
        holder.nick.setText(imageUrls.get(position).nick);
        holder.nick.setTypeface(InitApp.contentsTypeface);

        /* Getting countrycode and flag emoji from assets */
        String isoCountry = imageUrls.get(position).countrycode == null ? "" : imageUrls.get(position).countrycode;
        if (isoCountry.equals("XX") || isoCountry.equals("")) {

            try {
                holder.emoji.setImageDrawable(Drawable.createFromStream(activity.getAssets().open("emojis/emoji_country_unknown.png"), null));
            } catch (IOException e) {
                Crashlytics.logException(e);
            }

        } else {

            try {
                holder.emoji.setImageDrawable(Drawable.createFromStream(activity.getAssets().open(imageUrls.get(position).countryEmoji), null));
            } catch (IOException e) {
                Crashlytics.logException(e);
            }

        }
        float translationX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, activity.getResources().getDisplayMetrics()) / 2;
        holder.emoji.setTranslationX (translationX);
        holder.nick.setTranslationX (translationX);

        /* Setting like animation listener */
        holder.likeButton.addColorFilterToLayer("Layer 2/nowasoft Outlines", new PorterDuffColorFilter(imageUrls.get(position).getRangeColor(), PorterDuff.Mode.MULTIPLY));
        holder.likeButton.setProgress(imageUrls.get(position).voted.equals("1") ? 1 : 0);
        holder.likeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (imageUrls.get(position).voted.equals("0")) {

                    imageUrls.get(position).voted = "1";
                    imageUrls.get(position).points = (Integer.parseInt(imageUrls.get(position).points) + 1) + "";
                    holder.likeButton.addColorFilterToLayer("Layer 2/nowasoft Outlines", new PorterDuffColorFilter(imageUrls.get(position).getRangeColor(), PorterDuff.Mode.MULTIPLY));
                    SingletonNetwork.getInstance(context).Vote(imageUrls.get(position).id);
                    holder.likeButton.setSpeed(1);
                    holder.likeButton.playAnimation();

                    likeSound.seekTo(0);
                    likeSound.start();

                } else {

                    imageUrls.get(position).voted = "0";
                    imageUrls.get(position).points = (Integer.parseInt(imageUrls.get(position).points) - 1) + "";
                    holder.likeButton.addColorFilterToLayer("Layer 2/nowasoft Outlines", new PorterDuffColorFilter(imageUrls.get(position).getRangeColor(), PorterDuff.Mode.MULTIPLY));
                    SingletonNetwork.getInstance(context).Unvote(imageUrls.get(position).id);

                    holder.likeButton.setSpeed(-1);
                    holder.likeButton.playAnimation();

                }

                activity.NotifyBestTabAdapter();
                activity.NotifyHomeTabAdapter();

            }

        });

        /* Setting report button alert */
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.report_user_image));
        builder.setItems(new CharSequence[]{
                activity.getString(R.string.report_label0),
                activity.getString(R.string.report_label1),
                activity.getString(R.string.report_label2),
                activity.getString(R.string.report_label3)
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SingletonNetwork.getInstance(activity).Report(imageUrls.get(position).id, which);
            }
        });
        holder.reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.show();
            }
        });

        /* Setting up video playback if video entity */
        if (imageUrls.get(position).video.equals("1")) {

            RecyclerViewVideoListener videoListener = new RecyclerViewVideoListener(
                    activity,
                    mediaPlayer,
                    imageUrls.get(position),
                    holder.video,
                    holder.preloadProgressBar,
                    holder.soundAnimation
            );
            holder.video.setSurfaceTextureListener(videoListener);

        } else {

            holder.video.setSurfaceTextureListener(null);

        }

        /* Setting volume toggle */
        if (imageUrls.get(position).video.equals("1")) {
            holder.video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mute = !mute;
                    mediaPlayer.setVolume(mute ? 0 : 1, mute ? 0 : 1);
                    holder.soundAnimation.pauseAnimation();
                    holder.soundAnimation.setSpeed(mute ? -4 : 4);
                    holder.soundAnimation.playAnimation();
                }
            });
        } else {
            holder.video.setOnClickListener(null);
        }

        /* Setting volume toggle visibility */
        holder.soundAnimation.setVisibility(imageUrls.get(position).video.equals("1") ? View.VISIBLE : View.GONE);

        /* Setting visibility */
        mediaPlayer.reset();
        mediaPlayer.setVolume(0, 0);
        holder.image.setVisibility(View.VISIBLE);
        holder.video.setAlpha(0);
        holder.preloadProgressBar.setVisibility(View.GONE);

        /* Fading in first element if whole list refreshed */
        if (fullRefresh && position == 0) {
            Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }
                @Override
                public void onAnimationEnd(Animation animation) { holder.itemView.setLayerType(View.LAYER_TYPE_NONE, null); }
                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            holder.itemView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            holder.itemView.startAnimation(fadeIn);
            fullRefresh = false;
        }

    }

    /* Method to mute media player */
    public void MuteMediaPlayer() {

        mute = true;
        this.mediaPlayer.setVolume(0, 0);

    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

}