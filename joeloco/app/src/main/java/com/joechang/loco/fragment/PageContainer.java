package com.joechang.loco.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.view.ViewGroup;

/**
 * Author:  joechang
 * Date:    3/3/15
 * Purpose: Quick little class that holds a "tab" or "page", the name of the page, the fragment
 * that is supposed to be intialized within it.  This also holds the framelayout which gets replaced
 * once the asynchronous population of the fragment is done.
 */
public class PageContainer implements Comparable {

    private Integer tabIndex;
    private CharSequence mTitle;
    private PlaceholderFragment mPlaceholder;
    private Fragment mFragment;
    private AsyncLoadingFragment.ReadyAction readyAction;

    public PageContainer(int tabIndex, CharSequence mTitle) {
        this.tabIndex = tabIndex;
        this.mTitle = mTitle;
    }

    public int getTabIndex() {
        return tabIndex;
    }

    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public void setTitle(CharSequence mTitle) {
        this.mTitle = mTitle;
    }

    public PlaceholderFragment getPlaceholderFragment() {
        return this.mPlaceholder;
    }

    public void setPlaceholderFragment(PlaceholderFragment pf) {
        this.mPlaceholder = pf;
    }

    /**
     * Once the fragment is initialized, setFragment so that we don't need to use the placeholder.
     * @return the real fragment.
     */
    public Fragment getFragment() {
        return mFragment;
    }

    public void setFragment(Fragment mFragment) {
        this.mFragment = mFragment;
    }

    /**
     * Forgive me, we need this because the pageradapter sometimes recreates fragments on the fly.
     * So we need to keep this somewhere so we can reinit it.  (See below, instantiateItem).
     * @param ra
     */
    public void onFinishedLoading(PlaceholderFragment.ReadyAction ra) {
        this.readyAction = ra;
    }

    public PlaceholderFragment.ReadyAction getReadyAction() {
        return this.readyAction;
    }

    public void showLoadingProgressBar() {
        getPlaceholderFragment().showProgressBar(true);
    }

    public void hideLoadingProgressBar() {
        getPlaceholderFragment().showProgressBar(false);
    }


    @Override
    public int compareTo(Object another) {
        return this.tabIndex.compareTo(((PageContainer)another).tabIndex);
    }

    public static FragmentPagerAdapter getPagerAdapter(FragmentManager fm, PageContainer[] pages) {
        FragmentPagerAdapter fpa = new ContainerPagerAdapter(fm, pages);
        return fpa;
    }
}
    class ContainerPagerAdapter extends FragmentPagerAdapter {
        private PageContainer[] pages;

        public ContainerPagerAdapter(FragmentManager fm, PageContainer[] subPages) {
            super(fm);
            this.pages = subPages;
        }

        @Override
        public Fragment getItem(int i) {
            return new PlaceholderFragment();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PlaceholderFragment pf = (PlaceholderFragment)super.instantiateItem(container, position);
            pages[position].setPlaceholderFragment(pf);
            pf.onFinishedLoading(pages[position].getReadyAction());
            pf.doReadyAction();
            return pf;
        }

        @Override
        public int getCount() {
            return pages.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pages[position].getTitle();
        }
}
