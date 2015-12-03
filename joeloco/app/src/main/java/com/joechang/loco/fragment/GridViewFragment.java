package com.joechang.loco.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.joechang.loco.R;
import com.joechang.loco.firebase.FirebaseAdapter;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.imageslider.adapter.FirebaseGridViewImageAdapter;
import com.joechang.loco.imageslider.helper.AppConstant;
import com.joechang.loco.imageslider.helper.Utils;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GridViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GridViewFragment extends Fragment implements AsyncLoadingFragment {

    protected static final String ARG_IMG_WIDTH = "imgWidth";
    private int imgWidth;
    private ReadyAction mReadyAction;                   //What to run when gridView is finished,

    public GridViewFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param imgWidth how many pictures go across
     * @return A new instance of fragment GridViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GridViewFragment newInstance(int imgWidth) {
        GridViewFragment fragment = new GridViewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_IMG_WIDTH, imgWidth);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imgWidth = getArguments().getInt(ARG_IMG_WIDTH);
        }
    }

    public FirebaseAdapter getAdapter(Context cxt, LayoutInflater inflater, int gridWidth) {
        return new FirebaseGridViewImageAdapter(
                FirebaseManager.getInstance().getImageUploadFirebase(),
                cxt,
                inflater,
                gridWidth);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_grid_view, container, false);
        createGridView(inflater, v);

        return v;
    }

    protected GridView createGridView(LayoutInflater inflater, View v) {
        final GridView gv = (GridView)v.findViewById(R.id.grid_view);
        initializeGrid(inflater.getContext(), gv);

        FirebaseAdapter fa = getAdapter(getActivity(), inflater, getImgWidth());

        if (fa != null) {
            fa.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    if (getOnFinishedLoadingAction() != null) {
                        getOnFinishedLoadingAction().doAction(gv.getId());
                    }
                }
            });

            gv.setAdapter(fa);
        }

        return gv;
    }

    public boolean refresh() {
        if (isAdded()) {
            FirebaseAdapter adapter = getAdapter(getActivity(), getActivity().getLayoutInflater(), imgWidth);
            GridView gv = (GridView) getActivity().findViewById(R.id.grid_view);
            if (gv != null) {
                //Sometimes we are in a race where this GV is not visible! (on rotation)
                gv.setAdapter(adapter);
                return true;
            }
        }

        return false;
    }

    private void initializeGrid(Context c, GridView gv) {
        Utils utils = new Utils(c);
        Resources r = getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                AppConstant.GRID_PADDING, r.getDisplayMetrics());
        setImgWidth((int)((utils.getScreenWidth() - ((AppConstant.NUM_OF_COLUMNS + 1) * padding)) / AppConstant.NUM_OF_COLUMNS));
        gv.setNumColumns(AppConstant.NUM_OF_COLUMNS);
        gv.setColumnWidth(getImgWidth());
        gv.setStretchMode(GridView.NO_STRETCH);
        gv.setPadding((int) padding, (int) padding, (int) padding,
                (int) padding);
        gv.setHorizontalSpacing((int) padding);
        gv.setVerticalSpacing((int) padding);
    }

    public int getImgWidth() {
        return imgWidth;
    }

    public void setImgWidth(int imgWidth) {
        this.imgWidth = imgWidth;
    }

    @Override
    public ReadyAction getOnFinishedLoadingAction() {
        return mReadyAction;
    }

    @Override
    public void onFinishedLoading(ReadyAction ra) {
        mReadyAction = ra;
    }
}
