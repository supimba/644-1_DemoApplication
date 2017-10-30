package ch.hevs.android.demoapplication.db.async.account;

import android.os.AsyncTask;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.List;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.AccountEntity;

public class GetOwnAccounts extends AsyncTask<String, Void, List<AccountEntity>> {

    // Weak references will still allow the Activity to be garbage-collected
    private final WeakReference<View> mView;

    public GetOwnAccounts(View view) {
        mView = new WeakReference<>(view);
    }

    @Override
    protected List<AccountEntity> doInBackground(String... strings) {
        DatabaseCreator dbCreator = DatabaseCreator.getInstance(mView.get().getContext());
        return dbCreator.getDatabase().accountDao().getOwnedSync(strings[0]);
    }
}
