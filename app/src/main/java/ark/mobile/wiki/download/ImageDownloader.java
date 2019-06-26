package ark.mobile.wiki.download;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.ByteBuffer;

import ark.mobile.wiki.MainActivity;
import ark.mobile.wiki.formating.layoutBuilder.LayoutBuilder;
import ark.mobile.wiki.util.output.Out;
import ark.mobile.wiki.database.DBImage;
import ark.mobile.wiki.database.SQLite;
import ark.mobile.wiki.util.image.DownloadableImage;
import ark.mobile.wiki.util.image.ImageLink;
import ark.mobile.wiki.util.units.UnitConverter;

public class ImageDownloader extends AsyncTask<DownloadableImage, Integer, Void> {

    public static final float IMAGE_THUMB_SIZE_INCREASE_RATIO = 5f;

    private boolean downloaded = false;
    private OnDownloadListener whenDownloadedListener;


    public ImageDownloader(){}

    public void setWhenDownloadedListener(OnDownloadListener listener){
        whenDownloadedListener = listener;
    }

    @Override
    protected Void doInBackground(DownloadableImage... images) {
        if(whenDownloadedListener == null)
            throw new RuntimeException("No action is defined after download is complete. Use setWhenDownloadListener().");

        HttpURLConnection con = null;
        try {
            DownloadableImage image = images[0];
            String pageURL = image.getImageFullURL();
            long beginTime = System.currentTimeMillis();

            URL url = new URL(pageURL);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode >= 400)
                throw new RuntimeException("The following response code - " + responseCode + " - was returned after requesting content from page " + pageURL);

            InputStream input = con.getInputStream();
            byte[] imageData = extract(input);
            input.close();
            con.disconnect();

            DBImage dbImage = image.toDBImage();
            dbImage.data = imageData;

            if(!dbImage.getImageType().equals("svg")){

                Bitmap bitmap = BitmapFactory.decodeByteArray(dbImage.data, 0, dbImage.data.length);

                int width;
                if(image.getDesiredWidth() == -1)
                    width = MainActivity.getDisplayWidth();
                else{
                    width = (int)(image.getDesiredWidth()*IMAGE_THUMB_SIZE_INCREASE_RATIO+.5f);
//                    float textSize = UnitConverter.dpFromPx(LayoutBuilder.BASIC_TEXT_SIZE);

                }

                if(!(bitmap.getWidth() < width)){
//                    Out.println("Width: "+width+", height: "+(int)(bitmap.getHeight()*width*1f/bitmap.getWidth()+.5f));
                    Bitmap resized = Bitmap.createScaledBitmap(bitmap, width, (int)(bitmap.getHeight()*width*1f/bitmap.getWidth()+.5f), true);
//                    int size = resized.getRowBytes() * resized.getHeight();
//                    ByteBuffer byteBuffer = ByteBuffer.allocate(size);
//                    resized.copyPixelsToBuffer(byteBuffer);

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    resized.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    dbImage.thumb = byteArray;
                }else{
                    dbImage.thumb = dbImage.data;
                }
            }else{
                dbImage.thumb = dbImage.data;
            }

            saveToDatabase(dbImage);

            long downloadTime = System.currentTimeMillis();
            Out.println("Download time: " + (downloadTime - beginTime) + " ms of image "+new ImageLink(image.getImageFullURL()).URL+" - data size: full image = "+dbImage.data.length+"B, thumb = "+dbImage.thumb.length+"B");

            downloaded = true;
            whenDownloadedListener.completed();


        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(con != null)
                con.disconnect();
        }

        return null;
    }

    private void saveToDatabase(DBImage image){
        SQLite db = SQLite.getInstance();
        if(db.imageExistsWithData(image)){
            db.updateImage(image);
        }else{
            db.addImage(image);
        }
    }

    private byte[] extract(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read = 0;
        while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, read);
        }
        baos.flush();
        return baos.toByteArray();
    }

    public boolean isComplete(){
        return downloaded;
    }

}
