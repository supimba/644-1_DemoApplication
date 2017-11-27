package ch.hevs.android.demoapplication.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.database.sqlite.SQLiteConstraintException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ch.hevs.android.demoapplication.entity.ClientEntity;

public class ClientListViewModel extends AndroidViewModel {

    private static final String TAG = "ClientListViewModel";

    private final MutableLiveData<List<ClientEntity>> mObservableClients;

    public ClientListViewModel(@NonNull Application application) {
        super(application);

        mObservableClients = new MutableLiveData<>();

        if (mObservableClients.getValue() == null) {
            FirebaseDatabase.getInstance()
                    .getReference()
                    .orderByChild("clients")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                mObservableClients.setValue(toClients(dataSnapshot));
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
     * Expose the LiveData ClientEntities query so the UI can observe it.
     */
    public LiveData<List<ClientEntity>> getClients() {
        return mObservableClients;
    }

    public void deleteClient(ClientEntity client) {
        FirebaseDatabase.getInstance()
                .getReference("clients")
                .child(client.getId())
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
        /*
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User account deleted");
                    } else {
                        Log.d(TAG, "signInWithEmail:failure", task.getException());
                    }
                }
            });
        }
        */
        mObservableClients.getValue().remove(client);


    }

    public void updateClient(ClientEntity client) {
        FirebaseDatabase.getInstance()
                .getReference("clients")
                .child(client.getId())
                .updateChildren(client.toMap(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.d(TAG, "Update failure!", databaseError.toException());
                        } else {
                            Log.d(TAG, "Update successful!");
                        }
                    }
                });
        mObservableClients.getValue().set(mObservableClients.getValue().indexOf(client), client);
    }

    private List<ClientEntity> toClients(DataSnapshot snapshot) {
        List<ClientEntity> accounts = new ArrayList<>();
        for (DataSnapshot childSnapshot : snapshot.getChildren()) {
            ClientEntity entity = childSnapshot.getValue(ClientEntity.class);
            entity.setId(childSnapshot.getKey());
            accounts.add(entity);
        }
        return accounts;
    }
}
