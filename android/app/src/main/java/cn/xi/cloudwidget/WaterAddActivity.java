package cn.xi.cloudwidget;

import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.graphics.drawable.ColorDrawable;
import android.os.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.util.*;

/** A confirmed measurement, not an accidental whole-widget increment. */
public class WaterAddActivity extends Activity {
    private String cardId,day;private int widget=-1;private boolean saving;EditText amount;
    private TextView current,add;private LinearLayout actions;
    static int parseMl(String text,int min,int max){String value=text.trim();if(!value.matches("[0-9]{1,8}"))throw new IllegalArgumentException("请输入 "+min+"–"+max+" 的整数 mL");long ml=Long.parseLong(value);if(ml<min||ml>max)throw new IllegalArgumentException("请输入 "+min+"–"+max+" 的整数 mL");return (int)ml;}
    static AlertDialog numberDialog(Activity activity,String title,int selected,int min,int max,java.util.function.IntConsumer change){EditText input=new EditText(activity);input.setSingleLine(true);input.setRawInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);input.setText(String.valueOf(selected));input.setSelectAllOnFocus(true);input.setContentDescription(title);AlertDialog dialog=new AlertDialog.Builder(activity).setTitle(title+"（mL）").setView(input).setNegativeButton("取消",null).setPositiveButton("确定",null).create();dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{try{change.accept(parseMl(input.getText().toString(),min,max));dialog.dismiss();}catch(IllegalArgumentException error){input.setError(error.getMessage());}}));dialog.show();return dialog;}
    @Override public void onCreate(Bundle state){super.onCreate(state);widget=getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,-1);cardId=getIntent().getStringExtra("card");day=getIntent().getStringExtra("day");if(widget>=0&&WidgetGeometry.capture(this,getIntent()))WidgetProvider.update(this,widget);
        Card card=card();if(card==null){finish();return;}LinearLayout body=Ui.column(this);body.setPadding(Ui.dp(this,22),Ui.dp(this,18),Ui.dp(this,22),Ui.dp(this,18));Ui.root(this,body);((ViewGroup)findViewById(android.R.id.content)).getChildAt(0).setBackground(Controls.surface(this,Controls.PAPER,18,0));
        body.addView(Controls.heading(this,card.title.isEmpty()?"记录饮水":card.title));current=Ui.text(this,"",19);body.addView(current);body.addView(Controls.hint(this,"选择或填写本次实际饮水量，点「添加」后才记录。"));
        LinkedHashSet<Integer> options=new LinkedHashSet<>(Arrays.asList(150,250,500));options.add(card.waterQuickMl);Integer[] values=options.toArray(new Integer[0]);String[] labels=new String[values.length];for(int n=0;n<labels.length;n++)labels[n]="+"+values[n];
        amount=new EditText(this);amount.setSingleLine(true);amount.setRawInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);amount.setContentDescription("本次饮水量 mL");amount.setText(state==null?String.valueOf(card.waterQuickMl):state.getString("amount",String.valueOf(card.waterQuickMl)));amount.setSelectAllOnFocus(true);
        Controls.choices(this,body,labels,-1,n->amount.setText(String.valueOf(values[n])));body.addView(Controls.hint(this,"本次实际饮水量 · mL"));body.addView(amount);add=Controls.primary(this,"添加",this::submit);body.addView(add);actions=Ui.column(this);actions.setPadding(0,Ui.dp(this,6),0,0);body.addView(actions);
        body.addView(Controls.action(this,"编辑组件",this::edit));body.addView(Controls.action(this,"取消",this::finish));getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));getWindow().setLayout(Math.min(getResources().getDisplayMetrics().widthPixels-Ui.dp(this,24),Ui.dp(this,420)),-2);getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);refresh();
    }
    private Card card(){Store store=new Store(this);if(widget>=0&&!store.binding(widget).equals(cardId))return null;Card card=store.get(cardId);return card!=null&&card.type==5&&card.shape!=2?card:null;}
    boolean validDay(){return LocalDate.now().toString().equals(day);}
    @Override public void onResume(){super.onResume();if(current!=null){refresh();current.post(()->{if(!isFinishing()&&widget>=0&&WidgetGeometry.capture(this,getIntent()))WidgetProvider.update(this,widget);});}}
    private void refresh(){Card card=card();if(card==null){finish();return;}current.setText(card.waterTotal(LocalDate.now())+" / "+card.waterGoalMl+" mL");boolean valid=validDay();add.setEnabled(valid);amount.setEnabled(valid);actions.removeAllViews();if(!valid){actions.addView(Controls.hint(this,"日期已切换，请重新打开桌面喝水组件。"));if(widget>=0)WidgetProvider.update(this,widget);}else if(card.hasWater(LocalDate.now()))actions.addView(Controls.action(this,"撤销最后一次记录",this::undo));}
    void submit(){if(saving)return;try{int ml=parseMl(amount.getText().toString(),1,3000);change(ml,false);}catch(IllegalArgumentException error){amount.setError(error.getMessage());}}
    void undo(){change(0,true);}
    private void change(int ml,boolean undo){if(saving)return;Card card=card();if(card==null){finish();return;}saving=true;try{if(!new Store(this).waterChange(cardId,day,ml,undo)){saving=false;refresh();Toast.makeText(this,"页面已过期或没有可撤销记录，请重新打开",Toast.LENGTH_SHORT).show();return;}try{WidgetProvider.updateCard(this,cardId);}catch(RuntimeException refresh){Toast.makeText(this,"记录已保存，桌面稍后刷新",Toast.LENGTH_SHORT).show();finish();return;}Toast.makeText(this,undo?"已撤销最后一次记录":"已记录 "+ml+" mL",Toast.LENGTH_SHORT).show();finish();}catch(Exception error){saving=false;amount.setError(error.getMessage()==null?"记录失败，原记录保留":error.getMessage());}}
    private void edit(){Intent intent=new Intent(getIntent()).setClass(this,EditorActivity.class);startActivity(intent);finish();}
    @Override public void onSaveInstanceState(Bundle state){if(amount!=null)state.putString("amount",amount.getText().toString());super.onSaveInstanceState(state);}
}
