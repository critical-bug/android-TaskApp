package jp.techacademy.critical_bug.taskapp;

import android.util.Log;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

class Migration implements RealmMigration {
    @Override
    public void migrate(final DynamicRealm realm, long oldVersion, final long newVersion) {
        Log.d("TaskApp", "old schema " + oldVersion + ", new schema " + newVersion);
        RealmSchema schema = realm.getSchema();

        if (oldVersion == 0) {
            schema.get("Task")
                    .addField("category", String.class);
            oldVersion++;
        }

    }
}
