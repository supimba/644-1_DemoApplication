package ch.hevs.android.demoapplication.db.dao;

import android.arch.lifecycle.LiveData;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import ch.hevs.android.demoapplication.db.entity.ClientEntity;

public class ClientDao {

    private DatabaseReference mDatabase;

    public ClientDao() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public LiveData<ClientEntity> getById(String id) {
        return null;
    }

    public ClientEntity getByIdSync(String id) {
        return null;
    }

    public LiveData<List<ClientEntity>> getAll() {
        return null;
    }

    public List<ClientEntity> getAllSync() {
        return null;
    }

    public String insert(ClientEntity client) {
        return null;
    }

    public void update(ClientEntity client) {

    }

    public void delete(ClientEntity client) {

    }
}
