package com.koszelew.flashbomb.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.yarolegovich.discretescrollview.DiscreteScrollView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class HomeTabRecyclerViewAdapter extends RecyclerView.Adapter<HomeTabRecyclerViewAdapter.ViewHolder> {

    /* Variables */
    private final MainActivity activity;
    private final Context context;
    private final ArrayList<FlashbombEntity> imageUrls;
    private int itemHeight = 0;
    public MediaPlayer mediaPlayer;
    private boolean mute = true;
    private MediaPlayer likeSound;
    public boolean fullRefresh = true;

    /* Constructor */
    public HomeTabRecyclerViewAdapter(MainActivity activity, ArrayList<FlashbombEntity> imageUrls, int itemHeight) {

        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.imageUrls = imageUrls;
        this.itemHeight = itemHeight;
        this.mediaPlayer = new MediaPlayer();
        activity.SetHomeListAdapter(this);
        this.likeSound = MediaPlayer.create(context, R.raw.like_sound);

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextureView video;
        PanoramaImageView image;
        private ProgressBar preloadProgressBar;
        private TextView title;
        private TextView location;
        private ImageView emoji;
        private ImageView reportButton;
        private ImageView titleCamera;
        private LottieAnimationView likeButton;
        private LottieAnimationView soundAnimation;

        public ViewHolder(View itemView) {

            super(itemView);

            /* Finding views */
            video = itemView.findViewById(R.id.homeItemPreviewVideo);
            image = itemView.findViewById(R.id.homeItemPreviewImage);
            preloadProgressBar = itemView.findViewById(R.id.homeItemPreviewProgressBar);
            title = itemView.findViewById(R.id.homeItemPreviewTitle);
            titleCamera = itemView.findViewById(R.id.homeItemTitleDown);
            location = itemView.findViewById(R.id.homeItemCountry);
            emoji = itemView.findViewById(R.id.homeItemEmoji);
            reportButton = itemView.findViewById(R.id.homeItemReport);
            likeButton = itemView.findViewById(R.id.homeItemLike);
            soundAnimation = itemView.findViewById(R.id.homeItemSound);

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.home_listview_item, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        /* Setting ListView item height */
        holder.itemView.getLayoutParams().height = itemHeight;

        /* Prefetching next thumbnail and video */
        if ((position + 1) < imageUrls.size()) {
            FlashbombEntityLoader.getInstance(context).FetchImageFromUrl(imageUrls.get(position + 1).thumbnail);
        }
        if ((position + 1) < imageUrls.size() && imageUrls.get(position + 1).video.equals("1")) {
            FlashbombEntityLoader.getInstance(context).FetchVideo(imageUrls.get(position + 1).url);
        }

        /* If normal item (not title) */
        if (!imageUrls.get(position).is_title) {

            /* Loading thumbnail */
            holder.image.setGyroscopeObserver(activity.panoramaGyroscopeListener);
            FlashbombEntityLoader.getInstance(context).LoadImageFromUrl(imageUrls.get(position).thumbnail, holder.image);

            /* Setting video placeholder white */
            holder.image.setBackgroundColor(Color.WHITE);

            /* Getting countrycode and flag emoji from assets */
            String isoCountry = imageUrls.get(position).countrycode == null ? "" : imageUrls.get(position).countrycode;
            String locality = imageUrls.get(position).locality;
            if (isoCountry.equals("XX") || isoCountry.equals("")) {

                holder.location.setText(activity.getString(R.string.unknown_country));
                try {
                    holder.emoji.setImageDrawable(Drawable.createFromStream(activity.getAssets().open("emojis/emoji_country_unknown.png"), null));
                } catch (IOException e) {
                    Crashlytics.logException(e);
                }

            } else {

                holder.location.setText(locality.length() == 0 ? new Locale("", isoCountry).getDisplayCountry() : locality);
                try {
                    holder.emoji.setImageDrawable(Drawable.createFromStream(activity.getAssets().open(imageUrls.get(position).countryEmoji), null));
                } catch (IOException e) {
                    Crashlytics.logException(e);
                }

            }
            holder.location.setTypeface(InitApp.contentsTypeface);
            float translationX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, activity.getResources().getDisplayMetrics()) / 2;
            holder.emoji.setTranslationX(translationX);
            holder.location.setTranslationX(translationX);

            /* Setting animation like listener */
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
                    activity.NotifyObservationsTabAdapter();

                }

            });

            /* Checking if picture is from current session */
            final SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
            if (!imageUrls.get(position).session_id.equals(sharedPref.getString(context.getString(R.string.PREFERENCES_LATEST_SESSION_ID_KEY), "-1"))) {
                holder.likeButton.setClickable(false);
                holder.likeButton.setAlpha(0.25f);
            } else {
                holder.likeButton.setClickable(true);
                holder.likeButton.setAlpha(1.0f);
            }

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
            holder.likeButton.setVisibility(View.VISIBLE);
            holder.reportButton.setVisibility(View.VISIBLE);
            holder.video.setAlpha(0);
            holder.title.setVisibility(View.GONE);
            holder.titleCamera.setVisibility(View.GONE);
            holder.titleCamera.setOnClickListener(null);
            holder.preloadProgressBar.setVisibility(View.GONE);

        } else {

            /* Setting emoji */
            holder.emoji.setImageDrawable(null);
            holder.emoji.setTranslationX(0);

            /* Setting title bg */
            holder.image.setBackgroundColor(Color.BLACK);
            holder.image.setImageDrawable(null);

            /* Getting weekday */
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

            /* Setting video */
            mediaPlayer.reset();
            mediaPlayer.setVolume(0, 0);
            holder.video.setSurfaceTextureListener(null);

            /* Setting visibility */
            holder.likeButton.setVisibility(View.GONE);
            holder.reportButton.setVisibility(View.INVISIBLE);
            holder.video.setAlpha(0);
            holder.soundAnimation.setVisibility(View.GONE);
            holder.preloadProgressBar.setVisibility(View.GONE);
            holder.title.setVisibility(View.VISIBLE);
            holder.title.setTypeface(InitApp.headersTypeface);
            holder.location.setTypeface(InitApp.contentsTypeface);
            holder.location.setTranslationX(0);
            holder.titleCamera.setVisibility(View.VISIBLE);

            /* Session active design */
            if (InitApp.isSessionActive) {
                holder.titleCamera.setColorFilter(activity.getResources().getColor(R.color.black));
                holder.titleCamera.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                holder.titleCamera.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.camera_flashtime));
                holder.title.setText(activity.getString(R.string.flashtime));
                holder.location.setText("");
            } else {
                holder.titleCamera.setColorFilter(activity.getResources().getColor(R.color.gray_light));
                holder.titleCamera.setLayerType(View.LAYER_TYPE_NONE, null);
                holder.titleCamera.clearAnimation();
                holder.title.setText(weekDay + "\n" + new SimpleDateFormat("HH:mm").format(imageUrls.get(position).session_id_date));
                holder.location.setText(new SimpleDateFormat("dd/MM/yyyy").format(imageUrls.get(position).session_id_date));
            }
            holder.titleCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (InitApp.isSessionActive) {
                        activity.OpenCamera(-1, -1, -1, "default");
                    } else if (imageUrls.size() > position + 1) {
                        ((DiscreteScrollView) activity.findViewById(R.id.homePageList)).smoothScrollToPosition(position + 1);
                        activity.ShowTutorialDialog(false, activity.getString(R.string.tutorial_wait_for_flashtime));
                    }
                }
            });

        }

        /* Fading in first element if whole list refreshed */
        if (fullRefresh && position == 0) {
            Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    holder.itemView.setLayerType(View.LAYER_TYPE_NONE, null);
                }

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