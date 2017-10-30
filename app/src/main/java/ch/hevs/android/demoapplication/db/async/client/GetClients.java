package ch.hevs.android.demoapplication.db.async.client;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.List;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.ClientEntity;

public class GetClients extends AsyncTask<Void, Void, List<ClientEntity>> {

    // Weak references will still allow the Activity to be garbage-collected
    private final WeakReference<View> mView;

    public GetClients(View view) {
        mView = new WeakReference<>(view);
    }

    @Override
    protected List<ClientEntity> doInBackground(Void... voids) {
        DatabaseCreator dbCreator = DatabaseCreator.getInstance(mView.get().getContext());
        return dbCreator.getDatabase().clientDao().getAllSync();
    }
}
