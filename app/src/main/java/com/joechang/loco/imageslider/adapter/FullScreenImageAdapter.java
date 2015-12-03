package com.joechang.loco.imageslider.adapter;

import com.joechang.loco.R;
import com.joechang.loco.imageslider.helper.TouchImageView;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

public class FullScreenImageAdapter extends PagerAdapter {

	private Activity _activity;
	private ArrayList<String> _imagePaths;
    private List<Integer> _sampleImageResources;
	private LayoutInflater inflater;

	// constructor
	public FullScreenImageAdapter(Activity activity,
			ArrayList<String> imagePaths) {
		this._activity = activity;
		this._imagePaths = imagePaths;
	}

    // constructor for fake resources
    public FullScreenImageAdapter(Activity activity, List<Integer> sampleImageResources) {
        this._activity = activity;
        this._sampleImageResources = sampleImageResources;
    }

	@Override
	public int getCount() {
		if (this._sampleImageResources != null) {
            return this._sampleImageResources.size();
        }

        return this._imagePaths.size();
	}

	@Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }
	
	@Override
    public Object instantiateItem(ViewGroup container, int position) {
        TouchImageView imgDisplay;
        Button btnClose;
 
        inflater = (LayoutInflater) _activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container,
                false);

        imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.imgDisplay);
        btnClose = (Button) viewLayout.findViewById(R.id.btnClose);
        
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        //So hacky
        Bitmap bitmap = null;

        if (this._sampleImageResources != null) {
            bitmap = BitmapFactory.decodeResource(
                    _activity.getApplicationContext().getResources(),
                    this._sampleImageResources.get(position));
        } else {
            bitmap = BitmapFactory.decodeFile(_imagePaths.get(position), options);
        }

        imgDisplay.setImageBitmap(bitmap);

        // close button click event
        btnClose.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				_activity.finish();
			}
		}); 

        ((ViewPager) container).addView(viewLayout);
 
        return viewLayout;
	}
	
	@Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);
 
    }

}
