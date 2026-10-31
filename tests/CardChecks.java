package cn.xi.cloudwidget;
import java.time.*;
public class CardChecks {
    static int checks;
    static void check(boolean ok,String label){if(!ok)throw new AssertionError(label);checks++;System.out.println("PASS "+label);}
    public static void main(String[] args){
        LocalDate day=LocalDate.of(2026,10,27);
        check(Card.countdown(day.toString(),day).equals("就是今天"),"target date is today");
        check(Card.countdown(day.plusDays(1).toString(),day).equals("还有 1 天"),"future date");
        check(Card.countdown(day.minusDays(2).toString(),day).equals("已过 2 天"),"expired date");
        check(Card.countdown("2024-03-01",LocalDate.of(2024,2,28)).equals("还有 2 天"),"leap day included");
        check(Card.countdown("2027-01-01",LocalDate.of(2026,12,31)).equals("还有 1 天"),"year boundary");
        Card a=new Card();Card b=new Card();check(!a.id.equals(b.id),"independent unique card IDs");
        check(a.items.length==3,"provisional maximum is three items");
        a.toggle(0,day);check(a.done(0,day),"complete");a.toggle(0,day);check(!a.done(0,day),"undo");
        a.toggle(0,day);check(!a.done(0,day.plusDays(1)),"new day starts incomplete");a.toggle(2,day.plusDays(1));check(a.done(0,day)&&a.done(2,day.plusDays(1)),"new day preserves yesterday");
        a.toggle(-1,day);a.toggle(99,day);check(a.done(0,day),"invalid item cannot corrupt history");
        a.items[1]=" ";a.toggle(1,day);check(!a.done(1,day),"blank item is inactive");
        a.title="中文长句，今天也要温柔一点：2026，龘。";a.opacity=0;a.photo="private-copy.png";
        Card restored=Card.from(a.json().toString());check(restored.title.equals(a.title)&&restored.photo.equals(a.photo)&&restored.opacity==0,"configuration roundtrip");
        check(restored.done(0,day)&&restored.done(2,day.plusDays(1)),"history survives serialization");
        restored.items[0]="换一句话";check(restored.done(0,day),"renaming an item keeps stable history ID");
        check(!b.done(0,day),"another card remains independent");
        System.out.println("Checks passed: "+checks);
    }
}
