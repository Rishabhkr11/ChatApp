package rishabh.example.whatsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity
{

    private PhotoView imageView;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageView = findViewById(R.id.image_viewer);
        imageUrl = getIntent().getStringExtra("url");

        Picasso.get().load(imageUrl).into(imageView);
    }
}