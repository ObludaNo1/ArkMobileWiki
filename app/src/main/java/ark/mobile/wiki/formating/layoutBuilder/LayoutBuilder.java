package ark.mobile.wiki.formating.layoutBuilder;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ark.mobile.wiki.ImageViewActivity;
import ark.mobile.wiki.MainActivity;
import ark.mobile.wiki.R;
import ark.mobile.wiki.formating.HTMLToLayoutConverter;
import ark.mobile.wiki.jsoup.nodes.Attribute;
import ark.mobile.wiki.jsoup.nodes.Attributes;
import ark.mobile.wiki.jsoup.nodes.Element;
import ark.mobile.wiki.jsoup.nodes.Node;
import ark.mobile.wiki.page.TextViewContainer;
import ark.mobile.wiki.util.boxing.IntegerBox;
import ark.mobile.wiki.util.output.Out;

public abstract class LayoutBuilder {

    public static final float HEADER_2_SIZE = 27f;
    public static final float HEADER_3_SIZE = 20f;
    public static final float HEADER_4_SIZE = 18f;
    public static final float TAB_TITLE_SIZE = 18f;
    public static final float BASIC_TEXT_SIZE = 16f;

    public static final String LINK_COLOR = "#397d75";

//    private static ImageSpan myImage;

    public static Button getButton(Context context, String text){
        Button button = new Button(context);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        button.setLayoutParams(params);
        button.setTransformationMethod(null);
        button.setText(text);
        return button;
    }

    public static HorizontalScrollView getHorizontalScrollView(Context context){
        HorizontalScrollView view = new HorizontalScrollView(context);
        HorizontalScrollView.LayoutParams params = new HorizontalScrollView.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
        view.setFillViewport(true);
        return view;
    }

    public static LinearLayout getLinearLayout(Context context){
        LinearLayout layout = new LinearLayout(context);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(linearLayoutParams);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    public static TextView getBasicTextView(Context context, String text){
        TextView textView = getEmptyTextView(context);
        textView.setText(text);
        return  textView;
    }

    public static SpannableString getTextFromElement(Context context, Element e){

        Element edited = new Element(e.tagName());
        editAndCopyElementText(e, edited);
        SpannableString ss = new SpannableString(edited.wholeText());
        TextViewContainer container = new TextViewContainer(null, ss);
        appendSpanToText(context, container, edited, new IntegerBox(0));
        return container.ss;
    }

    public static TextViewContainer getTextViewContainer(Context context, Element e){
        TextView textView = getEmptyTextView(context);

        Element edited = new Element(e.tagName());
        editAndCopyElementText(e, edited);

//        Out.println("##########################################################################");
//        Out.println(e+"\nis rekurzive text");
//        Out.println(edited+"\nis rekurzive text ? "+ HTMLToLayoutConverter.isRekurziveText(e));

        SpannableString ss = new SpannableString(edited.wholeText());

        TextViewContainer container = new TextViewContainer(textView, ss);

        appendSpanToText(context, container, edited, new IntegerBox(0));

        container.reApplyText();

        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);

        return container;
    }

    public static TextView getStyledTextView(Context context, Element e){

        TextView textView = getEmptyTextView(context);

        textView.setText(getTextFromElement(context, e));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);
        return textView;

    }

    public static void editAndCopyElementText(Element patern, Element copy){

//        Out.println(patern);
        boolean addNewLine = false;
        if(patern.tagName().equals("li")){
            copy.append("\u2022 ");
            addNewLine = true;
        }

        Attributes attrs = patern.attributes();
        for(Attribute attr : attrs)
            copy.attr(attr.getKey(), attr.getValue());


        if(patern.tagName().equals("br") || patern.tagName().equals("p"))
            addNewLine = true;

        for(Node child : patern.childNodes()){

            if(child instanceof Element) {

                Element childE = (Element) child;
                Element copyChild = new Element(childE.tagName());

                copy.appendChild(copyChild);
                editAndCopyElementText(childE, copyChild);
            }else{
                copy.append(child.toString());
            }
        }
        if(addNewLine)
            copy.append("\n");
        if(patern.tagName().equals("img")){
            copy.append("\u2b1b");
//            Out.println("There is an image while copying elements: "+patern);
        }

    }

    private static void appendSpanToText(final Context context, TextViewContainer container, final Element e, final IntegerBox index) {

//        Out.println("Append to span: "+e);
        if(e.tagName().equals("a") && HTMLToLayoutConverter.isLinkToAvailablePage(e.attr("href"))){
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override public void onClick(View textView) {
//                    Out.println(this, "Clicked on "+e.toString());
//                        Toast.makeText(context, "Clicked on part of text", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("page_name",e.attr("href"));
                    context.startActivity(intent);
                }
                @Override public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            container.ss.setSpan(clickableSpan, index.value, e.wholeText().length()+index.value, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        Object span = null;
        if(e.tagName().equals("a")){
            span = new ForegroundColorSpan(Color.parseColor(LINK_COLOR));
        }else if(e.tagName().equals("b") || e.tagName().equals("dl")){
            span = new StyleSpan(Typeface.BOLD);
        }else if(e.tagName().equals("i")){
            span = new StyleSpan(Typeface.ITALIC);
        }else if(e.tagName().equals("font")){
            if(e.hasAttr("color")){
                span = new ForegroundColorSpan(Color.parseColor(e.attr("color")));
            }
        }else if(e.tagName().equals("code")){
            Typeface font = Typeface.createFromAsset(MainActivity.getActivity().getAssets(), "fonts/SourceCodePro-Regular.ttf");
            span = new CustomTypefaceSpan(font);
            container.ss.setSpan(span, index.value, e.wholeText().length()+index.value, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span = new BackgroundColorSpan(Color.WHITE);
            container.ss.setSpan(span, index.value, e.wholeText().length()+index.value, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span = new ForegroundColorSpan(Color.DKGRAY);
            container.ss.setSpan(span, index.value, e.wholeText().length()+index.value, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }else if(e.tagName().equals("u")){
        }else if(e.tagName().equals("h4")){
        }else if(e.tagName().equals("small")){
        }else if(e.tagName().equals("big")){
        }else if(e.tagName().equals("sub")){
        }

        //"p","a","span","b","i","u","h2","h3","h4","code","pre","font","small","big","sub"/*horní index*/

        if(span != null) container.ss.setSpan(span, index.value, e.wholeText().length()+index.value, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        if(e.tagName().equals("img")) {
            ImageSpan imageSpan = new ImageSpan(MainActivity.getEmptyImage());
            container.ss.setSpan(imageSpan, index.value, 1+index.value, 0);
            container.addImage(index.value, e.attr("src"));
//            Out.println("Span <"+e.tagName()+"> from "+index.value+" to "+(e.wholeText().length()+index.value)+" - value:\n"+e);
        }

//        Out.println("Span <"+e.tagName()+"> from "+index.value+" to "+(e.wholeText().length()+index.value));

        for(Node child : e.childNodes()){
//            Out.println("Child {"+child+"}");
            if(!(child instanceof Element)){
                String text = child.toString();
                int numberOfSpecialCharsSpace = 0;
                boolean inSpecialChar = false;
                for(int i = 0; i < text.length(); i++){
                    if(text.charAt(i) == '&') inSpecialChar = true;
                    if(text.charAt(i) == ';') inSpecialChar = false;
                    if(inSpecialChar) numberOfSpecialCharsSpace++;
                }
                index.value += child.toString().length() - numberOfSpecialCharsSpace;
            }else{
                Element childE = (Element) child;
                appendSpanToText(context, container, childE, index);
            }
        }

    }

    public static TextView getEmptyTextView(Context context){
        TextView textView = new TextView(context);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(linearLayoutParams);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, BASIC_TEXT_SIZE);
        textView.setTextColor(Color.BLACK);
        return textView;
    }

    public static TextView getHeader2(Context context, String text){
        TextView textView = getEmptyTextView(context);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, HEADER_2_SIZE);
        return textView;
    }

    public static TextView getHeader3(Context context, String text){
        TextView textView = getEmptyTextView(context);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, HEADER_3_SIZE);
        return textView;
    }

    public static TextView getHeader4(Context context, String text){
        TextView textView = getEmptyTextView(context);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, HEADER_4_SIZE);
        return textView;
    }

    public static TextView getTabHeader(Context context, String text){
        TextView textView = getEmptyTextView(context);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TAB_TITLE_SIZE);
        return textView;
    }

    public static ImageView getImageView(Context context){
        ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(linearLayoutParams);
        return imageView;
    }

    public static ImageView getImageLinkWithLink(final Context context, final Element e){
        ImageView imageView = new ImageView(context);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(linearLayoutParams);
        imageView.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View view) {
            Intent intent = new Intent(context, ImageViewActivity.class);
            intent.putExtra("imageURL", e.attr("src"));
            context.startActivity(intent);

        }});
        return imageView;
    }



//    public static HorizontalScrollView getTable(Context context, Element e){
//        HorizontalScrollView wrapper = getHorizontalScrollView(context);
//        for(Element child : e.children()){
//            if(child.tagName().equals("tr") || child.tagName().equals("th")){
//
//            }else{
//
//            }
//        }
//    }

//    public static List<MenuItem> getNavBarMenuItems(SubMenu where, List<String> pageNames){
//        List<MenuItem> items = new ArrayList<>();
//        for(String s : pageNames)
//            items.add(getNavBarItem(s));
//        return items;
//    }
//
//    public static MenuItem getNavBarItem(String name){
//        MenuItem item = new
//    }

}
