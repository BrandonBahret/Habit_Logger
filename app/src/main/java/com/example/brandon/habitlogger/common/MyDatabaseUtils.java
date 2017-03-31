package com.example.brandon.habitlogger.common;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

/**
 * Created by Brandon on 2/18/2017.
 * Helper methods to access data in sqlite databases
 */

public class MyDatabaseUtils {

    public interface AccessAttributesMethods {
        <Type> Type getAttribute(long recordId, String columnKey, Class<Type> clazz);

        int setAttribute(long recordId, String columnKey, Object object);
    }

    public interface GetRecordFromCursor<Out> {
        Out getRecordFromCursor(ContentValues cursor);
    }

    //region Methods to count records
    public static long getNumberOfRows(SQLiteDatabase readableDatabase, String tableName,
                                String recordKey, long recordId) {
        return DatabaseUtils.queryNumEntries(
                readableDatabase, tableName,
                recordKey + "=?", new String[]{String.valueOf(recordId)}
        );
    }

    public static long getNumberOfRows(SQLiteDatabase readableDatabase, String tableName) {
        return DatabaseUtils.queryNumEntries(readableDatabase, tableName, null, null);
    }
    //endregion

    //region Methods to manipulate records
    /**
     * @param readableDatabase Sqlite database object to query.
     * @param tableName        The name of the table to query.
     * @param recordKey        The name of the column that holds your record ids.
     * @param recordId         The id of the record to select.
     * @return A cursor pointing to the selected record
     */
    @Nullable
    public static <Out> Out getRecord(SQLiteDatabase readableDatabase, String tableName,
                                      String recordKey, long recordId,
                                      GetRecordFromCursor<Out> recordGetter) {

        Cursor c = readableDatabase.query(
                tableName, null, recordKey + "=?",
                new String[]{String.valueOf(recordId)},
                null, null, null
        );

        if (c != null && c.moveToFirst()) {
            ContentValues contentValues = new ContentValues(c.getColumnCount());
            DatabaseUtils.cursorRowToContentValues(c, contentValues);
            Out result = recordGetter.getRecordFromCursor(contentValues);
            c.close();
            return result;
        }

        return null;
    }

    public static long deleteRecord(SQLiteDatabase writableDatabase, String tableName, String recordKey, long recordId){
        return writableDatabase.delete(
                tableName, recordKey + " =?", new String[]{String.valueOf(recordId)}
        );
    }
    //endregion

    //region Methods to get attributes from a database

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
        if (c.moveToFirst()) result = MyDatabaseUtils.getObjectFromCursor(c, 0);
        c.close();

        return clazz.cast(result);
    }

    public static Object getObjectFromCursor(Cursor cursor, int columnIndex) {
        final int type = cursor.getType(columnIndex);

        switch (type) {
            case (Cursor.FIELD_TYPE_STRING):
                return cursor.getString(columnIndex);

            case (Cursor.FIELD_TYPE_INTEGER):
                return cursor.getLong(columnIndex);

            case (Cursor.FIELD_TYPE_FLOAT):
                return cursor.getDouble(columnIndex);

            case (Cursor.FIELD_TYPE_BLOB):
                return cursor.getBlob(columnIndex);

            default:
                return null;
        }
    }
    //endregion

    //region Methods to set attributes in a database

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
                                   String recordKey, long recordId, String columnKey, Object object) {

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
    //endregion

}
