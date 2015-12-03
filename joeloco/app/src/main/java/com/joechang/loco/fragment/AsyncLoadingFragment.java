package com.joechang.loco.fragment;

/**
 * Author:  joechang
 * Date:    3/31/15
 * Purpose: Interface designating that the fragment has an adapter that loads asynchronously
 * ie Firebase, in the background.  So this fragment will want to have methods for others to insert
 * for execution (Callback) when the loading is done.
 */
public interface AsyncLoadingFragment {

    public ReadyAction getOnFinishedLoadingAction();
    public void onFinishedLoading(ReadyAction ra);

    /**
     * Small interface that is a callback when the placeholder is ready to insert.
     */
    public interface ReadyAction {
        public void doAction(final int insertViewId);
    }


}
