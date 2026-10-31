package cn.xi.cloudwidget;
import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.io.*;
import java.util.concurrent.*;
public class LibraryActivity extends Activity {
    private final ExecutorService work=Executors.newSingleThreadExecutor();private AssetStore.Pack pending;private AlertDialog previewDialog;
    @Override public void onCreate(Bundle b){super.onCreate(b);show();}
    @Override public void onConfigurationChanged(android.content.res.Configuration config){super.onConfigurationChanged(config);show();}
    void show(){LinearLayout body=Ui.column(this);Ui.root(this,body);body.addView(Controls.action(this,"‹ 返回",this::finish));body.addView(Controls.heading(this,"素材库"));fillLibrary(body);}
    void fillLibrary(LinearLayout body){
        body.addView(Controls.hint(this,"你的插画抽屉 · 34 张内置与本地导入"));
        LinearLayout actions=new LinearLayout(this);actions.addView(Controls.action(this,"＋ PNG 图片",()->pick("image/png",32)),new LinearLayout.LayoutParams(0,Ui.dp(this,48),1));actions.addView(Controls.action(this,"⇩ ZIP 素材包",()->pick("application/zip",31)),new LinearLayout.LayoutParams(0,Ui.dp(this,48),1));body.addView(actions);
        Controls.choices(this,body,new String[]{"全部","狗狗","配件","纸张","背景","已导入"},filter,n->{filter=n;show();});
        java.util.List<AssetStore.Asset> assets=new java.util.ArrayList<>();for(AssetStore.Asset a:new AssetStore(this).all())if(filter==0||filter==5&&!a.ref.startsWith("builtin:")||filter>0&&filter<5&&a.category.equals(new String[]{"","dog","accessory","paper","background"}[filter]))assets.add(a);
        AssetPicker.fill(this,body,assets,a->new AlertDialog.Builder(this).setTitle(a.name).setMessage(a.pack+"\n"+a.author+"\n版本 "+a.version).setPositiveButton("关闭",null).show(),3);
    }
    int filter=0;
    void pick(String type,int code){try{startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType(type).addCategory(Intent.CATEGORY_OPENABLE),code);}catch(ActivityNotFoundException e){toast("没有可用的文件选择器");}}
    @Override public void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;toast("正在检查本地素材…");work.execute(()->{try(InputStream in=getContentResolver().openInputStream(data.getData())){AssetStore store=new AssetStore(this);if(request==32){store.importPng(in);runOnUiThread(()->{if(!isFinishing()&&!isDestroyed()){toast("PNG 已加入素材库");show();}});}else if(request==31){AssetStore.Pack pack=store.prepareZip(in);runOnUiThread(()->{if(isFinishing()||isDestroyed()){store.discard(pack);return;}pending=pack;LinearLayout preview=Ui.column(this);preview.addView(Ui.text(this,pack.details(),14));AssetPicker.fill(this,preview,pack.assets.subList(0,Math.min(3,pack.assets.size())),a->{},3);previewDialog=new AlertDialog.Builder(this).setTitle(pack.title()).setView(preview).setNegativeButton("取消",(d,w)->{store.discard(pack);pending=null;}).setOnCancelListener(d->{store.discard(pack);pending=null;}).setPositiveButton("导入到素材库",(d,w)->work.execute(()->{try{store.commit(pack);runOnUiThread(()->{if(!isFinishing()&&!isDestroyed()){toast("素材包已导入");show();}});}catch(Exception e){runOnUiThread(()->toast(e.getMessage()));}finally{store.discard(pack);pending=null;}})).show();});}}catch(Exception|OutOfMemoryError e){runOnUiThread(()->toast("未导入："+e.getMessage()));}});}
    void toast(String s){if(!isFinishing()&&!isDestroyed())Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    @Override public void onDestroy(){new AssetStore(this).discard(pending);work.shutdown();super.onDestroy();}
}
