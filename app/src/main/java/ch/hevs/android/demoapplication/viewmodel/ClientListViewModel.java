package ch.hevs.android.demoapplication.viewmodel;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import java.util.List;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.ClientEntity;

public class ClientListViewModel extends AndroidViewModel {

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
     * Expose the LiveData Products query so the UI can observe it.
     */
    public LiveData<List<ClientEntity>> getClients() {
        return mObservableClients;
    }
}
