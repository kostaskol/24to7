package gr.mapeu.a24to7_rebuild.Managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import gr.mapeu.a24to7_rebuild.Bundles.ProductBundle;
import gr.mapeu.a24to7_rebuild.Etc.Constants;

public class DatabaseManager extends SQLiteOpenHelper {
    public DatabaseManager(Context context) {
        super(context, Constants.DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "create table " + Constants.TABLE_NAME + "(" +
                Constants.COL_PHARM + " TEXT," +
                Constants.COL_PROD + " TEXT," +
                "primary key (" + Constants.COL_PHARM + "," + Constants.COL_PROD +
                ")" +
                ")";
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int prev, int newV) {
        sqLiteDatabase.execSQL("drop table if exists " + Constants.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    boolean saveProd(ProductBundle bundle) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Constants.COL_PHARM, bundle.getPharmacy());
        values.put(Constants.COL_PROD, bundle.getProduct());

        long tmp;
        try {
            tmp = db.insert(Constants.TABLE_NAME, null, values);
        } catch (SQLiteException sqle){
            sqle.printStackTrace();
            return false;
        } finally {
            db.close();
        }

        return tmp != -1;

    }

    public boolean pharmExists(String pharmCode) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * from " +
                Constants.TABLE_NAME + " where " + Constants.COL_PHARM + " = " + pharmCode;
        Cursor cursor = null;
        int count;
        try {
            cursor = db.rawQuery(query, null);
            count = cursor.getCount();
        } catch (SQLiteException sqle) {
            sqle.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            db.close();
        }
        return count > 0;
    }

    /*
     * Returns true if the product code has been found
     * AND
     * has been deleted from the database
     */
    public boolean scanProd(String pharmCode, String prodCode) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Making sure that the pharmacy code matches the product code as well
        String selectQuery = "select * from " + Constants.TABLE_NAME +
                " where " + Constants.COL_PHARM + " = " + pharmCode + " and " +
                Constants.COL_PROD + " = " + prodCode;
        Cursor cursor;
        try {
            cursor = db.rawQuery(selectQuery, null);
        } catch (SQLiteException sqle) {
            sqle.printStackTrace();

            db.close();
            return false;
        }

        // If we get no results from the query, we return false
        // TODO: maybe replace boolean return value with error codes?
        if (cursor.getCount() == 0) {
            cursor.close();
            db.close();
            return false;
        }

        cursor.close();
        String delQuery = "delete from " + Constants.TABLE_NAME + " where " +
                Constants.COL_PHARM + " = " + pharmCode + " and " +
                Constants.COL_PROD + " = " + prodCode;

        try {
            db.execSQL(delQuery);
            return true;
        } catch (SQLiteException sqle) {
            sqle.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }

    /*
     * Debug only
     * TODO: Remove before release
     */
    public void printList() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + Constants.TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Log.d("DB Debug", "Pharmacy: " + cursor.getString(cursor.getColumnIndex(Constants.COL_PHARM)));
                Log.d("DB Debug", "Product: " + cursor.getString(cursor.getColumnIndex(Constants.COL_PROD)));
                Log.d("DB Debug", "NEXT: ______________________________");
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }

    public int remainingPharm(String pharmCode) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + Constants.TABLE_NAME + " where " +
                Constants.COL_PHARM + " = " + pharmCode;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            return cursor.getCount();
        } catch (SQLiteException sqle) {
            sqle.printStackTrace();
            return -1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public int remainingOverall() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + Constants.TABLE_NAME;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            return cursor.getCount();
        } catch (SQLiteException sqle) {
            sqle.printStackTrace();
            return -1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    public boolean deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "delete from " + Constants.TABLE_NAME;
        try {
            db.execSQL(query);
            return true;
        } catch(SQLiteException sqle) {
            sqle.printStackTrace();
            return false;
        } finally {
            db.close();
        }
    }
}
