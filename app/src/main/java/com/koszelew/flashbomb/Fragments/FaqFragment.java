package com.koszelew.flashbomb.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.koszelew.flashbomb.R;

public class FaqFragment extends Fragment {

    /* Help flag */
    private boolean FaqLoaded = false;

    /* Initialization */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_faq, container, false);

        return view;

    }

    /* Method loading faq */
    public void LoadFaq() {

        if(FaqLoaded)
            return;

        final WebView webView = getView().findViewById(R.id.faqWebView);

        if(webView == null)
            return;

        final ProgressBar progressBar = getView().findViewById(R.id.faqWebViewProgressBar);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                    FaqLoaded = true;
                }
            }
        });
        webView.loadUrl(getString(R.string.faq_url));

    }

}
