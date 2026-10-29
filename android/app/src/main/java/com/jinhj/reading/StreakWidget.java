package com.jinhj.reading;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.SystemClock;
import android.view.View;
import android.widget.RemoteViews;

/** 2x1 듀오링고식 연속 독서 위젯 */
public class StreakWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) m.updateAppWidget(id, build(c));
        ReminderReceiver.schedule(c);
    }

    private static PendingIntent open(Context c, String action, int code) {
        Intent i = new Intent(c, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (action != null) i.putExtra("action", action);
        return PendingIntent.getActivity(c, code, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static RemoteViews build(Context c) {
        WidgetData w = WidgetData.load(c);
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget_streak);
        boolean running = w.timerStart > 0;
        boolean danger = !w.doneToday && !running && w.streak > 0 && WidgetData.hour() >= 21;

        v.setTextViewText(R.id.count, String.valueOf(w.streak));
        v.setImageViewResource(R.id.flame, (w.doneToday || running) ? R.drawable.flame_on : R.drawable.flame_off);
        v.setTextColor(R.id.count, (w.doneToday || running) ? Color.parseColor("#FFB020") : Color.parseColor("#8A8494"));

        int bg = w.doneToday ? R.drawable.streak_bg_done : danger ? R.drawable.streak_bg_danger : R.drawable.streak_bg;
        v.setInt(R.id.sroot, "setBackgroundResource", bg);

        if (running) {
            long base = SystemClock.elapsedRealtime() - (System.currentTimeMillis() - w.timerStart);
            v.setChronometer(R.id.schrono, base, "읽는 중 %s", true);
            v.setViewVisibility(R.id.schrono, View.VISIBLE);
            v.setViewVisibility(R.id.label, View.GONE);
            v.setOnClickPendingIntent(R.id.sroot, open(c, null, 10));
        } else {
            v.setChronometer(R.id.schrono, SystemClock.elapsedRealtime(), null, false);
            v.setViewVisibility(R.id.schrono, View.GONE);
            v.setViewVisibility(R.id.label, View.VISIBLE);
            String label;
            if (!w.hasData) label = "앱을 열어 시작";
            else if (w.doneToday) label = "오늘 완료! " + w.todayMin + "분";
            else if (danger) label = "끊기기 직전! 지금 읽기";
            else label = "오늘 독서는 했나요?";
            v.setTextViewText(R.id.label, label);
            v.setTextColor(R.id.label, danger ? Color.parseColor("#EF8A9A")
                    : w.doneToday ? Color.parseColor("#FFD27A") : Color.parseColor("#C9C2D4"));
            // 안 읽었으면 누르는 즉시 스톱워치 시작, 읽었으면 앱 열기
            v.setOnClickPendingIntent(R.id.sroot, w.doneToday ? open(c, null, 10) : open(c, "start", 11));
        }
        return v;
    }
}
