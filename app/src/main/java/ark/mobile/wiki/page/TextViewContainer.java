package ark.mobile.wiki.page;

import android.graphics.Bitmap;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ark.mobile.wiki.MainActivity;
import ark.mobile.wiki.util.output.Out;

public class TextViewContainer {

    public final TextView textView; //textview co už je přidaný do root layoutu
    public final SpannableString ss; //spannable string
    public final List<ImageInText> images; //seznam všech obrázků textu - mají svoji pozici ve spannable stringu a svoje URL - po načtení obrázků musí přepsat spannable string

    public TextViewContainer(TextView textView, SpannableString ss) {
        this.textView = textView;
        this.ss = ss;
        if(textView != null) textView.setText(ss);
        this.images = new ArrayList<>();
    }

    public void addImage(int index, String URL){
        ImageInText imageInText = new ImageInText();
        imageInText.start = index;
        imageInText.URL = URL;
        images.add(imageInText);
    }

    public void reApplyText(){
        MainActivity.replaceTextOfView(ss, textView);
//        textView.setText(ss);
    }

    public void replaceImage(Bitmap data, int index){
        ImageInText imageInText = images.get(index);
        ss.setSpan(new ImageSpan(data), imageInText.start, imageInText.start+1, 0);
        reApplyText();
//        Out.println("ImageDownloaded");
    }

    public int containsImage(String URL){
        for(int i = 0; i < images.size(); i++){
            if(images.get(i).URL.equals(URL)) return i;
        }
        return -1;
    }

}
