package ch.hevs.android.demoapplication.viewmodel;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutionException;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.async.account.CreateAccount;
import ch.hevs.android.demoapplication.db.async.account.DeleteAccount;
import ch.hevs.android.demoapplication.db.async.account.UpdateAccount;
import ch.hevs.android.demoapplication.db.entity.AccountEntity;

public class AccountListViewModel extends AndroidViewModel {

    private static final String TAG = "AccountListViewModel";

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
     * Expose the LiveData AccountEntities query so the UI can observe it.
     */
    public LiveData<List<AccountEntity>> getAccounts() {
        return mObservableAccounts;
    }

    public void deleteAccount(Context context, AccountEntity account) {
        new DeleteAccount(context).execute(account);
        mObservableAccounts.getValue().remove(account);
    }

    public void addAccount(Context context, AccountEntity account) {
        try {
            Long id = new CreateAccount(context).execute(account).get();
            account.setId(id);
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, e.getMessage(), e);
            return;
        }
        mObservableAccounts.getValue().add(account);
    }

    public void updateAccount(Context context, AccountEntity account) {
        new UpdateAccount(context).execute(account);
        mObservableAccounts.getValue().set(mObservableAccounts.getValue().indexOf(account), account);
    }

    public AccountEntity getAccount(Context context, long id) {
        for (AccountEntity entity : mObservableAccounts.getValue()) {
            if (entity.getId().equals(id))
                return entity;
        }
        return null;
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
}
