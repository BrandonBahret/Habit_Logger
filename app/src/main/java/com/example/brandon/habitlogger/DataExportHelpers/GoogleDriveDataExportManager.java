package com.example.brandon.habitlogger.DataExportHelpers;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by Brandon on 11/7/2016.
 * This is a helper class to manage data with Google Drive.
 */

public class GoogleDriveDataExportManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final int REQUEST_CODE_RESOLUTION = 10;
    public static final String TAG = "GOOGLE_DRIVE_EXPORT";

    private static final String mainFolderName = "Habit Logger";

    private GoogleApiClient googleApiClient;
    private Activity activity;
    private HabitDatabase habitDatabase;

    private ArrayList<OnConnected> OnConnectedListeners = new ArrayList<>();

    public void setOnConnectedListener(OnConnected listener){
        OnConnectedListeners.add(listener);

        if(googleApiClient.isConnected()){
            listener.onConnected();
        }
    }

    public interface OnConnected{
        void onConnected();
    }

    public GoogleDriveDataExportManager(Activity activity){
        this.activity = activity;

        habitDatabase = new HabitDatabase(activity, null, false);

        googleApiClient = new GoogleApiClient.Builder(this.activity)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public boolean connect(){
        if(!googleApiClient.isConnected()){
            googleApiClient.connect();
        }

        return googleApiClient.isConnected();
    }

    private void showMessage(String message){
        Log.i(TAG, message);
    }


    public void doesDatabaseExist(ResultCallback<DriveApi.MetadataBufferResult> callback){
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, habitDatabase.getDatabaseName()))
                .build();

        Drive.DriveApi.getAppFolder(googleApiClient).queryChildren(googleApiClient, query)
                .setResultCallback(callback);
    }

    public void deleteDriveResource(DriveId id){
        id.asDriveResource().delete(googleApiClient);
    }

    public void backupDatabase(){
        doesDatabaseExist(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                MetadataBuffer data = metadataBufferResult.getMetadataBuffer();
                Metadata metadata = data.get(0);
                deleteDriveResource(metadata.getDriveId());
                data.release();
            }
        });

        Drive.DriveApi.newDriveContents(googleApiClient).setResultCallback(
                new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                        try {
                            DriveContents contents = driveContentsResult.getDriveContents();
                            OutputStream outputStream = contents.getOutputStream();
                            outputStream.write(habitDatabase.getBytes());
                            outputStream.close();

                            MetadataChangeSet metadata = new MetadataChangeSet.Builder()
                                    .setTitle(habitDatabase.getDatabaseName())
                                    .build();

                            Drive.DriveApi.getAppFolder(googleApiClient).createFile(googleApiClient, metadata, contents);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    public void restoreDatabase(){
        doesDatabaseExist(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                MetadataBuffer data = metadataBufferResult.getMetadataBuffer();
                Metadata metadata = data.get(0);
                final int filesize = (int)metadata.getFileSize();

                metadata.getDriveId().asDriveFile().open(googleApiClient, DriveFile.MODE_READ_ONLY, null).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                        try {
                            ByteBuffer bytes = ByteBuffer.allocate(filesize);

                            DriveContents contents = driveContentsResult.getDriveContents();
                            InputStream inputStream = contents.getInputStream();

                            int read = 0;
                            while(read != filesize){
                                read += inputStream.read(bytes.array());
                            }

                            habitDatabase.setBytes(bytes);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });

                metadataBufferResult.release();
            }
        });
    }

    public void getAppFolderContents(){
        Drive.DriveApi.getAppFolder(googleApiClient).listChildren(googleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                        MetadataBuffer metadata = metadataBufferResult.getMetadataBuffer();
                        for(Metadata data : metadata){
                            Log.i(TAG, data.getTitle());
                        }
                        metadataBufferResult.release();
                        metadata.release();
                    }
                });
    }

    public void resetAppFolder(){
        Drive.DriveApi.getAppFolder(googleApiClient).listChildren(googleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {

            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                MetadataBuffer data = metadataBufferResult.getMetadataBuffer();
                for(Metadata metadata : data){
                    metadata.getDriveId().asDriveResource().delete(googleApiClient);
                }
                data.release();
            }
        });


    }


    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "API client connected.");

        for(OnConnected listener : OnConnectedListeners){
            listener.onConnected();
        }

        OnConnectedListeners.clear();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), activity, 0).show();
        }
    }
}