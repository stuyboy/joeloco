package com.joechang.loco.imageslider.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.joechang.loco.R;

public class Utils {

	private Context _context;

	// constructor
	public Utils(Context context) {
		this._context = context;
	}

    /**
     * Hacking, but return the fake resources in the res/drawable folder
     * @return arraylist<integer>
     */
    public List<Integer> getSampleImageResources() {
        Integer[] drawables = {
                R.drawable.a,
                R.drawable.b,
                R.drawable.c,
                R.drawable.d,
                R.drawable.e,
                R.drawable.f
        };
        return Arrays.asList(drawables);
    }

	/*
	 * Reading file paths from SDCard
	 */
	public ArrayList<String> getFilePaths() {
		ArrayList<String> filePaths = new ArrayList<String>();

		//File directory = new File(
		//		android.os.Environment.getExternalStorageDirectory()
		//				+ File.separator + AppConstant.PHOTO_ALBUM);

        File directory = new File("/mnt/shared/samples");

		// check for directory
		if (directory.isDirectory()) {
			// getting list of file paths
			File[] listFiles = directory.listFiles();

			// Check for count
			if (listFiles.length > 0) {

				// loop through all files
				for (int i = 0; i < listFiles.length; i++) {

					// get file path
					String filePath = listFiles[i].getAbsolutePath();

					// check for supported file extension
					if (IsSupportedFile(filePath)) {
						// Add image path to array list
						filePaths.add(filePath);
					}
				}
			} else {
				// image directory is empty
				Toast.makeText(
						_context,
						AppConstant.PHOTO_ALBUM
								+ " is empty. Please load some images in it !",
						Toast.LENGTH_LONG).show();
			}

		} else {
			AlertDialog.Builder alert = new AlertDialog.Builder(_context);
			alert.setTitle("Error!");
			alert.setMessage(AppConstant.PHOTO_ALBUM
					+ " directory path is not valid! Please set the image directory name AppConstant.java class");
			alert.setPositiveButton("OK", null);
			alert.show();
		}

		return filePaths;
	}

	/*
	 * Check supported file extensions
	 * 
	 * @returns boolean
	 */
	private boolean IsSupportedFile(String filePath) {
		String ext = filePath.substring((filePath.lastIndexOf(".") + 1),
				filePath.length());

		if (AppConstant.FILE_EXTN
				.contains(ext.toLowerCase(Locale.getDefault())))
			return true;
		else
			return false;

	}

	/*
	 * getting screen width
	 */
	public int getScreenWidth() {
		int columnWidth;
		WindowManager wm = (WindowManager) _context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

		final Point point = new Point();
		try {
			display.getSize(point);
		} catch (java.lang.NoSuchMethodError ignore) { // Older device
			point.x = display.getWidth();
			point.y = display.getHeight();
		}
		columnWidth = point.x;
		return columnWidth;
	}
}
