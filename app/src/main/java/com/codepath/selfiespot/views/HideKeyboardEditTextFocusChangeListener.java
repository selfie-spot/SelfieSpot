package com.codepath.selfiespot.views;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * {@link View.OnFocusChangeListener} implementation that hides the keyboard on losing focus.
 */
public class HideKeyboardEditTextFocusChangeListener implements View.OnFocusChangeListener {
    public void onFocusChange(final View v, final boolean hasFocus){

        if(v instanceof EditText && ! hasFocus) {
            InputMethodManager imm =  (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}

