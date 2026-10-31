package cn.xi.cloudwidget;
import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;

public class MainActivity extends LibraryActivity {
    int page=0;
    @Override public void onCreate(Bundle b){super.onCreate(b);if(b!=null)page=b.getInt("page");else page=getIntent().getIntExtra("page",0);show();}
    @Override public void onSaveInstanceState(Bundle b){b.putInt("page",page);super.onSaveInstanceState(b);}
    @Override public void onResume(){super.onResume();new Store(this).clearPin();WidgetProvider.all(this);show();}
    @Override void show(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Controls.PAPER);
        LinearLayout body=Ui.column(this);body.setPadding(Ui.dp(this,20),Ui.dp(this,14),Ui.dp(this,20),Ui.dp(this,24));
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        String[] titles={"云朵小卡","我的组件","素材库","设置"};TextView title=Ui.text(this,titles[page],27);title.setTypeface(null,android.graphics.Typeface.BOLD);body.addView(title);
        if(page==0)home(body);else if(page==1)cards(body);else if(page==2)fillLibrary(body);else settings(body);
        LinearLayout nav=new LinearLayout(this);nav.setContentDescription("一级导航");nav.setPadding(Ui.dp(this,12),Ui.dp(this,4),Ui.dp(this,12),Ui.dp(this,4));nav.setBackground(Controls.surface(this,0xfffffdfb,0,0));
        String[] labels={"主页","组件","素材库","设置"};for(int i=0;i<4;i++){final int index=i;LinearLayout tab=new LinearLayout(this);tab.setOrientation(LinearLayout.VERTICAL);tab.setGravity(Gravity.CENTER);tab.setContentDescription("导航："+labels[i]);NavIcon icon=new NavIcon(this,i,i==page);tab.addView(icon,new LinearLayout.LayoutParams(Ui.dp(this,26),Ui.dp(this,26)));TextView label=Ui.text(this,labels[i],11);label.setPadding(0,Ui.dp(this,3),0,0);label.setGravity(Gravity.CENTER);label.setTextColor(i==page?Controls.ACCENT:Controls.MUTED);tab.addView(label);tab.setOnClickListener(v->{page=index;show();});nav.addView(tab,new LinearLayout.LayoutParams(0,Ui.dp(this,58),1));}root.addView(nav);
        root.setOnApplyWindowInsetsListener((v,insets)->{root.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets.consumeSystemWindowInsets();});setContentView(root);
    }
    void home(LinearLayout body){
        body.addView(Controls.hint(this,"给日常留一小块喜欢的风景"));Ui.gap(this,body,18);body.addView(Controls.heading(this,"从空白开始"));
        LinearLayout row=null;for(int n=0;n<7;n++){if(n%2==0){row=new LinearLayout(this);body.addView(row);}final int type=n;Card sample=n==6?Card.mini():n==5?Card.water():new Card();if(n<5)sample.type=n;else if(n==6)sample.addSticker("builtin:blue-books-dog");else sample.addWater(750,java.time.LocalDate.now(),System.currentTimeMillis());if(n==3)sample.body="慢慢来，\n也在向前走。";if(n==4)sample.title="今天的小计划";
            LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setPadding(Ui.dp(this,8),Ui.dp(this,10),Ui.dp(this,8),Ui.dp(this,8));tile.setBackground(Controls.surface(this,n==1?0xfff0f3ed:n==2?0xffedf2f5:0xfffffdf9,16,0));
            CardPreview preview=new CardPreview(this,sample);tile.addView(preview,new LinearLayout.LayoutParams(-1,Ui.dp(this,116)));TextView label=Ui.text(this,new String[]{"日期 / 时间","照片 / 插画","倒数日","便签 / 鼓励语","今日事项 / 打卡","今日喝水","1×1 素材"}[n],13);label.setGravity(Gravity.CENTER);tile.addView(label);tile.setContentDescription("新建："+(n==6?"1×1 素材":n==5?"今日喝水":Card.TYPES[n]));tile.setOnClickListener(v->startActivity(type==6?new Intent(this,EditorActivity.class).putExtra("shape",2).putExtra("type",1):new Intent(this,EditorActivity.class).putExtra("type",type)));preview.setOnClickListener(v->tile.performClick());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(0,Ui.dp(this,6),Ui.dp(this,10),Ui.dp(this,4));row.addView(tile,lp);
        }
        if(row!=null)row.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));
    }
    void cards(LinearLayout body){
        java.util.List<Card> list=new Store(this).all();body.addView(Controls.hint(this,list.isEmpty()?"还没有组件，去主页做一张吧。":"轻点编辑 · 长久保留你的每一次小进步"));LinearLayout row=null;
        for(int i=0;i<list.size();i++){if(i%2==0){row=new LinearLayout(this);body.addView(row);}Card card=list.get(i);LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setPadding(Ui.dp(this,6),Ui.dp(this,8),Ui.dp(this,6),0);CardPreview preview=new CardPreview(this,card);tile.addView(preview,new LinearLayout.LayoutParams(-1,Ui.dp(this,138)));preview.setOnClickListener(v->open(card));
            LinearLayout caption=new LinearLayout(this);TextView name=Ui.text(this,card.title.isEmpty()?(card.shape==2?"1×1 素材":Card.TYPES[card.type]):card.title,13);name.setSingleLine(true);name.setEllipsize(android.text.TextUtils.TruncateAt.END);name.setOnClickListener(v->open(card));caption.addView(name,new LinearLayout.LayoutParams(0,Ui.dp(this,44),1));TextView more=Controls.action(this,"⋯",()->{});more.setContentDescription("组件更多操作");more.setOnClickListener(v->more(v,card));caption.addView(more,new LinearLayout.LayoutParams(Ui.dp(this,40),Ui.dp(this,44)));tile.addView(caption);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(0,Ui.dp(this,6),Ui.dp(this,8),0);row.addView(tile,lp);
        }if(row!=null&&list.size()%2!=0)row.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));
    }
    void more(View anchor,Card card){PopupMenu menu=new PopupMenu(this,anchor);menu.getMenu().add("添加到桌面").setOnMenuItemClickListener(i->{pin(card);return true;});menu.getMenu().add("复制").setOnMenuItemClickListener(i->{Card copy=Card.from(card.json().toString());copy.id=UUID.randomUUID().toString();copy.title=card.title.isEmpty()?"组件副本":card.title+" · 副本";new Store(this).save(copy);show();toast("已复制为独立组件");return true;});menu.getMenu().add("删除").setOnMenuItemClickListener(i->{new AlertDialog.Builder(this).setTitle("删除这张组件？").setMessage("其他组件与素材仍会保留。").setNegativeButton("取消",null).setPositiveButton("删除",(d,w)->{Store store=new Store(this);store.delete(card.id);store.cleanPhotos(this);WidgetProvider.all(this);show();}).show();return true;});menu.show();}
    void pin(Card card){if(card.type==5&&card.shape==2){toast("今日喝水请使用 2×2 或 4×2");return;}AppWidgetManager manager=AppWidgetManager.getInstance(this);if(Build.VERSION.SDK_INT>=26&&manager.isRequestPinAppWidgetSupported()){Intent callback=new Intent(this,PinReceiver.class).setAction("cn.xi.cloudwidget.PIN").putExtra("card",card.id).setData(android.net.Uri.parse("cloudwidget://pin/"+card.id));PendingIntent result=PendingIntent.getBroadcast(this,card.id.hashCode(),callback,PendingIntent.FLAG_UPDATE_CURRENT|(Build.VERSION.SDK_INT>=31?PendingIntent.FLAG_MUTABLE:0));Store store=new Store(this);store.stagePin(card);if(!manager.requestPinAppWidget(new ComponentName(this,WidgetProvider.provider(card.shape)),null,result)){store.clearPin();guide();}}else guide();}
    void settings(LinearLayout body){
        body.addView(Controls.hint(this,"所有内容只留在这台手机"));Ui.gap(this,body,20);body.addView(Controls.heading(this,"数据"));setting(body,"导出数据","完整本地 ZIP · 卡片、照片、素材与历史",()->startActivity(new Intent(this,BackupActivity.class).putExtra("mode","export")));setting(body,"导入数据","先校验并确认 · 合并恢复，保留现有卡片",()->startActivity(new Intent(this,BackupActivity.class).putExtra("mode","import")));Ui.gap(this,body,20);body.addView(Controls.heading(this,"支持"));setting(body,"使用帮助","添加桌面组件与日常编辑",this::guide);setting(body,"关于云朵小卡","0.3.4 · 离线小组件",this::about);setting(body,"开源许可","原作、字体与素材说明",this::licenses);
    }
    void setting(LinearLayout body,String title,String subtitle,Runnable action){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,Ui.dp(this,10),0,Ui.dp(this,10));LinearLayout texts=new LinearLayout(this);texts.setOrientation(LinearLayout.VERTICAL);TextView label=Ui.text(this,title,16);if(action==null)label.setTextColor(Controls.MUTED);texts.addView(label);texts.addView(Controls.hint(this,subtitle));row.addView(texts,new LinearLayout.LayoutParams(0,-2,1));if(action!=null){row.addView(Controls.hint(this,"›"));row.setOnClickListener(v->action.run());}else row.setEnabled(false);body.addView(row);View line=new View(this);line.setBackgroundColor(0xffeee8e2);body.addView(line,new LinearLayout.LayoutParams(-1,Ui.dp(this,1)));}
    void open(Card card){startActivity(new Intent(this,EditorActivity.class).putExtra("card",card.id));}
    void guide(){new AlertDialog.Builder(this).setTitle("把小卡放到桌面").setMessage("1. 回到系统桌面，长按空白处。\n2. 打开「小组件」或「插件」，找到「云朵小卡」。\n3. 选择 1×1 素材、2×2 或 4×2，拖到桌面。\n4. 配置页点「从我的组件复制」，选择卡片，再保存。\n\n喝水卡选择同一张已存卡片可同步记录；其他卡片添加为独立配置。点击水杯记录饮水，其他卡片空白处可以编辑。具体占格由手机桌面决定。").setPositiveButton("知道啦",null).show();}
    void about(){new AlertDialog.Builder(this).setTitle("关于云朵小卡").setMessage("云朵小卡 0.3.4-candidate\n离线 Android 小组件 · 本地候选版\n\n基于原作改造\n原作：Material Photo Widget\n作者：Filipe Belatti\n所基于版本：v1.48.1\n\n狗狗插画由内置 ImageGen 生成。").setNegativeButton("返回",null).setPositiveButton("开源许可",(d,w)->licenses()).show();}
    void licenses(){try{String[] files=getAssets().list("licenses");new AlertDialog.Builder(this).setTitle("开源许可与素材说明").setItems(files,(d,n)->{try(InputStream in=getAssets().open("licenses/"+files[n])){String text=new String(Io.read(in),java.nio.charset.StandardCharsets.UTF_8);ScrollView scroll=new ScrollView(this);TextView t=Ui.text(this,text,12);t.setTextIsSelectable(true);t.setPadding(24,16,24,16);scroll.addView(t);new AlertDialog.Builder(this).setTitle(files[n]).setView(scroll).setPositiveButton("返回",null).show();}catch(IOException e){toast("许可读取失败");}}).setNegativeButton("返回",null).show();}catch(IOException e){toast("许可读取失败");}}
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}

