package com.example.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Task.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "tb_tasks";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_SLA_DATE = "sla_date";
    private static final String COLUMN_IS_FINISHED = "is_finished";
    private static final String COLUMN_FINISHED_DATE = "finished_date";

    public SQLiteHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        String query = ("CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_SLA_DATE + " TEXT,"
                + COLUMN_IS_FINISHED + " INTEGER,"
                + COLUMN_FINISHED_DATE + " TEXT" + ")");
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long createTask(@NonNull TaskModel task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME, task.getName());
        cv.put(COLUMN_SLA_DATE, task.getSlaDate());
        cv.put(COLUMN_IS_FINISHED, task.getIsFinished());
        cv.put(COLUMN_FINISHED_DATE, task.getFinishedDate());

        return db.insert(TABLE_NAME, null, cv);
    }

    public long updateTask(@NonNull TaskModel task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME, task.getName());
        cv.put(COLUMN_SLA_DATE, task.getSlaDate());
        cv.put(COLUMN_IS_FINISHED, task.getIsFinished());
        cv.put(COLUMN_FINISHED_DATE, task.getFinishedDate());

        return db.update(TABLE_NAME, cv, "id=" + task.getId(), null);
    }

    public long deleteTask(@NonNull TaskModel task) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "id=" + task.getId(), null);
    }

    public ArrayList<TaskModel> deleteSelectedTasks(ArrayList<TaskModel> selectedTasks) {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<TaskModel> deletedTasks = new ArrayList<>();
        TaskModel task;
        for (int i = 0; i < selectedTasks.size(); i++) {
            task = selectedTasks.get(i);
            long result = db.delete(TABLE_NAME, "id=" + task.getId(), null);
            if (result == 1)
                deletedTasks.add(task);
        }
        return deletedTasks;
    }

    public void deleteAllTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }

    public void deleteOnHoldTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_IS_FINISHED + " = 0");
    }

    public void deleteFinishedTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_IS_FINISHED + " = 1");
    }

    public ArrayList<TaskModel> getAllTasksOnHold() {

        ArrayList<TaskModel> allTasks = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_IS_FINISHED + " = 0";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor;

        if (db != null) {
            cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                TaskModel task = new TaskModel(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getString(4)
                );
                allTasks.add(task);
            }
            cursor.close();
        }

        return allTasks;
    }

    public ArrayList<TaskModel> getAllFinishedTasks() {
        ArrayList<TaskModel> finishedTasks = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_IS_FINISHED + " = 1";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor;

        if (db != null) {
            cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                TaskModel task = new TaskModel(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getString(4)
                );
                finishedTasks.add(task);
            }
            cursor.close();
        }

        return finishedTasks;
    }
}
