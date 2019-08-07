package com.koszelew.flashbomb.Utils.EntitiesProcessing;

import android.content.Context;
import android.support.v4.util.Pair;

import java.util.ArrayList;

/* Class implementing FlashbombPictures arrays */
public class SingletonFlashbombEntities {

    /* Class instance */
    private static SingletonFlashbombEntities mInstance;

    /* Context variable */
    private static Context mCtx;

    /* Downloaded data arrays */
    private final ArrayList<FlashbombEntity> HomeTabListViewArray = new ArrayList<FlashbombEntity>();
    private final ArrayList<FlashbombEntity> BestTabListViewArray = new ArrayList<FlashbombEntity>();
    private final ArrayList<FlashbombEntity> ObservationsTabListViewArray = new ArrayList<FlashbombEntity>();
    private final ArrayList<FlashbombInfluencer> InfluencersTabListViewArray = new ArrayList<FlashbombInfluencer>();
    private final ArrayList<FlashbombEntity> ProfileTabListViewArray = new ArrayList<FlashbombEntity>();
    private final ArrayList<String> UserObservations = new ArrayList<String>();
    private final ArrayList<Pair<String, Integer>> Blacklist = new ArrayList<Pair<String, Integer>>();
    private final ArrayList<FlashbombEntity> InfluencerGalleryListViewArray = new ArrayList<FlashbombEntity>();

    /* SearchView suggestions arrays and help variable */
    private final ArrayList<String> SearchNicksSubarray = new ArrayList<String>();
    private final ArrayList<String> SearchNicksArray = new ArrayList<String>();
    private String currentSearchPhrase = "";

    /* Constructor */
    private SingletonFlashbombEntities(Context context) {
        mCtx = context;
    }

    /* GetInstance method */
    public static synchronized SingletonFlashbombEntities getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SingletonFlashbombEntities(context);
        }
        return mInstance;
    }

    /* Getters */
    public ArrayList<FlashbombEntity> getHomeTabListViewArray() {
        return HomeTabListViewArray;
    }

    public ArrayList<FlashbombEntity> getBestTabListViewArray() {
        return BestTabListViewArray;
    }

    public ArrayList<FlashbombEntity> getObservationsTabListViewArray() {
        return ObservationsTabListViewArray;
    }

    public ArrayList<FlashbombInfluencer> getInfluencersTabListViewArray() {
        return InfluencersTabListViewArray;
    }

    public ArrayList<FlashbombEntity> getProfileTabListViewArray() {
        return ProfileTabListViewArray;
    }

    public ArrayList<String> getObservationsManagementListViewArray() {
        return UserObservations;
    }

    public ArrayList<Pair<String, Integer>> getBlacklistListViewArray() {
        return Blacklist;
    }

    public ArrayList<FlashbombEntity> getInfluencerGalleryListViewArray() {
        return InfluencerGalleryListViewArray;
    }

    public ArrayList<String> getSearchNicksArray() {
        return SearchNicksArray;
    }

    public ArrayList<String> getSearchNicksSubarray() {
        return SearchNicksSubarray;
    }

    public String getCurrentSearchPhrase() {
        return currentSearchPhrase;
    }

    /* Setters */
    public void setCurrentSearchPhrase(String phrase) {
        this.currentSearchPhrase = phrase;
    }

    /* Adding new picture */
    public void AddEntity(FlashbombEntity entity) {

        for (FlashbombEntity fp : HomeTabListViewArray) {
            if(!fp.id.equals("-1") && fp.id.equals(entity.id)) {
                int index = HomeTabListViewArray.indexOf(fp);
                HomeTabListViewArray.remove(fp);
                HomeTabListViewArray.add(index, entity);
                break;
            }
        }
        for (FlashbombEntity fp : BestTabListViewArray) {
            if(!fp.id.equals("-1") && fp.id.equals(entity.id)) {
                int index = BestTabListViewArray.indexOf(fp);
                BestTabListViewArray.remove(fp);
                BestTabListViewArray.add(index, entity);
                break;
            }
        }
        for (FlashbombEntity fp : ObservationsTabListViewArray) {
            if(!fp.id.equals("-1") && fp.id.equals(entity.id)) {
                int index = ObservationsTabListViewArray.indexOf(fp);
                ObservationsTabListViewArray.remove(fp);
                ObservationsTabListViewArray.add(index, entity);
                break;
            }
        }
        for (FlashbombEntity fp : ProfileTabListViewArray) {
            if(!fp.id.equals("-1") && fp.id.equals(entity.id)) {
                int index = ProfileTabListViewArray.indexOf(fp);
                ProfileTabListViewArray.remove(fp);
                ProfileTabListViewArray.add(index, entity);
                break;
            }
        }
        for (FlashbombEntity fp : InfluencerGalleryListViewArray) {
            if(!fp.id.equals("-1") && fp.id.equals(entity.id)) {
                int index = InfluencerGalleryListViewArray.indexOf(fp);
                InfluencerGalleryListViewArray.remove(fp);
                InfluencerGalleryListViewArray.add(index, entity);
                break;
            }
        }

        switch (entity.srcResponse) {
            case HOME_TAB:
                HomeTabListViewArray.add(entity);
                break;
            case BEST_TAB:
                BestTabListViewArray.add(entity);
                break;
            case OBSERVATIONS_TAB:
                ObservationsTabListViewArray.add(entity);
                break;
            case PROFILE_TAB:
                ProfileTabListViewArray.add(entity);
                break;
            case INFLUENCER_GALLERY:
                InfluencerGalleryListViewArray.add(entity);
                break;
        }

    }

    public void RemoveEntitiesByUser(String nick, boolean removeFromHome, boolean removeFromBest, boolean removeFromObs) {

        if(removeFromHome) {
            for (FlashbombEntity entity : HomeTabListViewArray) {
                if (entity.nick.equals(nick)) {
                    HomeTabListViewArray.remove(entity);
                    break;
                }
            }
        }
        if(removeFromBest) {
            for (FlashbombEntity entity : BestTabListViewArray) {
                if (entity.nick.equals(nick)) {
                    BestTabListViewArray.remove(entity);
                    break;
                }
            }
        }
        if(removeFromObs) {
            for (FlashbombEntity entity : ObservationsTabListViewArray) {
                if (entity.nick.equals(nick)) {
                    ObservationsTabListViewArray.remove(entity);
                    break;
                }
            }
        }

    }

    /* Adding new influencer */
    public void AddInfluencer(FlashbombInfluencer influencer) {

        InfluencersTabListViewArray.add(influencer);

    }

    /* Adding new observation */
    public void AddObservation(String obs) {

        if (!UserObservations.contains(obs)) {
            UserObservations.add(obs);
        }

    }

    /* Removing observation */
    public void RemoveObservation(String obs) {

        UserObservations.remove(obs);

    }

    /* Adding new blacklist item */
    public void AddBlacklistItem(Pair<String, Integer> blacklistItem) {

        for (Pair<String, Integer> item : Blacklist) {
            if (item.first.equals(blacklistItem.first))
                return;
        }
        Blacklist.add(blacklistItem);

    }

    /* Adding searched observation */
    public void AddSearchedNick(String obs) {

        if (!SearchNicksArray.contains(obs)) {
            SearchNicksArray.add(obs);
        }
        if (!SearchNicksSubarray.contains(obs)) {
            SearchNicksSubarray.add(obs);
        }

    }

    /* Filtering search subarray */
    public void ProcessSearchSubarray(String phrase) {

        /* Clearing subarray */
        SearchNicksSubarray.clear();

        /* Returning on empty phrase */
        if (phrase.equals(""))
            return;

        /* Getting nicks matching to phrase */
        for (String nick : SearchNicksArray) {
            if (nick.toLowerCase().contains(phrase.toLowerCase()))
                SearchNicksSubarray.add(nick);
        }

    }

    /* Clear all */
    public void ClearAll() {

        HomeTabListViewArray.clear();
        BestTabListViewArray.clear();
        ObservationsTabListViewArray.clear();
        InfluencersTabListViewArray.clear();
        ProfileTabListViewArray.clear();
        UserObservations.clear();

        SearchNicksSubarray.clear();
        SearchNicksArray.clear();
        currentSearchPhrase = "";

    }

}