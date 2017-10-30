package ch.hevs.android.demoapplication.db.async.client;

import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.view.View;

import java.lang.ref.WeakReference;

import ch.hevs.android.demoapplication.db.DatabaseCreator;
import ch.hevs.android.demoapplication.db.entity.ClientEntity;

public class CreateClient extends AsyncTask<ClientEntity, Void, Boolean> {

    // Weak references will still allow the Activity to be garbage-collected
    private final WeakReference<View> mView;

    public CreateClient(View view) {
        mView = new WeakReference<>(view);
    }

    @Override
    protected Boolean doInBackground(ClientEntity... params) {
        DatabaseCreator dbCreator = DatabaseCreator.getInstance(mView.get().getContext());
        boolean response = true;
        try {
            for (ClientEntity client : params)
                dbCreator.getDatabase()
                        .clientDao()
                        .insert(client);
        } catch (SQLiteConstraintException e) {
            response = false;
        }
        return response;
    }
}
