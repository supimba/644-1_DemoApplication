package ch.hevs.android.demoapplication.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.hevs.android.demoapplication.entity.AccountEntity;
import ch.hevs.android.demoapplication.ui.activity.MainActivity;

public class AccountListViewModel extends AndroidViewModel {

    private static final String TAG = "AccountListViewModel";

    private final MutableLiveData<List<AccountEntity>> mObservableAccounts;

    public AccountListViewModel(@NonNull Application application) {
        super(application);

        mObservableAccounts = new MutableLiveData<>();

        if (mObservableAccounts.getValue() == null) {
            FirebaseDatabase.getInstance()
                    .getReference("clients")
                    .orderByChild("accounts")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                mObservableAccounts.setValue(toAccounts(dataSnapshot));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "getAll: onCancelled", databaseError.toException());
                        }
                    });
        }
    }

    public AccountListViewModel(@NonNull Application application,
                                final String owner) {
        super(application);

        mObservableAccounts = new MutableLiveData<>();

        if (mObservableAccounts.getValue() == null) {
            FirebaseDatabase.getInstance()
                    .getReference("clients")
                    .child(owner)
                    .child("accounts")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                mObservableAccounts.setValue(toAccounts(dataSnapshot));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "getAll: onCancelled", databaseError.toException());
                        }
                    });
        }
    }

    /**
     * Expose the LiveData AccountEntities query so the UI can observe it.
     */
    public LiveData<List<AccountEntity>> getAccounts() {
        return mObservableAccounts;
    }

    public void deleteAccount(View view, AccountEntity account) {
        FirebaseDatabase.getInstance()
                .getReference("clients")
                .child(account.getOwner())
                .child("accounts")
                .child(account.getId())
                .removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.d(TAG, "Delete failure!", databaseError.toException());
                        } else {
                            Log.d(TAG, "Delete successful!");
                        }
                    }
                });
        mObservableAccounts.getValue().remove(account);
    }

    public void addAccount(View view, AccountEntity account) {
        account.setId(UUID.randomUUID().toString());
        FirebaseDatabase.getInstance()
                .getReference("clients")
                .child(account.getOwner())
                .child("accounts")
                .child(account.getId())
                .setValue(account, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.d(TAG, "Insert failure!", databaseError.toException());
                        } else {
                            Log.d(TAG, "Insert successful!");
                        }
                    }
                });

        mObservableAccounts.getValue().add(account);
    }

    public void updateAccount(View view, AccountEntity account) {
        FirebaseDatabase.getInstance()
                .getReference("clients")
                .child(account.getOwner())
                .child("accounts")
                .child(account.getId())
                .updateChildren(account.toMap(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.d(TAG, "Update failure!", databaseError.toException());
                        } else {
                            Log.d(TAG, "Update successful!");
                        }
                    }
                });
        mObservableAccounts.getValue().set(mObservableAccounts.getValue().indexOf(account), account);
    }

    /**
     * A creator is used to inject the account owner id into the ViewModel
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final String mAccountId;

        public Factory(@NonNull Application application, String accountId) {
            mApplication = application;
            mAccountId = accountId;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new AccountListViewModel(mApplication, mAccountId);
        }
    }

    private List<AccountEntity> toAccounts(DataSnapshot snapshot) {
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        List<AccountEntity> accounts = new ArrayList<>();
        SharedPreferences settings = getApplication().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
            AccountEntity entity = childSnapshot.getValue(AccountEntity.class);
            entity.setId(childSnapshot.getKey());
            entity.setOwner(user);
            accounts.add(entity);
        }
        return accounts;
    }
}
