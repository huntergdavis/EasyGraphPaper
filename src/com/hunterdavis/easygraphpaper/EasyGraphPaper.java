package com.hunterdavis.easygraphpaper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import android.provider.MediaStore;
import com.google.ads.AdRequest;
import com.google.ads.AdView;

import uk.co.senab.photoview.PhotoViewAttacher;

public class EasyGraphPaper extends Activity {

    // our photo return code
    static final int SELECT_PICTURE = 22;

	private int lineDensity = 20;
	private int width = 640;
	private int height = 480;
	private Bitmap staticBitmap = null;
    private Bitmap loadedImageBitmap = null;
    private boolean usingImage = false;
    PhotoViewAttacher mAttacher;
    ImageView mImageView;
    int currentColor = 0;

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
					regenLines(usingImage);
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
					regenLines(usingImage);
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




        Button loadButton = (Button) findViewById(R.id.loadButton);
        loadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                load();
            }
        });

        Button clearButton = (Button) findViewById(R.id.xButton);
        clearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                clear();
            }
        });


		// Look up the AdView as a resource and load a request.
		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());

        // Any implementation of ImageView can be used!
        mImageView = (ImageView) findViewById(R.id.ImageView01);

        // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
        mAttacher = new PhotoViewAttacher(mImageView);


        // pick color button
        // listener for frequency button
        OnClickListener colorListener = new OnClickListener() {
            public void onClick(View v) {
                changeColor();
            }

        };


        Button mycolorbutton = (Button) findViewById(R.id.colorButton);
        mycolorbutton.setOnClickListener(colorListener);
		
		regenLines(false);
		
	}

    public void changeColor() {

        ColorPickerDialog.OnColorChangedListener ourchangelistener = new ColorPickerDialog.OnColorChangedListener() {
            @Override
            public void colorChanged(int color) {
                currentColor = color;
                regenLines(usingImage);
            }
        };
        new ColorPickerDialog(EasyGraphPaper.this, ourchangelistener, 333444).show();
    }

    public void clear() {
        usingImage = false;
        regenLines(false);
    }

    public void load() {
        // do something when the button is clicked

        // in onCreate or any event where your want the user to
        // select a file
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Select Source Photo"),
                SELECT_PICTURE);
    }
	
	public void updateHeightWidth(){
		EditText widthText = (EditText) findViewById(R.id.width);
		EditText heightText = (EditText) findViewById(R.id.height);
		height = Integer.parseInt(heightText.getText().toString());
		width = Integer.parseInt(widthText.getText().toString());
	}

    public void updateHeightWidthDisplayFromValues() {
        EditText widthText = (EditText) findViewById(R.id.width);
        EditText heightText = (EditText) findViewById(R.id.height);

        widthText.setText("" + width);
        heightText.setText("" + height);
    }

	public void regenLines(boolean useImage) {
		updateHeightWidth();

		// create a width*height bitmap
		BitmapFactory.Options staticOptions = new BitmapFactory.Options();
		//staticOptions.inSampleSize = 2;

        if(useImage) {
            if(staticBitmap != null) {
                staticBitmap.recycle();
            }
            staticBitmap = loadedImageBitmap.copy(Bitmap.Config.ARGB_8888,true);
            paintPicture();
        }else {
            int rgbSize = width * height;
            int[] rgbValues = new int[rgbSize];
            for (int i = 0; i < rgbSize; i++) {
                rgbValues[i] = calculatePixelValue(i);
            }
            staticBitmap = Bitmap.createBitmap(rgbValues, width, height,
                    Bitmap.Config.RGB_565);
        }

		// set the imageview to the static
		mImageView.setImageBitmap(staticBitmap);
        mAttacher.update();

	}

    public void paintPicture() {
        // get our spacing values
        int xSpacing = (int) Math.floor(width/lineDensity);
        int ySpacing = (int) Math.floor(height/lineDensity);

        for(int i = 0; i< width; i++) {
            for (int j = 0; j < height; j++) {
                // test for x line
                if((i % xSpacing) == 0)
                {
                    staticBitmap.setPixel(i,j,currentColor);
                }
                // test for y line
                if((j % ySpacing) == 0)
                {
                    staticBitmap.setPixel(i,j,currentColor);
                }
            }
        }
    }
	
	public Integer calculatePixelValue(int location)
	{
		// get our x and y location
		int xLocation = (int) location%width;
		int yLocation = (int) Math.floor(location/width);
		
		// set our white and black values 
		int White = Integer.MAX_VALUE;

		// get our spacing values
		int xSpacing = (int) Math.floor(width/lineDensity);
		int ySpacing = (int) Math.floor(height/lineDensity);
		
		// test for x line
		if((xLocation % xSpacing) == 0)
		{
			return currentColor;
		}
		// test for y line
		if((yLocation % ySpacing) == 0)
		{
			return currentColor;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                usingImage = scaleURIAndDisplay(getApplicationContext(), selectedImageUri);

                if(usingImage) {
                    Log.e("got here", "got here hunter ------------------");
                    regenLines(true);
                }
            }
        }
    }


    public Boolean scaleURIAndDisplay(Context context, Uri uri) {
        InputStream photoStream = null;

        try {
            photoStream = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(photoStream,null,options);
        options.inSampleSize = calculateInSampleSize(options, width,height);

        // Decode bitmap with inSampleSize set
        try {
            photoStream = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        options.inJustDecodeBounds = false;
        loadedImageBitmap = BitmapFactory.decodeStream(photoStream, null, options);
        if (loadedImageBitmap == null) {
            return false;
        }

        width = loadedImageBitmap.getWidth();
        height = loadedImageBitmap.getHeight();
        updateHeightWidthDisplayFromValues();
        return true;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 2;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = Math.round((float)width / (float)reqWidth);
            }
        }
        return inSampleSize;
    }
}