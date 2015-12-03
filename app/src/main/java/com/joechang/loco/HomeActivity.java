package com.joechang.loco;

import android.os.Bundle;
import android.view.MenuItem;
import com.joechang.loco.fragment.CommandLineFragment;
import com.joechang.loco.model.ChatSession;
import com.joechang.loco.utils.UserInfoStore;

import java.util.logging.Logger;

public class HomeActivity extends BaseDrawerActionBarActivity {
    private static Logger log = Logger.getLogger(HomeActivity.class.getSimpleName());

    private CommandLineFragment clf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bb = new Bundle();
        bb.putString(ChatSession.ID, UserInfoStore.getInstance(this).getUserId());
        clf = CommandLineFragment.newInstance();
        clf.setArguments(bb);
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, clf)
                .commit();
    }

    @Override
    public NavigationEnum getNavigationEnum() {
        return NavigationEnum.HOME;
    }

    @Override
    public int getActionBarMenu() {
        return R.menu.commandline;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_clear:
                clf.clearMessages();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
