package ch.hevs.android.demoapplication.db.async.account;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.AccountEntity;

public class GetOwnAccounts extends AsyncTask<String, Void, List<AccountEntity>> {

    private Context mContext;

    public GetOwnAccounts(Context context) {
        mContext = context;
    }

    @Override
    protected List<AccountEntity> doInBackground(String... strings) {
        DatabaseCreator dbCreator = DatabaseCreator.getInstance(mContext);
        return dbCreator.getDatabase().accountDao().getOwnedSync(strings[0]);
    }
}
