package ark.mobile.wiki.formating.layoutBuilder;

import java.util.ArrayList;

import ark.mobile.wiki.jsoup.nodes.Element;
import ark.mobile.wiki.jsoup.select.Elements;

public class WikiSections {

    public class Subsection{
        public Elements elements = new Elements();
        public String subsectionTitle;
        public boolean isEmpty = true;

        public void add(Element e){
            elements.add(e);
            isEmpty = false;
        }

    }

    public class WikiSection{
        public ArrayList<Subsection> subsections = new ArrayList<>();
        public Subsection lastSub = new Subsection();
        public String sectionTitle;
        public boolean isEmpty = true;

        WikiSection(){
            subsections.add(lastSub);
        }

        void add(Element e){
            if(e.tagName().equals("h3")){
                lastSub = new Subsection();
                subsections.add(lastSub);
                lastSub.subsectionTitle = e.text();
            }
            lastSub.add(e);
            isEmpty = false;
        }


    }

    public ArrayList<WikiSection> sections = new ArrayList<>();
    public WikiSection lastSec = new WikiSection();

    public WikiSections(){
        sections.add(lastSec);
    }

    public void add(Element e){
        //když přijde h2, založí se nová sekce
        if(e.tagName().equals("h2")){
            lastSec = new WikiSection();
            sections.add(lastSec);
            lastSec.sectionTitle = e.text();
        }
        lastSec.add(e);
    }

}
