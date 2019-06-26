package ark.mobile.wiki.util.image;

import ark.mobile.wiki.database.DBImage;
import ark.mobile.wiki.util.output.Out;

public class ImageLink implements Comparable<ImageLink>, DownloadableImage {

    public String fullURL;
    public String URL;
    public String version;
    public int desiredWidth = -1;

    public ImageLink(){}

    public ImageLink(String fullURL){
        this.fullURL = fullURL;
        String shortURL;
        String[] URLSplit;
        if(fullURL.contains("thumb/")){
            shortURL = fullURL.replaceFirst("https:\\/\\/gamepedia\\.cursecdn\\.com\\/arksurvivalevolved_gamepedia\\/thumb\\/", "");
            if(shortURL.contains("?version=")){
                URLSplit = shortURL.split("\\/[^\\/]*?\\?version=");
            }else{
                URLSplit = shortURL.split("\\/[^\\/]*?$");
            }
            int lastSlash = shortURL.lastIndexOf('/');
            String thumbSize = "";
            while(Character.isDigit(shortURL.charAt(++lastSlash)))
                thumbSize+=shortURL.charAt(lastSlash);
            desiredWidth = Integer.parseInt(thumbSize);

        }else{
            shortURL = fullURL.replaceFirst("https:\\/\\/gamepedia\\.cursecdn\\.com\\/arksurvivalevolved_gamepedia\\/", "");
            URLSplit = shortURL.split("\\?version=");
        }
//        Out.println("Image "+fullURL+" parsed: desiredWidth = "+desiredWidth);
        URL = URLSplit[0];
        if(URLSplit.length > 1)
            version = URLSplit[1];
        else
            version = "";
//        Out.println("imagelink", fullURL+" --- "+URL);
    }

    @Override
    public DBImage toDBImage(){
        DBImage image = new DBImage(URL);
        image.version = version;
        image.desiredWidth = desiredWidth;
        return image;
    }

    @Override
    public String toString() {
        return "ImageLink - URL = "+URL+" of version "+version;
    }

    @Override
    public int compareTo(ImageLink il) {
        return il.URL.compareTo(this.URL);
    }

    @Override
    public String getImageFullURL() {
        return fullURL;
    }

    @Override
    public String getImageType() {
        int index = URL.lastIndexOf(".");
        return URL.substring(index+1);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(this == obj) return true;
        if(obj instanceof ImageLink){
            ImageLink il = (ImageLink) obj;
            return (URL+"_"+version).equals(il.URL+"_"+il.version);
        }
        return false;
    }

    @Override
    public int getDesiredWidth() {
        return desiredWidth;
    }
}
