package com.supsim.redditexplorer.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import com.supsim.redditexplorer.SyncAdapter;
import com.supsim.redditexplorer.data.RedditArticleContract;

/**
 * Created by johnrobinson on 03/09/2017.
 */

public class AccountGeneral {

    private static final String ACCOUNT_TYPE ="com.supsim.syncaccount";
    private static final String ACCOUNT_NAME = "Test Account";

    public static Account getAccount(){
        return new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
    }

    public static void createSyncAccount(Context context){
        boolean created = false;

        Account account = getAccount();
        AccountManager manager = (AccountManager)context.getSystemService(Context.ACCOUNT_SERVICE);

        if(manager.addAccountExplicitly(account, null, null)){
            final String AUTHORITY = RedditArticleContract.CONTENT_AUTHORITY;
            final long SYNC_FREQUENCY = 5 * 60;

            ContentResolver.setIsSyncable(account, AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
            ContentResolver.addPeriodicSync(account, AUTHORITY, new Bundle(), SYNC_FREQUENCY);

            created = true;
        }

        if (created){
            SyncAdapter.performSync();
        }
    }

}
