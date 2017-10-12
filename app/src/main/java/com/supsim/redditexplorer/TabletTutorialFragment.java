package com.supsim.redditexplorer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class TabletTutorialFragment extends Fragment {

    private static final String TAG = "TABLET_TUT_FRAG";

    public TabletTutorialFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate Ran");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.tablet_tutorial_detail, container, false);
        return rootView;
    }
}
