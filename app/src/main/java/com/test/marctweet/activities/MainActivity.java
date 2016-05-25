package com.test.marctweet.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.test.marctweet.Constants;
import com.test.marctweet.R;
import com.test.marctweet.search.SearchService;
import com.test.marctweet.adapters.StatusAdapter;
import com.test.marctweet.model.Status;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterSession;

public class MainActivity extends AppCompatActivity {

    public final static int REQUEST_LOGIN = 1;

    private static final String TAG = "MainActivity";

    private TextInputEditText mEdtSearch;
    private Button mBtnSearch;
    private RecyclerView mRvStatuses;

    private StatusAdapter mStatusAdapter;
    private SearchResultsReceiver mResultsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get a handle on the relevant views
        mEdtSearch = (TextInputEditText) findViewById(R.id.edtSearch);
        mBtnSearch = (Button) findViewById(R.id.btnSearch);

        // configure Views
        mBtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = mEdtSearch.getText().toString().trim();
                if (validateUserInput(input)) {
                    // Creates a new Intent to send to the download SearchService,
                    // the intent contains the validated user query to search for
                    Intent intent = new Intent(MainActivity.this, SearchService.class);
                    intent.setAction(Constants.KEY_ACTION_SEARCH);
                    intent.putExtra(Constants.KEY_EXTRA_QUERY, mEdtSearch.getText().toString());
                    startService(intent);
                } else {
                    String inputErrorString = getString(R.string.search_validation_error);
                    mEdtSearch.setError(inputErrorString);
                }
            }
        });

        // do a quick lookup to see if any data was persisted
        // to the bundle as a result of a configuration change
        Status[] data = null;
        if (savedInstanceState != null && savedInstanceState.containsKey(Constants.KEY_EXTRA_STATUSES)) {
            data = getStatusesArrayFromParcelledArray(savedInstanceState.getParcelableArray(Constants.KEY_EXTRA_STATUSES));
        }

        // create & configure the RecyclerView's adapter
        mStatusAdapter = new StatusAdapter(this, data, new StatusAdapter.OnStatusClickedListener() {
            @Override
            public void onClick(Status status) {
                // TODO: Display Tweet in fullscreen
                // could look at asking OS to display the tweet in the most appropriate app? (i.e. ACTION_VIEW)
                Toast.makeText(MainActivity.this, String.format("TODO: display tweet fullscreen for @%s", status.user.screenName), Toast.LENGTH_SHORT).show();
            }
        });

        mRvStatuses = (RecyclerView) findViewById(R.id.rvStatuses);
        mRvStatuses.setLayoutManager(new LinearLayoutManager(this));
        mRvStatuses.setAdapter(mStatusAdapter);

        // Ensure that if there is no active session with twitter
        //  that we boot the user on over to the LoginActivity
        TwitterSession session = Twitter.getSessionManager().getActiveSession();
        if (session == null) {
            Log.i(TAG, "There is no active session, sending user to login");
            Intent intentLogin = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intentLogin, REQUEST_LOGIN);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Construct our results receiver intent filter
        IntentFilter resultsIntentFilter = new IntentFilter(Constants.KEY_ACTION_BROADCAST_RESULTS);
        resultsIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        // Instantiates a new receiver register for broadcasts
        mResultsReceiver = new SearchResultsReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mResultsReceiver,
                resultsIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // If the receiver still exists, unregister it and explicitly set it to null
        if (mResultsReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mResultsReceiver);
            mResultsReceiver = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // ensure that we don't forget the results for the current search
        // we only need to worry about the reconstructing the adapter, the editText
        // will remember it's contents because it has an ID configured
        if (mStatusAdapter != null && mStatusAdapter.getItemCount() > 0) {
            outState.putParcelableArray(Constants.KEY_EXTRA_STATUSES, mStatusAdapter.getData());
        } else {
            outState.remove(Constants.KEY_EXTRA_STATUSES);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, String.format("onActivityResult - requestCode: [%s] resultCode: [%s] data: [%s]", requestCode, resultCode, data));
        switch (requestCode) {
            // NOTE: if the user hasn't logged in we can't allow them access to the app
            case REQUEST_LOGIN:
                if (resultCode != Activity.RESULT_OK) {
                    finish();
                }
                break;
        }
    }

    /**
     * Utility method that takes an Array of Parcelable objects and converts it to
     * an Array of Status objects
     * @param parcelledRates The Array of Parcelable objects to convert
     * @return An Array of Status objects
     */
    private Status[] getStatusesArrayFromParcelledArray(Parcelable[] parcelledRates) {
        Status [] statuses = new Status[parcelledRates.length];
        for (int i = 0; i < parcelledRates.length; i++) {
            statuses[i] = (Status)parcelledRates[i];
        }

        return statuses;
    }

    /**
     * Utility method that validates
     * @param input The String to validate
     * @return True if it passes validation rules; Otherwise, false
     */
    private boolean validateUserInput(String input) {
        // This is a naive implementation for now, but we can easily swap it out
        // with something more comprehensive later like a pre-compiled regular expression
        return !TextUtils.isEmpty(input) &&
                input.startsWith("#") &&
                input.length() > 1 &&
                !input.contains(" ");
    }

    /**
     * This class uses the BroadcastReceiver framework to detect and handle intent messages from
     * the SearchService that is performing twitter searches via Twitter's REST API.
     */
    private class SearchResultsReceiver extends BroadcastReceiver {

        private SearchResultsReceiver() {
            // prevent instantiation by other packages.
        }

        /**
         * called by the system when a broadcast Intent is matched by this class'
         * intent filters, this is called on the UI thread.
         *
         * @param context An Android context
         * @param intent The incoming broadcast Intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(Constants.KEY_EXTRA_SUCCESS, false)) {
                Parcelable[] parcelledStatuses = intent.getParcelableArrayExtra(Constants.KEY_EXTRA_STATUSES);
                Status[] statuses = getStatusesArrayFromParcelledArray(parcelledStatuses);
                mStatusAdapter.setData(statuses);
            } else {
                String errorMessage = intent.getStringExtra(Constants.KEY_EXTRA_ERROR_MESSSAGE);
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                mStatusAdapter.setData(null);
            }
        }
    }
}
