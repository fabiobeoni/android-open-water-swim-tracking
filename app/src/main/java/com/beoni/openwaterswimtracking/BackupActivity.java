
package com.beoni.openwaterswimtracking;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.beoni.openwaterswimtracking.bll.FirebaseManager;
import com.beoni.openwaterswimtracking.bll.SwimTrackManager;
import com.beoni.openwaterswimtracking.utils.ConnectivityUtils;
import com.beoni.openwaterswimtracking.utils.LLog;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_backup)
public class BackupActivity extends AppCompatActivity
{
    private static final String TAG = "BackupActivity";
    private static final int RC_SIGN_IN = 9001;

    //instance of the Google API client needed
    //to manage authentication with Gmail account
    private GoogleApiClient mGoogleApiClient;

    //instance of the Firebase Manager needed
    //to perform authentication on FB cloud
    //and data storing with Real-time Database
    @Bean
    FirebaseManager mFirebaseMng;

    //instance of the Swim Track Manager
    //needed to get the data stored locally
    //and to be backup/restored
    @Bean
    SwimTrackManager mSwimTrackMng;


    //=========== UI STATES AVAILABLE ============//

    /**
     * Info message, login button are displayed
     */
    private static final int UISTATE_LOGGED_OUT = 0;

    /**
     * Info message, backup/restore/sign-out buttons
     * are displayed
     */
    private static final int UISTATE_LOGGED_IN = 1;

    /**
     * Info message, action buttons displayed but disabled
     */
    private static final int UISTATE_PERFORMS_BACKUP_RESTORE = 2;

    //================== UI CONTROLS ===============//

    //build on-the-fly from activity
    private ProgressDialog mProgressDialog;

    @ViewById(R.id.btn_signin)
    SignInButton mBtnSignIn;

    @ViewById(R.id.btn_sign_out)
    Button mBtnSignOut;

    @ViewById(R.id.btn_backup)
    Button mBtnBackup;

    @ViewById(R.id.btn_restore)
    Button mBtnRestore;

    @ViewById(R.id.backup_message)
    TextView mTxtMessage;

    @ViewById(R.id.progress_bar)
    ProgressBar mProgress;

    /**
     * Sets the UI default state and initialize Google API client
     */
     @AfterViews
    void viewCreated(){

        setUIState(UISTATE_LOGGED_OUT);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener(){
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
                    {
                        Log.d(TAG, "onConnectionFailed:" + connectionResult);
                        Toast.makeText(getBaseContext(), R.string.google_api_error, Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    /**
    Attaches Firebase authentication change listener,
    and updates the UI displays right controls when
    the session starts/ends
     */
    @Override
    public void onStart() {
        super.onStart();
        mFirebaseMng.listenForAuthChanges(new FirebaseManager.IAuthChangeCallback()
        {
            @Override
            public void onLoggeIn(FirebaseUser user)
            {
                setUIState(UISTATE_LOGGED_IN);
            }

            @Override
            public void onLoggedOut()
            {
                setUIState(UISTATE_LOGGED_OUT);
            }
        });
    }

    /**
    Same as onStart() method but detaching the Firebase
    authentication listener
     */
    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
        if (mFirebaseMng != null)
            mFirebaseMng.stopListeningForAuthChanges();
    }

    /**
     * Gets the result of the Google authentication activity,
     * reports the result to the user. When
     * user is authenticated => registers/authenticates
     * to Firebase too and updates UI state
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                performFirebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                mTxtMessage.setText(R.string.authentication_fail);
                setUIState(UISTATE_LOGGED_OUT);
            }
        }
    }

    /**
     * When device is connected, starts the Google
     * authentication activity to let the user login
     */
    @Click(R.id.btn_signin)
    void onSignBtnClick() {
        if(ConnectivityUtils.isDeviceConnected(this))
        {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
        else
            Toast.makeText(getBaseContext(),R.string.no_connection,Toast.LENGTH_LONG).show();
    }

    /**
     * Closes the sessions on Firebase
     * as well as Google auth
     */
    @Click(R.id.btn_sign_out)
    void onSignoutBtnClick() {
        // Firebase sign out
        mFirebaseMng.signOut();
        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        setUIState(UISTATE_LOGGED_OUT);
                    }
                });
    }

    /**
     * When connection and swim tracks are available starts
     * performing background data backup. See performBackup()
     */
    @Click(R.id.btn_backup)
    void onBtnBackupClick(){
        if(!ConnectivityUtils.isDeviceConnected(this))
        {
            Toast.makeText(getBaseContext(),R.string.no_connection,Toast.LENGTH_LONG).show();
            return;
        }
        if(mSwimTrackMng.getSwimTracks(false).size()==0)
        {
            Toast.makeText(getBaseContext(),R.string.no_swim,Toast.LENGTH_LONG).show();
            return;
        }

        setUIState(UISTATE_PERFORMS_BACKUP_RESTORE);
        performBackup();
    }

    /**
     * Get data to be backup from swim track manager,
     * then perform the backup with the
     * firebase manager. When completed update the UI.
     * See updateUIAfterBackup()
     */
    @Background
    void performBackup(){
        String fileContent = mSwimTrackMng.getLocalDataForBackup();
        mFirebaseMng.backupOnFireDatabase(fileContent, new FirebaseManager.IBackupCallback()
        {
            @Override
            public void onSuccess(Object _null)
            {
                updateUIAfterBackup(true);
            }

            @Override
            public void onFail(Exception ex)
            {
                LLog.e(ex);
                updateUIAfterBackup(false);
            }
        });
    }

    /**
     * Update the UI to inform the user about
     * the backup result, when completed successfully
     * send the user back to the main activity
     * and request the to display the swim list.
     * @param completed backup successfully completed.
     */
    @UiThread
    void updateUIAfterBackup(boolean completed){
        if(completed){
            mProgress.setProgress(100);
            setUIState(UISTATE_LOGGED_IN);
            mTxtMessage.setText(R.string.task_completed);

            //after 1 second, send the user back to the
            //main activity
            new android.os.Handler().postDelayed(new Runnable() {
                public void run() {
                    Intent intent = new Intent(getBaseContext(),MainActivity_.class);
                    intent.putExtra(MainActivity.REQUEST_SELECTED_TAB_KEY, 1);
                    startActivity(intent);
                }
            }, 1000);
        }
        else{
            setUIState(UISTATE_LOGGED_IN);
            mTxtMessage.setText(R.string.error_backup);
        }
    }

    /**
     * When connection is available and the
     * the user confirms overriding of local
     * data, start the data restore from backup
     * in background mode.
     * See performRestore().
     */
    @Click(R.id.btn_restore)
    void onBtnRestoreClick(){
        if(!ConnectivityUtils.isDeviceConnected(this))
        {
            Toast.makeText(getBaseContext(),R.string.no_connection,Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.please_confirm)
                .setMessage(R.string.restore_confirm_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setUIState(UISTATE_PERFORMS_BACKUP_RESTORE);
                        performRestore();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                })
                .show();
    }

    /**
     * Get data from Firebase Database using
     * the firebase manager, then save data
     * locally overriding existing once (if any)
     * by the swim track manager.
     * See updateUIAfterRestore().
     */
    @Background
    void performRestore(){
        mFirebaseMng.restoreFromFireDatabase(new FirebaseManager.IBackupCallback()
        {
            @Override
            public void onSuccess(Object data)
            {
                mSwimTrackMng.restoreLocalDataFromBackup((String)data);
                updateUIAfterRestore(true);
            }

            @Override
            public void onFail(Exception ex)
            {
                LLog.e(ex);
                updateUIAfterRestore(false);
            }
        });
    }

    /**
     * Update the UI to inform the user about
     * the restore result, when completed successfully
     * send the user back to the main activity
     * and request the to display the swim list
     * with new data downloaded.
     * @param completed restore successfully completed
     */
    @UiThread
    void updateUIAfterRestore(boolean completed){
        if(completed){
            mProgress.setProgress(100);
            setUIState(UISTATE_LOGGED_IN);
            mTxtMessage.setText(R.string.task_completed);

            new android.os.Handler().postDelayed(new Runnable() {
                public void run() {
                    Intent intent = new Intent(getBaseContext(),MainActivity_.class);
                    intent.putExtra(MainActivity.REQUEST_SELECTED_TAB_KEY, 1);
                    intent.putExtra(SwimListFragment.UPDATE_LIST_KEY, true);
                    startActivity(intent);
                }
            }, 1000);
        }
        else{
            setUIState(UISTATE_LOGGED_IN);
            mTxtMessage.setText(R.string.error_restore);
        }
    }

    /**
     * Perform a login to Firebase Auth service with
     * Google account credentials
     * @param acct google signin account
     */
    private void performFirebaseAuthWithGoogle(GoogleSignInAccount acct) {

        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mFirebaseMng.signInWithCredentials(credential, new FirebaseManager.IAuthSignInCallback()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    Log.w(TAG, "signInWithCredential", task.getException());
                    Toast.makeText(getBaseContext(), R.string.authentication_fail,
                            Toast.LENGTH_SHORT).show();
                }

                hideProgressDialog();
            }
        });
    }

    /**
     * Shows/hides view controls according to the
     * given view state.
     * @param state the current state to display, see list of states available in this class.
     */
    private void setUIState(int state) {

        hideProgressDialog();

        switch (state){
            case UISTATE_LOGGED_OUT:
                mTxtMessage.setText(R.string.please_login);
                mBtnBackup.setVisibility(View.GONE);
                mBtnRestore.setVisibility(View.GONE);
                mBtnSignOut.setVisibility(View.GONE);
                mBtnSignIn.setVisibility(View.VISIBLE);
                mProgress.setVisibility(View.GONE);
                break;

            case UISTATE_LOGGED_IN:
                mTxtMessage.setText(R.string.backup_restore_info);
                mBtnBackup.setVisibility(View.VISIBLE);
                mBtnBackup.setEnabled(true);
                mBtnRestore.setVisibility(View.VISIBLE);
                mBtnRestore.setEnabled(true);
                mBtnSignOut.setVisibility(View.VISIBLE);
                mBtnSignOut.setEnabled(true);
                mBtnSignIn.setVisibility(View.GONE);
                mProgress.setVisibility(View.GONE);
                break;

            case UISTATE_PERFORMS_BACKUP_RESTORE:
                mTxtMessage.setText(R.string.performing_backup);
                mProgress.setVisibility(View.VISIBLE);
                mBtnBackup.setEnabled(false);
                mBtnRestore.setEnabled(false);
                mBtnSignOut.setEnabled(false);
                break;
        }

    }

    /**
     * Shows login progress dialog
     */
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.progress_auth));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    /**
     * Hides login progress dialog
     */
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

}
