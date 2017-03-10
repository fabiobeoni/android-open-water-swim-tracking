package com.beoni.openwaterswimtracking.bll;


import android.content.Context;
import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.androidannotations.annotations.EBean;

@EBean
public class FirebaseManager
{
    public interface IStorageCallback
    {
        void onSuccess(Object data);
        void onProgress(long progress);
        void onFail(Exception ex);
    }

    public interface IAuthChangeCallback
    {
        void onLoggeIn(FirebaseUser user);
        void onLoggedOut();
    }

    public interface IAuthSignInCallback
    {
        void onComplete(@NonNull Task<AuthResult> task);
    }


    private Context mContext;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthListener;
    private FirebaseStorage mFirebaseStorage;


    public FirebaseManager(Context mContext)
    {
        this.mContext = mContext;
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    public void upload(String referenceName, byte[] bytes, final IStorageCallback callback){
        StorageReference fileRef = mFirebaseStorage.getReference().child(referenceName);
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

    public void download(String referenceName, final IStorageCallback callback){
        StorageReference fileRef = mFirebaseStorage.getReference().child(referenceName);
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

    public void listenForAuthChanges(final IAuthChangeCallback callback){
        mFirebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null)
                    callback.onLoggeIn(user);
                else
                    callback.onLoggedOut();
            }
        };
    }

    public void stopListeningForAuthChanges(){
        mFirebaseAuthListener = null;
    }

    public void signInWithCredentials(AuthCredential credential, final IAuthSignInCallback callback){
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                callback.onComplete(task);
            }
        });
    }

    public void signOut(){
        mFirebaseAuth.signOut();
    }
}
