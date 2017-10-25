package ch.hevs.android.demoapplication.db.async.client;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.ClientEntity;

public class CreateClient extends AsyncTask<ClientEntity, Void, Boolean> {

    private Context mContext;

    public CreateClient(Context context) {
        mContext = context;
    }

    @Override
    protected Boolean doInBackground(ClientEntity... params) {
        DatabaseCreator dbCreator = DatabaseCreator.getInstance(mContext);
        boolean response = true;
        try {
            for (ClientEntity client : params)
                dbCreator.getDatabase().clientDao().insert(client);
        } catch (SQLiteConstraintException e) {
            response = false;
        }
        return response;
    }
}
