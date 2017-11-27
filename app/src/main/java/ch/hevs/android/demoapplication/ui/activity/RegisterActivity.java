package ch.hevs.android.demoapplication.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ch.hevs.android.demoapplication.R;
import ch.hevs.android.demoapplication.entity.ClientEntity;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private Boolean mResponse = false;
    private Toast mToast;
    private String mClientEmail;

    private EditText mEtFirstName;
    private EditText mEtLastName;
    private EditText mEtEmail;
    private EditText mEtPwd1;
    private EditText mEtPwd2;
    private Switch mAdminSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initializeForm();
        mToast = Toast.makeText(this, getString(R.string.client_created), Toast.LENGTH_LONG);
    }

    private void saveChanges(String firstName, String lastName, String email, String pwd, String pwd2, boolean admin) {
        if (!pwd.equals(pwd2) || pwd.length() < 5) {
            mEtPwd1.setError(getString(R.string.error_invalid_password));
            mEtPwd1.requestFocus();
            mEtPwd1.setText("");
            mEtPwd2.setText("");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEtEmail.setError(getString(R.string.error_invalid_email));
            mEtEmail.requestFocus();
            return;
        }
        ClientEntity newClient = new ClientEntity();
        newClient.setFirstName(firstName);
        newClient.setLastName(lastName);
        newClient.setEmail(email);
        newClient.setPassword(pwd);
        newClient.setAdmin(admin);

        addClient(newClient);
    }

    private void initializeForm() {
        mEtFirstName = findViewById(R.id.firstName);
        mEtLastName = findViewById(R.id.lastName);
        mEtEmail = findViewById(R.id.email);
        mEtPwd1 = findViewById(R.id.password);
        mEtPwd2 = findViewById(R.id.passwordRep);
        mAdminSwitch = findViewById(R.id.adminSwitch);
        Button saveBtn = findViewById(R.id.editButton);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChanges(
                        mEtFirstName.getText().toString(),
                        mEtLastName.getText().toString(),
                        mEtEmail.getText().toString(),
                        mEtPwd1.getText().toString(),
                        mEtPwd2.getText().toString(),
                        mAdminSwitch.isChecked()
                );
            }
        });
    }

    public void addClient(final ClientEntity client) {
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(client.getEmail(), client.getPassword())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail: success");
                            addClientInFirebase(client);
                        } else {
                            Log.d(TAG, "createUserWithEmail: failure", task.getException());
                            setResponse(false);
                        }
                    }
                });
    }

    private void addClientInFirebase(ClientEntity client) {
        FirebaseDatabase.getInstance()
                .getReference("clients")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(client, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.d(TAG, "Firebase DB Insert failure!", databaseError.toException());
                            setResponse(false);
                            FirebaseAuth.getInstance().getCurrentUser().delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Rollback: User account deleted");
                                            } else {
                                                Log.d(TAG, "Rollback: signInWithEmail:failure", task.getException());
                                            }
                                        }
                                    });
                        } else {
                            Log.d(TAG, "Firebase DB Insert successful!");
                            setResponse(true);
                        }
                    }
                });
    }

    private void setResponse(Boolean response) {
        if (response) {
            final SharedPreferences.Editor editor = getSharedPreferences(MainActivity.PREFS_NAME, 0).edit();
            editor.putBoolean(MainActivity.PREFS_ADM, mAdminSwitch.isChecked());
            editor.apply();
            mToast.show();
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            mEtEmail.setError(getString(R.string.error_invalid_email));
            mEtEmail.requestFocus();
        }
    }
}
