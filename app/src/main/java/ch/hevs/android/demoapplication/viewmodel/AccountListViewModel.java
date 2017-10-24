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
import ch.hevs.android.demoapplication.db.entity.AccountEntity;

public class AccountListViewModel extends AndroidViewModel {

    private static final MutableLiveData ABSENT = new MutableLiveData();
    {
        //noinspection unchecked
        ABSENT.setValue(null);
    }

    private final LiveData<List<AccountEntity>> mObservableAccounts;

    public AccountListViewModel(@NonNull Application application) {
        super(application);

        final DatabaseCreator databaseCreator = DatabaseCreator.getInstance(this.getApplication());

        LiveData<Boolean> databaseCreated = databaseCreator.isDatabaseCreated();
        mObservableAccounts = Transformations.switchMap(databaseCreated,
                new Function<Boolean, LiveData<List<AccountEntity>>>() {
                    @Override
                    public LiveData<List<AccountEntity>> apply(Boolean isDbCreated) {
                        if (!Boolean.TRUE.equals(isDbCreated)) { // Not needed here, but watch out for null
                            //noinspection unchecked
                            return ABSENT;
                        } else {
                            //noinspection ConstantConditions
                            return databaseCreator.getDatabase().accountDao().getAll();
                        }
                    }
                });

        databaseCreator.createDb(this.getApplication());
    }

    public AccountListViewModel(@NonNull Application application,
                                final String owner) {
        super(application);

        final DatabaseCreator databaseCreator = DatabaseCreator.getInstance(this.getApplication());

        LiveData<Boolean> databaseCreated = databaseCreator.isDatabaseCreated();
        mObservableAccounts = Transformations.switchMap(databaseCreated,
                new Function<Boolean, LiveData<List<AccountEntity>>>() {
                    @Override
                    public LiveData<List<AccountEntity>> apply(Boolean isDbCreated) {
                        if (!Boolean.TRUE.equals(isDbCreated)) { // Not needed here, but watch out for null
                            //noinspection unchecked
                            return ABSENT;
                        } else {
                            //noinspection ConstantConditions
                            return databaseCreator.getDatabase().accountDao().getOwned(owner);
                        }
                    }
                });

        databaseCreator.createDb(this.getApplication());
    }

    /**
     * Expose the LiveData Products query so the UI can observe it.
     */
    public LiveData<List<AccountEntity>> getAccounts() {
        return mObservableAccounts;
    }
}
