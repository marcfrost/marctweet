## Overview ##
The intention of this project is to just be a quick test of the twitter REST API in the form of a basic Android application. Requests to the Twitter search API are performed in the authenticated user context, for the search API this allows up to 180 requests per 15 minute window (https://dev.twitter.com/rest/public/rate-limiting). Upon Signing in, the user is able to perform a search for tweets containing a given hashtag. 

## Main Application Components ##
* LoginActivity - facilitates sign in process
* MainActivity - facilitates search using specified hashtag (Requires that the user has signed in)
* SearchService - Intent service to offload network requests off of the UI Thread

## Libs used ##
* Fabric Twitter SDK (TwitterCore) - Facilitates authentication of the user's Twitter account, supports persistence 
* OKHTTP - used for the networking layer, executing http requests to the REST API
* GSON - used for deserialization of JSON in the responses we receive back from the Twitter REST API 

## Screenshots: ##
![alt login-1](http://i.imgur.com/UImo8tn.png?1) ![alt login-2](http://i.imgur.com/rWEqjKI.png?1) ![alt main-1](http://i.imgur.com/NTb2pAN.png?1) ![alt main-2](http://i.imgur.com/vGsvf2d.png?1) 
