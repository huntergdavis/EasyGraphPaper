package com.hunterdavis.easygraphpaper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import android.provider.MediaStore;
import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class EasyGraphPaper extends Activity {
	/** Called when the activity is first created. */

	private int lineDensity = 20;
	private int width = 640;
	private int height = 480;
	private Bitmap staticBitmap = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Set onclicklisteners for the buttons
		OnClickListener lessButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked
				lineDensity -= 5;
				if (lineDensity < 1) {
					lineDensity = 5;
				} else {
					regenLines();
				}
			}
		};

		// Set onclicklisteners for the buttons
		OnClickListener moreButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked
				lineDensity += 5;
				if ((lineDensity > (width / 2)) || (lineDensity > (height / 2))) {
					lineDensity -= 5;
				} else {
					regenLines();
				}
			}
		};

		// Set onclicklisteners for the buttons
		OnClickListener saveButtonListner = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked
				saveImage(v.getContext());
			}
		};

		Button lessButton = (Button) findViewById(R.id.lessButton);
		lessButton.setOnClickListener(lessButtonListner);

		Button moreButton = (Button) findViewById(R.id.moreButton);
		moreButton.setOnClickListener(moreButtonListner);

		Button saveButton = (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(saveButtonListner);

		// Look up the AdView as a resource and load a request.
		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());
		
		regenLines();
		
	}
	
	public void updateHeightWidth(){
		EditText widthText = (EditText) findViewById(R.id.width);
		EditText heightText = (EditText) findViewById(R.id.height);
		height = Integer.parseInt(heightText.getText().toString());
		width = Integer.parseInt(widthText.getText().toString());
	}

	public void regenLines() {
		ImageView imgView = (ImageView) findViewById(R.id.ImageView01);
		updateHeightWidth();

		int rgbSize = width * height;
		int[] rgbValues = new int[rgbSize];
		for (int i = 0; i < rgbSize; i++) {
			rgbValues[i] = calculatePixelValue(i);
		}

		// create a width*height bitmap
		BitmapFactory.Options staticOptions = new BitmapFactory.Options();
		//staticOptions.inSampleSize = 2;
		staticBitmap = Bitmap.createBitmap(rgbValues, width, height,
				Bitmap.Config.RGB_565);

		// set the imageview to the static
		imgView.setImageBitmap(staticBitmap);

	}
	
	public Integer calculatePixelValue(int location)
	{
		// get our x and y location
		int xLocation = (int) location%width;
		int yLocation = (int) Math.floor(location/width);
		
		// set our white and black values 
		int White = Integer.MAX_VALUE;
		int Black = 0;
		
		// get our spacing values
		int xSpacing = (int) Math.floor(width/lineDensity);
		int ySpacing = (int) Math.floor(height/lineDensity);
		
		// test for x line
		if((xLocation % xSpacing) == 0)
		{
			return Black;
		}
		// test for y line
		if((yLocation % ySpacing) == 0)
		{
			return Black;
		}
		
		
		return White;
	}

	public Boolean saveImage(Context context) {
		updateHeightWidth();
		String extStorageDirectory = Environment.getExternalStorageDirectory()
				.toString();

		// actually save the file

		OutputStream outStream = null;
		String newFileName = "GraphPaper_withdensity-"+lineDensity+"-" + width
						+ "x" + height + ".png";
		

		if (newFileName != null) {
			{
				File file = new File(extStorageDirectory, newFileName);
				try {
					outStream = new FileOutputStream(file);
					staticBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
					try {
						outStream.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}
					try {
						outStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return false;
					}

					Toast.makeText(context, "Saved " + newFileName,
							Toast.LENGTH_LONG).show();
					new SingleMediaScanner(context, file);

				} catch (FileNotFoundException e) {
					// do something if errors out?
					return false;
				}
			}

			return true;

		}
		return false;
	}

}