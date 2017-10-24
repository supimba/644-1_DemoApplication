package ch.hevs.android.demoapplication.db.async.account;

import android.content.Context;
import android.os.AsyncTask;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.AccountEntity;

public class GetAccount extends AsyncTask<Long, Void, AccountEntity> {

    private Context mContext;

    public GetAccount(Context context) {
        mContext = context;
    }

    @Override
    protected AccountEntity doInBackground(Long... longs) {
        DatabaseCreator dbCreator = DatabaseCreator.getInstance(mContext);
        return dbCreator.getDatabase().accountDao().getByIdSync(longs[0]);
    }
}
