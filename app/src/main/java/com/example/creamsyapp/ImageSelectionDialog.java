package com.example.creamsyapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ImageSelectionDialog extends Dialog {
    private OnImageSelectedListener listener;
    private int selectedImage = -1;

    public interface OnImageSelectedListener {
        void onImageSelected(int imageResId);
    }

    public ImageSelectionDialog(@NonNull Context context, OnImageSelectedListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_image_selection);

        GridView gridView = findViewById(R.id.image_grid);
        ImageAdapter adapter = new ImageAdapter(getContext());
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) {
                    int imageResId = getImageResources().get(position);
                    listener.onImageSelected(imageResId);
                }
                dismiss();
            }
        });
    }

    private List<Integer> getImageResources() {
        List<Integer> imageResources = new ArrayList<>();
        imageResources.add(R.drawable.ic_default_product);
        return imageResources;
    }

    private class ImageAdapter extends BaseAdapter {
        private Context context;

        public ImageAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return getImageResources().size();
        }

        @Override
        public Object getItem(int position) {
            return getImageResources().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(120, 120));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(getImageResources().get(position));
            return imageView;
        }
    }
}