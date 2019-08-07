package com.koszelew.flashbomb.Utils.EntitiesProcessing;

/* Class representing single Flashbomb's picture */

import android.graphics.Color;

import com.koszelew.flashbomb.Utils.Networking.ResponseType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/* Class representing flashbomb entity (picture, video, event, profile...) */
public class FlashbombEntity {

    /* Public variables */
    public ResponseType srcResponse;
    public String id;
    public String points;
    public String nick;
    public String url;
    public String localPath;
    public String localPathThumbnail;
    public String countrycode;
    public String locality;
    public String countryEmoji;
    public String voted;
    public String session_id;
    public Date session_id_date;
    public String video;
    public String thumbnail;
    public String profile_thumbnail;
    public String best_state;

    public String observators;
    public String pictures_uploaded;

    public boolean is_title = false;

    /* Constructors */
    public FlashbombEntity(JSONObject picJSON, ResponseType type, boolean is_title) throws JSONException {

        this.srcResponse = type;
        this.is_title = is_title;

        if (picJSON != null) {

            /* Parsing JSON */
            this.id = picJSON.optString("id", "-1");
            this.nick = picJSON.optString("nick", "");
            this.url = picJSON.optString("url", "");
            this.points = picJSON.optString("points", "0");
            this.session_id_date = new Date(Long.parseLong(picJSON.optString("session_id", "0")) * 1000);
            this.session_id = picJSON.optString("session_id", "-1");
            this.countrycode = picJSON.optString("countrycode", "").toUpperCase();
            this.locality = picJSON.optString("locality", "");
            this.voted = picJSON.optString("voted", "0");
            this.video = picJSON.optString("video", "0");
            this.thumbnail = picJSON.optString("thumbnail", "");
            this.profile_thumbnail = picJSON.optString("profile_thumbnail", "");
            this.profile_thumbnail = this.profile_thumbnail.length() == 0 ? this.thumbnail : this.profile_thumbnail;
            this.best_state = picJSON.optString("best_state", "0");

            this.observators = picJSON.optString("observators", "0");
            this.pictures_uploaded = picJSON.optString("pictures_uploaded", "0");

            if(this.countrycode != null && this.countrycode.length() == 2 && !this.countrycode.equals("XX")) {

                this.countryEmoji = getFlagEmoji(this.countrycode);

            }

        }

    }
    public FlashbombEntity(ResponseType type, String session_id, String localPathThumbnail) {

        this.srcResponse = type;
        this.id = "-1";
        this.session_id = session_id;
        this.session_id_date = new Date(Long.parseLong(session_id) * 1000);
        this.localPathThumbnail = localPathThumbnail;
        this.localPath = localPathThumbnail.substring(0, localPathThumbnail.lastIndexOf("_thumbnail_")) + (localPathThumbnail.contains("video_thumbnail") ? ".mp4" : ".jpg");

    }

    /* Method returning range color depending on points */
    public int getRangeColor () {

        if (this.points == null)
            return Color.WHITE;

        if (Integer.parseInt(this.points) < 5)
            return Color.parseColor("#9679f7");
        if (Integer.parseInt(this.points) < 25)
            return Color.parseColor("#5bdcfb");
        if (Integer.parseInt(this.points) < 50)
            return Color.parseColor("#7af486");
        if (Integer.parseInt(this.points) < 100)
            return Color.parseColor("#ffec00");

        return Color.parseColor("#ff3300");

    }

    /* Method returning emoji name according to location */
    public static String getFlagEmoji(String countryCode) {

        if(countryCode != null && !countryCode.equals("XX") && !countryCode.equals("")) {
            countryCode = countryCode.toUpperCase();
            String firstChar = Integer.toHexString(Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6);
            String secondChar = Integer.toHexString(Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6);
            return "emojis/emoji_" + firstChar + "_" + secondChar + ".png";
        }
        return "emojis/emoji_country_unknown.png";

    }

}
