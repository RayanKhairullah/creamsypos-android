package com.example.creamsyapp;

import android.os.Parcel;
import android.os.Parcelable;

public class IceCreamProduct implements Parcelable {
    private String id;
    private String name;
    private double price;
    private int stock;

    public IceCreamProduct(String id, String name, double price, int stock) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    protected IceCreamProduct(Parcel in) {
        id = in.readString();
        name = in.readString();
        price = in.readDouble();
        stock = in.readInt();
    }

    public static final Creator<IceCreamProduct> CREATOR = new Creator<IceCreamProduct>() {
        @Override
        public IceCreamProduct createFromParcel(Parcel in) {
            return new IceCreamProduct(in);
        }

        @Override
        public IceCreamProduct[] newArray(int size) {
            return new IceCreamProduct[size];
        }
    };

    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeDouble(price);
        dest.writeInt(stock);
    }
}