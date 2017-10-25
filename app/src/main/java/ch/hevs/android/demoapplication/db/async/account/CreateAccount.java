package ch.hevs.android.demoapplication.db.async.account;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.AccountEntity;

public class CreateAccount extends AsyncTask<AccountEntity, Void, Long> {

    private Context mContext;

    public CreateAccount(Context context) {
        mContext = context;
    }

    @Override
    protected Long doInBackground(AccountEntity... params) throws SQLiteConstraintException {
        DatabaseCreator dbCreator = DatabaseCreator.getInstance(mContext);
        return dbCreator.getDatabase().accountDao().insert(params[0]);
    }

}
