package ark.mobile.wiki.page;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import ark.mobile.wiki.MainActivity;
import ark.mobile.wiki.database.DBImage;
import ark.mobile.wiki.formating.OnViewCreationListener;
import ark.mobile.wiki.jsoup.Jsoup;
import ark.mobile.wiki.jsoup.nodes.Document;
import ark.mobile.wiki.jsoup.nodes.Element;
import ark.mobile.wiki.jsoup.nodes.Node;
import ark.mobile.wiki.formating.HTMLToLayoutConverter;
import ark.mobile.wiki.formating.layoutBuilder.LayoutBuilder;
import ark.mobile.wiki.formating.layoutBuilder.WikiSections;
import ark.mobile.wiki.util.output.Out;

public class Page {

//    public static ImageView imageCheck;

    private ViewGroup root;
    private List<LinearLayout> listSectionLayouts;
    private List<PageImage> images;
    private List<TextViewContainer> textViewContainers;
    private ArrayList<Tabber2> tabbers;
    public static final int MARGIN = 20;

    private Context context;

    private int tabberLayer;
    private boolean parseIsCompleted;
    private boolean firstSectionDone;
    private OnViewCreationListener firstViewReadyListener;

    public Page(Context context, OnViewCreationListener firstViewReadyListener){
        root = LayoutBuilder.getLinearLayout(context);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)root.getLayoutParams();
        params.setMargins(MARGIN,MARGIN,MARGIN,MARGIN);
        root.setLayoutParams(params);

        tabbers = new ArrayList<>();
        images = new ArrayList<>();
        textViewContainers = new ArrayList<>();

        tabberLayer = -1;

        this.context = context;

        parseIsCompleted = false;

        listSectionLayouts = new ArrayList<>();

        this.firstViewReadyListener = firstViewReadyListener;
    }

    private Tabber2 getLastTabber(){
        return tabbers.get(tabbers.size()-1);
    }

    public void parseContent(String content) {

        Document doc = Jsoup.parse(content);
        Element rootElement = doc.body();
        long timeBegin = System.currentTimeMillis();
//        Out.println(rootElement);

        WikiSections sections = new WikiSections();
        for(Element e : rootElement.children()){
            sections.add(e);
        }
        for(WikiSections.WikiSection section : sections.sections){
            if(section.isEmpty) continue;
            Out.println("Section title");
            if("Spotlight".equalsIgnoreCase(section.sectionTitle)) continue;
            if("References".equalsIgnoreCase(section.sectionTitle)) continue;
            if("Contents".equalsIgnoreCase(section.sectionTitle)) continue;

            LinearLayout sectionLayout = LayoutBuilder.getLinearLayout(context);
            root.addView(sectionLayout);
            listSectionLayouts.add(sectionLayout);

            for(WikiSections.Subsection subsection : section.subsections){
                Out.println("Subsection title = " + subsection.subsectionTitle);
                if("Taming food".equalsIgnoreCase(subsection.subsectionTitle)) continue;
                if("Base Stats and Growth".equalsIgnoreCase(subsection.subsectionTitle)) continue;
                if(subsection.isEmpty) continue;
                LinearLayout subsectionLayout = LayoutBuilder.getLinearLayout(context);
                sectionLayout.addView(subsectionLayout);


                for(Element e : subsection.elements){
//                    Out.println("SUBSECTION");
//                    Out.println(e);
                    addToLayout(context, e, subsectionLayout);
                }

            }

//            if(1 == root.getChildCount()){
//                firstSectionDone = true;
//                firstViewReadyListener.completed(sectionLayout);
//            }
        }
//        Out.println("First View = "+listSectionLayouts.get(0));
//        Out.println("Parent = "+listSectionLayouts.get(0).getParent());
//        Out.println("Parent View = "+((ViewGroup)listSectionLayouts.get(0).getParent()));
//        Out.println("Parent View child count = "+((ViewGroup)listSectionLayouts.get(0).getParent()).getChildCount());
//        ((ViewGroup)listSectionLayouts.get(0).getParent()).removeView(listSectionLayouts.get(0));
//        for(int i = 0; i < listSectionLayouts.size(); i++){
//            root.addView(listSectionLayouts.get(i));
//        }
        Out.println("To layout converting: "+(System.currentTimeMillis()-timeBegin)+"ms");

//        imageCheck = LayoutBuilder.getImageView(context);
//        root.addView(imageCheck);

        parseIsCompleted = true;

    }

    private void addToLayout(Context context, Node node, ViewGroup into){

        if(!(node instanceof Element)){
            if(!node.toString().trim().equals(""))
                into.addView(LayoutBuilder.getBasicTextView(context, node.toString()));
            return;
        }
        final Element e = (Element) node;
//        Out.println("##########################################################################");
//        Out.println(e+"\nis rekurzive text ? "+ HTMLToLayoutConverter.isRekurziveText(e));
        if(e.tagName().equals("h2")){
            into.addView(LayoutBuilder.getHeader2(context, e.wholeText()));
        }else if(e.tagName().equals("h3")){
            into.addView(LayoutBuilder.getHeader3(context, e.wholeText()));
        }else if(HTMLToLayoutConverter.isRekurziveText(e)){
            TextViewContainer container = LayoutBuilder.getTextViewContainer(context, e);
            into.addView(container.textView);
            textViewContainers.add(container);
        }else if(e.tagName().equals("img")){
//            Out.println("Image "+e.attr("src"));
            ImageView imageView = LayoutBuilder.getImageLinkWithLink(context, e);
            String source = e.attr("src");
            imageView.setImageBitmap(MainActivity.getEmptyImage());
            PageImage image = new PageImage(imageView, source);
            images.add(image);
            into.addView(imageView);
        }else if(e.tagName().equals("div")){
            if(e.classNames().contains("tabber")){
                LinearLayout tabberLayout = LayoutBuilder.getLinearLayout(context);
                into.addView(tabberLayout);
                tabberLayer++;
                if(tabberLayer == 0)
                    tabbers.add(new Tabber2(tabberLayout, context));
                else
                    throw new RuntimeException("this is not yet");
    //                    getLastTabber().addTabber(tabberLayer, tabberLayout);
                for(Node child : e.childNodes())
                    addToLayout(context, child, into);
                tabberLayer--;
            }else if(e.classNames().contains("tabbertab")){
                LinearLayout layout = LayoutBuilder.getLinearLayout(context);
                getLastTabber().addTab(tabberLayer, layout, e.attr("title"), context);
                for(Node child : e.childNodes())
                    addToLayout(context, child, layout);
            }
//        }
//        }else if(HTMLToLayoutConverter.isConsiderable(e)){
//            Element parent = new Element(e.tagName());
//            if(HTMLToLayoutConverter.isTableElement(e)){
//                String[] checkAttributes = new String[]{"rowspan"};
//                for(int i = 0; i < checkAttributes.length; i++)
//                    if(!e.attr(checkAttributes[i]).equals(""))
//                        e.attr(checkAttributes[i],e.attr(checkAttributes[i]));
//            }
//            into.appendChild(parent);
//            for(Node child : e.childNodes())
//                addToLayout(child, parent);
        }else{
            for(Node child : e.childNodes())
                addToLayout(context, child, into);
        }
    }

    public View getViewContent(){
        return root;
    }

    public View getViewSectionContent(int sectionID) {return listSectionLayouts.get(sectionID);}

    public synchronized void imageIsReady(DBImage image){
//        if(true) return;
        if(!parseIsCompleted){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(image.thumb == null || image.thumb.length == 0){
            Out.println("Null thumb byte[]");
            Out.println(image);
            return;
        }
        Bitmap decodedByte = BitmapFactory.decodeByteArray(image.thumb, 0, image.thumb.length);
        ImageView imageView = LayoutBuilder.getImageView(context);
        imageView.setImageBitmap(decodedByte);
//        MainActivity.addToView(imageView, root);
//        root.addView(imageView);
        for(int i = 0; i < textViewContainers.size(); i++){
            int index = textViewContainers.get(i).containsImage(image.URL);
            if(index != -1){
                textViewContainers.get(i).replaceImage(decodedByte, index);
                break;
            }
        }
        for(int i = 0; i < images.size(); i++){
            if(images.get(i).image.equals(image.URL)){
                MainActivity.replaceImageBitmap(decodedByte, images.get(i).imageView);
                break;
            }
        }
    }



}
