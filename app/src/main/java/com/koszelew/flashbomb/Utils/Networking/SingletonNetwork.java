package com.koszelew.flashbomb.Utils.Networking;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.koszelew.flashbomb.Activities.MainActivity;
import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.FlashbombEntity;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.FlashbombInfluencer;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.SingletonFlashbombEntities;
import com.onesignal.OneSignal;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static net.gotev.uploadservice.Placeholders.PROGRESS;

/* Class implementing singleton Volley requests */
public class SingletonNetwork {

    /* Class instance */
    private static SingletonNetwork mInstance;
    /* Context variable */
    private static Context mCtx;
    /* Current ApiParser instance */
    private ApiParser parser;
    /* RequestQueue */
    private RequestQueue mRequestQueue;

    /* Constructor */
    private SingletonNetwork(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
        this.parser = ApiParser.getInstance(context);
    }

    /* GetInstance method */
    public static synchronized SingletonNetwork getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SingletonNetwork(context);
        }
        return mInstance;
    }

    /* GetRequestQueue method */
    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext(), new OkHttpStack());
        }
        return mRequestQueue;
    }

    /* Adding to request queue (sending request) */
    private <T> void addToRequestQueue(Request<T> req) {

        /* Log */
        Log.d("Flashbomb-Log", "[" + this.getClass().getSimpleName() + "] " + "addToRequestQueue: " + req.getUrl());

        getRequestQueue().add(req);

    }

    /* Request sending method */
    private void MakeRequest(final String url, final ResponseType responseType) {

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        parser.HandleResponse(responseType, response, false);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        parser.HandleError(responseType, error);
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                HashMap<String, String> headers = new HashMap<>();

                if (url.indexOf('?') < 0)
                    return headers;

                Long timestampLong = Math.round((double) System.currentTimeMillis() / 1000);
                String timestampString = timestampLong.toString();
                int timestampLastDigit = Integer.parseInt(timestampString.substring(timestampString.length() - 1));
                String urlParams = url.substring(url.indexOf('?')) + timestampString;
                for (int i = 0; i < timestampLastDigit; i++) {
                    urlParams = SHA1(urlParams);
                }

                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Basic " + Base64.encodeToString("JMNvAij5NUwmoXvB:OhXxzRy1zucaAIjV".getBytes(), Base64.NO_WRAP));
                headers.put("Accept", "application/json");
                headers.put("User-Agent", "Flashbomb-Android");
                headers.put("timestamp", timestampString);
                headers.put("checksum", urlParams);

                return headers;

            }
        };

        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 4, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        addToRequestQueue(jsObjRequest);

    }

    /* Updating home tab */
    public void UpdateHomeTab() {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        ArrayList<FlashbombEntity> pics = SingletonFlashbombEntities.getInstance(mCtx).getHomeTabListViewArray();
        String ignoredIDs = "";
        for (FlashbombEntity pic : pics) {
            if (!pic.id.equals("-1"))
                ignoredIDs = ignoredIDs + pic.id + "_";
        }

        int count = mCtx.getResources().getInteger(R.integer.HOME_LV_BATCH_SIZE);
        String url = mCtx.getString(R.string.API_URL) + "Home/homeTab.php?NICK=" + nick + "&PASSWORD=" + password + "&COUNT=" + count + "&IGNORE_PICTURE_IDS=" + ignoredIDs;
        MakeRequest(url, ResponseType.HOME_TAB);
    }

    /* Updating observations tab */
    public void UpdateObservationsTab() {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        ArrayList<FlashbombEntity> pics = SingletonFlashbombEntities.getInstance(mCtx).getObservationsTabListViewArray();
        String ignoredIDs = "";
        for (FlashbombEntity pic : pics) {
            if (!pic.id.equals("-1"))
                ignoredIDs = ignoredIDs + pic.id + "_";
        }

        int count = mCtx.getResources().getInteger(R.integer.OBSERVATIONS_LV_BATCH_SIZE);
        String url = mCtx.getString(R.string.API_URL) + "Observations/observationsTab.php?NICK=" + nick + "&PASSWORD=" + password + "&COUNT=" + count + "&IGNORE_PICTURE_IDS=" + ignoredIDs;
        MakeRequest(url, ResponseType.OBSERVATIONS_TAB);
    }

    /* Updating best tab */
    public void UpdateBestTab() {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        ArrayList<FlashbombEntity> pics = SingletonFlashbombEntities.getInstance(mCtx).getBestTabListViewArray();
        String latestSessionID = pics.size() > 0 ? pics.get(pics.size() - 1).session_id : "";

        int count = mCtx.getResources().getInteger(R.integer.BEST_LV_BATCH_SIZE);
        String url = mCtx.getString(R.string.API_URL) + "Best/bestTab.php?NICK=" + nick + "&PASSWORD=" + password + "&COUNT=" + count + "&LATEST_SESSION_ID=" + latestSessionID;
        MakeRequest(url, ResponseType.BEST_TAB);
    }

    /* Updating influencers tab */
    public void UpdateInfluencersTab() {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        ArrayList<FlashbombInfluencer> influencers = SingletonFlashbombEntities.getInstance(mCtx).getInfluencersTabListViewArray();
        String ignoredIDs = "";
        for (FlashbombInfluencer influencer : influencers) {
            if (!influencer.id.equals("-1"))
                ignoredIDs = ignoredIDs + influencer.id + "_";
        }

        int count = mCtx.getResources().getInteger(R.integer.INFLUENCERS_LV_BATCH_SIZE);
        String url = mCtx.getString(R.string.API_URL) + "Influencers/getInfluencers.php?NICK=" + nick + "&PASSWORD=" + password + "&COUNT=" + count + "&IGNORE_INFLUENCER_IDS=" + ignoredIDs;
        MakeRequest(url, ResponseType.INFLUENCERS_TAB);
    }

    /* Updating profile tab */
    public void UpdateProfileTab() {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        ArrayList<FlashbombEntity> pics = SingletonFlashbombEntities.getInstance(mCtx).getProfileTabListViewArray();
        String latestSessionID = pics.size() > 0 ? pics.get(pics.size() - 1).session_id : "";

        int count = mCtx.getResources().getInteger(R.integer.PROFILE_LV_BATCH_SIZE);
        String url = mCtx.getString(R.string.API_URL) + "Profile/profile.php?NICK=" + nick + "&PASSWORD=" + password + "&COUNT=" + count + "&LATEST_SESSION_ID=" + latestSessionID;
        MakeRequest(url, ResponseType.PROFILE_TAB);
    }

    /* Reveal best */
    public void RevealBest(String picID) {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        String url = mCtx.getString(R.string.API_URL) + "Best/reveal.php?NICK=" + nick + "&PASSWORD=" + password + "&PICTURE_ID=" + picID;
        MakeRequest(url, ResponseType.REVEAL_BEST);
    }

    /* Vote pic */
    public void Vote(String picID) {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        String url = mCtx.getString(R.string.API_URL) + "Home/vote.php?NICK=" + nick + "&PASSWORD=" + password + "&PICTURE_ID=" + picID;
        MakeRequest(url, ResponseType.VOTE);
    }

    /* Unvote pic */
    public void Unvote(String picID) {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        String url = mCtx.getString(R.string.API_URL) + "Home/unvote.php?NICK=" + nick + "&PASSWORD=" + password + "&PICTURE_ID=" + picID;
        MakeRequest(url, ResponseType.UNVOTE);
    }

    /* Join influencer group */
    public void JoinInfluencer(String influencerID) {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        String url = mCtx.getString(R.string.API_URL) + "Influencers/joinGroup.php?NICK=" + nick + "&PASSWORD=" + password + "&INFLUENCER=" + influencerID;
        MakeRequest(url, ResponseType.JOIN_INFLUENCER);
    }

    /* Leave influencer group */
    public void LeaveInfluencer(String influencerID) {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        String url = mCtx.getString(R.string.API_URL) + "Influencers/leaveGroup.php?NICK=" + nick + "&PASSWORD=" + password + "&INFLUENCER=" + influencerID;
        MakeRequest(url, ResponseType.LEAVE_INFLUENCER);
    }

    /* Updating influencer gallery */
    public void UpdateInfluencerGallery(String influencerID) {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        ArrayList<FlashbombEntity> pics = SingletonFlashbombEntities.getInstance(mCtx).getInfluencerGalleryListViewArray();
        String ignoredIDs = "";
        for (FlashbombEntity pic : pics) {
            if (!pic.id.equals("-1"))
                ignoredIDs = ignoredIDs + pic.id + "_";
        }

        int count = mCtx.getResources().getInteger(R.integer.INFLUENCERS_LV_BATCH_SIZE);
        String url = mCtx.getString(R.string.API_URL) + "Influencers/getInfluencerGallery.php?NICK=" + nick + "&PASSWORD=" + password + "&INFLUENCER=" + influencerID + "&COUNT=" + count + "&IGNORE_PICTURE_IDS=" + ignoredIDs;
        MakeRequest(url, ResponseType.INFLUENCER_GALLERY);
    }

    /* Report pic */
    public void Report(String picID, int reportType) {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        String url = mCtx.getString(R.string.API_URL) + "Utils/report.php?NICK=" + nick + "&PASSWORD=" + password + "&PICTURE_ID=" + picID + "&REPORT_TYPE=" + reportType;
        MakeRequest(url, ResponseType.REPORT);
    }

    /* Get observations */
    public void UpdateObservations() {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        String url = mCtx.getString(R.string.API_URL) + "Observations/getObservations.php?NICK=" + nick + "&PASSWORD=" + password;
        MakeRequest(url, ResponseType.GET_OBSERVATIONS);
    }

    /* Get blacklist */
    public void UpdateBlacklist() {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        String url = mCtx.getString(R.string.API_URL) + "Observations/getBlacklist.php?NICK=" + nick + "&PASSWORD=" + password;
        MakeRequest(url, ResponseType.GET_BLACKLIST);
    }

    /* Observe user */
    public void Observe(String userID) {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        String url = mCtx.getString(R.string.API_URL) + "Observations/observe.php?NICK=" + nick + "&PASSWORD=" + password + "&OBSERVATION=" + userID;
        MakeRequest(url, ResponseType.OBSERVE);
    }

    /* Unobserve user */
    public void Unobserve(String userID) {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        String url = mCtx.getString(R.string.API_URL) + "Observations/unobserve.php?NICK=" + nick + "&PASSWORD=" + password + "&OBSERVATION=" + userID;
        MakeRequest(url, ResponseType.UNOBSERVE);
    }

    /* Hide from user */
    public void Hide(String userID) {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        String url = mCtx.getString(R.string.API_URL) + "Observations/hide.php?NICK=" + nick + "&PASSWORD=" + password + "&OBSERVATOR=" + userID;
        MakeRequest(url, ResponseType.HIDE);
    }

    /* Unhide from user */
    public void Unhide(String userID) {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        String url = mCtx.getString(R.string.API_URL) + "Observations/unhide.php?NICK=" + nick + "&PASSWORD=" + password + "&OBSERVATOR=" + userID;
        MakeRequest(url, ResponseType.UNHIDE);
    }

    /* Become anonymous in best gallery */
    public void BestAnonymous() {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        String url = mCtx.getString(R.string.API_URL) + "Best/setAnonymous.php?NICK=" + nick + "&PASSWORD=" + password;
        MakeRequest(url, ResponseType.SET_ANONYMOUS);
    }

    /* Become non anonymous in best gallery */
    public void BestNonAnonymous() {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        String url = mCtx.getString(R.string.API_URL) + "Best/setNonAnonymous.php?NICK=" + nick + "&PASSWORD=" + password;
        MakeRequest(url, ResponseType.SET_NON_ANONYMOUS);
    }

    /* Search query (observations) */
    public void SearchObservationsQuery(String phrase) {
        String url = mCtx.getString(R.string.API_URL) + "Observations/friendsSearch.php?PHRASE=" + phrase;
        MakeRequest(url, ResponseType.SEARCH_OBSERVATIONS);
    }

    /* Logging in */
    public void LogIn(String nick, String password) {
        String platform = "";
        String device = "";
        String system = "";
        try {
            platform = URLEncoder.encode("Android", "utf-8");
            device = URLEncoder.encode(getDeviceName(), "utf-8");
            system = URLEncoder.encode(getAndroidVersion(), "utf-8");
        } catch (Exception e) {
            Log.e("Flashbomb-Log", "catch", e);
            Crashlytics.logException(e);
        }

        String notificationId = OneSignal.getPermissionSubscriptionState().getSubscriptionStatus().getUserId();

        String url = mCtx.getString(R.string.API_URL) + "Login_Register/login.php?NICK=" + nick + "&PASSWORD=" + password + "&PLATFORM=" + platform + "&DEVICE=" + device + "&SYSTEM=" + system + "&NOTIFICATION_ID=" + notificationId;
        MakeRequest(url, ResponseType.LOGIN);
    }

    /* Password reminder */
    public void ForgotPassword(String nick) {
        String url = mCtx.getString(R.string.API_URL) + "Login_Register/forgotPassword.php?NICK=" + nick;
        MakeRequest(url, ResponseType.FORGOT_PASSWORD);
    }

    /* Password change */
    public void ChangePassword(String newPassword) {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        String url = mCtx.getString(R.string.API_URL) + "Login_Register/changePassword.php?NICK=" + nick + "&PASSWORD=" + password + "&NEW_PASSWORD=" + newPassword;
        MakeRequest(url, ResponseType.CHANGE_PASSWORD);
    }

    /* Registering */
    public void Register(String nick, String password, String email, String birthyear) {
        String url = mCtx.getString(R.string.API_URL) + "Login_Register/register.php?NICK=" + nick + "&PASSWORD=" + password + "&EMAIL=" + email + "&BIRTHYEAR=" + birthyear;
        MakeRequest(url, ResponseType.REGISTER);
    }

    /* Resending activation link */
    public void ResendActivation() {
        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String password = sharedPref.getString(mCtx.getString(R.string.PREFERENCES_PASSWORD_KEY), "");

        String url = mCtx.getString(R.string.API_URL) + "Login_Register/resendActivation.php?NICK=" + nick + "&PASSWORD=" + password;
        MakeRequest(url, ResponseType.RESEND_ACTIVATION);
    }

    /* Upload photo */
    public void UploadPhoto(final String photoPath, final String comment, final String[] overrideParams, final String overrideSessionId) {

        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = overrideParams.length > 0 ? overrideParams[0].replace("+", ".").replace("<u>", "_") : sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String session_id = overrideParams.length > 1 ? overrideParams[1].replace("+", ".").replace("<u>", "_") : sharedPref.getString(mCtx.getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_KEY), "");
        session_id = overrideSessionId.length() > 0 ? overrideSessionId : session_id;
        String countrycode = overrideParams.length > 2 ? overrideParams[2].replace("+", ".").replace("<u>", "_") : sharedPref.getString(mCtx.getString(R.string.PREFERENCES_COUNTRYCODE_KEY), "XX");
        String locality = overrideParams.length > 3 ? overrideParams[3].replace("+", ".").replace("<u>", "_") : sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOCALITY_KEY), "");
        String latitude = overrideParams.length > 4 ? overrideParams[4].replace("+", ".").replace("<u>", "_") : sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LATITUDE_KEY), "");
        String longitude = overrideParams.length > 5 ? overrideParams[5].replace("+", ".").replace("<u>", "_") : sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LONGITUDE_KEY), "");

        try {
            locality = URLEncoder.encode(locality, "utf-8");
        } catch (Exception e) {
            Log.e("Flashbomb-Log", "catch", e);
            Crashlytics.logException(e);
        }

        if (!comment.equals("reupload")) {
            try {
                File image = new File(photoPath);
                Bitmap thumbnail = BitmapFactory.decodeFile(image.getAbsolutePath());
                thumbnail = Bitmap.createScaledBitmap(thumbnail, 200, Math.round((float) thumbnail.getHeight() * (200f / (float) thumbnail.getWidth())), true);
                File output = new File(mCtx.getFilesDir() +
                        "/Flashbomb/image_thumbnail" + "_" +
                        (
                                        nick.replace("_", "<u>") + "_" +
                                        session_id.replace("_", "<u>") + "_" +
                                        countrycode.replace("_", "<u>") + "_" +
                                        locality.replace("_", "<u>") + "_" +
                                        latitude.replace("_", "<u>") + "_" +
                                        longitude.replace("_", "<u>")
                        ).replace(".", "+") +
                        ".jpg"
                );
                FileOutputStream out = new FileOutputStream(output);
                thumbnail.compress(Bitmap.CompressFormat.JPEG, mCtx.getResources().getInteger(R.integer.JPG_QUALITY), out);
                out.flush();
                out.close();
                thumbnail.recycle();
                System.gc();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            String uploadId =
                    new MultipartUploadRequest(mCtx, mCtx.getString(R.string.CLOUDINARY_UPLOAD_URL))
                            .addFileToUpload(photoPath, "file")
                            .addParameter("upload_preset", mCtx.getString(R.string.CLOUDINARY_IMAGES_PRESET))
                            .addParameter("tags",
                                    "type:REGULAR,nick:" + nick +
                                            ",session_id:" + session_id +
                                            ",countrycode:" + countrycode +
                                            ",locality:" + locality +
                                            ",comment:" + comment.replace(",", "_") +
                                            ",latitude:" + latitude +
                                            ",longitude:" + longitude)
                            .setNotificationConfig(GetUploadNotificationConfig())
                            .setAutoDeleteFilesAfterSuccessfulUpload(false)
                            .setMaxRetries(mCtx.getResources().getInteger(R.integer.UPLOAD_MAX_RETRIES))
                            .startUpload();
        } catch (Exception e) {
            Log.e("Flashbomb-Log", "catch", e);
            Crashlytics.logException(e);
        }

    }

    /* Upload video */
    public void UploadVideo(final String videoPath, final String comment, final String[] overrideParams, final String overrideSessionId) {

        SharedPreferences sharedPref = mCtx.getSharedPreferences(mCtx.getString(R.string.PREFERENCES_TAG), Context.MODE_PRIVATE);
        String nick = overrideParams.length > 0 ? overrideParams[0].replace("+", ".").replace("<u>", "_") : sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOGIN_KEY), "");
        String session_id = overrideParams.length > 1 ? overrideParams[1].replace("+", ".").replace("<u>", "_") : sharedPref.getString(mCtx.getString(R.string.PREFERENCES_ACTIVATED_SESSION_ID_KEY), "");
        session_id = overrideSessionId.length() > 0 ? overrideSessionId : session_id;
        String countrycode = overrideParams.length > 2 ? overrideParams[2].replace("+", ".").replace("<u>", "_") : sharedPref.getString(mCtx.getString(R.string.PREFERENCES_COUNTRYCODE_KEY), "XX");
        String locality = overrideParams.length > 3 ? overrideParams[3].replace("+", ".").replace("<u>", "_") : sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LOCALITY_KEY), "");
        String latitude = overrideParams.length > 4 ? overrideParams[4].replace("+", ".").replace("<u>", "_") : sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LATITUDE_KEY), "");
        String longitude = overrideParams.length > 5 ? overrideParams[5].replace("+", ".").replace("<u>", "_") : sharedPref.getString(mCtx.getString(R.string.PREFERENCES_LONGITUDE_KEY), "");

        try {
            locality = URLEncoder.encode(locality, "utf-8");
        } catch (Exception e) {
            Log.e("Flashbomb-Log", "catch", e);
            Crashlytics.logException(e);
        }

        if (!comment.equals("reupload")) {
            try {
                Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND);
                thumbnail = Bitmap.createScaledBitmap(thumbnail, 200, Math.round((float) thumbnail.getHeight() * (200f / (float) thumbnail.getWidth())), true);
                File output = new File(mCtx.getFilesDir() +
                        "/Flashbomb/video_thumbnail" + "_" +
                        (
                                        nick.replace("_", "<u>") + "_" +
                                        session_id.replace("_", "<u>") + "_" +
                                        countrycode.replace("_", "<u>") + "_" +
                                        locality.replace("_", "<u>") + "_" +
                                        latitude.replace("_", "<u>") + "_" +
                                        longitude.replace("_", "<u>")
                        ).replace(".", "+") +
                        ".jpg"
                );
                FileOutputStream out = new FileOutputStream(output);
                thumbnail.compress(Bitmap.CompressFormat.JPEG, mCtx.getResources().getInteger(R.integer.JPG_QUALITY), out);
                out.flush();
                out.close();
                thumbnail.recycle();
                System.gc();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            String uploadId =
                    new MultipartUploadRequest(mCtx, mCtx.getString(R.string.CLOUDINARY_UPLOAD_URL))
                            .addFileToUpload(videoPath, "file")
                            .addParameter("upload_preset", mCtx.getString(R.string.CLOUDINARY_VIDEOS_PRESET))
                            .addParameter("tags",
                                    "type:REGULAR,nick:" + nick +
                                            ",session_id:" + session_id +
                                            ",countrycode:" + countrycode +
                                            ",locality:" + locality +
                                            ",comment:" + comment.replace(",", "_") +
                                            ",latitude:" + latitude +
                                            ",longitude:" + longitude)
                            .setNotificationConfig(GetUploadNotificationConfig())
                            .setAutoDeleteFilesAfterSuccessfulUpload(false)
                            .setMaxRetries(mCtx.getResources().getInteger(R.integer.UPLOAD_MAX_RETRIES))
                            .startUpload();
        } catch (Exception e) {
            Log.e("Flashbomb-Log", "catch", e);
            Crashlytics.logException(e);
        }

    }

    /* Get upload notification config */
    private UploadNotificationConfig GetUploadNotificationConfig() {

        UploadNotificationConfig notifConfig = new UploadNotificationConfig();

        PendingIntent clickIntent = PendingIntent.getActivity(mCtx, 1, new Intent(mCtx, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        notifConfig
                .setTitleForAllStatuses(mCtx.getResources().getString(R.string.app_name))
                .setRingToneEnabled(true)
                .setClickIntentForAllStatuses(clickIntent)
                .setClearOnActionForAllStatuses(true);

        notifConfig.getProgress().message = mCtx.getResources().getString(R.string.upload_progress) + " (" + PROGRESS + ")";
        notifConfig.getProgress().iconColorResourceID = mCtx.getResources().getColor(R.color.black);
        notifConfig.getProgress().largeIcon = BitmapFactory.decodeResource(mCtx.getResources(), R.drawable.ic_onesignal_large_icon_default);

        notifConfig.getCompleted().message = mCtx.getResources().getString(R.string.upload_success);
        notifConfig.getCompleted().iconColorResourceID = mCtx.getResources().getColor(R.color.black);
        notifConfig.getCompleted().largeIcon = BitmapFactory.decodeResource(mCtx.getResources(), R.drawable.ic_onesignal_large_icon_ok);

        notifConfig.getError().message = mCtx.getResources().getString(R.string.upload_error);
        notifConfig.getError().iconColorResourceID = mCtx.getResources().getColor(R.color.red_blacklist);
        notifConfig.getError().largeIcon = BitmapFactory.decodeResource(mCtx.getResources(), R.drawable.ic_onesignal_large_icon_fail);

        notifConfig.getCancelled().message = mCtx.getResources().getString(R.string.upload_cancelled);
        notifConfig.getCancelled().iconColorResourceID = mCtx.getResources().getColor(R.color.red_blacklist);
        notifConfig.getCancelled().largeIcon = BitmapFactory.decodeResource(mCtx.getResources(), R.drawable.ic_onesignal_large_icon_fail);

        return notifConfig;

    }

    /* SHA1 enryption */
    public static String SHA1(String text) {

        MessageDigest md;
        byte[] textBytes;
        byte[] sha1hash = new byte[0];
        try {

            md = MessageDigest.getInstance("SHA-1");
            textBytes = text.getBytes("iso-8859-1");
            md.update(textBytes, 0, textBytes.length);
            sha1hash = md.digest();

        } catch (Exception e) {
            Log.e("Flashbomb-Log", "catch", e);
            Crashlytics.logException(e);
        }

        return convertToHex(sha1hash);
    }

    /* SHA256 enryption */
    public static String SHA256(String text) {

        MessageDigest md;
        byte[] textBytes;
        byte[] sha1hash = new byte[0];
        try {

            md = MessageDigest.getInstance("SHA-256");
            textBytes = text.getBytes("iso-8859-1");
            md.update(textBytes, 0, textBytes.length);
            sha1hash = md.digest();

        } catch (Exception e) {
            Log.e("Flashbomb-Log", "catch", e);
            Crashlytics.logException(e);
        }

        return convertToHex(sha1hash);
    }

    private static String convertToHex(byte[] data) {

        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();

    }

    /* Getting device info methods */
    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private static String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "SDK " + sdkVersion + " (" + release + ")";
    }

}