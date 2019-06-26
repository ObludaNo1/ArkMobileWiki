package ark.mobile.wiki.database;

public class DBPage {

    public final String name;
    public String content;
    public String version;
    public DBImage[] images;

    public DBPage(String name) {
        this.name = name;
    }

    public DBPage(String name, String content, String version, DBImage[] images) {
        this.name = name;
        this.content = content;
        this.version = version;
        this.images = images;
    }

    @Override
    public String toString() {
        return "Page <"+name+"> of version <"+version+">, "+((content == null || "".equals(content))?("does not have data"):("has data"))+" and "+((images==null)?("does not contain images"):("has "+images.length+" images"))+".";
    }

}
