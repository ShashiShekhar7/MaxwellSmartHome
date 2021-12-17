package com.shashi.maxwellsmarthome;

import android.graphics.Color;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class Util {

    public Snackbar showSnackBar(View view, String msg) {
        Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT);

        View view1 = snackbar.getView();
        view1.setBackgroundColor(Color.parseColor("#ff0000"));

        snackbar.show();
        return snackbar;
    }
}
