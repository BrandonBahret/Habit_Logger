package com.example.brandon.habitlogger.common;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Brandon on 2/18/2017.
 * Helper methods to access data in sqlite databases
 */

public class MyDatabaseUtils {

    public interface AccessAttributesMethods {
        <Type> Type getAttribute(long recordId, String columnKey, Class<Type> clazz);

        int setAttribute(long recordId, String columnKey, Object object);
    }

    /**
     * @param readableDatabase Sqlite database object to query.
     * @param tableName        The name of the table to query.
     * @param recordKey        The name of the column that holds your record ids.
     * @param recordId         The id of the record to select.
     * @param columnKey        The key matching the attribute to return
     * @param clazz            The class cast of the expected object
     * @return The attribute casted to type clazz
     */
    public static <Type> Type getAttribute(SQLiteDatabase readableDatabase, String tableName,
                                           String recordKey, long recordId, String columnKey,
                                           Class<Type> clazz) {

        Cursor c = readableDatabase.query(
                tableName,
                new String[]{columnKey},
                recordKey + " =?",
                new String[]{String.valueOf(recordId)},
                null, null, null);

        Object result = null;
        if (c.moveToFirst()) {
            result = MyDatabaseUtils.getObjectFromCursor(c, 0);
        }
        c.close();

        return clazz.cast(result);
    }

    /**
     * @param writableDatabase Sqlite database object to update.
     * @param tableName        The name of the table to update.
     * @param recordKey        The name of the column that holds your record ids.
     * @param recordId         The id of the record to select.
     * @param columnKey        The key matching the attribute to update
     * @param object           The value to store under columnKey
     * @return The number of rows effected.
     */
    public static int setAttribute(SQLiteDatabase writableDatabase, String tableName,
                                   String recordKey, long recordId, String columnKey,
                                   Object object) {

        ContentValues value = new ContentValues(1);

        if (object instanceof String) {
            value.put(columnKey, (String) object);
        }
        else if (object instanceof Long) {
            value.put(columnKey, (Long) object);
        }
        else if (object instanceof Boolean) {
            value.put(columnKey, (Boolean) object ? 1 : 0);
        }

        return writableDatabase.update(tableName, value,
                recordKey + " =?", new String[]{String.valueOf(recordId)});
    }

    public static Object getObjectFromCursor(Cursor cursor, int columnIndex) {
        final int type = cursor.getType(columnIndex);
        Object result;

        switch (type) {
            case (Cursor.FIELD_TYPE_STRING): {
                result = cursor.getString(columnIndex);
            }
            break;

            case (Cursor.FIELD_TYPE_INTEGER): {
                result = cursor.getLong(columnIndex);
            }
            break;

            case (Cursor.FIELD_TYPE_FLOAT): {
                result = cursor.getDouble(columnIndex);
            }
            break;

            case (Cursor.FIELD_TYPE_BLOB): {
                result = cursor.getBlob(columnIndex);
            }
            break;

            default:
                result = null;
        }

        return result;
    }
}
