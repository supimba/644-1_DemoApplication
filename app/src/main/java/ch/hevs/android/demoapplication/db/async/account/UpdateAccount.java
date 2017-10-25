package ch.hevs.android.demoapplication.db.async.account;

import android.content.Context;
import android.os.AsyncTask;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.AccountEntity;

public class UpdateAccount extends AsyncTask<AccountEntity, Void, Void> {

    private Context mContext;

    public UpdateAccount(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(AccountEntity... params) {
        DatabaseCreator dbCreator = DatabaseCreator.getInstance(mContext);
        for (AccountEntity account : params)
            dbCreator.getDatabase().accountDao().update(account);
        return null;
    }
}
