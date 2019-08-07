package com.koszelew.flashbomb.Listeners;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.koszelew.flashbomb.Activities.MainActivity;
import com.koszelew.flashbomb.Adapters.ObservationsSearchListViewAdapter;
import com.koszelew.flashbomb.R;
import com.koszelew.flashbomb.Utils.EntitiesProcessing.SingletonFlashbombEntities;
import com.koszelew.flashbomb.Utils.Networking.SingletonNetwork;

public class SearchListener implements TextWatcher {

    /* Variables */
    private final MainActivity activity;
    private final ListView searchListView;
    private final ImageView searchNoResults;

    /* Constructor */
    public SearchListener(MainActivity activity, Context context) {

        this.activity = activity;
        this.searchListView = (ListView) activity.findViewById(R.id.mainActivitySearchListView);
        this.searchNoResults = (ImageView) activity.findViewById(R.id.mainActivitySearchNoResults);

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        /* If phrase id equal to 0, show current observations */
        if(s.length() == 0) {

            /* Process sub array */
            SingletonFlashbombEntities.getInstance(activity).getSearchNicksSubarray().clear();
            SingletonFlashbombEntities.getInstance(activity).getSearchNicksSubarray().addAll(SingletonFlashbombEntities.getInstance(activity).getObservationsManagementListViewArray());

            if(SingletonFlashbombEntities.getInstance(activity).getSearchNicksSubarray().size() == 0) {
                HideSuggestions();
                return;
            }

            /* Notify sugesstions adapter */
            ObservationsSearchListViewAdapter adapter = (ObservationsSearchListViewAdapter) searchListView.getAdapter();
            if (adapter != null)
                adapter.notifyDataSetChanged();

            /* ShowError suggestions */
            ShowSuggestions();

        /* If phrase is shorter than 3 characters */
        } else if (s.length() < 3) {

            /* Hide suggestions */
            HideSuggestions();

        /* If phrase has exactly 3 characters */
        } else if (s.length() == 3) {

            /* If phrase is different than currently downloaded */
            if (!(s.toString().toLowerCase()).equals(SingletonFlashbombEntities.getInstance(activity).getCurrentSearchPhrase().toLowerCase())) {

                /* Clearing old values */
                SingletonFlashbombEntities.getInstance(activity).getSearchNicksArray().clear();
                SingletonFlashbombEntities.getInstance(activity).getSearchNicksSubarray().clear();

                /* Downloading new suggestions */
                SingletonNetwork.getInstance(activity).SearchObservationsQuery(s.toString());

            /* If phrase is the same as currently downloaded */
            } else {

                /* Process suggestions subarray */
                SingletonFlashbombEntities.getInstance(activity).ProcessSearchSubarray(s.toString());

            }

            /* Notify sugesstions adapter */
            ObservationsSearchListViewAdapter adapter = (ObservationsSearchListViewAdapter) searchListView.getAdapter();
            if (adapter != null)
                adapter.notifyDataSetChanged();

            /* ShowError suggestions */
            if(SingletonFlashbombEntities.getInstance(activity).getSearchNicksSubarray().size() > 0) {
                ShowSuggestions();
            } else {
                HideSuggestions();
            }

        /* If phrase longer than 3 characters */
        } else {

            /* Process suggestions subarray */
            SingletonFlashbombEntities.getInstance(activity).ProcessSearchSubarray(s.toString());

            /* Notify sugesstions adapter */
            ObservationsSearchListViewAdapter adapter = (ObservationsSearchListViewAdapter) searchListView.getAdapter();
            if (adapter != null)
                adapter.notifyDataSetChanged();

            /* ShowError suggestions */
            if(SingletonFlashbombEntities.getInstance(activity).getSearchNicksSubarray().size() > 0) {
                ShowSuggestions();
            } else {
                HideSuggestions();
            }

        }

    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    /* Method showing suggestions */
    public void ShowSuggestions() {

        searchListView.setVisibility(View.VISIBLE);
        searchNoResults.setVisibility(View.GONE);

    }

    /* Method hiding suggestions */
    public void HideSuggestions() {

        searchListView.setVisibility(View.GONE);
        searchNoResults.setVisibility(View.VISIBLE);

    }

}
