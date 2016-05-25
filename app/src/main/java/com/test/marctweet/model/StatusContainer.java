package com.test.marctweet.model;


import com.google.gson.annotations.SerializedName;

public class StatusContainer {

    @SerializedName("statuses")
    public Status[] statuses;

}
