package com.vcab.vcabcustomer.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vcab.vcabcustomer.MainActivity;
import com.vcab.vcabcustomer.MessagesClass;
import com.vcab.vcabcustomer.R;

import java.util.Arrays;
import java.util.List;

public class UserAuthenticationActivity extends AppCompatActivity {

    private final static int LOGIN_REQUEST_CODE = 7171; // any Number
    private List<AuthUI.IdpConfig> provider;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_user_authentication);

        init();
    }

    private void init() {

        provider = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build()
                , new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        firebaseAuth = FirebaseAuth.getInstance();


/*        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();

            }
        };*/

        //same as above (-> lamda expression)
        listener = myFirebaseAuth -> {

            FirebaseUser user = myFirebaseAuth.getCurrentUser();
            if (user != null) {
                ProgressBar progress_bar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);

                progress_bar.setVisibility(View.VISIBLE);
                DocumentReference nycRef = FirebaseFirestore.getInstance().collection("users").document("customers")
                        .collection("userData").document(user.getUid());

                nycRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();

                            if (document.exists()) {
                                startActivity(new Intent(UserAuthenticationActivity.this, MainActivity.class));
                            } else {
                                startActivity(new Intent(UserAuthenticationActivity.this, UserDetailsActivity.class));
                            }

                            progress_bar.setVisibility(View.GONE);
                            finish();
                        } else {
                            progress_bar.setVisibility(View.GONE);
                            MessagesClass.showToastMsg("Not ok big", UserAuthenticationActivity.this);
                        }
                    }
                });

            } else {
                showLoginLayout();
            }
        };

    }

    private void showLoginLayout() {
        AuthMethodPickerLayout authMethodPickerLayout = new AuthMethodPickerLayout.Builder(R.layout.activity_user_authentication)
                .setPhoneButtonId(R.id.loginWithPhone)
                .setGoogleButtonId(R.id.loginWithGoogle)
                .build();

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setAvailableProviders(provider)
                .setIsSmartLockEnabled(false)
                .setTheme(R.style.LoginTheme)
                .build();

        signInLauncher.launch(signInIntent);
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {

                    IdpResponse response = result.getIdpResponse();

                    if (result.getResultCode() == RESULT_OK) {

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    } else {
                        MessagesClass.showToastMsg("Failed to sign in: " + response.getError().getMessage(), UserAuthenticationActivity.this);
                    }


                }
            }
    );

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (firebaseAuth != null && listener != null) {
            firebaseAuth.removeAuthStateListener(listener);
        }
        super.onStop();

    }
}