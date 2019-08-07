package com.koszelew.flashbomb.Utils.Networking;

import com.android.volley.toolbox.HurlStack;
import com.squareup.okhttp.OkUrlFactory;

/**
 * An HttpStack subclass
 * using OkHttp as transport layer.
 */

public class OkHttpStack extends HurlStack {

    private final OkUrlFactory mFactory;

    public OkHttpStack() {

        this(new com.squareup.okhttp.OkHttpClient());

    }

    public OkHttpStack(com.squareup.okhttp.OkHttpClient client) {

        if (client == null) {
            throw new NullPointerException("Null client.");
        }
        mFactory = new OkUrlFactory(client);

    }
}