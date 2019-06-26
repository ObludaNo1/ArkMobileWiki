package ark.mobile.wiki;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ark.mobile.wiki.database.DBImage;
import ark.mobile.wiki.database.SQLite;
import ark.mobile.wiki.photoview.PhotoView;
import ark.mobile.wiki.util.output.Out;

public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        PhotoView photoView = findViewById(R.id.photoViewPhoto);
        SQLite db = SQLite.getInstance();
        String imageURL = getIntent().getStringExtra("imageURL");
        DBImage dbImage = db.getLargeImage(imageURL);
        Out.println("Launched image activity with image "+imageURL);
        if(dbImage.data == null) finish();
        Bitmap bitmap = BitmapFactory.decodeByteArray(dbImage.data, 0, dbImage.data.length);
        photoView.setImageBitmap(bitmap);
    }
}
