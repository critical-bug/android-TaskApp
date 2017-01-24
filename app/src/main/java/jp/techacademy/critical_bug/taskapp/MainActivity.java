package jp.techacademy.critical_bug.taskapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
                // TODO 入力・編集する画面に遷移する
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                // TODO タスクを削除する
                return false;
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
        ArrayList<Task> taskArrayList = new ArrayList<>();

        for (Task realmTask : mTaskRealmResults) {
            if (!realmTask.isValid()) continue;

            Task task = new Task();

            task.setId(realmTask.getId());
            task.setTitle(realmTask.getTitle());
            task.setContent(realmTask.getContent());
            task.setDate(realmTask.getDate());

            taskArrayList.add(task);
        }

        mTaskAdapter.setTaskArrayList(taskArrayList);
        mListView.setAdapter(mTaskAdapter);
        mTaskAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }
}
