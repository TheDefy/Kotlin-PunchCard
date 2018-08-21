package com.bbtree.cardreader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.bbtree.cardreader.greendao.gen.CardInfoDao;
import com.bbtree.cardreader.greendao.gen.CardRecordDao;
import com.bbtree.cardreader.greendao.gen.DaoMaster;
import com.bbtree.cardreader.greendao.gen.PlayNumDao;
import com.bbtree.cardreader.greendao.gen.SpeakerConfigDao;
import com.bbtree.cardreader.greendao.gen.SpeakerDao;
import com.bbtree.cardreader.greendao.gen.TempRecordDao;
import com.orhanobut.logger.Logger;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.EncryptedDatabase;


/**
 * Created by qiujj on 2017/3/23.
 * 特殊情况可能需要自己编写数据库迁移脚本，这种时候可以自定义DBHelper，定义方式如下，注意继承类
 */

public class MyEncryptedSQLiteOpenHelper extends DaoMaster.OpenHelper {
    private Context context;
    private final int version = DaoMaster.SCHEMA_VERSION;
    private String name;
    private boolean loadSQLCipherNativeLibs = true;

    public MyEncryptedSQLiteOpenHelper(Context context, String name) {
        super(context, name);
    }

    public MyEncryptedSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
        this.context=context;
        this.name=name;
    }

    @Override
    public Database getEncryptedWritableDb(String password) {
        MyEncryptedHelper encryptedHelper = new MyEncryptedHelper(context,name,version,loadSQLCipherNativeLibs);
        return encryptedHelper.wrap(encryptedHelper.getWritableDatabase(password));
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        //加密升级调用
        Logger.t("BBtreeDataBaseHelper").d("============ Database onUpgrade %s ============",version);
        MigrationHelper.DEBUG = true;
        if(version == 7){
            MigrationHelper.migrate(db,
                    CardInfoDao.class,
                    PlayNumDao.class,
                    SpeakerConfigDao.class,
                    SpeakerDao.class,
                    CardRecordDao.class,
                    TempRecordDao.class
            );
        }
//        DaoMaster.dropAllTables(db, true);
//        onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        Logger.t("BBtreeDataBaseHelper").d("============SQLiteDatabase onUpgrade============");
    }


    private class MyEncryptedHelper extends net.sqlcipher.database.SQLiteOpenHelper {
        public MyEncryptedHelper(Context context, String name, int version, boolean loadLibs) {
            super(context, name, null, version);
            if (loadLibs) {
                net.sqlcipher.database.SQLiteDatabase.loadLibs(context);
            }
        }

        @Override
        public void onCreate(net.sqlcipher.database.SQLiteDatabase db) {
            MyEncryptedSQLiteOpenHelper.this.onCreate(wrap(db));
        }

        @Override
        public void onUpgrade(net.sqlcipher.database.SQLiteDatabase db, int oldVersion, int newVersion) {
            MyEncryptedSQLiteOpenHelper.this.onUpgrade(wrap(db), oldVersion, newVersion);
        }

        @Override
        public void onOpen(net.sqlcipher.database.SQLiteDatabase db) {
            MyEncryptedSQLiteOpenHelper.this.onOpen(wrap(db));
        }

        protected Database wrap(net.sqlcipher.database.SQLiteDatabase sqLiteDatabase) {
            return new EncryptedDatabase(sqLiteDatabase);
        }
    }
}
