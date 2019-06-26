package ark.mobile.wiki.page;

import android.content.Context;
import android.widget.LinearLayout;

import java.util.List;

import ark.mobile.wiki.formating.layoutBuilder.LayoutBuilder;

public class Tab2 {

    LinearLayout layout;
    LinearLayout childLayout;
    private List<Object> children;

    public Tab2(LinearLayout layout, Context context, String tabTitle){
        this.layout = layout;
        this.childLayout = LayoutBuilder.getLinearLayout(context);
        this.layout.addView(LayoutBuilder.getHeader3(context, "\u058e"+tabTitle));
        this.layout.addView(childLayout);
    }



}
