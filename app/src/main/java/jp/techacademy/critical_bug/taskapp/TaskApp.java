package jp.techacademy.critical_bug.taskapp;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class TaskApp extends Application {
    // getDefaultInstance では「必要なときのみ」マイグレーションすることができなかった
    public static RealmConfiguration realmConfiguration;
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        realmConfiguration = new RealmConfiguration.Builder()
                .schemaVersion(2) // Must be bumped when the schema changes
                .migration(new Migration())
                .build();
    }
}
