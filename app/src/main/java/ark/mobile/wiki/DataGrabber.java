package ark.mobile.wiki;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import ark.mobile.wiki.download.ImageDownloader;
import ark.mobile.wiki.formating.layoutBuilder.LayoutBuilder;
import ark.mobile.wiki.page.Page;
import ark.mobile.wiki.database.DBImage;
import ark.mobile.wiki.download.AsyncImageDownloadersManager;
import ark.mobile.wiki.download.OnDownloadListener;
import ark.mobile.wiki.formating.OnViewCreationListener;
import ark.mobile.wiki.util.output.Out;
import ark.mobile.wiki.util.network.InternetCheck;
import ark.mobile.wiki.database.DBPage;
import ark.mobile.wiki.database.SQLite;
import ark.mobile.wiki.download.SyncJSONDownload;
import ark.mobile.wiki.formating.DownloadedJSONFormater;
import ark.mobile.wiki.formating.HTMLToLayoutConverter;

public class DataGrabber extends AsyncTask<String, Void, Integer>{

    private Page page;
    private Context context;

    private OnViewCreationListener listener;

    public DataGrabber(Context context, OnViewCreationListener onComplete) {this.context = context; this.listener = onComplete;}

    @Override
    protected Integer doInBackground(String... strings) {
        grabPage(strings[0]);
        return null;
    }

    public void grabPage(String pageName){

//        SQLite db = SQLite.getInstance();
//        DBImage image = new DBImage("rex156.png");
//        db.removeImage(image);
//
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        MainActivity.getEmptyImage().compress(Bitmap.CompressFormat.PNG, 100, stream);
//        byte[] byteArray = stream.toByteArray();
//
//        image.thumb = byteArray;
//        image.data  = image.thumb;
//        image.desiredWidth = 30;
//        image.version = "verze";
//        boolean success = db.addImage(image);
//        Out.println("Added image: "+image);
//        Out.println("Added? "+success);
//        DBImage returned = db.getThumbImage(image.URL);
//        Out.println("Returned image: "+returned+", desiredWidth = "+returned.desiredWidth);
//
//
//        ImageView imageView = LayoutBuilder.getImageView(context);
//        imageView.setImageBitmap(BitmapFactory.decodeByteArray(returned.thumb, 0, returned.thumb.length));
//        LinearLayout layout = LayoutBuilder.getLinearLayout(context);
//        layout.addView(LayoutBuilder.getBasicTextView(context, "Before image"));
//        layout.addView(imageView);
//        layout.addView(LayoutBuilder.getBasicTextView(context, "After image"));
//
//        Out.println("Původní a vrácená data se rovnají? "+ Arrays.equals(image.thumb, returned.thumb));
//        Out.println(Arrays.toString(image.thumb));
//        Out.println(Arrays.toString(returned.thumb));
//        listener.completed(layout);
//
//        if(true) return;

        page = new Page(context, new OnViewCreationListener() { @Override public void completed(View view) {
            listener.completed(view);
        }});
        if(InternetCheck.isOnline()){
            Out.println(this, "Device is online! Internet is available.");
            downloadNeededContent(pageName);
        }else{
            renderFromDB(pageName);
        }
    }

    private void renderFromDB(String pageName) {
//        if(true) return;
        Out.println(this, "render from DB");
        SQLite db = SQLite.getInstance();
//        db.printAllContent();
//        HTMLToLayoutConverter converter = new HTMLToLayoutConverter();
        DBPage dbPage = db.getPage(pageName);
        page.parseContent(dbPage.content);

        DBImage[] alreadyDownloadedImages = db.getImagesWithData(dbPage);
        for(int i = 0; i < alreadyDownloadedImages.length; i++){
            page.imageIsReady(db.getThumbImage(alreadyDownloadedImages[i]));
        }

        View view = page.getViewContent();
        listener.completed(view);
    }

    private void downloadNeededContent(final String pageName) {

        final SQLite db = SQLite.getInstance();
        SyncJSONDownload syncDownloader = new SyncJSONDownload();
        DownloadedJSONFormater formater = new DownloadedJSONFormater();
        HTMLToLayoutConverter converter = new HTMLToLayoutConverter();

        String pageRevid = syncDownloader.download(pageName, SyncJSONDownload.version);
        if(pageRevid == null) {
            renderFromDB(pageName);
            return;
        } //když selže download
        pageRevid = formater.formatRevid(pageRevid);

        Out.println(this, "Revid naformátováno = "+pageRevid);

        DBPage dbPage = new DBPage(pageName);
        dbPage.version = pageRevid;

        if(!db.pageNeedsRenewing(dbPage)){
            renderFromDB(pageName);
            return;
        }

        Out.println(this, "Stránka potřebuje stáhnout.");
//        if(true) return;
        String pageText = syncDownloader.download(pageName, SyncJSONDownload.html);
        if(pageText == null){
            renderFromDB(pageName);
            return;
        }
        pageText = formater.formatText(pageText);
        dbPage.images = formater.imageListFromHTML(pageText);
        pageText = converter.preprocessing(pageText);
//        String[] pageTextArray = pageText.split("\n");
//        for(int i = 0; i < pageTextArray.length; i++){
//            Out.println(" ",pageTextArray[i]);
//        }
        dbPage.content = pageText;
//        Out.println(this, pageText);
        Out.println(this, "Content was printed");
        Out.println(this, "Vytvořen seznam IMG z HTML");

        if(!db.pageExists(dbPage))
            db.addPage(dbPage);
        else
            db.updatePage(dbPage);

//        DBPage fromDB = db.getPage(page.name);
        Out.println(this, "Page from DB");
//        Out.println(" ", fromDB.content);
        Out.println(this, "V DB vytvořen / updatován záznam");
//        if(true)return;

        final DBImage[] redownloadImages = db.getImagesWithMissingData(dbPage);

//        AsyncImageDownloadersManager downloads = new AsyncImageDownloadersManager(AsyncImageDownloadersManager.DEFAULT_DOWNLOAD_THREAD_COUNT, new OnDownloadListener() {@Override public void completed() {
//            Out.println("all was succesfully completed");
//            renderFromDB(pageName);
//        }});
        for(int i = 0; i < redownloadImages.length; i++){
            ImageDownloader imageDownloader = new ImageDownloader();
            final int finalI = i;
            imageDownloader.setWhenDownloadedListener(new OnDownloadListener() { @Override public void completed() {
                page.imageIsReady(db.getThumbImage(redownloadImages[finalI]));
            }});
            imageDownloader.execute(redownloadImages[i]);
        }
//        Out.println(this, "page.images.length: "+page.images.length);
//        Out.println(this, "redownloadImages.length: "+redownloadImages.length);
//        downloads.runDownloads(redownloadImages);
//        for(int i = 0; i < redownloadImages.length; i++)
//            downloads.runDownload(redownloadImages[i]);
//        downloads.runDownload(redownloadImages[0]);
        renderFromDB(pageName);

    }

}
