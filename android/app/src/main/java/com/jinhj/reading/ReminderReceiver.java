package com.jinhj.reading;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

/** 매일 알림(오늘 안 읽었을 때만) + 자정 직후 위젯 갱신 */
public class ReminderReceiver extends BroadcastReceiver {
    static final String ACT_REMIND = "com.jinhj.reading.REMIND";
    static final String ACT_MIDNIGHT = "com.jinhj.reading.MIDNIGHT";
    static final String CH = "reminder";

    @Override
    public void onReceive(Context c, Intent intent) {
        String a = intent.getAction();
        if (ACT_REMIND.equals(a)) {
            WidgetData w = WidgetData.load(c);
            if (w.hasData && !w.doneToday && w.timerStart == 0) notify(c, w);
            WidgetData.refreshAll(c);
        } else if (ACT_MIDNIGHT.equals(a)) {
            WidgetData.refreshAll(c);
        }
        schedule(c); // 부팅/업데이트/알람 후 다음 일정 다시 등록
    }

    static int remindHour(Context c) {
        return c.getSharedPreferences("widget", Context.MODE_PRIVATE).getInt("remindHour", 21);
    }

    private static PendingIntent pi(Context c, String action, int code) {
        Intent i = new Intent(c, ReminderReceiver.class).setAction(action);
        return PendingIntent.getBroadcast(c, code, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static long next(int hour, int minute) {
        Calendar t = Calendar.getInstance();
        t.set(Calendar.HOUR_OF_DAY, hour); t.set(Calendar.MINUTE, minute);
        t.set(Calendar.SECOND, 0); t.set(Calendar.MILLISECOND, 0);
        if (t.getTimeInMillis() <= System.currentTimeMillis() + 1000) t.add(Calendar.DATE, 1);
        return t.getTimeInMillis();
    }

    static void schedule(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent remind = pi(c, ACT_REMIND, 100);
        int h = remindHour(c);
        if (h >= 0) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next(h, 0), remind);
        else am.cancel(remind);
        am.setAndAllowWhileIdle(AlarmManager.RTC, next(0, 1), pi(c, ACT_MIDNIGHT, 101));
    }

    private static void notify(Context c, WidgetData w) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        if (Build.VERSION.SDK_INT >= 26)
            nm.createNotificationChannel(new NotificationChannel(CH, "독서 알림", NotificationManager.IMPORTANCE_DEFAULT));
        Intent open = new Intent(c, MainActivity.class).putExtra("action", "start")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent p = PendingIntent.getActivity(c, 200, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        String title = w.streak > 0 ? "🔥 " + w.streak + "일 연속 기록이 끊기기 직전이에요" : "📖 오늘 독서는 했나요?";
        Notification n = new Notification.Builder(c, CH)
                .setSmallIcon(R.drawable.ic_stat_flame)
                .setContentTitle(title)
                .setContentText("10분만 읽어도 이어져요 · 누르면 바로 스톱워치 시작")
                .setContentIntent(p)
                .setAutoCancel(true)
                .build();
        try { nm.notify(1, n); } catch (SecurityException ignored) {}
    }
}
