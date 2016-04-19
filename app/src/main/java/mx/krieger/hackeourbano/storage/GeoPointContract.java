package mx.krieger.hackeourbano.storage;

import android.provider.BaseColumns;

public class GeoPointContract {

    public GeoPointContract() {}

    public static abstract class GeoPointEntry implements BaseColumns {
        public static final String TABLE_NAME = "point";
        public static final String COLUMN_NAME_TRAIL = "trail";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
    }
}
