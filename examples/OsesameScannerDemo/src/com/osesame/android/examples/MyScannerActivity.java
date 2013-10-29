package com.osesame.android.examples;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.osesame.android.scanner.OsesameScannerActivity;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 16/7/2013
 * Time: 22:56
 */

public class MyScannerActivity extends OsesameScannerActivity {

  @Override
  public void prepareLayout(RelativeLayout mainLayout) {

    // add red horizontal line
    RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, 2);
    params1.addRule(RelativeLayout.CENTER_IN_PARENT);
    View line = new View(this);
    line.setBackgroundColor(Color.RED);
    mainLayout.addView(line, params1);

    // add description text at bottom
    RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
    params2.addRule(RelativeLayout.CENTER_HORIZONTAL);
    TextView textView = new TextView(this);
    textView.setTextColor(Color.GRAY);
    textView.setTextSize(18);
    textView.setPadding(0, 0, 0, 25);
    textView.setText("Place a barcode inside the viewfinder rectangle to scan it.");
    mainLayout.addView(textView, params2);
  }

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }
}