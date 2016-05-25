package com.test.marctweet;

import com.google.gson.JsonSyntaxException;
import com.test.marctweet.model.Status;
import com.test.marctweet.search.SearchParser;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests behaviour of the SearchParser
 */
public class SearchParserUnitTests {

    @Test
    public void parseDeserializesRealResponseSuccessfully() throws Exception {
        SearchParser parser = new SearchParser();
        Status[] statuses = parser.parseJson(ParseTestData.SEARCH_RESPONSE);

        // some checks that ensure that for a deterministic
        // set of data that the results are as expected
        assertNotNull(statuses);
        assertTrue(statuses.length == 2);
        assertNotNull(statuses[0].user);
        assertNotNull(statuses[1].user);
        assertNotNull(statuses[0].user.screenName);
        assertNotNull(statuses[1].user.screenName);
    }

    @Test(expected=IllegalArgumentException.class)
    public void parseThrowsIllegalArgumentExceptionForNullArgument() throws Exception {
        SearchParser parser = new SearchParser();
        parser.parseJson(null);
    }

    @Test(expected=JsonSyntaxException.class)
    public void parseThrowsExceptionForBogusJson() throws Exception {
        SearchParser parser = new SearchParser();
        parser.parseJson("Wait a minute... That's not JSON!");
    }
}