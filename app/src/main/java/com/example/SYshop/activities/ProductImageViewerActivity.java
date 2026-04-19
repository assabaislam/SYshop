package com.example.SYshop.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager2.widget.ViewPager2;

import com.example.SYshop.R;
import com.example.SYshop.adapters.ZoomableImagePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ProductImageViewerActivity extends BaseActivity {

    public static final String EXTRA_IMAGE_URL = "extra_image_url";
    public static final String EXTRA_IMAGE_RES_LIST = "extra_image_res_list";
    public static final String EXTRA_START_POSITION = "extra_start_position";

    private ViewPager2 imagePager;
    private TextView indicatorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_image_viewer);

        imagePager = findViewById(R.id.imagePager);
        indicatorText = findViewById(R.id.indicatorText);
        ImageView closeBtn = findViewById(R.id.closeBtn);

        closeBtn.setOnClickListener(v -> finish());

        ArrayList<Integer> imageResList = getIntent().getIntegerArrayListExtra(EXTRA_IMAGE_RES_LIST);
        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        int startPosition = Math.max(getIntent().getIntExtra(EXTRA_START_POSITION, 0), 0);

        List<Integer> safeImages = imageResList == null ? new ArrayList<>() : imageResList;
        ZoomableImagePagerAdapter adapter = new ZoomableImagePagerAdapter(this, safeImages, imageUrl);
        imagePager.setAdapter(adapter);

        if (startPosition < adapter.getItemCount()) {
            imagePager.setCurrentItem(startPosition, false);
        }

        updateIndicator(imagePager.getCurrentItem(), adapter.getItemCount());
        imagePager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicator(position, adapter.getItemCount());
            }
        });
    }

    private void updateIndicator(int position, int total) {
        indicatorText.setText((position + 1) + " / " + Math.max(total, 1));
    }
}
