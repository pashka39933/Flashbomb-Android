package com.koszelew.flashbomb.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koszelew.flashbomb.Activities.MainActivity;
import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.FlashbombEntityLoader;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.FlashbombInfluencer;
import com.koszelew.flashbomb.Utils.Networking.SingletonNetwork;
import com.koszelew.flashbomb.Utils.Other.InitApp;
import com.koszelew.flashbomb.Utils.Other.ServerTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class InfluencersTabRecyclerViewAdapter extends RecyclerView.Adapter<InfluencersTabRecyclerViewAdapter.ViewHolder> {

    /* Variables */
    private final MainActivity activity;
    private final Context context;
    private final ArrayList<FlashbombInfluencer> influencers;
    private int itemHeight = 0;
    public boolean fullRefresh = true;

    /* Constructor */
    public InfluencersTabRecyclerViewAdapter(MainActivity activity, ArrayList<FlashbombInfluencer> influencers, int itemHeight) {

        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.influencers = influencers;
        this.itemHeight = itemHeight;

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout joined_layout;
        private LinearLayout profile_layout;
        private RelativeLayout not_joined_layout;
        private TextView join_button;
        private ImageView avatar_joined, avatar_not_joined;
        private ImageView avatar_flashtime_overlay;
        private TextView nick_joined, nick_not_joined;
        private TextView group_size;
        private RelativeLayout gallery_layout;
        private TextView gallery_title;
        private ImageView gallery_thumbnail;
        private RelativeLayout flashtime_layout;
        private ImageView flashtime_icon;

        public ViewHolder(View itemView) {

            super(itemView);

            /* Finding views */
            this.joined_layout = itemView.findViewById(R.id.influencerItemJoinedLayout);
            this.profile_layout = itemView.findViewById(R.id.influencerItemProfileLayout);
            this.join_button = itemView.findViewById(R.id.influencerItemJoinButton);
            this.not_joined_layout = itemView.findViewById(R.id.influencerItemNotJoinedLayout);
            this.avatar_joined = itemView.findViewById(R.id.influencerItemProfileAvatarJoined);
            this.avatar_not_joined = itemView.findViewById(R.id.influencerItemAvatarNotJoined);
            this.avatar_flashtime_overlay = itemView.findViewById(R.id.influencerItemProfileAvatarFlashtimeOverlay);
            this.nick_joined = itemView.findViewById(R.id.influencerItemProfileNickJoined);
            this.nick_not_joined = itemView.findViewById(R.id.influencerItemNickNotJoined);
            this.group_size = itemView.findViewById(R.id.influencerItemProfileGroupSize);
            this.gallery_layout = itemView.findViewById(R.id.influencerItemGalleryLayout);
            this.gallery_title = itemView.findViewById(R.id.influencerGalleryTime);
            this.gallery_thumbnail = itemView.findViewById(R.id.influencerGalleryThumbnail);
            this.flashtime_layout = itemView.findViewById(R.id.influencerItemFlashtimeLayout);
            this.flashtime_icon = itemView.findViewById(R.id.influencerItemFlashtimeIcon);

        }
    }

    @Override
    public InfluencersTabRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.influencer_listview_item, parent, false);
        return new InfluencersTabRecyclerViewAdapter.ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        /* Setting ListView item height */
        holder.itemView.getLayoutParams().height = itemHeight;

        /* Setting influencer details */
        holder.nick_joined.setText(influencers.get(position).nick);
        holder.nick_joined.setTypeface(InitApp.clickablesTypeface);
        holder.nick_not_joined.setText(influencers.get(position).nick);
        holder.nick_not_joined.setTypeface(InitApp.headersTypeface);

        holder.group_size.setText(influencers.get(position).group_size + " " + activity.getString(R.string.influencer_members));
        holder.group_size.setTypeface(InitApp.contentsTypeface);

        holder.gallery_title.setTypeface(InitApp.headersTypeface);

        holder.join_button.setTypeface(InitApp.headersTypeface);
        holder.join_button.setOnClickListener(null);

        holder.joined_layout.setVisibility(View.GONE);
        holder.joined_layout.setOnClickListener(null);
        holder.profile_layout.setOnClickListener(null);
        holder.not_joined_layout.setVisibility(View.GONE);
        holder.gallery_layout.setVisibility(View.GONE);
        holder.flashtime_layout.setVisibility(View.GONE);
        holder.avatar_flashtime_overlay.setVisibility(View.GONE);

        /* Joined influencer element */
        if (influencers.get(position).group_joined.equals("1")) {

            FlashbombEntityLoader.getInstance(context).LoadImageFromUrl(influencers.get(position).avatar, holder.avatar_joined);
            FlashbombEntityLoader.getInstance(context).LoadImageFromUrl(influencers.get(position).session_thumbnail, holder.gallery_thumbnail);
            holder.itemView.getLayoutParams().height = itemHeight;

            /* Flashtime on */
            holder.joined_layout.setVisibility(View.VISIBLE);
            holder.flashtime_layout.setVisibility(View.VISIBLE);
            holder.flashtime_icon.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            holder.flashtime_icon.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.camera_flashtime));

            /* Flashtime timeleft counting */
            long timeleft = (Long.parseLong(influencers.get(position).flashtime_id) + Long.parseLong(influencers.get(position).flashtime_duration)) - ServerTime.getCurrentTimestamp();
            timeleft = (timeleft > 0) ? timeleft : 0;
            timeleft = (timeleft < Long.parseLong(influencers.get(position).flashtime_duration)) ? timeleft : Long.parseLong(influencers.get(position).flashtime_duration);
            timeleft = influencers.get(position).session_id.equals(influencers.get(position).flashtime_id) ? 0 : timeleft;

            /* Flashtime button callback */
            final long finalTimeleft = timeleft;
            holder.joined_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.OpenCamera(finalTimeleft, Long.parseLong(influencers.get(position).flashtime_duration), Long.parseLong(influencers.get(position).flashtime_id), "fan");
                }
            });

            /* Flashtime timeleft execution */
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    /* Flashtime off */
                    holder.flashtime_layout.setVisibility(View.GONE);
                    holder.flashtime_icon.clearAnimation();
                    holder.flashtime_icon.setLayerType(View.LAYER_TYPE_NONE, null);

                    holder.joined_layout.setVisibility(View.VISIBLE);
                    holder.gallery_layout.setVisibility(View.VISIBLE);

                    Calendar c = Calendar.getInstance();
                    c.setTime(influencers.get(position).session_id_date);
                    int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                    String weekDay =
                            dayOfWeek == 1 ? activity.getString(R.string.sunday) :
                                    dayOfWeek == 2 ? activity.getString(R.string.monday) :
                                            dayOfWeek == 3 ? activity.getString(R.string.tuesday) :
                                                    dayOfWeek == 4 ? activity.getString(R.string.wednesday) :
                                                            dayOfWeek == 5 ? activity.getString(R.string.thursday) :
                                                                    dayOfWeek == 6 ? activity.getString(R.string.friday) :
                                                                            dayOfWeek == 7 ? activity.getString(R.string.saturday) : "";
                    holder.gallery_title.setText(weekDay + "\n" + new SimpleDateFormat("HH:mm").format(influencers.get(position).session_id_date));

                    holder.itemView.getLayoutParams().height = itemHeight;

                    holder.joined_layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activity.OpenInfluencerGallery(influencers.get(position));
                        }
                    });

                }
            }, timeleft * 1000);

            /* Leaving group behaviour */
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
            String nick = sharedPref.getString(context.getString(R.string.PREFERENCES_LOGIN_KEY), "");
            if(!nick.equals(influencers.get(position).nick)) {
                holder.joined_layout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                        alertDialog.setTitle(context.getString(R.string.leave_influencer_confirmation_title));
                        alertDialog.setMessage(context.getString(R.string.leave_influencer_confirmation_message).replace("{1}", influencers.get(position).nick));
                        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SingletonNetwork.getInstance(context).LeaveInfluencer(influencers.get(position).id);
                            }

                        });
                        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface arg0) {
                                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(context.getResources().getColor(R.color.yellow_transparent_best));
                                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.red));
                                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.green));
                            }
                        });
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();

                        return true;

                    }
                });
            } else {
                /* Checking if Flashtime available */
                if (ServerTime.getCurrentTimestamp() >= Long.parseLong(influencers.get(position).next_flashtime_unlock_time)) {
                    holder.avatar_flashtime_overlay.setVisibility(View.VISIBLE);
                    holder.avatar_flashtime_overlay.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                    holder.avatar_flashtime_overlay.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.camera_flashtime));

                    holder.profile_layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            /* Launching influencer's flashtime camera */
                            activity.OpenCamera(-1, -1, -1, "influencer");
                        }
                    });
                }
            }

        } else {

            /* Not joined influencer element */
            FlashbombEntityLoader.getInstance(context).LoadImageFromUrl(influencers.get(position).avatar, holder.avatar_not_joined);
            holder.itemView.getLayoutParams().height = Math.round((float) itemHeight * 0.666f);
            holder.not_joined_layout.setVisibility(View.VISIBLE);
            holder.join_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                    alertDialog.setTitle(context.getString(R.string.join_influencer_confirmation_title));
                    alertDialog.setMessage(context.getString(R.string.join_influencer_confirmation_message).replace("{1}", influencers.get(position).nick));
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SingletonNetwork.getInstance(context).JoinInfluencer(influencers.get(position).id);
                        }

                    });
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(context.getResources().getColor(R.color.yellow_transparent_best));
                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.red));
                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.green));
                        }
                    });
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();

                }
            });

        }

        /* Fading in element */
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

    }

    @Override
    public int getItemCount() {
        return influencers.size();
    }

}