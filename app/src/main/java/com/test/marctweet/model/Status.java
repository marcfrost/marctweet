package com.test.marctweet.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Status implements Parcelable {

    @SerializedName("text")
    public String text;

    @SerializedName("user")
    public User user;

    protected Status(Parcel in) {
        text = in.readString();
        user = in.readParcelable(User.class.getClassLoader());
    }

    public static final Creator<Status> CREATOR = new Creator<Status>() {
        @Override
        public Status createFromParcel(Parcel in) {
            return new Status(in);
        }

        @Override
        public Status[] newArray(int size) {
            return new Status[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(text);
        parcel.writeParcelable(user, i);
    }
}