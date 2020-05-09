package config;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class ChatDatabase extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "chat_database";
    private static final String DB_TABLE = "chat_table";
    private static final String CHAT_ID = "chat_id";
    private static final String CHAT_IMAGE = "chat_image";

    public ChatDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PRODUCTS_TABLE = "CREATE TABLE " +
                DB_TABLE + "("
                + CHAT_ID + " TEXT NOT NULL, "
                + CHAT_IMAGE + " BLOB NOT NULL " + ")";
        try {
            db.execSQL(CREATE_PRODUCTS_TABLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int p2, int p3) {
        String TABLE_UPGRADE = "DROP TABLE IF EXISTS "+DB_TABLE;
        db.execSQL(TABLE_UPGRADE);
        onCreate(db);
    }

    public boolean addChatImage(byte[] image) {
        ContentValues cv = new ContentValues();
        cv.put(CHAT_ID, "1");
        cv.put(CHAT_IMAGE, image);

        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + DB_TABLE + " WHERE " + CHAT_ID + " =  \"" + "1" + "\"";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() <= 0) {
            db.insert(DB_TABLE, null, cv);
            db.close();
            cursor.close();
            return false;
        } else {
            db.close();
            cursor.close();
            return true;
        }
    }

    public byte[] findChatImage(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + DB_TABLE + " WHERE " + CHAT_ID + " =  \"" + id + "\"";
        byte[] model;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            model = cursor.getBlob(cursor.getColumnIndex(CHAT_IMAGE));
            cursor.close();
        } else {
            model = null;
        }
        db.close();
        return model;
    }

    public List<String> getChatImage() {
        List<String> lists = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + DB_TABLE;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                lists.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lists;
    }

    public void updateChatImage(byte[] model) {
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(CHAT_IMAGE, model);

        String query = "SELECT * FROM " + DB_TABLE;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(0).equals("1")) {
                    db = this.getWritableDatabase();
                    db.update(DB_TABLE, cv, CHAT_ID + " =? ", new String[]{String.valueOf(cursor.getString(0))});
                }
            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
    }

    public void deleteChatImage() {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + DB_TABLE;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(0).equals("1")) {
                    db = this.getWritableDatabase();
                    db.delete(DB_TABLE, CHAT_ID + " =? ", new String[]{String.valueOf(cursor.getString(0))});
                }
            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
    }
}
