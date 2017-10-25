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
import ch.hevs.android.demoapplication.db.entity.ClientEntity;

public class ClientViewModel extends AndroidViewModel {

    private static final MutableLiveData ABSENT = new MutableLiveData();
    {
        //noinspection unchecked
        ABSENT.setValue(null);
    }

    private final LiveData<ClientEntity> mObservableClient;

    public ObservableField<ClientEntity> client = new ObservableField<>();

    private final String mClientId;

    public ClientViewModel(@NonNull Application application,
                            final String clientId) {
        super(application);
        mClientId = clientId;

        final DatabaseCreator databaseCreator = DatabaseCreator.getInstance(this.getApplication());

        mObservableClient = Transformations.switchMap(databaseCreator.isDatabaseCreated(), new Function<Boolean, LiveData<ClientEntity>>() {
            @Override
            public LiveData<ClientEntity> apply(Boolean isDbCreated) {
                if (!isDbCreated) {
                    //noinspection unchecked
                    return ABSENT;
                } else {
                    //noinspection ConstantConditions
                    return databaseCreator.getDatabase().clientDao().getById(mClientId);
                }
            }
        });

        databaseCreator.createDb(this.getApplication());

    }

    public LiveData<ClientEntity> getObservableClient() {
        return mObservableClient;
    }

    public void setClient(ClientEntity account) {
        this.client.set(account);
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

        private final String mClientId;

        public Factory(@NonNull Application application, String clientId) {
            mApplication = application;
            mClientId = clientId;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new ClientViewModel(mApplication, mClientId);
        }
    }
}
