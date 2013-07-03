package com.osesame.android.scanner;

import android.app.Activity;
import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 27/6/2013
 * Time: 20:33
 */

public class ScannerActivity extends Activity {

  protected ImageScanner mScanner;

  static {
    System.loadLibrary("iconv");
  }

  public void setupScanner() {
    mScanner = new ImageScanner();
    mScanner.setConfig(0, Config.X_DENSITY, 3);
    mScanner.setConfig(0, Config.Y_DENSITY, 3);

    int[] symbols = getIntent().getIntArrayExtra(OsesameConstants.SCAN_MODES);
    if (symbols != null) {
      mScanner.setConfig(Symbol.NONE, Config.ENABLE, 0);
      for (int symbol : symbols) {
        mScanner.setConfig(symbol, Config.ENABLE, 1);
      }
    }
  }
}
