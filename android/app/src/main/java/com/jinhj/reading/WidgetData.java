package com.jinhj.reading;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** 웹앱이 넘겨준 데이터를 읽고, 날짜가 바뀌어도 앱을 열지 않고 상태(완료/위험/끊김)를 계산 */
class WidgetData {
    String header = "📚 독서 체크리스트";
    String headerStale = "🔥 오늘 아직 안 읽었어요";
    String body = "앱을 한 번 열면 진도가 표시돼요";
    long timerStart = 0;
    int todayMin = 0;
    int streak = 0;          // 현재 살아있는 연속일 (끊겼으면 0)
    boolean doneToday = false;
    boolean hasData = false;

    static String day(Date d) { return new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(d); }

    static WidgetData load(Context c) {
        WidgetData w = new WidgetData();
        String json = c.getSharedPreferences("widget", Context.MODE_PRIVATE).getString("data", null);
        if (json == null) return w;
        try {
            JSONObject o = new JSONObject(json);
            w.hasData = true;
            String today = day(new Date());
            Calendar y = Calendar.getInstance(); y.add(Calendar.DATE, -1);
            String yesterday = day(y.getTime());
            boolean sameDay = today.equals(o.optString("day"));
            w.header = sameDay ? o.optString("header", w.header) : w.headerStale;
            w.body = o.optString("body", w.body);
            w.timerStart = o.optLong("timerStart", 0);
            String lastDay = o.optString("lastDay", "");
            int streakAtLast = o.optInt("streakAtLast", 0);
            if (today.equals(lastDay)) { w.doneToday = true; w.streak = streakAtLast; }
            else if (yesterday.equals(lastDay)) { w.doneToday = false; w.streak = streakAtLast; }
            else { w.doneToday = false; w.streak = 0; }
            w.todayMin = w.doneToday ? o.optInt("todayMin", 0) : 0;
        } catch (Exception ignored) {
        }
        return w;
    }

    static int hour() { return Calendar.getInstance().get(Calendar.HOUR_OF_DAY); }

    /** 두 위젯 모두 갱신 */
    static void refreshAll(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        for (int id : m.getAppWidgetIds(new ComponentName(c, ReadingWidget.class)))
            m.updateAppWidget(id, ReadingWidget.build(c));
        for (int id : m.getAppWidgetIds(new ComponentName(c, StreakWidget.class)))
            m.updateAppWidget(id, StreakWidget.build(c));
    }
}
