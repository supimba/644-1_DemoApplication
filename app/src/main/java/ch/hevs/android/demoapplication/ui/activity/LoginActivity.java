package ch.hevs.android.demoapplication.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.hevs.android.demoapplication.R;
import ch.hevs.android.demoapplication.entity.AccountEntity;
import ch.hevs.android.demoapplication.entity.ClientEntity;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private AutoCompleteTextView mEmailView;
    private TextInputEditText mPasswordView;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        addData();

        mProgressBar = findViewById(R.id.progress);

        // Set up the login form.
        mEmailView = findViewById(R.id.email);

        mPasswordView = findViewById(R.id.password);

        Button emailSignInButton = findViewById(R.id.email_sign_in_button);
        emailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                mEmailView.setText("");
                mPasswordView.setText("");
            }
        });
    }

    /**
     * Attempts to sign in or register the client specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        if (validateForm(email, password)) {
            mProgressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            mProgressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "loginUserWithEmail: success");

                                // We need an Editor object to make preference changes.
                                // All objects are from android.context.Context
                                final SharedPreferences.Editor editor = getSharedPreferences(MainActivity.PREFS_NAME, 0).edit();
                                FirebaseDatabase.getInstance()
                                        .getReference("clients")
                                        .child(mAuth.getCurrentUser().getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    editor.putBoolean(MainActivity.PREFS_ADM, dataSnapshot.getValue(ClientEntity.class).getAdmin());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Log.d(TAG, "getAdminRights: onCancelled", databaseError.toException());
                                            }
                                        });
                                editor.apply();

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                mEmailView.setText("");
                                mPasswordView.setText("");
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.d(TAG, "loginUserWithEmail: failure", task.getException());
                                mEmailView.setError(getString(R.string.error_invalid_email));
                                mEmailView.requestFocus();
                                mPasswordView.setText("");
                            }
                        }
                    });
        }
    }

    private boolean validateForm(String email, String password) {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            mPasswordView.setText("");
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        return !cancel;
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private void addData() {
        List<ClientEntity> clients = new ArrayList<>();
        List<AccountEntity> accounts = new ArrayList<>();

        ClientEntity client1 = new ClientEntity();
        ClientEntity client2 = new ClientEntity();
        ClientEntity client3 = new ClientEntity();
        ClientEntity client4 = new ClientEntity();

        client1.setFirstName("Michel");
        client1.setLastName("Platini");
        client1.setEmail("m.p@fifa.com");
        client1.setId("afxWlnpnwcQz52vNfwOK0gESJum1");
        client1.setPassword("platini1");
        client1.setAdmin(false);
        clients.add(client1);

        client2.setFirstName("Sepp");
        client2.setLastName("Blatter");
        client2.setEmail("s.b@fifa.com");
        client2.setId("T5Ut1jePpkM7tLADc26OlfqAj9J3");
        client2.setPassword("blatter1");
        client2.setAdmin(true);
        clients.add(client2);

        client3.setFirstName("Ebbe");
        client3.setLastName("Schwartz");
        client3.setEmail("e.s@fifa.com");
        client3.setId("CZKUC27B2DRL5UVanNt7kturoJi2");
        client3.setPassword("schwartz1");
        client3.setAdmin(false);
        clients.add(client3);

        client4.setFirstName("Aleksander");
        client4.setLastName("Ceferin");
        client4.setEmail("a.c@fifa.com");
        client4.setId("l9AAskVtKEP2YxrOgO8OBiSzlKi2");
        client4.setPassword("ceferin1");
        client4.setAdmin(false);
        clients.add(client4);

        AccountEntity account1 = new AccountEntity();
        AccountEntity account2 = new AccountEntity();
        AccountEntity account3 = new AccountEntity();
        AccountEntity account4 = new AccountEntity();
        AccountEntity account5 = new AccountEntity();
        AccountEntity account6 = new AccountEntity();
        AccountEntity account7 = new AccountEntity();
        AccountEntity account8 = new AccountEntity();


        account1.setId(UUID.randomUUID().toString());
        account1.setBalance(20000d);
        account1.setName("Savings");
        account1.setOwner(clients.get(0).getId());
        accounts.add(account1 );

        account2.setId(UUID.randomUUID().toString());
        account2.setBalance(1840000d);
        account2.setName("Secret");
        account2.setOwner(clients.get(0).getId());
        accounts.add(account2);

        account3.setId(UUID.randomUUID().toString());
        account3.setBalance(21000d);
        account3.setName("Savings");
        account3.setOwner(clients.get(1).getId());
        accounts.add(account3);

        account4.setId(UUID.randomUUID().toString());
        account4.setBalance(1820000d);
        account4.setName("Secret");
        account4.setOwner(clients.get(1).getId());
        accounts.add(account4);

        account5.setId(UUID.randomUUID().toString());
        account5.setBalance(18500d);
        account5.setName("Savings");
        account5.setOwner(clients.get(2).getId());
        accounts.add(account5);

        account6.setId(UUID.randomUUID().toString());
        account6.setBalance(1810000d);
        account6.setName("Secret");
        account6.setOwner(clients.get(2).getId());
        accounts.add(account6);

        account7.setId(UUID.randomUUID().toString());
        account7.setBalance(19000d);
        account7.setName("Savings");
        account7.setOwner(clients.get(3).getId());
        accounts.add(account7);

        account8.setId(UUID.randomUUID().toString());
        account8.setBalance(1902360d);
        account8.setName("Secret");
        account8.setOwner(clients.get(3).getId());
        accounts.add(account8);

        for (ClientEntity client : clients) {
            mDatabase.child("clients").child(client.getId()).setValue(client);
        }

        for (AccountEntity account : accounts) {
            mDatabase.child("clients").child(account.getOwner()).child("accounts").child(account.getId()).setValue(account);
        }
    }
}

