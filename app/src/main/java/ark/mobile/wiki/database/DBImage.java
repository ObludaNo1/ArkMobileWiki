package ark.mobile.wiki.database;

import ark.mobile.wiki.util.image.DownloadableImage;

public class DBImage implements DownloadableImage {

    public static final String URLPrefix = "https://gamepedia.cursecdn.com/arksurvivalevolved_gamepedia/";

    public final String URL;
    public String version;
    public byte[] data;
    public byte[] thumb;
    public int desiredWidth;

    public DBImage(String URL) {
        this.URL = URL;
    }

    public DBImage(String URL, byte[] data, String version) {
        this.URL = URL;
        this.data = data;
        this.version = version;
    }

    @Override
    public String toString() {
        return "Image - "+URL+", version "+version+", "+((data==null)?("no data"):("+ data"))+", "+((thumb==null)?("no thumb"):("+ thumb"))+".";
    }

    public static String toFullURL(String URL, String version){
        return URLPrefix + URL + ((version == null || "".equals(version))?("?version="+version):(""));
    }

    @Override
    public String getImageFullURL() {
        return toFullURL(URL, version);
    }

    @Override
    public String getImageType() {
        int index = URL.lastIndexOf(".");
        return URL.substring(index+1);
    }

    @Override
    public DBImage toDBImage() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(this == obj) return true;
        if(obj instanceof DBImage){
            DBImage im = (DBImage) obj;
            return (URL+"_"+version).equals(im.URL+"_"+im.version);
        }
        return false;
    }

    @Override
    public int getDesiredWidth() {
        return desiredWidth;
    }
}
