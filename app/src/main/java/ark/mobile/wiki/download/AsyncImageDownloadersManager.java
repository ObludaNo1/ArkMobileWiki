package ark.mobile.wiki.download;

import ark.mobile.wiki.util.output.Out;
import ark.mobile.wiki.util.collections.LinkedQueue;
import ark.mobile.wiki.util.image.DownloadableImage;
import ark.mobile.wiki.util.image.ImageLink;

public class AsyncImageDownloadersManager {

    public static final int DEFAULT_DOWNLOAD_THREAD_COUNT = 1;

    private static final int QUEUE_ADD = 0;
    private static final int QUEUE_REMOVE = 1;
    private static final int ASSIGN_DOWNLOADER = 2;

    private int totalDownloads;
    private int downloadsFinished;

    private long timeFirstDownloadBegin;
    private long timeAllDownloadsEnded;

    private int nextFreeSlot = 0;

    private boolean isCompleted;
    private OnDownloadListener whenDownloadedListener;

    private ImageDownloader[] downloaders;

    private LinkedQueue<DownloadableImage> downloadQueue;

    public AsyncImageDownloadersManager( OnDownloadListener listener){
        this(1, listener);
    }

    public AsyncImageDownloadersManager(int threadsToDownload, OnDownloadListener listener){
        this.downloaders = new ImageDownloader[1];
        this.downloadQueue = new LinkedQueue<>();
        this.isCompleted = true;
        this.totalDownloads = 0;
        this.downloadsFinished = 0;
        this.whenDownloadedListener = listener;
    }

    public void runDownload(DownloadableImage image){
        Out.println("runDownload() with "+new ImageLink(image.getImageFullURL()).URL);
        if(timeFirstDownloadBegin == 0) timeFirstDownloadBegin = System.currentTimeMillis();
        doAction(QUEUE_ADD, image, -1);
    }

    public void runDownloads(DownloadableImage[] images){
        Out.println(this, "Download image count: "+images.length);
        for(int i = 0; i < Math.min(images.length, 10); i++)
            runDownload(images[i]);
    }

    private synchronized void doAction(int action, DownloadableImage image, int slot){
        if(action == QUEUE_ADD) addImage(image);
//        if(action == QUEUE_REMOVE) removeFromQueue(image);
        if(action == ASSIGN_DOWNLOADER) assignDownloaderAndRemoveFromQueue(slot);
    }

    private void addImage(DownloadableImage image) {
        isCompleted = false;
        totalDownloads++;

        int slot = getFreeDownloaderSlot();
        if(slot > -1)
            assignDownloader(slot, image);
        else
            downloadQueue.add(image);
    }

    private void assignDownloaderAndRemoveFromQueue(final int slot){
//        doAction(QUEUE_REMOVE, image, -1);
        DownloadableImage image = downloadQueue.getAndRemoveFirst();
        if(image != null)
            assignDownloader(slot, image);

    }

    private void assignDownloader(final int slot, final DownloadableImage image){
        Out.println("Download slot "+slot+" assigned to "+new ImageLink(image.getImageFullURL()).URL);
        downloaders[slot] = new ImageDownloader();
        downloaders[slot].setWhenDownloadedListener(new OnDownloadListener() {@Override public void completed() {
            downloadsFinished++;
            doAction(ASSIGN_DOWNLOADER, null, slot);
            checkAllDownloaded();
        }});
        downloaders[slot].execute(image);
    }

    private synchronized void checkAllDownloaded() {
        if(downloadsFinished == totalDownloads){
            isCompleted = true;
            timeAllDownloadsEnded = System.currentTimeMillis();
            long totalDownloadTime = timeAllDownloadsEnded - timeFirstDownloadBegin;
            Out.println(this, "Total download time: "+totalDownloadTime/1000L+"s "+totalDownloadTime%1000L+"ms.");
            whenDownloadedListener.completed();
        }
    }

    private int getFreeDownloaderSlot(){
        if(nextFreeSlot < downloaders.length) return nextFreeSlot++;
        return -1;
    }

    public double downloadedRatio(){
        if(totalDownloads == 0) return Double.NaN;
        return ((double) downloadsFinished)/totalDownloads;
    }

}
