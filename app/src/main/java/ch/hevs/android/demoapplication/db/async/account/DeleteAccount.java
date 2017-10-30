package ch.hevs.android.demoapplication.db.async.account;

import android.os.AsyncTask;
import android.view.View;

import java.lang.ref.WeakReference;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.AccountEntity;

public class DeleteAccount extends AsyncTask<AccountEntity, Void, Void> {

    // Weak references will still allow the Activity to be garbage-collected
    private final WeakReference<View> mView;

    public DeleteAccount(View view) {
        mView = new WeakReference<>(view);
    }

    @Override
    protected Void doInBackground(AccountEntity... params) {
        DatabaseCreator dbCreator = DatabaseCreator.getInstance(mView.get().getContext());
        for (AccountEntity account : params)
            dbCreator.getDatabase().accountDao().delete(account);
        return null;
    }
}
