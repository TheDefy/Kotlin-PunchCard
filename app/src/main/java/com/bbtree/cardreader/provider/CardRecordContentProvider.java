package com.bbtree.cardreader.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.bbtree.cardreader.BBTreeApp;
import com.bbtree.cardreader.greendao.gen.CardRecordDao;
import com.bbtree.cardreader.greendao.gen.DaoSession;

import org.greenrobot.greendao.DaoLog;
import org.greenrobot.greendao.database.StandardDatabase;

/**
 * Created by chenglei on 2017/9/27.
 */


public class CardRecordContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.bbtree.cardreader.provider.CardRecordContentProvider";
    public static final String BASE_PATH = "CARD_RECORD";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/" + BASE_PATH;
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/" + BASE_PATH;

    private static final String TABLENAME = "CARD_RECORD";

    private static final String PK = CardRecordDao.Properties.Id.columnName;

    private static final int ITEM = 1;
    private static final int ITEM_ID = 2;

    private static final UriMatcher sURIMatcher;

    static {
        sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, ITEM);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ITEM_ID);
    }

    @Override
    public boolean onCreate() {
        DaoLog.d("Content Provider started: " + CONTENT_URI);
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        long id = 0;
        String path = "";
        SQLiteDatabase db = ((StandardDatabase) BBTreeApp.getApp().getDaoSessionInstance().getDatabase()).getSQLiteDatabase();
        switch (uriType) {
            case ITEM:
                id = db.insert(TABLENAME, null, values);
                path = BASE_PATH + "/" + id;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(path);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
//        Database db = getDaoSessionInstance1().getDatabase();
        SQLiteDatabase db = ((StandardDatabase) BBTreeApp.getApp().getDaoSessionInstance().getDatabase()).getSQLiteDatabase();
        int rowsDeleted = 0;
        String id;
        switch (uriType) {
            case ITEM:
                rowsDeleted = db.delete(TABLENAME, selection, selectionArgs);
                break;
            case ITEM_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(TABLENAME, PK + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(TABLENAME, PK + "=" + id + " and "
                            + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
//        Database db = getDaoSessionInstance1().getDatabase();
        SQLiteDatabase db = ((StandardDatabase) BBTreeApp.getApp().getDaoSessionInstance().getDatabase()).getSQLiteDatabase();
        int rowsUpdated = 0;
        String id;
        switch (uriType) {
            case ITEM:
                rowsUpdated = db.update(TABLENAME, values, selection, selectionArgs);
                break;
            case ITEM_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(TABLENAME, values, PK + "=" + id, null);
                } else {
                    rowsUpdated = db.update(TABLENAME, values, PK + "=" + id
                            + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case ITEM:
                queryBuilder.setTables(TABLENAME);
                break;
            case ITEM_ID:
                queryBuilder.setTables(TABLENAME);
                queryBuilder.appendWhere(PK + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

//        DaoSession db = getDaoSessionInstance1();
        DaoSession db = BBTreeApp.getApp().getDaoSessionInstance();
        Cursor cursor = queryBuilder.query(((StandardDatabase) db.getDatabase()).getSQLiteDatabase(), projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public final String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case ITEM:
                return CONTENT_TYPE;
            case ITEM_ID:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}
