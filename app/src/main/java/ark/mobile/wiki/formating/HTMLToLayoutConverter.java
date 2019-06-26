package ark.mobile.wiki.formating;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import ark.mobile.wiki.jsoup.Jsoup;
import ark.mobile.wiki.jsoup.nodes.Document;
import ark.mobile.wiki.jsoup.nodes.Element;
import ark.mobile.wiki.jsoup.nodes.Node;
import ark.mobile.wiki.page.Page;
import ark.mobile.wiki.page.PageImage;
import ark.mobile.wiki.util.availablePages.AvailablePages;
import ark.mobile.wiki.util.output.Out;
import ark.mobile.wiki.database.DBPage;
import ark.mobile.wiki.formating.layoutBuilder.LayoutBuilder;
import ark.mobile.wiki.util.image.ImageLink;

public class HTMLToLayoutConverter {

    public static final String[] TEXT_ELEMENTS = new String[]{"p","a","span","b","i","u","h2","h3","h4","code","pre","font","small","big","sub"/*horní index*/,"dl"/*neívm k čemu ale bude z něj <b>*/,"br"};
    public static final String[] LIST_ELEMENTS = new String[]{"ul","ol","li"};
    public static final String[] TABLE_ELEMENTS = new String[]{"table","td","th","tbody","thead"};
//    public static final String[] PARENT_ELEMENTS = new String[]{""};

    private List<PageImage> pageImages = new ArrayList<>();

//    public View getLayoutContent(DBPage dbPage, Context context){
//
//        Page page = new Page(context);
//        page.parseContent(dbPage.content);
//
//        return page.getViewContent();
//
//    }

    //uložím vrátím nový HTML, jenom bude zjednodušený
    //z html na layout to přetvořím až později - klikatelný odkazy jsou přes SpannebleString a uložit by je asi nešlo, stejně jako listenery
    public String preprocessing(String html){

        Document oldDoc = Jsoup.parse(html);

        oldDoc.select("*[style*=display:none]").remove();
        oldDoc.select("*.mw-editsection").remove();
        oldDoc.select("*.mw-empty-elt").remove();
        oldDoc.select("style").remove();
        oldDoc.select("script").remove();

        Element dossierImages = oldDoc.getElementsByClass("info-arkitex info-framework").first();

        Element mv_parser_output = oldDoc.getElementsByClass("mw-parser-output").first();

        //vytvořit dossier a odstranit ho z rootu

        Document newDoc = Jsoup.parse("<html><body></body></html>");
        Element root = newDoc.body();

//        Out.println(this, "##########################################################################");
//        Out.println(this, "");
//        Out.println(this, "             Není dodělaný zjednodušení HTML stromu");
//        Out.println(this, "");
//        Out.println(this, "##########################################################################");

        for(int i = 1; i < mv_parser_output.children().size(); i++){
            addSimplifiedNodesToNewTree(mv_parser_output.child(i), root);
        }

        //add dossier

        return " \n"+newDoc.toString();

    }

    private void addSimplifiedNodesToNewTree(Node node, Element into){
        //zajímá mě: h2, h3, všechny textový, div.tabber, img, (table - až později,)

        //TODO
        //TODO co se vyloučí:
        //TODO sekce s videama

        if(!(node instanceof Element)){
            if(!node.toString().trim().equals(""))
                into.appendChild(node.clone());
            return;
        }
        Element e = (Element) node;

//        if(isTextElement(e) || e.tagName().equals(""));
//        if(hasText(e)){
        if(e.tagName().equals("h2")){
            into.appendChild(new Element("h2").text(e.text()));
        }else if(e.tagName().equals("h3")){
            into.appendChild(new Element("h3").text(e.text()));
//        }else if(isRekurziveText(e)){
//            appendSimplifiedTextNodes(e, into);
        }else if(e.tagName().equals("img")){
            ImageLink il = new ImageLink(e.attr("src"));
            if(!il.getImageType().equals("svg")){
                Element img = new Element("img").attr("src",il.URL);
                if(e.hasAttr("width")) img.attr("width",e.attr("width"));
                if(e.hasAttr("height")) img.attr("height",e.attr("height"));
                into.appendChild(img);
            }
        }else if(e.tagName().equals("div")){
            if(e.classNames().contains("tabber")){
                Element parent = new Element("div").addClass("tabber");
                into.appendChild(parent);
                for(Node child : e.childNodes())
                    addSimplifiedNodesToNewTree(child, parent);
            }else if(e.classNames().contains("tabbertab") && into.className().equals("tabber")){
                Element parent = new Element("div").addClass("tabbertab").attr("title", e.attr("title"));
                into.appendChild(parent);
                for(Node child : e.childNodes())
                    addSimplifiedNodesToNewTree(child, parent);
            }else if(hasRelativePosition(e)){
                Element parent = new Element("div").addClass("multiimage");
                into.appendChild(parent);
                for(Node child : e.childNodes())
                    addSimplifiedNodesToNewTree(child, parent);
            }else if(hasAbsolutePosition(e) && e.children().size() != 0 && e.child(0).tagName().equals("img")) {
                Element childImage = new Element("img").attr("src", new ImageLink(e.child(0).attr("src")).URL);
                into.appendChild(childImage);
            }else if(e.classNames().contains("slideboxlightshow")){
            }else{
                for(Node child : e.childNodes())
                    addSimplifiedNodesToNewTree(child, into);
            }
        }else if(isTextElement(e)) {
            Element text;
            if (e.tagName().equals("a")) {
                if (isLinkToAvailablePage(e.attr("href"))) {
//                    Out.println("href",e.attr("href"));
                    text = new Element("a").attr("href", e.attr("href").split("#")[0].substring(1));
                } else {
                    text = new Element("span");
                }
            }else if(e.tagName().equals("font")) {
                text = new Element("font");
                if(!e.attr("color").equals("")) {
                    text.attr("color", e.attr("color"));
                }
            }else{
                text = new Element(e.tagName());
            }
            for(Node child : e.childNodes())
                addSimplifiedNodesToNewTree(child, text);
            into.appendChild(text);
//            }
        //tabulka
        // ostatní elementy
        }else if(isConsiderable(e)){
            Element parent = new Element(e.tagName());
            if(isTableElement(e)){
                String[] checkAttributes = new String[]{"rowspan"};
                for(int i = 0; i < checkAttributes.length; i++)
                    if(!e.attr(checkAttributes[i]).equals(""))
                        e.attr(checkAttributes[i],e.attr(checkAttributes[i]));
            }
            into.appendChild(parent);
            for(Node child : e.childNodes())
                addSimplifiedNodesToNewTree(child, parent);
        }else{
            for(Node child : e.childNodes())
                addSimplifiedNodesToNewTree(child, into);
        }
    }

    public static boolean isLinkToAvailablePage(String link){
//        Out.println("isLinkToAvailablePage? "+link);
        if(link.length() == 0) return false;
//        Out.println("link.length > 0");
        if(link.charAt(0) == '#') return false;
//        Out.println("link.charAt(0) != #");
        return AvailablePages.isPageAvailable(link);
//        return true;
    }


    public static boolean isRekurziveText(Element e){
        if(!isTextElement(e)) return false;
        if(e.children().isEmpty()) return true;
        for(Element child : e.children()){
            if(!isRekurziveText(child)) return false;
        }
        return true;
    }

    public static boolean hasText(Element e){
        if(!isTextElement(e)) return false;
        if(e.text() == null ) return false;
        if(e.text().trim().equals("")) return false;
        return true;
    }

    public static boolean isTableElement(Element e){
        for(int i = 0; i < TABLE_ELEMENTS.length; i++){
            if(e.tagName().equals(TABLE_ELEMENTS[i])) return true;
        }
        return false;
    }

    public static boolean isConsiderable(Element e){
        for(int i = 0; i < LIST_ELEMENTS.length; i++){
            if(e.tagName().equals(LIST_ELEMENTS[i])) return true;
        }
        return isTableElement(e);
    }

    public static boolean isTextElement(Element e){
        for(int i = 0; i < TEXT_ELEMENTS.length; i++){
            if(e.tagName().equals(TEXT_ELEMENTS[i])) return true;
        }
        for(int i = 0; i < LIST_ELEMENTS.length; i++){
            if(e.tagName().equals(LIST_ELEMENTS[i])) return true;
        }
        //obrázek v textu
        if(e.tagName().equals("img")){
            if(!e.hasAttr("width")) return false;
            if(!e.hasAttr("height")) return false;
            int width = Integer.parseInt(e.attr("width"));
            int height= Integer.parseInt(e.attr("height"));
            if(width <= 50 && height <= 50) return true;
        }
        //pro případ, že bych chtěl řešit, že neobsahuje jiný než textový elementy - stránka obsahuje <li> s obrázkem uvnitř
//        if(e.tagName().equals("li")){
//            for(Element child : e.children())
//                if(!isRekurziveText(child)) return false;
//            return true;
//        }
        return false;
    }

    public static boolean hasAttributeOfValue(Element e, String attr, String value){
        if(e.attr(attr).equals(value)) return true;
        String style = e.attr("style");
        if(style.equals("")) return false;
        String[] styleProps = style.split(";");
        for(int i = 0; i < styleProps.length; i++){
            String[] prop = styleProps[i].split(":");
            if(prop[0].trim().equals(attr))
                if(prop[1].trim().equals(value)) return true;
        }
        return false;
    }

    public static boolean hasAbsolutePosition(Element e){
        return hasAttributeOfValue(e, "position", "absolute");
    }

    public static boolean hasRelativePosition(Element e){
        return hasAttributeOfValue(e, "position", "absolute");
    }


}
