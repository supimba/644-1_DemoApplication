package ch.hevs.android.demoapplication.db.async.client;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import java.lang.ref.WeakReference;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.ClientEntity;

public class GetClient extends AsyncTask<String, Void, ClientEntity> {

    // Weak references will still allow the Activity to be garbage-collected
    private final WeakReference<View> mView;

    public GetClient(View view) {
        mView = new WeakReference<>(view);
    }

    @Override
    protected ClientEntity doInBackground(String... strings) {
        DatabaseCreator dbCreator = DatabaseCreator.getInstance(mView.get().getContext());
        return dbCreator.getDatabase().clientDao().getByIdSync(strings[0]);
    }
}
