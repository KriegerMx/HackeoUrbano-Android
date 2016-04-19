package mx.krieger.hackeourbano.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PointDBOpenHelper extends SQLiteOpenHelper {
    private static final String[][] POINT_COLUMNS = {
            {GeoPointContract.GeoPointEntry.COLUMN_NAME_TRAIL, "INTEGER"},
            {GeoPointContract.GeoPointEntry.COLUMN_NAME_LATITUDE, "REAL"},
            {GeoPointContract.GeoPointEntry.COLUMN_NAME_LONGITUDE, "REAL"}
    };

    public static final String DATABASE_NAME = "HACKEO_URBANO_DB";
    private static final int DATABASE_VERSION = 1;

    private static final String POINT_TABLE_CREATE_PREFIX = "CREATE TABLE " + GeoPointContract.GeoPointEntry.TABLE_NAME + " (";
    private static final String POINT_TABLE_CREATE_SUFFIX = ");";
    private static final String POINT_SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + GeoPointContract.GeoPointEntry.TABLE_NAME;

    public PointDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String creatorString = POINT_TABLE_CREATE_PREFIX;
        creatorString += GeoPointContract.GeoPointEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, ";

        for(int i = 0, size = POINT_COLUMNS.length; i < size; i++){
            creatorString +=  POINT_COLUMNS[i][0] + " " + POINT_COLUMNS[i][1];
            if(i != size-1)
                creatorString += ", ";
        }

        creatorString += POINT_TABLE_CREATE_SUFFIX;
        db.execSQL(creatorString);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(POINT_SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}