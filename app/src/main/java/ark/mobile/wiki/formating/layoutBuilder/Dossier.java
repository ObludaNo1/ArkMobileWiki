package ark.mobile.wiki.formating.layoutBuilder;

import ark.mobile.wiki.jsoup.nodes.Element;
import ark.mobile.wiki.jsoup.select.Elements;

public class Dossier{

    private Element root;
    private Elements modules;

    /*
    Children:
    0 - images
    1 - spicies info
    2 - release versions
    3 - spawn commands
    4 - domestication info
    5 - some other data
    6 - reproduction
    7 - habitat
    */

    public Dossier (Element dossier){
        this.root = dossier;
        modules = root.children();

        Element module0 = modules.get(0);
        Elements module0Images = module0.getElementsByTag("img");
        if(module0Images.size() != 5) throw new RuntimeException("Images are not known!");

    }



}