package com.koszelew.flashbomb.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.FlashbombEntity;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.FlashbombEntityLoader;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.PhotoFilters;
import com.koszelew.flashbomb.Utils.Networking.SingletonNetwork;
import com.koszelew.flashbomb.Utils.Other.FlashbombToast;
import com.koszelew.flashbomb.Utils.Other.InitApp;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import net.gotev.uploadservice.UploadService;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ProfileGridViewAdapter extends ArrayAdapter {

    /* Variables */
    private final Activity activity;
    private final Context context;
    private final LayoutInflater inflater;
    private final ArrayList<FlashbombEntity> imageUrls;

    /* Latest downloaded entity nad views */
    ProgressBar downloadProgress = null;
    LinearLayout downloadInfoView = null;
    private FlashbombEntity downloadedEntity = null;

    /* Constructor */
    public ProfileGridViewAdapter(Activity activity, ArrayList<FlashbombEntity> imageUrls) {

        super(activity, R.layout.profile_gridview_item, imageUrls);

        this.activity = activity;
        this.context = activity;
        this.imageUrls = imageUrls;

        inflater = LayoutInflater.from(context);

    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.profile_gridview_item, parent, false);
        }

        /* Finding views */
        final RelativeLayout imageLayout = convertView.findViewById(R.id.profileGridViewItemImageLayout);
        final RelativeLayout bombsLayout = convertView.findViewById(R.id.profileGridViewItemBombsLayout);
        final RelativeLayout observatorsLayout = convertView.findViewById(R.id.profileGridViewItemObservatorsLayout);
        final RelativeLayout downloadLayout = convertView.findViewById(R.id.profileGridViewItemDownloadLayout);
        final LinearLayout offlineImageLayout = convertView.findViewById(R.id.profileGridViewItemImageOffline);
        final RelativeLayout onlineImageLayout = convertView.findViewById(R.id.profileGridViewItemImageOnline);
        final ImageView image = convertView.findViewById(R.id.profileGridViewItemImage);
        final ImageView heart = convertView.findViewById(R.id.profileGridViewItemImageHeart);
        final TextView points = convertView.findViewById(R.id.profileGridViewItemPoints);
        final ImageView reupload = convertView.findViewById(R.id.profileGridViewItemReupload);
        final ImageView download = convertView.findViewById(R.id.profileGridViewDownloadIcon);
        final ImageView alert = convertView.findViewById(R.id.profileGridViewAlertIcon);

        /* Hiding everything at start */
        imageLayout.setVisibility(View.GONE);
        bombsLayout.setVisibility(View.GONE);
        observatorsLayout.setVisibility(View.GONE);
        downloadLayout.setVisibility(View.GONE);
        onlineImageLayout.setVisibility(View.VISIBLE);
        offlineImageLayout.setVisibility(View.VISIBLE);

        /* Setting layout depending on position */
        if(imageUrls.size() > position) {
            try {

                /* First position - observators count */
                if (position == 0) {

                    ((TextView) convertView.findViewById(R.id.profileGridViewObservatorsCount)).setText(imageUrls.get(position).observators);
                    ((TextView) convertView.findViewById(R.id.profileGridViewObservatorsCount)).setTypeface(InitApp.contentsTypeface);
                    observatorsLayout.setVisibility(View.VISIBLE);

                /* Second position - images uploaded */
                } else if (position == 1) {

                    ((TextView) convertView.findViewById(R.id.profileGridViewBombsCount)).setText(imageUrls.get(position).pictures_uploaded);
                    ((TextView) convertView.findViewById(R.id.profileGridViewBombsCount)).setTypeface(InitApp.contentsTypeface);
                    bombsLayout.setVisibility(View.VISIBLE);

                /* Third and odd positions - images descriptions */
                } else if (position % 2 == 0) {

                    String time = new SimpleDateFormat("HH:mm").format(imageUrls.get(position).session_id_date);
                    String date = new SimpleDateFormat("dd/MM/yyyy").format(imageUrls.get(position).session_id_date);
                    ((TextView) convertView.findViewById(R.id.profileGridViewDownloadDate)).setText(time + "\n" + date);
                    ((TextView) convertView.findViewById(R.id.profileGridViewDownloadDate)).setTypeface(InitApp.contentsTypeface);

                    final ProgressBar dynamicDownloadProgress = convertView.findViewById(R.id.profileGridViewDownloadProgress);
                    final LinearLayout dynamicDownloadInfoView = convertView.findViewById(R.id.profileGridViewDownloadInfo);

                    if(imageUrls.get(position).url != null) {
                        downloadLayout.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                downloadedEntity = imageUrls.get(position);
                                downloadProgress = dynamicDownloadProgress;
                                downloadInfoView = dynamicDownloadInfoView;
                                DownloadEntity();
                            }
                        });
                    } else {
                        downloadLayout.setOnClickListener(null);
                    }
                    download.setVisibility(imageUrls.get(position).url != null ? View.VISIBLE : View.GONE);
                    alert.setVisibility(imageUrls.get(position).url != null ? View.GONE : View.VISIBLE);
                    downloadLayout.setVisibility(View.VISIBLE);

                /* Fourth and even positions - image thumbnails */
                } else {

                    if (imageUrls.get(position).profile_thumbnail != null) {

                        FlashbombEntityLoader.getInstance(context).LoadImageFromUrl(imageUrls.get(position).profile_thumbnail, image);
                        points.setText(imageUrls.get(position).points);
                        points.setTypeface(InitApp.headersTypeface);
                        heart.setColorFilter(new PorterDuffColorFilter(imageUrls.get(position).getRangeColor(), PorterDuff.Mode.MULTIPLY));

                        offlineImageLayout.setOnClickListener(null);

                    } else if (imageUrls.get(position).localPathThumbnail != null) {

                        FlashbombEntityLoader.getInstance(context).LoadImageFromFile(new File(imageUrls.get(position).localPathThumbnail), image);

                        offlineImageLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (UploadService.getTaskList().size() == 0) {
                                    String localPath = imageUrls.get(position).localPathThumbnail;
                                    String[] params = localPath.substring(localPath.lastIndexOf("thumbnail"), localPath.lastIndexOf(".")).replace("thumbnail_", "").split("_");
                                    if(localPath.contains("video_thumbnail"))
                                        SingletonNetwork.getInstance(context).UploadVideo(imageUrls.get(position).localPath, "reupload", params, params[1]);
                                    else
                                        SingletonNetwork.getInstance(context).UploadPhoto(imageUrls.get(position).localPath, "reupload", params, params[1]);
                                }
                                ImageView profileButton = activity.findViewById(R.id.profileButton);
                                if(profileButton != null) {
                                    profileButton.setImageResource(R.drawable.ic_profile);
                                }
                                FlashbombToast.ShowInfo(activity, activity.getString(R.string.reupload_during_upload), 2000);
                            }
                        });

                    }

                    offlineImageLayout.setVisibility(imageUrls.get(position).profile_thumbnail == null ? View.VISIBLE : View.GONE);
                    onlineImageLayout.setVisibility(imageUrls.get(position).profile_thumbnail == null ? View.GONE : View.VISIBLE);
                    imageLayout.setVisibility(View.VISIBLE);
                }

            } catch (Exception e) {
                Log.e("Flashbomb-Log", "catch", e);
                Crashlytics.logException(e);
            }
        }

        return convertView;
    }

    /* Method to download image/video */
    public void DownloadEntity() {

        if(downloadProgress == null || downloadInfoView == null || downloadedEntity == null)
            return;

        if (InitApp.RequestLackingPermissions(activity, activity.getResources().getInteger(R.integer.EXTERNAL_SAVE_PERMISSIONS_REQUEST_CODE)))
            return;

        downloadProgress.setVisibility(View.VISIBLE);
        downloadInfoView.setVisibility(View.GONE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Flashbomb/" + "Moment" + downloadedEntity.id + (downloadedEntity.video.equals("1") ? ".mp4" : ".jpg");
                FileDownloader.setup(context);
                FileDownloader.getImpl().create(downloadedEntity.url)
                        .setPath(downloadPath)
                        .setListener(new FileDownloadListener() {
                            @Override
                            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                            }

                            @Override
                            protected void started(BaseDownloadTask task) {
                                downloadProgress.setIndeterminate(false);
                                downloadProgress.setProgress(0);
                            }

                            @Override
                            protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                            }

                            @Override
                            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                                downloadProgress.setProgress(Math.round(100 * (float) soFarBytes / (float) totalBytes));
                            }

                            @Override
                            protected void blockComplete(BaseDownloadTask task) {
                            }

                            @Override
                            protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                            }

                            @Override
                            protected void completed(BaseDownloadTask task) {
                                if (downloadedEntity.video.equals("0")) {
                                    new AsyncWatermarkApplier().execute(downloadPath);
                                } else {
                                    MediaScannerConnection.scanFile(context, new String[]{downloadPath}, new String[]{"video/mp4"}, null);
                                }
                                downloadProgress.setVisibility(View.GONE);
                                downloadInfoView.setVisibility(View.VISIBLE);
                                FlashbombToast.ShowInfo(activity, context.getString(R.string.download_complete_info), 1000);
                            }

                            @Override
                            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                            }

                            @Override
                            protected void error(BaseDownloadTask task, Throwable e) {
                                downloadProgress.setVisibility(View.GONE);
                                downloadInfoView.setVisibility(View.VISIBLE);
                                FlashbombToast.ShowError(activity, context.getString(R.string.download_error_info), 1000);
                                e.printStackTrace();
                                Crashlytics.logException(e);
                            }

                            @Override
                            protected void warn(BaseDownloadTask task) {
                            }
                        }).start();
            }
        }, 750);

    }

    /* AsyncTask to process watermark */
    private class AsyncWatermarkApplier extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(params[0], options);
                bitmap = PhotoFilters.applyFlashbombWatermark(bitmap, context);
                FileOutputStream out = new FileOutputStream(params[0]);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                bitmap.recycle();
                System.gc();
            } catch (Exception e) {
                Log.e("Flashbomb-Log", "catch", e);
                Crashlytics.logException(e);
            }

            MediaScannerConnection.scanFile(context, new String[]{params[0]}, new String[]{"image/jpg"}, null);

            return null;

        }

        @Override
        protected void onPostExecute(Void result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

    }

}