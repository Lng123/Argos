package ca.bcit.argos.database;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;


public class DataHandler extends SQLiteOpenHelper {
    //information of database
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "bikeRackDB.db";
    private static final String TABLE_NAME = "BikeRack";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_NAME0 = "StreetNumber";
    private static final String COLUMN_NAME1 = "StreetName";
    private static final String COLUMN_NAME2 = "StreetSide";
    private static final String COLUMN_NAME3 = "SkytrainStationName";
    private static final String COLUMN_NAME4 = "BIA";
    private static final String COLUMN_NAME5 = "NumberOfRacks";
    private static final String COLUMN_NAME6 = "YearInstalled";

    //initialize the database
    public DataHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = ("CREATE TABLE " + TABLE_NAME + "(" + COLUMN_ID +
                "INTEGER PRIMARYKEY, " + COLUMN_NAME0 + "INTEGER, " +
                COLUMN_NAME1 + "TEXT, " + COLUMN_NAME2 + "TEXT, " + COLUMN_NAME3 +
                "TEXT, " + COLUMN_NAME4 + "TEXT, " + COLUMN_NAME5 + "INTEGER, " +
                COLUMN_NAME6 + "TEXT)");
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {}
    public String loadHandler() {
        String result = "";
        String query = "Select*FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            int result_0 = cursor.getInt(0);
            String result_1 = cursor.getString(1);
            result += String.valueOf(result_0) + " " + result_1 +
                    System.getProperty("line.separator");
        }
        cursor.close();
        db.close();
        return result;
    }
    public void addHandler(BikeRack bikerack) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, bikerack.getID());
        values.put(COLUMN_NAME0, bikerack.getStreetNumber());
        values.put(COLUMN_NAME1, bikerack.getStreetName());
        values.put(COLUMN_NAME2, bikerack.getStreetSide());
        values.put(COLUMN_NAME3, bikerack.getSkytrainStationName());
        values.put(COLUMN_NAME4, bikerack.getBIA());
        values.put(COLUMN_NAME5, bikerack.getNumberOfRacks());
        values.put(COLUMN_NAME6, bikerack.getYearInstalled());
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_NAME, null, values);
        db.close();
    }
    public BikeRack findHandler(String bkcol, String bkval) {
        String query = "Select * FROM " + TABLE_NAME + "WHERE" + bkcol + " = "
                + "'" + bkval + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        BikeRack bikeRack = new BikeRack();
        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            bikeRack.setID(Integer.parseInt(cursor.getString(0)));
            bikeRack.setStreetNumber(cursor.getInt(1));
            bikeRack.setStreetName(cursor.getString(2));
            bikeRack.setStreetSide(cursor.getString(3));
            bikeRack.setSkytrainStationName(cursor.getString(4));
            bikeRack.setBIA(cursor.getString(5));
            bikeRack.setNumberOfRacks(cursor.getInt(6));
            bikeRack.setYearInstalled(cursor.getString(7));
            cursor.close();
        } else {
            bikeRack = null;
        }
        db.close();
        return bikeRack;
    }

    public boolean deleteHandler(int ID) {
        boolean result = false;
        String query = "Select*FROM" + TABLE_NAME + "WHERE" + COLUMN_ID + "= '" + String.valueOf(ID) + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        BikeRack bikeRack = new BikeRack();
        if (cursor.moveToFirst()) {
            bikeRack.setID(Integer.parseInt(cursor.getString(0)));
            db.delete(TABLE_NAME, COLUMN_ID + "=?",
                    new String[] {
                String.valueOf(bikeRack.getID())
            });
            cursor.close();
            result = true;
        }
        db.close();
        return result;
    }

    public boolean updateHandler(int ID, String bkcol, String bkval) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put(COLUMN_ID, ID);
        args.put(bkcol, bkval);
        return db.update(TABLE_NAME, args, COLUMN_ID + "=" + ID, null) > 0;
    }

    public boolean updateHandler(int ID, String bkcol, int bkval) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put(COLUMN_ID, ID);
        args.put(bkcol, bkval);
        return db.update(TABLE_NAME, args, COLUMN_ID + "=" + ID, null) > 0;
    }

}
