package com.osesame.android.examples;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import com.osesame.android.scanner.OsesameConstants;
import com.osesame.android.scanner.OsesamePhotoScannerActivity;
import com.osesame.android.scanner.OsesameScannerActivity;

public class MainActivity extends Activity {

  private static final int SCANNER_REQUEST = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
  }

  public void launchScanner(View v) {
    if (isCameraAvailable()) {
      Intent intent = new Intent(this, MyScannerActivity.class);
      startActivityForResult(intent, SCANNER_REQUEST);
    } else {
      Toast.makeText(this, "Rear Facing Camera Unavailable", Toast.LENGTH_SHORT).show();
    }
  }

  public void launchPhotoScanner(View v) {
    Intent intent = new Intent(this, OsesamePhotoScannerActivity.class);
    startActivityForResult(intent, SCANNER_REQUEST);
  }

  public boolean isCameraAvailable() {
    PackageManager pm = getPackageManager();
    return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case SCANNER_REQUEST:
        if (resultCode == RESULT_OK) {
          Toast.makeText(this, "Scan Result = " + data.getStringExtra(OsesameConstants.SCAN_RESULT),
              Toast.LENGTH_SHORT).show();
        } else if(resultCode == RESULT_CANCELED && data != null) {
          String error = data.getStringExtra(OsesameConstants.ERROR_INFO);
          if(!TextUtils.isEmpty(error)) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
          }
        }
        break;
    }
  }
}
