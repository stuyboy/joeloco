package com.joechang.loco.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

/**
 * Author:  joechang
 * Date:    2/24/15
 * Purpose: A generic fragment that can serve as a placeholder while asynchronously a fragment can replace.
 */
public class PlaceholderFragment extends Fragment implements AsyncLoadingFragment {
    public static String DYNAMIC_ID = "_INDEX";
    private ProgressBar progressBar = null;
    private boolean showProgressBar = true;
    private int insertionId;

    private volatile boolean initialized = false;

    private volatile ReadyAction readyAction;

    public PlaceholderFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.progressBar = getDefaultProgressBar(getActivity());
        ViewGroup vg = defaultProgressBarLayout(getActivity(), this.progressBar);
        showProgressBar(showProgressBar);

        this.insertionId = vg.getId();

        //IF this is the first time of instantiation, then we're ready to populate.
        if (savedInstanceState == null) {
            doReadyAction();
        }

        return vg;
    }

    public void doReadyAction() {
        if (getOnFinishedLoadingAction() != null && this.insertionId != 0 && !initialized) {
            getOnFinishedLoadingAction().doAction(this.insertionId);
            this.initialized = true;
        }
    }

    public void showProgressBar(boolean t) {
        showProgressBar = t;
        if (this.progressBar != null) {
            this.progressBar.setVisibility(t ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Useful little method to get a layout (that can be setContentView) that just has a progress bar
     * (spinner) in the middle of it.
     * @param c
     * @return viewGroup
     */
    public static ViewGroup defaultProgressBarLayout(Context c) {
        return defaultProgressBarLayout(c, getDefaultProgressBar(c));
    }

    public static ProgressBar getDefaultProgressBar(Context c) {
        return new ProgressBar(c, null, android.R.attr.progressBarStyleSmall);
    }

    public static ViewGroup defaultProgressBarLayout(Context c, ProgressBar pb) {
        pb.setIndeterminate(true);

        RelativeLayout layout = new RelativeLayout(c);
        layout.setId(View.generateViewId());

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        layout.addView(pb, params);

        return layout;
    }

    @Override
    public ReadyAction getOnFinishedLoadingAction() {
        return readyAction;
    }

    @Override
    public void onFinishedLoading(ReadyAction readyAction) {
        this.readyAction = readyAction;
    }
}
