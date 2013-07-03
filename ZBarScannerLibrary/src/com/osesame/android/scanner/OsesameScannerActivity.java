package com.osesame.android.scanner;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

public class OsesameScannerActivity extends ScannerActivity implements Camera.PreviewCallback, OsesameConstants {

  private static final String TAG = "OsesameScannerActivity";
  private CameraPreview mPreview;
  private Camera mCamera;
  private Handler mAutoFocusHandler;
  private boolean mPreviewing = true;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if(!isCameraAvailable()) {
      // Cancel request if there is no rear-facing camera.
      cancelRequest();
      return;
    }

    // Hide the window title.
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

    mAutoFocusHandler = new Handler();

    // Create and configure the ImageScanner;
    setupScanner();

    // Create a RelativeLayout container that will hold a SurfaceView,
    // and set it as the content of our activity.

    RelativeLayout mainLayout = new RelativeLayout(this);
    mainLayout.setLayoutParams(new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    mPreview = new CameraPreview(this, this, autoFocusCB);
    RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    params1.addRule(RelativeLayout.CENTER_IN_PARENT);
    mainLayout.addView(mPreview, params1);

    RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params2.addRule(RelativeLayout.CENTER_IN_PARENT);
    TextView txtDemo = new TextView(this);
    txtDemo.setText("DEMO");
    txtDemo.setTextSize(30);
    txtDemo.setTextColor(Color.GRAY);
    mainLayout.addView(txtDemo, params2);

    setContentView(mainLayout);
  }

  @Override
  protected void onResume() {
    super.onResume();

    // Open the default i.e. the first rear facing camera.
    mCamera = Camera.open();
    if(mCamera == null) {
      // Cancel request if mCamera is null.
      cancelRequest();
      return;
    }

    mPreview.setCamera(mCamera);
    mPreview.showSurfaceView();

    mPreviewing = true;
  }

  @Override
  protected void onPause() {
    super.onPause();

    // Because the Camera object is a shared resource, it's very
    // important to release it when the activity is paused.
    if (mCamera != null) {
      mPreview.setCamera(null);
      mCamera.cancelAutoFocus();
      mCamera.setPreviewCallback(null);
      mCamera.stopPreview();
      mCamera.release();

      // According to Jason Kuang on http://stackoverflow.com/questions/6519120/how-to-recover-camera-preview-from-sleep,
      // there might be surface recreation problems when the device goes to sleep. So lets just hide it and
      // recreate on resume
      mPreview.hideSurfaceView();

      mPreviewing = false;
      mCamera = null;
    }
  }

  public boolean isCameraAvailable() {
    PackageManager pm = getPackageManager();
    return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
  }

  public void cancelRequest() {
    Intent dataIntent = new Intent();
    dataIntent.putExtra(ERROR_INFO, "Camera unavailable");
    setResult(Activity.RESULT_CANCELED, dataIntent);
    finish();
  }

  public void onPreviewFrame(byte[] data, Camera camera) {
    Camera.Parameters parameters = camera.getParameters();
    Camera.Size size = parameters.getPreviewSize();

    Image barcode = new Image(size.width, size.height, "Y800");
    barcode.setData(data);

    int result = mScanner.scanImage(barcode);

    if (result != 0) {
      mCamera.cancelAutoFocus();
      mCamera.setPreviewCallback(null);
      mCamera.stopPreview();
      mPreviewing = false;
      SymbolSet syms = mScanner.getResults();
      for (Symbol sym : syms) {
        String symData = sym.getData();
        if (!TextUtils.isEmpty(symData)) {
          Intent dataIntent = new Intent();
          dataIntent.putExtra(SCAN_RESULT, symData);
          dataIntent.putExtra(SCAN_RESULT_TYPE, sym.getType());
          setResult(Activity.RESULT_OK, dataIntent);
          finish();
          break;
        }
      }
    }
  }
  private Runnable doAutoFocus = new Runnable() {
    public void run() {
      if(mCamera != null && mPreviewing) {
        mCamera.autoFocus(autoFocusCB);
      }
    }
  };

  // Mimic continuous auto-focusing
  Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
    public void onAutoFocus(boolean success, Camera camera) {
      mAutoFocusHandler.postDelayed(doAutoFocus, 1000);
    }
  };
}
