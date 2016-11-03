package com.andy.learn;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_in_one);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment frag = new AgendaFragment(System.currentTimeMillis(), false);
        ft.replace(R.id.main_pane, frag);
        ft.commit();
    }
}
