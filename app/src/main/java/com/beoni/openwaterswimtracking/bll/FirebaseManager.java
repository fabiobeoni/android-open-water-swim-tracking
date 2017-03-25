package com.beoni.openwaterswimtracking.bll;

import android.support.annotation.NonNull;
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

/**
 * Class managing the Firebase API and providing client methods to login/backup/restore
 * user data on Firebase cloud database.
 */
@EBean
public class FirebaseManager
{
    /**
     * Register to this interface to get notified
     * about data transfer status to/from Firebase
     * (backup and restore actions).
     */
    public interface IBackupCallback
    {
        void onSuccess(Object data);
        void onFail(Exception ex);
    }

    /**
     * Register to this interface to get notified
     * about user authentication status changes
     * on Firebase service.
     */
    public interface IAuthChangeCallback
    {
        void onLoggeIn(FirebaseUser user);
        void onLoggedOut();
    }

    /**
     * Register to this interface to get notified
     * about authentication completion on Firebase
     * authentication service.
     */
    public interface IAuthSignInCallback
    {
        void onComplete(@NonNull Task<AuthResult> task);
    }

    /**
     * Firebase database object address where user data are stored.
     * It depends on Firebase user ID.
     */
    private static final String BACKUP_ADDRESS = "/{0}/backup";

    //needed Firebase classes to manage
    //user authentication as well as data storing
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthListener;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;

    /**
     * Initialize the manager and Firebase auth and db
     */
    public FirebaseManager()
    {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Performs user data backup all-at-once
     * under the predefined user data address
     * oon Firebase /{userID}/backup.
     * @param data
     * @param callback
     */
    public void backupOnFireDatabase(String data, final IBackupCallback callback){
        DatabaseReference databaseReference = mFirebaseDatabase.getReference(getBackupAddressByUser());
        databaseReference.setValue(data);
        callback.onSuccess(null);
    }

    /**
     * Perform data restore from Firebase database
     * getting data from user backup address (/{userID}/backup)
     * and providing it to the client.
     * @param callback
     */
    public void restoreFromFireDatabase(final IBackupCallback callback){
        DatabaseReference databaseReference = mFirebaseDatabase.getReference(getBackupAddressByUser());
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

    /**
     * Create the listener for authentication events
     * (on logged in / on logged out) on Firebase
     * service.
     * @param callback
     */
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

    /**
     * Reset the authentication listener set by listenForAuthChanges().
     */
    public void stopListeningForAuthChanges(){
        mFirebaseAuthListener = null;
    }

    /**
     * Open a session with given credentials
     * to the Firebase service and returns the
     * login result to the client.
     * @param credential
     * @param callback
     */
    public void signInWithCredentials(AuthCredential credential, final IAuthSignInCallback callback){
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                //TODO: check why I need to explicitly invoke it
                mFirebaseAuthListener.onAuthStateChanged(mFirebaseAuth);

                callback.onComplete(task);
            }
        });
    }

    /**
     * Closes the session with Firebase service.
     */
    public void signOut(){
        mFirebaseAuth.signOut();
    }

    /**
     * Format the user backup address on Firebase
     * based on user ID.
     * @return String
     */
    private String getBackupAddressByUser() {
        return BACKUP_ADDRESS.replace("{0}", mFirebaseUser.getUid());
    }
}
