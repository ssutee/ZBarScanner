package com.osesame.android.scanner;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import net.sourceforge.zbar.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 27/6/2013
 * Time: 16:44
 */

public class OsesamePhotoScannerActivity extends ScannerActivity {

  private static final String TAG = "OsesamePhotoScannerActivity";
  private Handler mHandler = new Handler();

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Hide the window title.
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

    RelativeLayout mainLayout = new RelativeLayout(this);
    mainLayout.setLayoutParams(new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    ProgressBar spinner = new ProgressBar(this);
    spinner.setIndeterminate(true);
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.addRule(RelativeLayout.CENTER_IN_PARENT);
    mainLayout.addView(spinner, params);
    setContentView(mainLayout);
    setupScanner();
    if (savedInstanceState == null) {
      pickImageFromGallery();
    }
  }

  protected void pickImageFromGallery() {
    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    startActivityForResult(Intent.createChooser(intent, "Select picture..."),
        OsesameConstants.GALLERY_REQ_CODE);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == OsesameConstants.GALLERY_REQ_CODE && data != null) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          scanImage(readBitmap(data.getData()));
          mHandler.post(new Runnable() {
            @Override
            public void run() {
              finish();
            }
          });
        }
      }).start();
    } else {
      setResult(Activity.RESULT_CANCELED);
      finish();
    }
  }

  public Bitmap readBitmap(Uri selectedImage) {
    Bitmap bm = null;
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = 5;
    AssetFileDescriptor fileDescriptor = null;
    try {
      fileDescriptor = this.getContentResolver().openAssetFileDescriptor(selectedImage,"r");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    finally{
      try {
        bm = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
        fileDescriptor.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return bm;
  }

  private void scanImage(Bitmap image) {
    Image barcode = new Image(image.getWidth(), image.getHeight(), "Y800");
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    image.compress(Bitmap.CompressFormat.PNG, 100, stream);
    barcode.setData(stream.toByteArray());
    int result = mScanner.scanImage(barcode);
    if (result != 0) {
      SymbolSet syms = mScanner.getResults();
      for (Symbol sym : syms) {
        String symData = sym.getData();
        if (!TextUtils.isEmpty(symData)) {
          Intent dataIntent = new Intent();
          Log.d(TAG, symData);
          dataIntent.putExtra(OsesameConstants.SCAN_RESULT, symData);
          dataIntent.putExtra(OsesameConstants.SCAN_RESULT_TYPE, sym.getType());
          setResult(Activity.RESULT_OK, dataIntent);
          break;
        }
      }
    } else {
      setResult(Activity.RESULT_CANCELED);
    }
  }
}