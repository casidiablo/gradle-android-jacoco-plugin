package com.android.tests.basic;

/**
 * Created by acsia on 13/03/14.
 */
public class InstrumentationRunner extends android.test.InstrumentationTestRunner {

    @Override
    public void start() {
        super.start();
//        AgentOptions o = new AgentOptions();
//        o.setDestfile("/sdcard/jacoco");
//        Log.i("TEST", "=-==> "+ Agent.getInstance(o).getData().getSessionId());

    }

    @Override
    public void onDestroy() {
//        Log.i("TEST", "=-==> " + Agent.getInstance().getExecutionData(false));
        super.onDestroy();
    }
}
