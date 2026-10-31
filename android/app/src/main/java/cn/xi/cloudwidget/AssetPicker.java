package cn.xi.cloudwidget;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.function.*;
public class AssetPicker {
    public static AlertDialog show(Activity activity,Consumer<AssetStore.Asset> selected){
        LinearLayout root=Ui.column(activity);LinearLayout filters=new LinearLayout(activity);filters.setOrientation(LinearLayout.VERTICAL);root.addView(filters);ScrollView scroll=new ScrollView(activity);LinearLayout grid=new LinearLayout(activity);grid.setOrientation(LinearLayout.VERTICAL);scroll.addView(grid);int maxHeight=(int)(activity.getResources().getDisplayMetrics().heightPixels/activity.getResources().getDisplayMetrics().density*.48f);root.addView(scroll,new LinearLayout.LayoutParams(-1,Ui.dp(activity,Math.max(120,Math.min(360,maxHeight)))));
        AlertDialog dialog=new AlertDialog.Builder(activity).setTitle("选择贴纸").setView(root).setNegativeButton("取消",null).create();
        String[] labels={"全部","狗狗","配件","纸张 / 背景","已导入"};IntConsumer[] filter=new IntConsumer[1];filter[0]=index->{filters.removeAllViews();Controls.choices(activity,filters,labels,index,filter[0]);grid.removeAllViews();java.util.List<AssetStore.Asset> assets=new ArrayList<>();for(AssetStore.Asset a:new AssetStore(activity).all())if(index==0||index==1&&a.category.equals("dog")||index==2&&a.category.equals("accessory")||index==3&&(a.category.equals("paper")||a.category.equals("background"))||index==4&&!a.ref.startsWith("builtin:"))assets.add(a);fill(activity,grid,assets,a->{dialog.dismiss();selected.accept(a);},3);};filter[0].accept(0);dialog.show();return dialog;
    }
    public static void fill(Activity activity,LinearLayout grid,java.util.List<AssetStore.Asset> assets,Consumer<AssetStore.Asset> callback,int columns){
        AssetStore store=new AssetStore(activity);LinearLayout row=null;for(int n=0;n<assets.size();n++){if(n%columns==0){row=new LinearLayout(activity);grid.addView(row);}AssetStore.Asset a=assets.get(n);LinearLayout tile=new LinearLayout(activity);tile.setOrientation(LinearLayout.VERTICAL);tile.setGravity(Gravity.CENTER);tile.setPadding(5,5,5,5);ImageView image=new ImageView(activity);image.setScaleType(ImageView.ScaleType.FIT_CENTER);Bitmap bitmap=store.bitmap(a,150);image.setImageBitmap(bitmap);tile.addView(image,new LinearLayout.LayoutParams(-1,Ui.dp(activity,76)));TextView label=Ui.text(activity,a.name,12);label.setGravity(Gravity.CENTER);label.setMaxLines(2);tile.addView(label);tile.setBackground(Controls.surface(activity,0xfffffdf9,12,0));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(3,3,3,3);row.addView(tile,lp);tile.setContentDescription(a.name+"，"+a.pack);tile.setOnClickListener(v->callback.accept(a));}
        if(row!=null&&assets.size()%columns!=0)for(int n=assets.size()%columns;n<columns;n++)row.addView(new View(activity),new LinearLayout.LayoutParams(0,1,1));
        if(assets.isEmpty())grid.addView(Ui.text(activity,"这里还没有素材，可以先导入。",14));
    }
}
