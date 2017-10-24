package ch.hevs.android.demoapplication.viewmodel;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.AccountEntity;

public class AccountViewModel extends AndroidViewModel {

    private static final MutableLiveData ABSENT = new MutableLiveData();
    {
        //noinspection unchecked
        ABSENT.setValue(null);
    }

    private final LiveData<AccountEntity> mObservableProduct;

    public ObservableField<AccountEntity> account = new ObservableField<>();

    private final long mAccountId;

    public AccountViewModel(@NonNull Application application,
                            final long productId) {
        super(application);
        mAccountId = productId;

        final DatabaseCreator databaseCreator = DatabaseCreator.getInstance(this.getApplication());

        mObservableProduct = Transformations.switchMap(databaseCreator.isDatabaseCreated(), new Function<Boolean, LiveData<AccountEntity>>() {
            @Override
            public LiveData<AccountEntity> apply(Boolean isDbCreated) {
                if (!isDbCreated) {
                    //noinspection unchecked
                    return ABSENT;
                } else {
                    //noinspection ConstantConditions
                    return databaseCreator.getDatabase().accountDao().getById(mAccountId);
                }
            }
        });

        databaseCreator.createDb(this.getApplication());

    }

    public LiveData<AccountEntity> getObservableProduct() {
        return mObservableProduct;
    }

    public void setAccount(AccountEntity account) {
        this.account.set(account);
    }

    /**
     * A creator is used to inject the client ID into the ViewModel
     * <p>
     * This creator is to showcase how to inject dependencies into ViewModels. It's not
     * actually necessary in this case, as the client ID can be passed in a public method.
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final int mAccountId;

        public Factory(@NonNull Application application, int accountId) {
            mApplication = application;
            mAccountId = accountId;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new AccountViewModel(mApplication, mAccountId);
        }
    }
}
