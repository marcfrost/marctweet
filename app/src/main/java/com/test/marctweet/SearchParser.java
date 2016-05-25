package com.test.marctweet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.test.marctweet.model.StatusContainer;
import com.test.marctweet.model.Status;

public class SearchParser {

    /**
     * This method parses the json string
     *
     * @param json a JSON String ready to be parsed
     * @throws IllegalArgumentException thrown if an IO error occurs during parsing
     */
    public Status[] parseJson(final String json) throws IllegalArgumentException {
        if (json == null) {
            throw new IllegalArgumentException("Can't parse a null string");
        }

        Gson gson = new GsonBuilder().create();
        StatusContainer searchResponse = gson.fromJson(json, StatusContainer.class);
        return searchResponse.statuses;
    }
}
