package ch.hevs.android.demoapplication.db.async.account;

import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.view.View;

import java.lang.ref.WeakReference;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.AccountEntity;

public class CreateAccount extends AsyncTask<AccountEntity, Void, Void> {

    // Weak references will still allow the Activity to be garbage-collected
    private final WeakReference<View> mView;

    public CreateAccount(View view) {
        mView = new WeakReference<>(view);
    }

    @Override
    protected Void doInBackground(AccountEntity... params) throws SQLiteConstraintException {
        DatabaseCreator dbCreator = DatabaseCreator.getInstance(mView.get().getContext());
        dbCreator.getDatabase().accountDao().insert(params[0]);
        return null;
    }

}
