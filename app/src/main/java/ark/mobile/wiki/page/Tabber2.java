package ark.mobile.wiki.page;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import ark.mobile.wiki.MainActivity;
import ark.mobile.wiki.util.output.Out;
import ark.mobile.wiki.formating.layoutBuilder.LayoutBuilder;

public class Tabber2 {

    private List<Tab2> tabs;

    private LinearLayout layout;
    private LinearLayout childLayout;
    private LinearLayout buttonBar;

    public Tabber2(LinearLayout layout, Context context){
        this.tabs = new ArrayList<>();
        this.layout = layout;


        this.buttonBar = LayoutBuilder.getLinearLayout(context);
        this.buttonBar.setOrientation(LinearLayout.HORIZONTAL);

        HorizontalScrollView scrollView = LayoutBuilder.getHorizontalScrollView(context);
        scrollView.addView(buttonBar);
        this.layout.addView(scrollView);

        this.childLayout = LayoutBuilder.getLinearLayout(context);
        this.layout.addView(childLayout);
    }

//    public void addTabber(int tabberLayer, LinearLayout layout, Context context) {
////        Out.println(this, "AddTabber("+tabberLayer+"); tabs.size() "+tabs.size());
//        if(tabberLayer == 0){
//            tabs.add(new Tabber(layout, context));
//        }else{
//            Tabber lastTabber = tabs.get(tabs.size()-1);
//            lastTabber.addTabber(tabberLayer-1, layout);
////            if(tabberLayer == 1){
////                this.layout.addView(layout);
////            }
//        }
//    }

    public void addTab(int tabberLayer, final LinearLayout childContent, final String tabTitle, final Context context){
//        Out.println(this, "AddTab("+tabberLayer+"); tabs.size() "+tabs.size());
        if(tabberLayer == 0){
            tabs.add(new Tab2(childContent, context, tabTitle));
            if(childLayout.getChildCount() == 0){
                childLayout.addView(childContent);
            }
            Button button = LayoutBuilder.getButton(context, tabTitle);
            final int tabIndex = tabs.size()-1;
            button.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View view) {
                Out.println(this, "Clicken on "+tabTitle);
                childLayout.removeAllViews();
                childLayout.addView(tabs.get(tabIndex).layout);
            }});
            buttonBar.addView(button);
        }
        else{
            throw new RuntimeException("this is not yet");
//            tabs.get(tabs.size()-1).addView(tabberLayer-1, view, tabTitle);
        }
    }

    private Tab2 lastTab(){
        if(tabs.size() == 0) return null;
        return tabs.get(tabs.size()-1);
    }

}
