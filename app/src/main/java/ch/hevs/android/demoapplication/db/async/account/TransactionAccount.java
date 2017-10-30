package ch.hevs.android.demoapplication.db.async.account;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;
import android.view.View;

import java.lang.ref.WeakReference;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.AccountEntity;

public class TransactionAccount extends AsyncTask<Pair<AccountEntity, AccountEntity>, Void, Void> {

    // Weak references will still allow the Activity to be garbage-collected
    private final WeakReference<View> mView;

    public TransactionAccount(View view) {
        mView = new WeakReference<>(view);
    }

    @Override
    protected Void doInBackground(Pair<AccountEntity, AccountEntity>[] pairs) {
        DatabaseCreator dbCreator = DatabaseCreator.getInstance(mView.get().getContext());
        for (Pair<AccountEntity, AccountEntity> accounts : pairs)
            dbCreator.getDatabase()
                    .accountDao()
                    .transaction(accounts.first, accounts.second);
        return null;
    }
}
