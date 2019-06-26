package ark.mobile.wiki.formating;

import java.util.ArrayList;

import ark.mobile.wiki.jsoup.Jsoup;
import ark.mobile.wiki.jsoup.nodes.Document;
import ark.mobile.wiki.jsoup.nodes.Element;
import ark.mobile.wiki.jsoup.select.Elements;
import ark.mobile.wiki.util.output.Out;
import ark.mobile.wiki.util.collections.BinaryTree;
import ark.mobile.wiki.database.DBImage;
import ark.mobile.wiki.util.image.ImageLink;

public class DownloadedJSONFormater {

    public String formatRevid(String json){
        int begin = json.indexOf("\"revid\"")+8;
        int i = begin;
        while(Character.isLetterOrDigit(json.charAt(i++)));
        return json.substring(begin, i-1);
    }

    public String formatText(String json){

        long beginTime = System.currentTimeMillis();

        int textStartPos = json.indexOf("\"text\"")+7;

        if(json.charAt(textStartPos) == '{'){
            textStartPos = json.indexOf(":", textStartPos);
            textStartPos = json.indexOf("\"", textStartPos);
            textStartPos++;
        }else if(json.charAt(textStartPos) == '"'){
            textStartPos++;
        }else{
            throw new RuntimeException("Unexpected character at position "+(textStartPos)+": "+json.charAt(textStartPos));
        }

        int textEndPos = searchForTextEnd(json, textStartPos);

        json = json.substring(textStartPos, textEndPos);

        json = json.replaceAll("\\\\n", "\n");
        json = json.replaceAll("\\\\t", "\t");
        json = replaceUnicode(json);
        json = json.replaceAll("\\\\\"", "\"");

        long endTime = System.currentTimeMillis();
        Out.println(this, "Parse time: "+ (endTime-beginTime)+" ms");

        return json;
    }

    public DBImage[] imageListFromHTML(String html){
//        boolean jeTrue = false;
//        if(jeTrue) throw new RuntimeException("Je to špatně. Stahují se miniatury. Je potřeba stáhnout plnou velikost. Nevybírat SRC obrázku, ale href jeho rodičovskýho <a>.\n" +
//                "https://ark.gamepedia.com/api.php?page=File:Dossier_Rex.png&prop=text&action=parse&format=json →\n" +
//                "znova stáhnout novou stránku, parsovat HTML, najít ten obrázek.");
//        if(jeTrue) throw new RuntimeException("Odstranit duplicity.");
//        if(jeTrue) throw new RuntimeException("Nepotřebuju ukládat celý URL. https://gamepedia.cursecdn.com/arksurvivalevolved_gamepedia/ je všude stejný.");

        //Stačí parsovat ten odkaz
        /*
        https://gamepedia.cursecdn.com/arksurvivalevolved_gamepedia/thumb/4/42/Dossier_Rex.png/388px-Dossier_Rex.png?version=d53c7fe473f62ae7d7b39d70248cfc67

                        ↓↓↓

        https://gamepedia.cursecdn.com/arksurvivalevolved_gamepedia/4/42/Dossier_Rex.png?version=d53c7fe473f62ae7d7b39d70248cfc67

        odstranit "/thumb" a poslední část před verzí
         */

        Document doc = Jsoup.parse(html);
        Elements imgs = doc.getElementsByTag("img");

        ArrayList<ImageLink> imageLinks = new ArrayList<>();
        BinaryTree<ImageLink> imageURLs = new BinaryTree<>();

        for(Element e : imgs){
            imageLinks.add(new ImageLink(e.absUrl("src")));
        }
//        Out.println(this, "Image srcs = "+imgs.size());
        imageURLs.addAll(imageLinks);
        Out.println(this, "Added all srcs to binary tree");
        imageLinks = imageURLs.toArrayList();
        Out.println(this, "Added all srcs to arraylist");
        imageURLs = null;
        Out.println(this, "Images before: "+imgs.size()+", images after: "+imageLinks.size());

        DBImage[] images = new DBImage[imageLinks.size()];
        int it = 0;
        for(ImageLink il : imageLinks) images[it++] = il.toDBImage();
        return images;
    }

    public DBImage[] imageListFromLayout(String layout){
        throw new RuntimeException("Not defined yet.");
    }


    static int searchForTextEnd(String s, int startAt){
        for(int i = startAt + 1; i < s.length(); i++)
            if(s.charAt(i) == '"' && s.charAt(i-1) != '\\')
                return i;
        return -1;
    }

    private static String replaceUnicode(String json) {
//        Out.println("DownloadedJSONFormater error - Replace unicode characters is not done yet.");
        ArrayList<String> unicodes = new ArrayList<>();
        for(int i = 0; i < json.length()-5; i++){
            if(json.charAt(i) == '\\' && json.charAt(i+1) == 'u'){
                unicodes.add(json.substring(i+2, i+6));
                i+=5;
            }
        }
        for(int i = 0; i < unicodes.size(); i++){
            json = json.replaceFirst("\\\\u....",Character.toString((char)Integer.parseInt(unicodes.get(i), 16)));
        }
        return json;
    }


}
