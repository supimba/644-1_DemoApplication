package ch.hevs.android.demoapplication.db.async.account;

import android.os.AsyncTask;
import android.view.View;

import java.lang.ref.WeakReference;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.AccountEntity;

public class GetAccount extends AsyncTask<Long, Void, AccountEntity> {

    // Weak references will still allow the Activity to be garbage-collected
    private final WeakReference<View> mView;

    public GetAccount(View view) {
        mView = new WeakReference<>(view);
    }

    @Override
    protected AccountEntity doInBackground(Long... longs) {
        DatabaseCreator dbCreator = DatabaseCreator.getInstance(mView.get().getContext());
        return dbCreator.getDatabase().accountDao().getByIdSync(longs[0]);
    }
}
