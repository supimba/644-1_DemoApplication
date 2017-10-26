package ch.hevs.android.demoapplication.db.async.account;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.AccountEntity;

public class TransactionAccount extends AsyncTask<Pair<AccountEntity, AccountEntity>, Void, Void> {

    private Context mContext;

    public TransactionAccount(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Pair<AccountEntity, AccountEntity>[] pairs) {
        DatabaseCreator dbCreator = DatabaseCreator.getInstance(mContext);
        for (Pair<AccountEntity, AccountEntity> accounts : pairs)
            dbCreator.getDatabase()
                    .accountDao()
                    .transaction(accounts.first, accounts.second);
        return null;
    }
}
