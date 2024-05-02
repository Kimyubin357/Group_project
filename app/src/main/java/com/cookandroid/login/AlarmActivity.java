package com.cookandroid.login;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AlarmActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1000;
    private AlarmAdapter mAdapter;
    private SQLiteDatabase database;
    private AlarmDbHelper dbHelper;
    private String cancelId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_activity_main);

        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(AlarmActivity.this, SetAlarm.class), REQUEST_CODE);
            }
        });

        final ListView listView = findViewById(R.id.alarmlist);
        final Cursor cursor = getAlarmCursor();
        mAdapter = new AlarmAdapter(this, cursor);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final long deleted = id;
                SQLiteDatabase db = AlarmDbHelper.getInstance(AlarmActivity.this).getWritableDatabase();
                int deletedCount = db.delete(Databases.CreateDB.TABLE_NAME,
                        Databases.CreateDB._ID + "=" + deleted, null);

                if (deletedCount == 0) {
                    Toast.makeText(AlarmActivity.this, "알림 삭제 오류", Toast.LENGTH_SHORT).show();
                } else {
                    mAdapter.swapCursor(getAlarmCursor());
                    cancelId = cursor.getString(cursor.getColumnIndexOrThrow(Databases.CreateDB.ALARMTIME));
                    Intent intent = new Intent(AlarmActivity.this, AlarmReceiver.class);
                    PendingIntent cancelP = PendingIntent.getBroadcast(AlarmActivity.this, Integer.parseInt(cancelId), intent, PendingIntent.FLAG_MUTABLE);
                    cancelP.cancel();
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, final long id) {
                final long deleted = id;
                AlertDialog.Builder builder = new AlertDialog.Builder(AlarmActivity.this);
                builder.setTitle("알림 삭제");
                builder.setMessage("알림을 삭제하시겠습니까?");
                builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase db = AlarmDbHelper.getInstance(AlarmActivity.this).getWritableDatabase();
                        int deletedCount = db.delete(Databases.CreateDB.TABLE_NAME,
                                Databases.CreateDB._ID + "=" + deleted, null);

                        cancelId = cursor.getString(cursor.getColumnIndexOrThrow(Databases.CreateDB.ALARMTIME));

                        if (deletedCount == 0) {
                            Toast.makeText(AlarmActivity.this, "알림 삭제 오류", Toast.LENGTH_SHORT).show();
                        } else {
                            mAdapter.swapCursor(getAlarmCursor());
                            Toast.makeText(AlarmActivity.this, "알림을 삭제했습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AlarmActivity.this, AlarmReceiver.class);
                            PendingIntent cancelP = PendingIntent.getBroadcast(AlarmActivity.this, Integer.parseInt(cancelId), intent, PendingIntent.FLAG_MUTABLE);
                            cancelP.cancel();
                        }
                    }
                });
                builder.setNegativeButton("취소", null);
                builder.show();
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            mAdapter.swapCursor(getAlarmCursor());
        }
    }

    private Cursor getAlarmCursor() {
        dbHelper = AlarmDbHelper.getInstance(this);
        return dbHelper.getReadableDatabase()
                .query(Databases.CreateDB.TABLE_NAME, null, null, null, null, null, null);
    }

    private class AlarmAdapter extends CursorAdapter {
        public AlarmAdapter(Context context, Cursor c) {
            super(context, c, false);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context)
                    .inflate(R.layout.item_alarm, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView ampmtext = view.findViewById(R.id.ampmText);
            ampmtext.setText(cursor.getString(cursor.getColumnIndexOrThrow(Databases.CreateDB.AMPM)));

            TextView hourtext = view.findViewById(R.id.hourText);
            hourtext.setText(cursor.getString(cursor.getColumnIndexOrThrow(Databases.CreateDB.HOUR)));

            TextView colon = view.findViewById(R.id.colonText);
            colon.setText(":");

            TextView minutetext = view.findViewById(R.id.minuteText);
            minutetext.setText(cursor.getString(cursor.getColumnIndexOrThrow(Databases.CreateDB.MINUTE)));

            TextView drug = view.findViewById(R.id.drug_text);
            drug.setText(cursor.getString(cursor.getColumnIndexOrThrow(Databases.CreateDB.DRUGTEXT)));
        }
    }
}