package com.beoni.openwaterswimtracking.bll;


import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.androidannotations.annotations.EBean;

@EBean
public class FirebaseStorageManager
{
    public interface IAsyncTaskCallback
    {
        void onSuccess(Object data);

        void onProgress(long progress);

        void onFail(Exception ex);
    }

    private Context mContext;
    FirebaseStorage storage;

    public FirebaseStorageManager(Context mContext)
    {
        this.mContext = mContext;
        storage = FirebaseStorage.getInstance();
    }

    public void upload(String referenceName, byte[] bytes, final IAsyncTaskCallback callback){
        StorageReference fileRef = storage.getReference().child(referenceName);
        fileRef.putBytes(bytes).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                callback.onFail(exception);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                callback.onSuccess(taskSnapshot.getDownloadUrl());
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
            {
                callback.onProgress(taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
            }
        });
    }

    public void download(String referenceName, final IAsyncTaskCallback callback){
        StorageReference fileRef = storage.getReference().child(referenceName);
        long data = 5 * 1024 * 1024; //max download 5MB

        fileRef.getBytes(data).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                callback.onFail(e);
            }
        }).addOnSuccessListener(new OnSuccessListener<byte[]>()
        {
            @Override
            public void onSuccess(byte[] bytes)
            {
                callback.onSuccess(bytes);
            }
        });
    }
}
