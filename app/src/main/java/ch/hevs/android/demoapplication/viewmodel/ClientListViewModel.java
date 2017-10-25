package ch.hevs.android.demoapplication.viewmodel;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutionException;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.async.client.CreateClient;
import ch.hevs.android.demoapplication.db.async.client.DeleteClient;
import ch.hevs.android.demoapplication.db.async.client.UpdateClient;
import ch.hevs.android.demoapplication.db.entity.ClientEntity;

public class ClientListViewModel extends AndroidViewModel {

    private static final String TAG = "AccountListViewModel";

    private static final MutableLiveData ABSENT = new MutableLiveData();
    {
        //noinspection unchecked
        ABSENT.setValue(null);
    }

    private final LiveData<List<ClientEntity>> mObservableClients;

    public ClientListViewModel(@NonNull Application application) {
        super(application);

        final DatabaseCreator databaseCreator = DatabaseCreator.getInstance(this.getApplication());

        LiveData<Boolean> databaseCreated = databaseCreator.isDatabaseCreated();
        mObservableClients = Transformations.switchMap(databaseCreated,
                new Function<Boolean, LiveData<List<ClientEntity>>>() {
                    @Override
                    public LiveData<List<ClientEntity>> apply(Boolean isDbCreated) {
                        if (!Boolean.TRUE.equals(isDbCreated)) { // Not needed here, but watch out for null
                            //noinspection unchecked
                            return ABSENT;
                        } else {
                            //noinspection ConstantConditions
                            return databaseCreator.getDatabase().clientDao().getAll();
                        }
                    }
                });

        databaseCreator.createDb(this.getApplication());
    }

    /**
     * Expose the LiveData ClientEntities query so the UI can observe it.
     */
    public LiveData<List<ClientEntity>> getClients() {
        return mObservableClients;
    }

    public void deleteClient(Context context, ClientEntity client) {
        new DeleteClient(context).execute(client);
        mObservableClients.getValue().remove(client);
    }

    public boolean addClient(Context context, ClientEntity client) throws SQLiteConstraintException {
        boolean response;
        try {
            response = new CreateClient(context).execute(client).get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, e.getMessage(), e);
            return false;
        }
        mObservableClients.getValue().add(client);
        return response;
    }

    public void updateClient(Context context, ClientEntity client) {
        new UpdateClient(context).execute(client);
        mObservableClients.getValue().set(mObservableClients.getValue().indexOf(client), client);
    }

    public ClientEntity getClient(Context context, String email) {
        for (ClientEntity entity : mObservableClients.getValue()) {
            if (entity.getEmail().equals(email))
                return entity;
        }
        return null;
    }
}
