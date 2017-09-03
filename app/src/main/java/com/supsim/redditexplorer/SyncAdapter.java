package com.supsim.redditexplorer;


import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.supsim.redditexplorer.account.AccountGeneral;
import com.supsim.redditexplorer.data.RedditArticleContract;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SyncAdapter";
    ContentResolver mContentResolver;

    public SyncAdapter(Context context, boolean autoInitialize){
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient providerClient, SyncResult syncResult){
        //TODO Data Transfer Code
        Log.d(TAG, "On Perform Sync Ran");
    }

    public static void performSync(){
        //TODO
        Log.d(TAG, "Perform Sync was called");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(AccountGeneral.getAccount(), RedditArticleContract.CONTENT_AUTHORITY, bundle);
    }
}
