package com.example.brandon.habitlogger.HabitDatabase;

import android.content.Context;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Locale;

/**
 * Created by Brandon on 11/1/2016.
 * This class is used to manage local data back-ups
 */

public class DataExportManager extends AppCompatActivity {

    static public String backupPathPublic = Environment.getExternalStorageDirectory() + File.separator + "Habit_Logger_Export_Data";
    static public String backupPathPrivate;
    static public String dataPathPrivate;
    static public String dataPathPublic = backupPathPublic + File.separator + "Data";
    static public String dbBackupFilename = "habit_database_backup.db";
    static public String categoryFolderFormatString = "Category - %s";

    private Context context;
    HabitDatabase habitDatabase;

    public DataExportManager(Context context){
        this.context = context;
        habitDatabase = new HabitDatabase(context);
        backupPathPrivate = context.getFilesDir().toString();
        dataPathPrivate = backupPathPrivate + File.separator + "Data";
    }

    /**
     * Checks if external storage is available for read and write.
     * @return Backs-up the main database in the "Habit_Logger" directory.
     */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * @param isPublic This selects external or internal storage.
     * @return True if we have a directory "Habit_Logger/Data."
     */
    private boolean updateDirectories(boolean isPublic) {
        // Get the directory for the user's public pictures directory.

        String path = isPublic ? dataPathPublic : dataPathPrivate;

        if(isExternalStorageWritable()) {
            File dataDirectory = new File(path);

            if (!dataDirectory.exists()) {
                if (!dataDirectory.mkdirs()) {
                    return false;
                }
            }

            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Exports the main database into the "Habit_Logger" directory.
     * @return True if successful, false if it failed.
     */
    public boolean exportDatabase(boolean isPublic){
        try {
            if(updateDirectories(isPublic)) {
                String exportPath = isPublic ? backupPathPublic : backupPathPrivate;
                File sd = new File(exportPath);

                if (sd.canWrite()) {
                    String currentDBPath = context.getDatabasePath(DatabaseHelper.DATABASE_NAME).getPath();

                    File currentDB = new File(currentDBPath);
                    File backupDB  = new File(sd, dbBackupFilename);

                    if (currentDB.exists()) {
                        FileChannel src = new FileInputStream(currentDB).getChannel();
                        FileChannel dst = new FileOutputStream(backupDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                    }
                }
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Replace the main database to the back-up on file.
     * @param isPublic This selects external or internal storage.
     * @return True if successful, false if it failed.
     */
    public boolean importDatabase(boolean isPublic){
        try {
            if(updateDirectories(true)) {
                String databasePath = (isPublic ? backupPathPublic : backupPathPrivate) + File.separator + dbBackupFilename;
                File backupDB = new File(databasePath);

                if (backupDB.canRead()) {
                    String currentDBPath = context.getDatabasePath(DatabaseHelper.DATABASE_NAME).getPath();

                    File currentDB = new File(currentDBPath);

                    if (currentDB.exists()) {
                        FileChannel dst = new FileOutputStream(currentDB).getChannel();
                        FileChannel src = new FileInputStream(backupDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                    }
                }
                else{
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * @param backup The habit object to be stored.
     * @param isPublic This selects external or internal storage.
     * @return True if successful, false if it failed.
     */
    public boolean exportHabit(Habit backup, boolean isPublic){
        String habitFolderPath = String.format(Locale.US, categoryFolderFormatString,
                backup.getCategory().getName());

        String filename = backup.getName() + ".csv";
        String dataPath = isPublic ? dataPathPublic : dataPathPrivate;
        String filepath = dataPath + File.separator + habitFolderPath;
        return saveHabitCSV(filepath, filename, backup.toCSV(), isPublic);
    }

    private boolean saveHabitCSV(String pathParent, String pathChild, String CSV, boolean isPublic) {
        try {
            if (updateDirectories(isPublic)) {
                File habitDir = new File(pathParent);

                boolean exists = habitDir.exists();
                if(!exists){
                    exists = habitDir.mkdir();
                }

                if (exists) {
                    File habitFile = new File(pathParent, pathChild);

                    OutputStream fileWriter = new FileOutputStream(habitFile);
                    fileWriter.write(CSV.getBytes());
                    fileWriter.close();
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * @param categoryName The category name of the habit to delete.
     * @param habitName The habit name to delete.
     * @param isPublic This selects external or internal storage.
     * @return True if successful, false if it failed.
     */
    public boolean deleteHabit(String categoryName, String habitName, boolean isPublic){
        if(isExternalStorageWritable()){
            String habitFolderPath = String.format(Locale.US, categoryFolderFormatString, categoryName);
            String filename = habitName + ".csv";
            String rootPath = isPublic ? backupPathPublic : backupPathPrivate;
            String dataPath = rootPath + File.separator + "Data";

            File habit = new File(dataPath + File.separator + habitFolderPath, filename);
            boolean success = habit.delete();

            File dataDirectory = new File(dataPath);
            File dataContents[] = dataDirectory.listFiles();
            for (File eachFile : dataContents) {
                if (eachFile.isDirectory()) {
                    if (eachFile.list().length == 0) {
                        if (!eachFile.delete()) {
                            return false;
                        }
                    }
                }
            }

            return success;
        }
        else{
            return false;
        }
    }

    public String getFilePathFromHabit(Habit habit, boolean isPublic){
        String habitFolderPath = String.format(Locale.US, categoryFolderFormatString, habit.getCategory().getName());
        String filename = habit.getName() + ".csv";
        return dataPathPublic + File.separator + habitFolderPath + File.separator + filename;
    }

    /**
     * @param categoryName The category name to retrieve.
     * @param habitName The habit name to retrieve.
     * @param isPublic This selects external or internal storage.
     * @return True if successful, false if it failed.
     */
    public Habit getHabitByName(String categoryName, String habitName, boolean isPublic){
        String habitFolderPath = String.format(Locale.US, categoryFolderFormatString, categoryName);
        String filename = habitName + ".csv";
        String filePath = dataPathPublic + File.separator + habitFolderPath + File.separator + filename;

        return getHabit(filePath, isPublic);
    }


    public Habit getHabit(String filePath, boolean isPublic){
        if(updateDirectories(isPublic)) {
            try {
                File habitFile = new File(filePath);

                StringBuilder csv = new StringBuilder();
                BufferedReader br = new BufferedReader(new FileReader(habitFile));
                String line;

                while ((line = br.readLine()) != null) {
                    csv.append(line);
                    csv.append('\n');
                }
                br.close();
                return Habit.fromCSV(csv.toString());

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        else{
            return null;
        }
    }

    public boolean importHabit(String filePath, boolean isPublic){
        Habit newHabit = getHabit(filePath, isPublic);

        long habitId = habitDatabase.getHabitIdFromObject(newHabit);

        boolean result;

        if(habitId == -1) {
            result = (habitDatabase.addHabit(newHabit) != -1);
        }
        else {
            result = (habitDatabase.updateHabit(habitId, newHabit) != -1);
        }

        return result;
    }
}
