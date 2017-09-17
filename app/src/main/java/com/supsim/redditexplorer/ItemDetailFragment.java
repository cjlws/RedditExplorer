package com.supsim.redditexplorer;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.supsim.redditexplorer.dummy.DummyContent;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    private String pageTitle = "Unknown";
    private String pageLink = "Oops";
    private String pageSub = "Gah";

    public static final String interprocessTitle = "interprocessTitle";
    public static final String interprocessLink = "interprocessLink";
    public static final String interprocessSubreddit = "interprocessSubreddit";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments().containsKey(interprocessLink)){
            pageLink = getArguments().getString(interprocessLink);
            Log.d("Frag", pageLink);
        }

        if(getArguments().containsKey(interprocessTitle)){
            pageTitle = getArguments().getString(interprocessTitle);
            Log.d("Frag", pageTitle);
        }

        if(getArguments().containsKey(interprocessSubreddit)){
            pageSub = getArguments().getString(interprocessSubreddit);
        }

        Activity activity = this.getActivity();
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)activity.findViewById(R.id.toolbar_layout);
        if(collapsingToolbarLayout != null){
            collapsingToolbarLayout.setTitle(pageSub);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        if(pageTitle != null){
            ((TextView)rootView.findViewById(R.id.item_detail)).setText(pageTitle);
        }

        return rootView;
    }
}
