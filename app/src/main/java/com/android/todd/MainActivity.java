/*
 * Copyright (c) 2012, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.android.todd;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.sfnative.SalesforceActivity;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Main activity
 */
public class MainActivity extends SalesforceActivity {

    private RestClient client;
    private CustomListAdapter listAdapter;
    private ListView listView;
    private ProgressDialog pDialog;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup view
        setContentView(R.layout.main);

        // TODO: Add the SwipeRefresh function to the listView.
//        SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);

        // Create list adapter
        listAdapter = new CustomListAdapter(this, new ArrayList<FeedItem>());
        listView = ((ListView) findViewById(android.R.id.list));
        listView.setAdapter(listAdapter);

    }
	
	@Override 
	public void onResume() {
		// Hide everything until we are logged in
		findViewById(R.id.root).setVisibility(View.INVISIBLE);

		super.onResume();
	}		
	
	@Override
	public void onResume(RestClient client) {
        // Keeping reference to rest client
        this.client = client; 

		// Show everything
        View v = findViewById(R.id.root);
        v.setVisibility(View.VISIBLE);

        try {
            sendNewsFeedRequest(v);
        } catch (UnsupportedEncodingException e) {
            Log.d("NEWS_FEED_EXCEPTION:", e.toString());
        }

	}

    public void sendNewsFeedRequest(View v) throws UnsupportedEncodingException {
        sendChatterRequest("chatter/feeds/news/me/feed-elements");
    }

	public void onLogoutClick(View v) {
		SalesforceSDKManager.getInstance().logout(this);
	}

    private void sendChatterRequest(String uri) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForChatterRetrieve(getString(R.string.api_version), uri);

        client.sendAsync(restRequest, new AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, RestResponse result) {
                Log.d("CHATTER_API_RESPONSE:", result.toString());
                try {
                    listAdapter.clear();

                    JSONArray elements = result.asJSONObject().getJSONArray("elements");
                    for (int i = 0; i < elements.length(); i++) {
                        if (elements.getJSONObject(i).getString("feedElementType").equals("FeedItem")) {
                            String displayName = elements.getJSONObject(i).getJSONObject("actor").getString("displayName");
                            String message = elements.getJSONObject(i).getJSONObject("body").getString("text");
                            String photoUrl = elements.getJSONObject(i).getJSONObject("actor").getJSONObject("photo").getString("fullEmailPhotoUrl");

                            String createdDate = elements.getJSONObject(i).getString("createdDate");

                            FeedItem fItem = new FeedItem(displayName, message, photoUrl, createdDate);

                            listAdapter.add(fItem);
                        }
                    }
                    if (listAdapter.getCount() == 0)
                        listView.setEmptyView(findViewById(android.R.id.empty));
                } catch (Exception e) {
                    onError(e);
                }
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(MainActivity.this,
                        MainActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
