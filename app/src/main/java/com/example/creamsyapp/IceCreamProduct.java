package com.example.creamsyapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class IceCreamProduct implements Parcelable {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    // Gunakan JsonAdapter untuk memastikan parsing yang benar
    @SerializedName("price")
    @JsonAdapter(DoubleTypeAdapter.class)
    private double price;

    @SerializedName("stock")
    @JsonAdapter(IntegerTypeAdapter.class)
    private int stock;

    @SerializedName("image_res_id")
    @JsonAdapter(IntegerTypeAdapter.class)
    private int imageResId;

    public IceCreamProduct(String id, String name, double price, int stock, int imageResId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.imageResId = imageResId;
    }

    // Constructor default untuk Gson
    public IceCreamProduct() {
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
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

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

    // Adapter untuk parsing double dari JSON
    public static class DoubleTypeAdapter extends TypeAdapter<Double> {
        @Override
        public void write(JsonWriter out, Double value) throws IOException {
            out.value(value);
        }

        @Override
        public Double read(JsonReader in) throws IOException {
            try {
                switch (in.peek()) {
                    case NUMBER:
                        return in.nextDouble();
                    case STRING:
                        return Double.parseDouble(in.nextString());
                    default:
                        in.skipValue();
                        return 0.0;
                }
            } catch (Exception e) {
                in.skipValue();
                return 0.0;
            }
        }
    }

    // Adapter untuk parsing integer dari JSON
    public static class IntegerTypeAdapter extends TypeAdapter<Integer> {
        @Override
        public void write(JsonWriter out, Integer value) throws IOException {
            out.value(value);
        }

        @Override
        public Integer read(JsonReader in) throws IOException {
            try {
                switch (in.peek()) {
                    case NUMBER:
                        return in.nextInt();
                    case STRING:
                        return Integer.parseInt(in.nextString());
                    default:
                        in.skipValue();
                        return 0;
                }
            } catch (Exception e) {
                in.skipValue();
                return 0;
            }
        }
    }
}