package com.example.brandon.habitlogger.DataExportHelpers;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.brandon.habitlogger.HabitDatabase.DatabaseSchema.DatabaseSchema;
import com.example.brandon.habitlogger.HabitDatabase.HabitDatabase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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

@SuppressWarnings({"unused", "WeakerAccess"})
public class GoogleDriveDataExportManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final int REQUEST_CODE_RESOLUTION = 10;
    public static final String TAG = "GOOGLE_DRIVE_EXPORT";

    private GoogleApiClient mGoogleApiClient;
    private HabitDatabase mHabitDatabase;
    private Activity mActivity;

    private ArrayList<OnConnected> OnConnectedListeners = new ArrayList<>();

    public void addOnConnectListener(OnConnected listener) {
        OnConnectedListeners.add(listener);

        if (mGoogleApiClient.isConnected()) {
            listener.onConnected();
        }
    }

    public interface OnConnected {
        void onConnected();
    }

    public GoogleDriveDataExportManager(Activity activity) {
        mActivity = activity;
        mHabitDatabase = new HabitDatabase(activity);
        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public boolean connect() {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        return mGoogleApiClient.isConnected();
    }

    public boolean isConnected() {
        return mGoogleApiClient.isConnected();
    }

    public void doesDatabaseExist(ResultCallback<DriveApi.MetadataBufferResult> callback) {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, DatabaseSchema.DATABASE_NAME))
                .build();

        Drive.DriveApi.getAppFolder(mGoogleApiClient).queryChildren(mGoogleApiClient, query)
                .setResultCallback(callback);
    }

    public void deleteDriveResource(DriveId id) {
        id.asDriveResource().delete(mGoogleApiClient);
    }

    public void backupDatabase() {
        doesDatabaseExist(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                MetadataBuffer data = metadataBufferResult.getMetadataBuffer();
                Metadata metadata = data.get(0);
                deleteDriveResource(metadata.getDriveId());
                data.release();
            }
        });

        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(
                new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                        try {
                            DriveContents contents = driveContentsResult.getDriveContents();
                            OutputStream outputStream = contents.getOutputStream();
                            outputStream.write(mHabitDatabase.databaseHelper.getBytes().array());
                            outputStream.close();

                            MetadataChangeSet metadata = new MetadataChangeSet.Builder()
                                    .setTitle(DatabaseSchema.DATABASE_NAME)
                                    .build();

                            Drive.DriveApi.getAppFolder(mGoogleApiClient)
                                    .createFile(mGoogleApiClient, metadata, contents);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    public void restoreDatabase() {
        doesDatabaseExist(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                MetadataBuffer data = metadataBufferResult.getMetadataBuffer();
                Metadata metadata = data.get(0);
                data.release();
                final int fileSize = (int) metadata.getFileSize();

                metadata.getDriveId().asDriveFile()
                        .open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                        .setResultCallback(
                                new ResultCallback<DriveApi.DriveContentsResult>() {
                                    @Override
                                    public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                                        try {
                                            ByteBuffer bytes = ByteBuffer.allocate(fileSize);

                                            DriveContents contents = driveContentsResult
                                                    .getDriveContents();
                                            InputStream inputStream = contents.getInputStream();

                                            int read = 0;
                                            while (read != fileSize) {
                                                read += inputStream.read(bytes.array());
                                            }

                                            mHabitDatabase.databaseHelper.setBytes(bytes);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                        );

                metadataBufferResult.release();
            }
        });
    }

    public void getAppFolderContents() {
        Drive.DriveApi.getAppFolder(mGoogleApiClient).listChildren(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                        MetadataBuffer metadata = metadataBufferResult.getMetadataBuffer();
                        for (Metadata data : metadata) {
                            Log.i(TAG, data.getTitle());
                        }
                        metadataBufferResult.release();
                        metadata.release();
                    }
                });
    }

    public void resetAppFolder() {
        Drive.DriveApi.getAppFolder(mGoogleApiClient).listChildren(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {

                    @Override
                    public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                        MetadataBuffer data = metadataBufferResult.getMetadataBuffer();
                        for (Metadata metadata : data) {
                            metadata.getDriveId().asDriveResource().delete(mGoogleApiClient);
                        }
                        data.release();
                    }
                });
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "API client connected.");

        for (OnConnected listener : OnConnectedListeners) {
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
                connectionResult.startResolutionForResult(mActivity, REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            int errorCode = connectionResult.getErrorCode();

            GoogleApiAvailability.getInstance()
                    .getErrorDialog(mActivity, errorCode, REQUEST_CODE_RESOLUTION)
                    .show();
        }
    }
}
