package ch.hevs.android.demoapplication.db.dao;

import android.arch.lifecycle.LiveData;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import ch.hevs.android.demoapplication.db.entity.AccountEntity;

public class AccountDao {

    private DatabaseReference mDatabase;

    public AccountDao() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public LiveData<AccountEntity> getById(String id) {
        return null;
    }

    public AccountEntity getByIdSync(String id) {
        return null;
    }

    public LiveData<List<AccountEntity>> getAll() {
        return null;
    }

    public void getOwned(AccountEntity account) {

    }

    public List<AccountEntity> getOwnedSync(AccountEntity account) {
        return null;
    }

    public LiveData<List<AccountEntity>> insert(AccountEntity account) {
        return null;
    }

    public void update(AccountEntity account) {
    }

    public void delete(AccountEntity account) {
    }

    public void transaction(AccountEntity sender, AccountEntity recipient) {

    }

    /**
     * There's currently no way to add additional constraints (beside ForeignKey) to columns.
     *
     * This means we currently cannot check on a DB level if the balance of a client will be
     * updated with a negative value.
     * So we need to ensure that the sender has enough money on his client BEFORE we call this
     * method because we want to ensure that people cannot get into debt.
     *
     * @param sender AccountEntity that sends the money
     * @param recipient AccountEntity that receives the money
     */
    /*
    @Transaction
    public void transaction(AccountEntity sender, AccountEntity recipient) {
        update(sender);
        update(recipient);
    }
    */
}
