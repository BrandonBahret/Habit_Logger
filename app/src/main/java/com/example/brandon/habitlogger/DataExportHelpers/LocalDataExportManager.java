package com.example.brandon.habitlogger.DataExportHelpers;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;

import com.example.brandon.habitlogger.HabitDatabase.DataModels.Habit;
import com.example.brandon.habitlogger.HabitDatabase.DatabaseSchema.DatabaseSchema;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Locale;

/**
 * Created by Brandon on 11/1/2016.
 * This class is used to manage local data back-ups
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class LocalDataExportManager {

    //region (File path strings)
    static public String backupPathPublic = Environment.getExternalStorageDirectory() + File.separator + "Habit_Logger_Export_Data";
    static public String backupPathPrivate;
    static public String entriesDataPathPrivate;
    static public String entriesDataPathPublic = backupPathPublic + File.separator + "Data";
    static public String dbBackupFilename = "habit_database_backup.db";
    static public String categoryFolderFormatString = "Category - %s";
    static public String currentDBPath;
    //endregion

    //region (Member attributes)
    private Context mContext;
    private HabitDatabase mHabitDatabase;
    //endregion

    public LocalDataExportManager(Context context) {
        mContext = context;
        mHabitDatabase = new HabitDatabase(context);
        currentDBPath = context.getDatabasePath(DatabaseSchema.DATABASE_NAME).getPath();
        backupPathPrivate = context.getFilesDir().toString();
        entriesDataPathPrivate = backupPathPrivate + File.separator + "Data";
    }

    /**
     * Checks if external storage is available for read and write.
     *
     * @return Backs-up the main database in the "Habit_Logger" directory.
     */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Creates the directories used for exported data
     * @param isPublic This selects external or internal storage.
     * @return True if we have a directory "Habit_Logger/Data."
     */
    private boolean updateDirectories(boolean isPublic) {

        String entriesDataPath = isPublic ? entriesDataPathPublic : entriesDataPathPrivate;

        if (isExternalStorageWritable()) {
            File dataDirectory = new File(entriesDataPath);
            return dataDirectory.exists() || dataDirectory.mkdirs();
        }

        else return false;
    }

    //region (import, export | the entire database) {}

    /**
     * Exports the main database into the "Habit_Logger" directory.
     *
     * @return True if successful, false if it failed.
     */
    public boolean exportDatabase(boolean isPublic) {

        if (updateDirectories(isPublic)) {
            File sourceDB = new File(currentDBPath);

            if (sourceDB.exists()) {
                String destinationPath = isPublic ? backupPathPublic : backupPathPrivate;
                File destination = new File(destinationPath);

                if (destination.canWrite()) {

                    File destinationDB = new File(destination, dbBackupFilename);

                    try {
                        FileChannel src = new FileInputStream(sourceDB).getChannel();
                        FileChannel dst = new FileOutputStream(destinationDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                        return true;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return false;
    }

    /**
     * Exports all of the data in csv format.
     * @return The destination of the exported data.
     */
    public String exportDatabaseAsCsv() {
        for (Habit eachHabit : mHabitDatabase.getHabits())
            exportHabit(eachHabit, true);

        return entriesDataPathPublic;
    }

    /**
     * Replace the main database to the back-up on file.
     *
     * @param isPublic This selects external or internal storage.
     * @return True if successful, false if it failed.
     */
    public boolean importDatabase(boolean isPublic) {
        if (updateDirectories(true)) {
            String databasePath = (isPublic ? backupPathPublic : backupPathPrivate) + File.separator + dbBackupFilename;
            File sourceDB = new File(databasePath);

            if (sourceDB.canRead()) {
                File destinationDB = new File(currentDBPath);

                if (destinationDB.exists()) {
                    try {
                        FileChannel dst = new FileOutputStream(destinationDB).getChannel();
                        FileChannel src = new FileInputStream(sourceDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                        return true;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return false;
    }

    //endregion

    //region (Import, Export, Delete | habit files) {}

    /**
     * @param filePath The file path of the habit in csv format.
     * @param isPublic True if this file is in the user accessible area of device storage.
     * @return True if successful, else false.
     */
    public boolean importHabit(String filePath, boolean isPublic) {
        Habit newHabit = getHabit(filePath, isPublic);
        if (newHabit != null) {
            long habitId = mHabitDatabase.getHabitIdFromObject(newHabit);

            if(habitId == -1)
                return mHabitDatabase.addHabit(newHabit) != -1;
            else
                return mHabitDatabase.updateHabit(habitId, newHabit) != -1;
        }

        return false;
    }

    //region Methods responsible for getting habit objects
    public String getFilePathFromHabit(Habit habit, boolean isPublic) {
        String rootPath = isPublic ? entriesDataPathPublic : entriesDataPathPrivate;
        String habitFolderPath = String.format(Locale.US, categoryFolderFormatString, habit.getCategory().getName());
        String filename = habit.getName() + ".csv";

        return rootPath + File.separator + habitFolderPath + File.separator + filename;
    }

    /**
     * @param categoryName The category name to retrieve.
     * @param habitName    The habit name to retrieve.
     * @param isPublic     This selects external or internal storage.
     * @return The habit found
     */
    @Nullable
    public Habit getHabitByName(String categoryName, String habitName, boolean isPublic) {
        String habitFolderPath = String.format(Locale.US, categoryFolderFormatString, categoryName);
        String filename = habitName + ".csv";
        String filePath = entriesDataPathPublic + File.separator + habitFolderPath + File.separator + filename;

        return getHabit(filePath, isPublic);
    }


    @Nullable
    public Habit getHabit(String filePath, boolean isPublic) {
        if (updateDirectories(isPublic)) {
            try {
                File habitFile = new File(filePath);

                StringBuilder csvBuilder = new StringBuilder();
                BufferedReader fileReader = new BufferedReader(new FileReader(habitFile));

                String line;
                while ((line = fileReader.readLine()) != null) {
                    csvBuilder.append(line);
                    csvBuilder.append('\n');
                }
                fileReader.close();

                return Habit.fromCSV(csvBuilder.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }
    //endregion

    //region Methods responsible for exporting habit objects

    /**
     * @param backup   The habit object to be stored.
     * @param isPublic This selects external or internal storage.
     * @return The filepath of the csv, null if failed.
     */
    public String exportHabit(Habit backup, boolean isPublic) {
        String habitFolderPath = String.format(Locale.US, categoryFolderFormatString,
                backup.getCategory().getName());

        String filename = backup.getName() + ".csv";
        String dataPath = isPublic ? entriesDataPathPublic : entriesDataPathPrivate;
        String filepath = dataPath + File.separator + habitFolderPath;

        if (exportHabitAsCSV(filepath, filename, backup.toCSV(), isPublic)) {
            return filepath;
        }
        else {
            throw new Error("Failed to export habit.");
        }
    }

    private boolean exportHabitAsCSV(String pathParent, String pathChild, String habitCSV, boolean isPublic) {

        if (updateDirectories(isPublic)) {
            try {
                File habitDir = new File(pathParent);

                if (habitDir.exists() || habitDir.mkdir()) {
                    File habitFile = new File(pathParent, pathChild);

                    OutputStream fileWriter = new FileOutputStream(habitFile);
                    fileWriter.write(habitCSV.getBytes());
                    fileWriter.close();
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }


    public void shareExportHabit(Habit habitToShare) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Habit Logger");
            shareIntent.putExtra(Intent.EXTRA_TEXT, habitToShare.toCSV());

            mContext.startActivity(Intent.createChooser(shareIntent, "Export CSV..."));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //endregion

    /**
     * @param categoryName The category name of the habit to delete.
     * @param habitName    The habit name to delete.
     * @param isPublic     This selects external or internal storage.
     * @return True if successful, false if it failed.
     */
    public boolean deleteHabit(String categoryName, String habitName, boolean isPublic) {
        if (isExternalStorageWritable()) {
            String dataRoot = isPublic ? entriesDataPathPublic : entriesDataPathPrivate;

            String categoryFolderPath = dataRoot + File.separator +
                    String.format(Locale.US, categoryFolderFormatString, categoryName);

            File habitFile = new File(categoryFolderPath, habitName + ".csv");

            if (habitFile.delete()) {
                // If the category folder is now empty, delete it.
                File categoryDirectory = new File(categoryFolderPath);
                if (categoryDirectory.list().length == 0) {
                    if (!categoryDirectory.delete())
                        return false;
                }

                return true;
            }
        }

        return false;
    }

    //endregion

}