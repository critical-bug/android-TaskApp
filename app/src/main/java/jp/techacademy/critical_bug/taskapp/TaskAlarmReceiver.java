package jp.techacademy.critical_bug.taskapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import io.realm.Realm;

public class TaskAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        // 通知の設定を行う
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.small_icon);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.large_icon));
        builder.setWhen(System.currentTimeMillis());
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setAutoCancel(true);

        // EXTRA_TASK から Task の id を取得して、 id から Task のインスタンスを取得する
        final int taskId = intent.getIntExtra(MainActivity.EXTRA_TASK, -1);
        final Realm realm = Realm.getInstance(TaskApp.realmConfiguration);
        final Task task = realm.where(Task.class).equalTo("id", taskId).findFirst();

        // タスクの情報を設定する
        builder.setTicker(task.getTitle()); // 5.0以降は表示されない
        builder.setContentTitle(task.getTitle());
        builder.setContentText(task.getContent());

        // 通知をタップしたらアプリを起動するようにする
        final Intent startAppIntent = new Intent(context, MainActivity.class);
        startAppIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, startAppIntent, 0);
        builder.setContentIntent(pendingIntent);

        // 通知を表示する
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(task.getId(), builder.build());
        realm.close();
    }
}
