package com.beoni.openwaterswimtracking.bll;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.EBean;

@EBean
public class FirebaseManager
{
    public interface ICallback
    {
        void onSuccess(Object data);
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


    private static final String BACKUP_FILE = "/{0}/backup";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthListener;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;


    public FirebaseManager()
    {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    public void backupOnFireDatabase(String data, final ICallback callback){
        DatabaseReference databaseReference = mFirebaseDatabase.getReference(getReferenceNameByUser());
        databaseReference.setValue(data);
        callback.onSuccess(null);
    }

    public void restoreFromFireDatabase(final ICallback callback){
        DatabaseReference databaseReference = mFirebaseDatabase.getReference(getReferenceNameByUser());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                callback.onSuccess(dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                callback.onFail(databaseError.toException());
            }
        });
    }

    public void listenForAuthChanges(final IAuthChangeCallback callback){
        mFirebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseUser = firebaseAuth.getCurrentUser();
                if (mFirebaseUser != null)
                    callback.onLoggeIn(mFirebaseUser);
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
                //TODO: check with forum why I need to explicitly invoke it
                mFirebaseAuthListener.onAuthStateChanged(mFirebaseAuth);

                callback.onComplete(task);
            }
        });
    }

    public void signOut(){
        mFirebaseAuth.signOut();
    }

    private String getReferenceNameByUser() {
        return BACKUP_FILE.replace("{0}", mFirebaseUser.getUid());
    }
}
