package ch.hevs.android.demoapplication.db.async.client;

import android.os.AsyncTask;
import android.view.View;

import java.lang.ref.WeakReference;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.ClientEntity;

public class UpdateClient extends AsyncTask<ClientEntity, Void, Void> {

    // Weak references will still allow the Activity to be garbage-collected
    private final WeakReference<View> mView;

    public UpdateClient(View view) {
        mView = new WeakReference<>(view);
    }

    @Override
    protected Void doInBackground(ClientEntity... params) {
        DatabaseCreator dbCreator = DatabaseCreator.getInstance(mView.get().getContext());
        for (ClientEntity client : params)
            dbCreator.getDatabase().clientDao().update(client);
        return null;
    }
}
