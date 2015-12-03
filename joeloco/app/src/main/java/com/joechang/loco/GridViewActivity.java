package com.joechang.loco;

import com.joechang.loco.fragment.GridViewFragment;

import android.os.Bundle;

public class GridViewActivity extends BaseDrawerActionBarActivity {

	private int columnWidth = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, GridViewFragment.newInstance(columnWidth))
                    .commit();
        }
	}

    @Override
    public NavigationEnum getNavigationEnum() {
        return NavigationEnum.BROWSE;
    }
}
