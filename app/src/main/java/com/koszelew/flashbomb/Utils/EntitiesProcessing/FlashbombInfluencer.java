package com.koszelew.flashbomb.Utils.EntitiesProcessing;

/* Class representing single Flashbomb's influencer */

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/* Class representing flashbomb influencer */
public class FlashbombInfluencer {

    /* Public variables */
    public String id;
    public String nick;
    public String avatar;
    public String group_size;
    public String session_id;
    public Date session_id_date;
    public String session_message;
    public String session_thumbnail;
    public String next_flashtime_unlock_time;
    public Date next_flashtime_unlock_time_date;
    public String flashtime_id;
    public Date flashtime_id_date;
    public String flashtime_duration;
    public String group_joined;

    /* Constructors */
    public FlashbombInfluencer(JSONObject infJSON) throws JSONException {

        if (infJSON != null) {

            /* Parsing JSON */
            this.id = infJSON.optString("id", "-1");
            this.nick = infJSON.optString("nick", "");
            this.avatar = infJSON.optString("avatar", "");
            this.group_size = infJSON.optString("group_size", "");
            this.session_id = infJSON.optString("session_id", "");
            this.session_id_date = new Date(Long.parseLong(infJSON.optString("session_id", "0")) * 1000);
            this.session_message = infJSON.optString("session_message", "");
            this.session_thumbnail = infJSON.optString("session_thumbnail", "");
            this.next_flashtime_unlock_time = infJSON.optString("next_flashtime_unlock_time", "");
            this.next_flashtime_unlock_time_date = new Date(Long.parseLong(infJSON.optString("next_flashtime_unlock_time", "0")) * 1000);
            this.flashtime_id = infJSON.optString("flashtime_id", "");
            this.flashtime_id_date = new Date(Long.parseLong(infJSON.optString("flashtime_id", "0")) * 1000);
            this.flashtime_duration = infJSON.optString("flashtime_duration", "");
            this.group_joined = infJSON.optString("group_joined", "");

        }

    }

}
