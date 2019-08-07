package com.koszelew.flashbomb.Utils.Other;

public class ServerTime {

    private static long downloadedServerTimestamp = 0L;
    private static long downloadTimestamp = 0L;

    public static long getCurrentTimestamp() {

        return (Math.round((double) System.currentTimeMillis() / 1000));
        //return downloadedServerTimestamp + (Math.round((double) System.currentTimeMillis() / 1000)) - downloadTimestamp;

    }

    public static void setServerTimestamp(String downloadedServerTs) {

        downloadTimestamp = Math.round((double) System.currentTimeMillis() / 1000);
        downloadedServerTimestamp = Long.parseLong(downloadedServerTs);

    }

}
