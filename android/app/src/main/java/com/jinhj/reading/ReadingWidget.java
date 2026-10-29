package com.jinhj.reading;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.widget.RemoteViews;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** 홈 화면 위젯: 진도 요약 + 스톱워치(시작/종료) */
public class ReadingWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) m.updateAppWidget(id, build(c));
    }

    static void refreshAll(Context c) {
        WidgetData.refreshAll(c);
    }

    private static PendingIntent open(Context c, String action, int code) {
        Intent i = new Intent(c, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (action != null) i.putExtra("action", action);
        return PendingIntent.getActivity(c, code, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static RemoteViews build(Context c) {
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget);
        String header = "📚 독서 체크리스트";
        String body = "앱을 한 번 열면 진도가 표시돼요";
        long timerStart = 0;
        int todayMin = 0;

        String json = c.getSharedPreferences("widget", Context.MODE_PRIVATE).getString("data", null);
        if (json != null) {
            try {
                JSONObject o = new JSONObject(json);
                String now = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(new Date());
                boolean isToday = now.equals(o.optString("day"));
                header = isToday ? o.optString("header", header) : o.optString("headerStale", header);
                body = o.optString("body", body);
                timerStart = o.optLong("timerStart", 0);
                todayMin = isToday ? o.optInt("todayMin", 0) : 0;
            } catch (Exception ignored) {
            }
        }

        v.setTextViewText(R.id.header, header);
        v.setTextViewText(R.id.body, body);

        if (timerStart > 0) {
            long base = SystemClock.elapsedRealtime() - (System.currentTimeMillis() - timerStart);
            v.setChronometer(R.id.chrono, base, null, true);
            v.setViewVisibility(R.id.chrono, View.VISIBLE);
            v.setViewVisibility(R.id.today, View.GONE);
            v.setTextViewText(R.id.btn, "■ 종료");
            v.setInt(R.id.btn, "setBackgroundResource", R.drawable.btn_stop);
            v.setOnClickPendingIntent(R.id.btn, open(c, "stop", 2));
        } else {
            v.setChronometer(R.id.chrono, SystemClock.elapsedRealtime(), null, false);
            v.setViewVisibility(R.id.chrono, View.GONE);
            v.setViewVisibility(R.id.today, View.VISIBLE);
            v.setTextViewText(R.id.today, "오늘 " + todayMin + "분");
            v.setTextViewText(R.id.btn, todayMin > 0 ? "▶ 이어서" : "▶ 시작");
            v.setInt(R.id.btn, "setBackgroundResource", R.drawable.btn_start);
            v.setOnClickPendingIntent(R.id.btn, open(c, "start", 1));
        }
        v.setOnClickPendingIntent(R.id.root, open(c, null, 0));
        return v;
    }
}
