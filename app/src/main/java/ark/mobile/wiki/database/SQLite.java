package ark.mobile.wiki.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;

import ark.mobile.wiki.MainActivity;
import ark.mobile.wiki.util.output.Out;

public class SQLite extends SQLiteOpenHelper {

    public static final String FULL_IMAGE_PREFIX = "full_";
    public static final String THUMB_IMAGE_PREFIX = "thumb_";

    private static SQLite instance;
    public static SQLite getInstance(){
        if(instance == null)
            instance = new SQLite(MainActivity.getContext());
        return instance;
    }

    public static final String name = "ArkDatabase";
    public static final int version = 1;

    private String PagesTableName = "pages";
    private String PageName = "p_page";
    private String PageContent = "p_content";
    private String PageVersion = "p_version";
    private String PageDateAdded = "p_added";

    private String ImagesTableName = "images";
    private String ImageURL = "i_URL";
    private String ImageData = "i_data";
    private String ImageThumb = "i_thumb";
    private String ImageThumbSize = "i_thumb_size";
    private String ImageVersion = "i_version";
    private String ImageDateAdded = "i_added";
//    private String ImageType = "i_type";

    private String CrossTableName = "imagesToPages";
//    private String CrossID = "id";
    private String CrossImageID = "c_imageID";
    private String CrossPageID = "c_pageID";


    private SQLite(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "create table "+PagesTableName+" (" +
            PageName + " TEXT PRIMARY KEY,"+
            PageContent + " TEXT,"+
            PageDateAdded + " INTEGER,"+
            PageVersion + " TEXT"+
            ");"
        );
        db.execSQL(
            "create table "+ImagesTableName+" (" +
            ImageURL + " TEXT PRIMARY KEY,"+
            ImageData + " INTEGER,"+
            ImageThumb + " INTEGER,"+
            ImageThumbSize + " INTEGER,"+
            ImageDateAdded + " INTEGER,"+
//            ImageType + " TEXT, "+
            ImageVersion + " TEXT "+
            ");"
        );
        db.execSQL(
            "create table "+CrossTableName+" (" +
//            CrossID + " INTEGER PRIMARY KEY,"+
            CrossPageID + " INTEGER,"+
            CrossImageID + " INTEGER"+
            ");"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        dropAll(db);
        onCreate(db);
    }

    public void dropAll(SQLiteDatabase db){
        db.execSQL("drop table if exists " + PagesTableName + ";");
        db.execSQL("drop table if exists " + ImagesTableName + ";");
        db.execSQL("drop table if exists " + CrossTableName + ";");
    }

    public boolean pageExists(DBPage page){
        Cursor c = getReadableDatabase().rawQuery("select "+PageName+" from "+PagesTableName+" where "+PageName+"='"+page.name+"';", null);
        boolean exists = c.moveToNext();
        closeCursor(c);
        Out.println("Page "+page.name+" exists? "+exists);
        return exists;
    }

    public boolean imageExists(DBImage image){
        Cursor c = getReadableDatabase().rawQuery("select "+ImageVersion+" from "+ImagesTableName+" where "+ImageURL+"='"+image.URL+"';", null);
        boolean exists = c.moveToNext();
        closeCursor(c);
        return exists;
    }

    public boolean imageExistsWithData(DBImage image){
        Cursor c = getReadableDatabase().rawQuery("select 1 from "+ImagesTableName+" where "+ImageURL+"='"+image.URL+"' and "+ImageThumb+"=1 and "+ImageData+"=1;", null);
        boolean exists = c.moveToNext();
        closeCursor(c);
        return exists;
    }

    public boolean isPageAndImageConnected(DBPage page, DBImage image){
        Cursor c = getReadableDatabase().rawQuery("select 1 from "+CrossTableName+" where "+CrossPageID+"='"+page.name+"' and "+CrossImageID+"='"+ImageURL+"';",null);
        boolean exists = c.moveToNext();
        closeCursor(c);
        return exists;
    }

    //zastaralá stránka - pokud neexistuje, vrátí true
    public boolean pageIsObsolete(DBPage page){
        if(page.version == null || page.version == "") return true;
        Cursor cursor = getReadableDatabase().rawQuery("select "+PageVersion+" from "+PagesTableName+" where "+PageName+"='"+page.name+"';", null);
        String savedVersion = "";
        if(cursor.moveToNext()){
            savedVersion = cursor.getString(0);
        }
        closeCursor(cursor);
        Out.println("Page "+page.name+" is obsolete? "+(!page.version.equalsIgnoreCase(savedVersion)));
        return !page.version.equalsIgnoreCase(savedVersion);
    }

    //zastaralý IMG - pokud neexistuje, vrátí true
    public boolean imageIsObsolete(DBImage image){
        if(image.version == null || image.version == "") return true;
        Cursor cursor = getReadableDatabase().rawQuery("select "+ImageVersion+" from "+ImagesTableName+" where "+ImageURL+"='"+image.URL+"';", null);
        String savedVersion = "";
        if(cursor.moveToNext()){
            savedVersion = cursor.getString(0);
        }
        closeCursor(cursor);
        return !image.version.equalsIgnoreCase(savedVersion);
    }

    public boolean pageNeedsRenewing(DBPage page){
        if(!pageExists(page)) return true;
        return pageIsObsolete(page);
    }

    public boolean imageNeedsRenewing(DBImage image){
        if(!imageExists(image)) return true;
        return imageIsObsolete(image);
    }

    public DBPage getPage(String pageName){
        return getPage(new DBPage(pageName));
    }

    public DBPage getPage(DBPage page){
        Cursor cursor = getReadableDatabase().rawQuery("select "+PageVersion+", "+PageContent+" from "+PagesTableName+" where "+PageName+"='"+page.name+"';", null);
        if(cursor.moveToNext()){
            page.version = cursor.getString(0);
            page.content = cursor.getString(1);
        }else{
            return null;
        }
        closeCursor(cursor);
        getImageURLsOfPage(page);
        return page;
    }

    public DBImage getThumbImage(String imageURL){
        return getThumbImage(new DBImage(imageURL));
    }

    public DBImage getThumbImage(DBImage image){
        Cursor cursor = getReadableDatabase().rawQuery("select "+ImageVersion+", "+ImageThumb+", "+ImageThumbSize+" from "+ImagesTableName+" where "+ImageURL+"='"+image.URL+"';", null);
        if(cursor.moveToNext()){
            image.version = cursor.getString(0);
            image.thumb = ImageDiskHandler.load(imageURLToFileName(image.URL, false));
            image.desiredWidth = cursor.getInt(2);
        }else{
            return null;
        }
        closeCursor(cursor);
        return image;
    }

    public DBImage getLargeImage(String imageURL){
        return getLargeImage(new DBImage(imageURL));
    }

    public DBImage getLargeImage(DBImage image){
        Cursor cursor = getReadableDatabase().rawQuery("select "+ImageVersion+", "+ImageData+" from "+ImagesTableName+" where "+ImageURL+"='"+image.URL+"';", null);
        if(cursor.moveToNext()){
            image.version = cursor.getString(0);
            image.data = ImageDiskHandler.load(imageURLToFileName(image.URL, true));
        }else{
            return null;
        }
        closeCursor(cursor);
        return image;
    }

    public boolean addPage(DBPage page){
//        if(pageExists(page)) throw new RuntimeException("Page "+page.name+" already exists in DB.");
        if(pageExists(page)) return false;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues pageData = new ContentValues();
        pageData.put(PageName, page.name);
        pageData.put(PageContent, page.content);
        pageData.put(PageVersion, page.version);
        pageData.put(PageDateAdded, System.currentTimeMillis());
        db.insert(PagesTableName, null, pageData);

//        Out.println(this, "##############################################################################################################");
//        Out.println(this, " ");
//        Out.println(this, "             je potřeba předělat přidání obrázků - takhle se přidá každej znova");
//        Out.println(this, " ");
//        Out.println(this, "##############################################################################################################");
        for(int i = 0; i < page.images.length; i++)
            addOrUpdateImage(page.images[i]);

        addCrossRows(page);
        return true;
    }

    public void updatePage(DBPage page){
        if(!pageExists(page)) return;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues pageData = new ContentValues();
        pageData.put(PageContent, page.content);
        pageData.put(PageVersion, page.version);
        pageData.put(PageDateAdded, System.currentTimeMillis());
        db.update(PagesTableName, pageData, PageName+"='"+page.name+"'", null);
//        db.close();

        for(int i = 0; i < page.images.length; i++){
            if(page.images[i].data != null){
                updateImage(page.images[i]);
            }
        }

        updateCrossRows(page);

    }

    public boolean addOrUpdateImage(DBImage image){
        if(imageExists(image)){
            return updateImage(image);
        }else{
            forceAddImage(image);
        }
        return true;
    }

    public boolean addImage(DBImage image){
//        if(imageExists(image)) throw new RuntimeException("Image with URL "+image.URL+" already exists in DB.");
        if(imageExists(image)) return false;
        forceAddImage(image);
        return true;
    }

    public void forceAddImage(DBImage image){
//        Out.println("ForceAddImage "+image.URL+" with desired width "+image.desiredWidth);
        SQLiteDatabase db = getWritableDatabase();
        ContentValues imageData = new ContentValues();
        imageData.put(ImageURL, image.URL);
        db.insert(ImagesTableName, null, imageData);

        updateImage(image);
//        imageData.put(ImageThumb, 1);
//        imageData.put(ImageData, 1);
//        imageData.put(ImageVersion, image.version);
//        imageData.put(ImageThumbSize, image.desiredWidth);
//        imageData.put(ImageDateAdded, System.currentTimeMillis());
//        if(!ImageDiskHandler.saveFile(imageURLToFileName(image.URL, false), image.thumb)) return;
//        if(!ImageDiskHandler.saveFile(imageURLToFileName(image.URL, true), image.data)) return;
    }

    public boolean updateImage(DBImage image){
//        Out.println("UpdateImage "+image.URL+" with desired width "+image.desiredWidth);
//        Out.println("UpdateImage - "+ Arrays.toString(image.thumb));
        SQLiteDatabase db = getWritableDatabase();
        ContentValues imageData = new ContentValues();
        imageData.put(ImageThumbSize, image.desiredWidth);
        imageData.put(ImageVersion, image.version);
        imageData.put(ImageThumb, ((image.thumb==null)?(0):(1)));
        imageData.put(ImageData, ((image.data==null)?(0):(1)));
        imageData.put(ImageDateAdded, System.currentTimeMillis());
        if(image.thumb != null)
            if(!ImageDiskHandler.saveFile(imageURLToFileName(image.URL, false), image.thumb)) return false;
//        Out.println("Thumb added");
        if(image.data != null)
            if(!ImageDiskHandler.saveFile(imageURLToFileName(image.URL, true), image.data)) return false;
//        Out.println("Full added");
        db.update(ImagesTableName, imageData, ImageURL+"='"+image.URL+"'", null);
        return true;
    }

    private boolean addCrossRows(DBPage page){
        SQLiteDatabase db = getWritableDatabase();
        DBImage[] notConnectedImages = notConnectedImagesWithPage(page);
        for(int i = 0; i < notConnectedImages.length; i++){
            ContentValues crossData = new ContentValues();
            crossData.put(CrossPageID, page.name);
            crossData.put(CrossImageID, notConnectedImages[i].URL);
            db.insert(CrossTableName, null, crossData);
        }
        return true;
    }

    private boolean updateCrossRows(DBPage page){
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.delete(CrossTableName, CrossPageID+"='"+page.name+"'", null);
        addCrossRows(page);
        db.endTransaction();
        return true;
    }

    public DBImage[] connectedImagesWithPage(DBPage page){
        Cursor cursor = getReadableDatabase().rawQuery("select i."+ImageURL+", i."+ImageVersion+" from "+CrossTableName+" c join "+ImagesTableName+" i on c."+CrossImageID+"=i."+ImageURL+" where "+CrossPageID+"='"+page.name+"';",null);
        ArrayList<DBImage> imagesConnected = new ArrayList<>();
        while(cursor.moveToNext()){
            DBImage image = new DBImage(cursor.getString(0));
            image.version = cursor.getString(1);
            imagesConnected.add(image);
        }
        closeCursor(cursor);
        return imagesConnected.toArray(new DBImage[imagesConnected.size()]);
    }

    public DBImage[] notConnectedImagesWithPage(DBPage page){
        DBImage[] connectedImages = connectedImagesWithPage(page);
        ArrayList<DBImage> imagesNotConnected = new ArrayList<>();
        for(int i = 0; i < page.images.length; i++){
            boolean isConnected = false;
            for(int j = 0; j < connectedImages.length; j++){
                if(page.images[i].URL.equals(connectedImages[j])){
                    isConnected = true;
                    break;
                }
            }
            if(!isConnected)
                imagesNotConnected.add(page.images[i]);
        }
        return imagesNotConnected.toArray(new DBImage[imagesNotConnected.size()]);
    }

    public DBImage[] getImageURLsOfPage(String pageName){
        return getImageURLsOfPage(new DBPage(pageName));
    }

    public DBImage[] getImageURLsOfPage(DBPage page){
        if(page.images != null) return page.images;
        //SELECT IMAGENAME FROM PAGETABLE p join crossTable c on p.pagename = c.pagename join imagetable i on c.imageurl = i.url;
        String query = "select i."+ImageURL+", i."+ImageVersion+" from "+ImagesTableName+" i join "+CrossTableName+" c on c."+CrossImageID+"=i."+ImageURL+" join "+PagesTableName+" p on p."+PageName+"=c."+CrossPageID+" where p."+PageName+"='"+page.name+"';";
//        Out.println(this, query);
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        ArrayList<DBImage> DBImages = new ArrayList<>();
        if(cursor.moveToNext()){
            DBImage image = new DBImage(cursor.getString(0));
            image.version = cursor.getString(1);
            DBImages.add(image);
        }
        closeCursor(cursor);
        return DBImages.toArray(new DBImage[DBImages.size()]);
    }

    public DBImage[] getImagesWithMissingData(DBPage page){
        String query = "select "+ImageURL+", "+ImageThumbSize+" from "+ImagesTableName+" i join "+CrossTableName+" c on c."+CrossImageID+"=i."+ImageURL+" join "+PagesTableName+" p on p."+PageName+"=c."+CrossPageID+" where p."+PageName+"='"+page.name+"' and i."+ImageData+"=0;";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        ArrayList<DBImage> DBImages = new ArrayList<>();
        while(cursor.moveToNext()){
            DBImage image = new DBImage(cursor.getString(0));
            image.desiredWidth = cursor.getInt(1);
            DBImages.add(image);
        }
        closeCursor(cursor);
        return DBImages.toArray(new DBImage[DBImages.size()]);
    }

    public DBImage[] getImagesWithData(DBPage page){
        String query = "select "+ImageURL+" from "+ImagesTableName+" i join "+CrossTableName+" c on c."+CrossImageID+"=i."+ImageURL+" join "+PagesTableName+" p on p."+PageName+"=c."+CrossPageID+" where p."+PageName+"='"+page.name+"' and i."+ImageData+"=1;";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        ArrayList<DBImage> DBImages = new ArrayList<>();
        while(cursor.moveToNext()){
            DBImage image = new DBImage(cursor.getString(0));
            DBImages.add(image);
        }
        closeCursor(cursor);
        return DBImages.toArray(new DBImage[DBImages.size()]);
    }



    public DBImage[] getObsoleteImages(DBPage page){

        if(page.images == null || page.images.length == 0)
            return null;

        String query = "select "+ImageURL+", "+ImageVersion+" from "+ImagesTableName+" i join "+CrossTableName+" c on c."+CrossImageID+"=i."+ImageURL+" join "+PagesTableName+" p on p."+PageName+"=c."+CrossPageID+" where p."+PageName+"='"+page.name+"';";
//        Out.println(this, query);
        Cursor cursor = getReadableDatabase().rawQuery(query, null);

        ArrayList<DBImage> obsoleteImages = new ArrayList<>();
        while(cursor.moveToNext()){
            DBImage image = new DBImage(cursor.getString(0));
            image.version = cursor.getString(1);
            for(int i = 0; i < page.images.length; i++){
                if(!image.URL.equals(page.images[i].URL)) continue;
                if(!image.version.equals(page.images[i].version))
                    obsoleteImages.add(image);
                break;
            }
        }
        closeCursor(cursor);
        return obsoleteImages.toArray(new DBImage[obsoleteImages.size()]);
    }

    public void printAllContent(){
        Out.println(this, " ");
        Out.println(this, " ");
        Out.println(this, "###############################################################################################");
        Out.println(this, "Printing all DB content");
        Out.println(this, " ");
        Out.println(this, "Pages:");
        Out.println(this, "-----------------------------------------------------------------------------------------------");
        Cursor cursor;
        cursor = getReadableDatabase().rawQuery("select "+PageName+", "+PageVersion+" from "+PagesTableName+";", null);
        while(cursor.moveToNext()){
            DBPage page = new DBPage(cursor.getString(0));
            page.version = cursor.getString(1);
            Out.println(this, page);
        }
        closeCursor(cursor);
        Out.println(this, " ");
        Out.println(this, "Images:");
        Out.println(this, "-----------------------------------------------------------------------------------------------");
        cursor = getReadableDatabase().rawQuery("select "+ImageURL+", "+ImageVersion+" from "+ImagesTableName+" where "+ImageData+"=0;", null);
        while(cursor.moveToNext()){
            DBImage image = new DBImage(cursor.getString(0));
            image.version = cursor.getString(1);
            Out.println(this, image);
        }
        closeCursor(cursor);
        cursor = getReadableDatabase().rawQuery("select "+ImageURL+", "+ImageVersion+" from "+ImagesTableName+" where "+ImageData+"=1;", null);
        while(cursor.moveToNext()){
            DBImage image = new DBImage(cursor.getString(0));
            image.version = cursor.getString(1);
            image.data = new byte[]{0};
            Out.println(this, image);
        }
        Out.println(this, " ");
        Out.println(this, "Cross table:");
        Out.println(this, "-----------------------------------------------------------------------------------------------");
        closeCursor(cursor);
        cursor = getReadableDatabase().rawQuery("select "+CrossPageID+", "+CrossImageID+" from "+CrossTableName+";", null);
        while(cursor.moveToNext()){
            Out.println(this, cursor.getString(0)+" -- "+cursor.getString(1));
        }
        closeCursor(cursor);
        Out.println(this, " ");
        Out.println(this, "End of printing");
        Out.println(this, "###############################################################################################");
        Out.println(this, " ");

    }

    public void printImages(){
        Cursor cursor;
        Out.println(this, "###############################################################################################");
        Out.println(this, "Printing images");
        Out.println(this, "");
        Out.println(this, "------------------------------");
        Out.println(this, "Not null thumb and data");
        cursor = getReadableDatabase().rawQuery("select "+ImageURL+" from "+ImagesTableName+" where "+ImageThumb+"=1 and "+ImageData+"=1;", null);
        while(cursor.moveToNext()){
            DBImage image = new DBImage(cursor.getString(0));
            Out.println(this, image);
        }
        closeCursor(cursor);
        Out.println(this, "");
        Out.println(this, "------------------------------");
        Out.println(this, "Null thumb and not null data");
        cursor = getReadableDatabase().rawQuery("select "+ImageURL+" from "+ImagesTableName+" where "+ImageThumb+"=0 and "+ImageData+"=1;", null);
        while(cursor.moveToNext()){
            DBImage image = new DBImage(cursor.getString(0));
            Out.println(this, image);
        }
        closeCursor(cursor);
        Out.println(this, "");
        Out.println(this, "------------------------------");
        Out.println(this, "Not null thumb and null data");
        cursor = getReadableDatabase().rawQuery("select "+ImageURL+" from "+ImagesTableName+" where "+ImageThumb+"=1 and "+ImageData+"=0;", null);
        while(cursor.moveToNext()){
            DBImage image = new DBImage(cursor.getString(0));
            Out.println(this, image);
        }
        closeCursor(cursor);
        Out.println(this, "");
        Out.println(this, "------------------------------");
        Out.println(this, "Null thumb and data");
        cursor = getReadableDatabase().rawQuery("select "+ImageURL+" from "+ImagesTableName+" where "+ImageThumb+"=0 and "+ImageData+"=0;", null);
        while(cursor.moveToNext()){
            DBImage image = new DBImage(cursor.getString(0));
            Out.println(this, image);
        }
        closeCursor(cursor);
        Out.println(this, "End of printing");
        Out.println(this, "###############################################################################################");

    }

    private void closeCursor(Cursor cursor){
        if (cursor != null && !cursor.isClosed())
            cursor.close();
    }

    public String imageURLToFileName(String input, boolean full){
        String name = input.replaceAll("/","[");
        if(full)
            name = FULL_IMAGE_PREFIX+name;
        else
            name = THUMB_IMAGE_PREFIX+name;
        return name;
    }

    public String fileNameToImageURL(String input){
        int nameBegin = name.indexOf('_');
        String name = input.substring(nameBegin+1);
        name.replaceAll("\\[","/");
        return name;
    }

    public void removeImage(DBImage image){
        Cursor c = getWritableDatabase().rawQuery("delete from "+ImagesTableName+" where "+ImageURL+"='"+image.URL+"';", null);
        closeCursor(c);
    }

}
