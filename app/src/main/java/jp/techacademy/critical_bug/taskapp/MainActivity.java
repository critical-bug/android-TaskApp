package jp.techacademy.critical_bug.taskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_TASK = "jp.techacademy.taro.kirameki.taskapp.TASK";

    private Realm mRealm;
    private RealmResults<Task> mTaskRealmResults;
    private ListView mListView;
    private TaskAdapter mTaskAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, InputActivity.class));
            }
        });

        mRealm = Realm.getDefaultInstance();
        mTaskRealmResults = mRealm.where(Task.class).findAll().sort("date", Sort.DESCENDING);
        mRealm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(final Realm element) {
                reloadListView();
            }
        });

        mTaskAdapter = new TaskAdapter(MainActivity.this);
        mListView = (ListView) findViewById(R.id.listView1);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                // アイテムをタップしたら入力・編集する画面
                final Task task = (Task) parent.getAdapter().getItem(position);

                final Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra(EXTRA_TASK, task.getId());

                startActivity(intent);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            private Task task;
            private void deleteAlarm() {
                final Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
                final PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                        MainActivity.this,
                        task.getId(),
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

                final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.cancel(resultPendingIntent);
            }
            final private DialogInterface.OnClickListener deleteOnClick = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    final RealmResults<Task> results = mRealm.where(Task.class).equalTo("id", task.getId()).findAll();

                    mRealm.beginTransaction();
                    results.deleteAllFromRealm();
                    mRealm.commitTransaction();

                    deleteAlarm();

                    reloadListView();
                }
            };
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                // アイテムのロングタップで削除
                task = (Task) parent.getAdapter().getItem(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("削除");
                builder.setMessage(task.getTitle() + "を削除しますか");
                builder.setPositiveButton("OK", deleteOnClick);
                builder.setNegativeButton("CANCEL", null);

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });

        if (mTaskRealmResults.size() == 0) {
            // アプリ起動時にタスクの数が0であった場合は表示テスト用のタスクを作成する
            addTaskForTest();
        }

        reloadListView();
    }

    private void addTaskForTest() {
        Task task = new Task();
        task.setTitle("作業");
        task.setContent("プログラムを書いてPUSHする");
        task.setDate(new Date());
        task.setId(0);
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(task);
        mRealm.commitTransaction();
    }

    private void reloadListView() {
        final ArrayList<Task> copiedTasks = new ArrayList<>();

        // Realmのデータベースから取得した内容をAdapterなど別の場所で使う場合は直接渡すのではなくコピーして渡す
        for (final Task task : mTaskRealmResults) {
            if (!task.isValid()) continue;

            final Task newTask = new Task();
            newTask.setId(task.getId());
            newTask.setTitle(task.getTitle());
            newTask.setContent(task.getContent());
            newTask.setDate(task.getDate());

            copiedTasks.add(newTask);
        }

        mTaskAdapter.setTaskArrayList(copiedTasks);
        mListView.setAdapter(mTaskAdapter);
        mTaskAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }
}
