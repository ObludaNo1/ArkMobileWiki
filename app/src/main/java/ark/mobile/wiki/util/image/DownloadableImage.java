package ark.mobile.wiki.util.image;

import ark.mobile.wiki.database.DBImage;

public interface DownloadableImage {

    public String getImageFullURL();

    public String getImageType();

    public DBImage toDBImage();

    public int getDesiredWidth();

}
