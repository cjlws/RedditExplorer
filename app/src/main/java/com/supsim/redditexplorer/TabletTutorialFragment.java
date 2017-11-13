package com.supsim.redditexplorer;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;


public class TabletTutorialFragment extends Fragment {

    Animation wiggle;
    boolean keepWiggling;
    TextView callToAction;
    Handler handler;

//    public TabletTutorialFragment(){
//
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        wiggle = AnimationUtils.loadAnimation(getContext(), R.anim.textwiggle);
        keepWiggling = true;
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tablet_tutorial_detail, container, false);

        callToAction = (TextView) rootView.findViewById(R.id.tabletTutorialCallToAction);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (keepWiggling) handler.postDelayed(runnable, 3000);
    }

    @Override
    public void onResume() {
        super.onResume();
        keepWiggling = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        keepWiggling = false;
    }

    private void triggerWiggle() {
        if (callToAction != null) {
            callToAction.startAnimation(wiggle);
        }
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            triggerWiggle();
            if (keepWiggling) handler.postDelayed(runnable, 4000);
        }
    };
}
