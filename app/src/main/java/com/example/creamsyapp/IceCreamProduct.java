package com.example.creamsyapp;

import android.os.Parcel;
import android.os.Parcelable;

public class IceCreamProduct implements Parcelable {
    private String id;
    private String name;
    private double price;
    private int stock;
    private int imageResId; // Menyimpan resource ID gambar

    public IceCreamProduct(String id, String name, double price, int stock, int imageResId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.imageResId = imageResId;
    }

    protected IceCreamProduct(Parcel in) {
        id = in.readString();
        name = in.readString();
        price = in.readDouble();
        stock = in.readInt();
        imageResId = in.readInt();
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
    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

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
        dest.writeInt(imageResId);
    }
}