package ch.hevs.android.demoapplication.db.async.client;

import android.content.Context;
import android.os.AsyncTask;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.ClientEntity;

public class DeleteClient extends AsyncTask<ClientEntity, Void, Void> {

    private Context mContext;

    public DeleteClient(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(ClientEntity... params) {
        DatabaseCreator dbCreator = DatabaseCreator.getInstance(mContext);
        for (ClientEntity client : params)
            dbCreator.getDatabase().clientDao().delete(client);
        return null;
    }
}
