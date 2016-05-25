package com.test.marctweet;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.test.marctweet.model.Status;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;

import java.io.IOException;
import java.util.Map;

public class SearchService extends IntentService {

    private static final String TAG = "SearchService";

    /**
     * Compile time constant that allows us to toggle VERBOSE logging on for this class
     * Nice to have: No risk of these logs happening on a release build */
    private static final boolean VERBOSE_DEBUGGING = BuildConfig.DEBUG && true;

    private static final String HOST = "api.twitter.com";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_TYPE_JSON = "application/json";

    private final OkHttpClient okHttpClient;
    private final LocalBroadcastManager mBroadcaster;

    public SearchService() {
        super(TAG);
        okHttpClient = new OkHttpClient();
        mBroadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Basic configuration of the intent that will be used
        // to broadcast the results of this operation
        // Note: we assume failure
        Intent localIntent = new Intent();
        localIntent.setAction(Constants.KEY_ACTION_BROADCAST_RESULTS);
        localIntent.putExtra(Constants.KEY_EXTRA_SUCCESS, false);
        localIntent.addCategory(Intent.CATEGORY_DEFAULT);

        try {
            String action = intent.getAction();
            String query = intent.getStringExtra(Constants.KEY_EXTRA_QUERY);
            if (!Constants.KEY_ACTION_SEARCH.equals(action)) {
                throw new UnsupportedOperationException("Only twitter searches are currently supported");
            }

            if (TextUtils.isEmpty(query)) {
                throw new IllegalArgumentException("A search MUST contain a query");
            }

            // Build the URL that we'll be making a request to
            HttpUrl url = new HttpUrl.Builder()
                    .scheme("https")
                    .host(HOST)
                    .addPathSegment("1.1")
                    .addPathSegment("search")
                    .addPathSegment("tweets.json")
                    .addQueryParameter("q", query)
                    .build();

            // User can only use the app if they've authenticated using their
            // twitter account therefore we can expect an active session to exist
            // we use this session to retrieve the headers required for OAuth
            TwitterSession session = Twitter.getSessionManager().getActiveSession();
            TwitterAuthConfig authConfig = TwitterCore.getInstance().getAuthConfig();
            Map<String, String> authHeader = session.getAuthToken().getAuthHeaders(authConfig, "GET", url.toString(), null);

            // Configure request headers
            Headers.Builder headersBuilder = new Headers.Builder();
            headersBuilder.set(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_JSON);
            for (Object o : authHeader.entrySet()) {
                Map.Entry pair = (Map.Entry) o;
                headersBuilder.set((String) pair.getKey(), (String) pair.getValue());
            }
            Headers headers = headersBuilder.build();
            if (VERBOSE_DEBUGGING) {
                Log.i(TAG, String.format("HEADERS built=\n%s", headers));
            }

            // build the actual request object we're going to use & execute it synchronously
            Request request = new Request.Builder()
                    .url(url)
                    .headers(headers)
                    .get()
                    .build();
            if (VERBOSE_DEBUGGING) {
                Log.i(TAG, String.format("Initiating HTTP %s request to url=[%s]", request.method(), url));
            }

            Response response = okHttpClient.newCall(request).execute();
            String jsonResponse = response.body().string();
            if (VERBOSE_DEBUGGING) {
                Log.i(TAG, String.format("The response is: \nResponseCode: %s \nResponseBody: %s", response.code(), jsonResponse));
            }

            // if the response success determines the way we finalize the broadcast
            if (response.isSuccessful()) {
                SearchParser parser = new SearchParser();
                Status[] statuses = parser.parseJson(jsonResponse);
                localIntent.putExtra(Constants.KEY_EXTRA_SUCCESS, true);
                localIntent.putExtra(Constants.KEY_EXTRA_STATUSES, statuses);
            } else {
                // TODO: provide more meaningful error messages to the user
                // I don't want to simply display the API's error message to the user as this
                // creates issues if we were to say... localise the application
                // Refer to API documentation for known response codes, what they mean
                // https://dev.twitter.com/overview/api/response-codes
                localIntent.putExtra(Constants.KEY_EXTRA_ERROR_MESSSAGE, getString(R.string.search_error_generic));
            }
        } catch (IOException ioe) {
            Log.e(TAG, "There was a problem executing the HTTP request", ioe);
            localIntent.putExtra(Constants.KEY_EXTRA_ERROR_MESSSAGE, getString(R.string.search_error_io));
        } catch (Throwable t) {
            Log.e(TAG, "There was a problem.", t);
            localIntent.putExtra(Constants.KEY_EXTRA_ERROR_MESSSAGE, getString(R.string.search_error_generic));
        } finally {
            mBroadcaster.sendBroadcast(localIntent);
        }
    }
}
