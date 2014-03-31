package com.android.tests.basic;

import android.app.Activity;
import android.os.Bundle;

public class Main extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        sayHello();
    }

    public static void sayHello2() {
    }

    public void sayHello() {
        getActionBar();
    }

    static {
        sayHello2();
    }
}
