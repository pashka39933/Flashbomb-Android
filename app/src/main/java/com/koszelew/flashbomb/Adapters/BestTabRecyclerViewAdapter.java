package com.koszelew.flashbomb.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.koszelew.flashbomb.Activities.MainActivity;
import com.koszelew.flashbomb.Listeners.RecyclerViewVideoListener;
import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.UIComponents.PanoramaImageView;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.FlashbombEntity;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.FlashbombEntityLoader;
import com.koszelew.flashbomb.Utils.Networking.SingletonNetwork;
import com.koszelew.flashbomb.Utils.Other.InitApp;
import com.koszelew.flashbomb.Utils.Other.ServerTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class BestTabRecyclerViewAdapter extends RecyclerView.Adapter<BestTabRecyclerViewAdapter.ViewHolder> {

    /* Variables */
    private final MainActivity activity;
    private final Context context;
    private final ArrayList<FlashbombEntity> imageUrls;
    private int itemHeight = 0;
    public MediaPlayer mediaPlayer;
    private boolean mute = true;
    public boolean fullRefresh = true;

    /* Constructor */
    public BestTabRecyclerViewAdapter(MainActivity activity, ArrayList<FlashbombEntity> imageUrls, int itemHeight) {

        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.imageUrls = imageUrls;
        this.itemHeight = itemHeight;
        this.mediaPlayer = new MediaPlayer();
        activity.SetBestListAdapter(this);

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextureView video;
        private PanoramaImageView image;
        private ProgressBar preloadProgressBar;
        private TextView hour;
        private TextView date;
        private TextView points;
        private ImageView pointsHeart;
        private TextView nick;
        private LinearLayout overlay;
        private RelativeLayout nickOverlay;
        private LottieAnimationView soundAnimation;

        public ViewHolder(View itemView) {

            super(itemView);

            /* Finding views */
            video = itemView.findViewById(R.id.bestItemPreviewVideo);
            image = itemView.findViewById(R.id.bestItemPreviewImage);
            preloadProgressBar = itemView.findViewById(R.id.bestItemPreviewProgressBar);
            hour = itemView.findViewById(R.id.bestItemHour);
            date = itemView.findViewById(R.id.bestItemDate);
            points = itemView.findViewById(R.id.bestItemPoints);
            pointsHeart = itemView.findViewById(R.id.bestItemPreviewOverlayHeart);
            overlay = itemView.findViewById(R.id.bestItemPreviewOverlay);
            nick = itemView.findViewById(R.id.bestItemNick);
            nickOverlay = itemView.findViewById(R.id.bestItemNickOverlay);
            soundAnimation = itemView.findViewById(R.id.bestItemSound);

        }
    }

    @Override
    public BestTabRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.best_listview_item, parent, false);
        return new BestTabRecyclerViewAdapter.ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        /* Setting item height */
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

        /* Setting params */
        holder.hour.setText(new SimpleDateFormat("HH:mm").format(imageUrls.get(position).session_id_date));
        holder.hour.setTypeface(InitApp.contentsTypeface);
        Calendar c = Calendar.getInstance();
        c.setTime(imageUrls.get(position).session_id_date);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        String weekDay =
                dayOfWeek == 1 ? activity.getString(R.string.sunday) :
                        dayOfWeek == 2 ? activity.getString(R.string.monday) :
                                dayOfWeek == 3 ? activity.getString(R.string.tuesday) :
                                        dayOfWeek == 4 ? activity.getString(R.string.wednesday) :
                                                dayOfWeek == 5 ? activity.getString(R.string.thursday) :
                                                        dayOfWeek == 6 ? activity.getString(R.string.friday) :
                                                                dayOfWeek == 7 ? activity.getString(R.string.saturday) : "";
        long daysDiff = TimeUnit.DAYS.convert(ServerTime.getCurrentTimestamp() * 1000L - imageUrls.get(position).session_id_date.getTime(), TimeUnit.MILLISECONDS);
        holder.date.setText(daysDiff > 6 ? new SimpleDateFormat("dd/MM/yyyy").format(imageUrls.get(position).session_id_date) : weekDay);
        holder.date.setTypeface(InitApp.contentsTypeface);
        holder.points.setText(imageUrls.get(position).points);
        holder.points.setTypeface(InitApp.headersTypeface);
        holder.pointsHeart.setColorFilter(new PorterDuffColorFilter(imageUrls.get(position).getRangeColor(), PorterDuff.Mode.MULTIPLY));
        if(imageUrls.get(position).nick.length() < 3) {
            holder.nick.setText(context.getString(R.string.unknown_nick));
            holder.nick.setOnClickListener(null);
        } else {
            holder.nick.setText("#" + imageUrls.get(position).nick);
            holder.nick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final SharedPreferences sharedPref = activity.getSharedPreferences(activity.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
                    sharedPref.edit().putString(activity.getString(R.string.PREFERENCES_URL_OBSERVATION), imageUrls.get(position).nick).apply();
                    activity.AddObservationAlert();
                }
            });
        }
        holder.nick.setClickable(false);
        holder.nick.setTypeface(InitApp.headersTypeface);


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

        /* Setting overlay fadeout */
        if(imageUrls.get(position).best_state.equals("1")) {

            holder.overlay.setVisibility(View.GONE);
            holder.nickOverlay.setVisibility(View.GONE);
            holder.nick.setClickable(true);

        } else {

            holder.overlay.setVisibility(View.VISIBLE);
            holder.nickOverlay.setVisibility(View.VISIBLE);
            holder.overlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Animation fadeOut = AnimationUtils.loadAnimation(activity, R.anim.fade_out);
                    fadeOut.setDuration(500);
                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                            holder.overlay.setLayerType(View.LAYER_TYPE_NONE, null);
                            holder.nickOverlay.setLayerType(View.LAYER_TYPE_NONE, null);

                            holder.overlay.setVisibility(View.GONE);
                            holder.nickOverlay.setVisibility(View.GONE);
                            holder.overlay.setClickable(false);
                            holder.video.setClickable(true);
                            holder.video.setLongClickable(true);
                            holder.image.setLongClickable(true);
                            imageUrls.get(position).best_state = "1";
                            SingletonNetwork.getInstance(context).RevealBest(imageUrls.get(position).id);

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    holder.overlay.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    holder.overlay.startAnimation(fadeOut);
                    holder.nickOverlay.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    holder.nickOverlay.startAnimation(fadeOut);
                    holder.nick.setClickable(true);
                }
            });

        }

        /* Setting volume toggle visibility */
        holder.soundAnimation.setVisibility(imageUrls.get(position).video.equals("1") ? View.VISIBLE : View.GONE);

        /* Video touch listener */
        holder.video.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (holder.overlay.getVisibility() == View.GONE) {

                    holder.overlay.clearAnimation();
                    Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
                    fadeIn.setDuration(300);
                    fadeIn.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            holder.overlay.setLayerType(View.LAYER_TYPE_NONE, null);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    holder.overlay.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    holder.overlay.startAnimation(fadeIn);
                    holder.overlay.setVisibility(View.VISIBLE);

                }
                return false;
            }
        });
        holder.video.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    if (holder.overlay.getVisibility() == View.VISIBLE) {

                        Animation fadeOut = AnimationUtils.loadAnimation(activity, R.anim.fade_out);
                        fadeOut.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) { holder.overlay.setLayerType(View.LAYER_TYPE_NONE, null); }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        fadeOut.setDuration(300);
                        fadeOut.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                holder.overlay.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        if (holder.overlay.getAnimation() != null)
                            holder.overlay.getAnimation().cancel();
                        holder.overlay.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                        holder.overlay.startAnimation(fadeOut);

                    } else {

                        if (imageUrls.get(position).video.equals("1")) {
                            mute = !mute;
                            mediaPlayer.setVolume(mute ? 0 : 1, mute ? 0 : 1);
                            holder.soundAnimation.pauseAnimation();
                            holder.soundAnimation.setSpeed(mute ? -4 : 4);
                            holder.soundAnimation.playAnimation();
                        }

                    }
                }

                return false;

            }
        });

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