package jp.techacademy.critical_bug.taskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_TASK = "jp.techacademy.taro.kirameki.taskapp.TASK";

    private Realm mRealm;
    private ListView mListView;
    private TaskAdapter mTaskAdapter;
    private AutoCompleteTextView mAutoCompleteTextView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Log.d("TaskApp", "MainActivity onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, InputActivity.class));
            }
        });

        // 必要ならマイグレーションする
        mRealm = Realm.getInstance(TaskApp.realmConfiguration);
        final RealmResults<Task> taskRealmResults = mRealm.where(Task.class).findAll().sort("date", Sort.DESCENDING);
        mRealm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(final Realm element) {
                Log.d("TaskApp", "mRealm onChange");
                reloadListView(taskRealmResults);
                setupAutoComplete();
            }
        });

        mTaskAdapter = new TaskAdapter(MainActivity.this);
        mListView = (ListView) findViewById(R.id.listView1);
        mAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);

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

                    setupAutoComplete();
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
        findViewById(R.id.show_all_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mAutoCompleteTextView.setText("");
                reloadListView(mRealm.where(Task.class).findAll().sort("date", Sort.DESCENDING));
            }
        });

        if (taskRealmResults.size() == 0) {
            // アプリ起動時にタスクの数が0であった場合は表示テスト用のタスクを作成する
            addTaskForTest();
        }

        reloadListView(mRealm.where(Task.class).findAll().sort("date", Sort.DESCENDING));
        setupAutoComplete();
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

    private void reloadListView(final RealmResults<Task> taskRealmResults) {
        final ArrayList<Task> copiedTasks = new ArrayList<>();

        // Realmのデータベースから取得した内容をAdapterなど別の場所で使う場合は直接渡すのではなくコピーして渡す
        for (final Task task : taskRealmResults) {
            if (!task.isValid()) continue;

            final Task newTask = new Task();
            newTask.setId(task.getId());
            newTask.setTitle(task.getTitle());
            newTask.setContent(task.getContent());
            newTask.setDate(task.getDate());
            newTask.setCategory(task.getCategory());

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

    private void setupAutoComplete() {
        final HashSet<String> categories = new HashSet<>();
        for (final Task task: mRealm.where(Task.class).findAll()) {
            categories.add(task.getCategory());
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories.toArray(new String[categories.size()]));
        mAutoCompleteTextView.setAdapter(adapter);
        final AutoCompleteTextViewEventListener listener = new AutoCompleteTextViewEventListener(mAutoCompleteTextView);
        mAutoCompleteTextView.setOnFocusChangeListener(listener);
        mAutoCompleteTextView.setOnKeyListener(listener);
        mAutoCompleteTextView.setOnDismissListener(listener);
        mAutoCompleteTextView.setOnItemClickListener(listener);
        mAutoCompleteTextView.setOnEditorActionListener(listener);
    }

    private class AutoCompleteTextViewEventListener
            implements View.OnKeyListener,
            View.OnFocusChangeListener,
            AutoCompleteTextView.OnDismissListener,
            AdapterView.OnItemClickListener,
            TextView.OnEditorActionListener {
        private final AutoCompleteTextView mAutoCompleteTextView;

        private AutoCompleteTextViewEventListener(final AutoCompleteTextView autoCompleteTextView) {
            mAutoCompleteTextView = autoCompleteTextView;
        }

        @Override
        public void onFocusChange(final View v, final boolean hasFocus) {
            final String searchWord = ((AutoCompleteTextView) v).getText().toString();
            Log.d("TaskApp", "AutoCompleteTextView onFocusChange: hasFocus=" + hasFocus + ", searchWord=" + searchWord);
            reloadListView(findAllTask(searchWord));
        }

        @Override
        public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
            Log.d("TaskApp", "AutoCompleteTextView onKey");
            return false;
        }

        @Override
        public void onDismiss() {
            Log.d("TaskApp", "AutoCompleteTextView onDismiss");
        }

        @Override
        public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
            Log.d("TaskApp", "AutoCompleteTextView onItemClick");
            final String searchWord = mAutoCompleteTextView.getText().toString();
            reloadListView(findAllTask(searchWord));
        }

        @Override
        public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
            if (event != null) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.KEYCODE_NUMPAD_ENTER:
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mAutoCompleteTextView.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                        final String searchWord = ((AutoCompleteTextView) v).getText().toString();
                        reloadListView(findAllTask(searchWord));
                        return true;
                }
            }
            return false;
        }
    }

    /**
     * Realm に保存されている Task を返す。
     * @param category カテゴリとマッチさせる文字列。null, 空, 改行のみの場合はカテゴリ指定せず全ての Task を返す
     * @return
     */
    private RealmResults<Task> findAllTask(String category) {
        Log.d("TaskApp", "findAllTask: " + category);
        if (!category.isEmpty() || !category.replaceAll("^[\\n\\r]+$", "").isEmpty()) {
            return mRealm.where(Task.class).equalTo("category", category).findAll().sort("date", Sort.DESCENDING);
        } else {
            return mRealm.where(Task.class).findAll().sort("date", Sort.DESCENDING);
        }
    }
}
