package com.example.creamsyapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Transaction implements Parcelable {
    @SerializedName("id")
    private String id;

    @SerializedName("items")
    private List<IceCreamProduct> items;

    @SerializedName("total")
    private double total;

    @SerializedName("timestamp")
    private Date timestamp;

    public Transaction(String id, List<IceCreamProduct> items, double total, Date timestamp) {
        this.id = id;
        this.items = items;
        this.total = total;
        this.timestamp = timestamp;
    }

    protected Transaction(Parcel in) {
        id = in.readString();
        items = in.createTypedArrayList(IceCreamProduct.CREATOR);
        total = in.readDouble();
        long time = in.readLong();
        timestamp = time == -1 ? null : new Date(time);
    }

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

    // Getters
    public String getId() { return id; }
    public List<IceCreamProduct> getItems() { return items; }
    public double getTotal() { return total; }
    public Date getTimestamp() { return timestamp; }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(timestamp);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeTypedList(items);
        dest.writeDouble(total);
        dest.writeLong(timestamp != null ? timestamp.getTime() : -1);
    }
}