package ch.hevs.android.demoapplication.db.async.client;

import android.content.Context;
import android.os.AsyncTask;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.ClientEntity;

public class GetClient extends AsyncTask<String, Void, ClientEntity> {

    private Context mContext;

    public GetClient(Context context) {
        mContext = context;
    }

    @Override
    protected ClientEntity doInBackground(String... strings) {
        DatabaseCreator dbCreator = DatabaseCreator.getInstance(mContext);
        return dbCreator.getDatabase().clientDao().getByIdSync(strings[0]);
    }
}
