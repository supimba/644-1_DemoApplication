package ch.hevs.android.demoapplication.db.async.client;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.ClientEntity;

public class GetClients extends AsyncTask<Void, Void, List<ClientEntity>> {

    private Context mContext;

    public GetClients(Context context) {
        mContext = context;
    }

    @Override
    protected List<ClientEntity> doInBackground(Void... voids) {
        DatabaseCreator dbCreator = DatabaseCreator.getInstance(mContext);
        return dbCreator.getDatabase().clientDao().getAllSync();
    }
}
