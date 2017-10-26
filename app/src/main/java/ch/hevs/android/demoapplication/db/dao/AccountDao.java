package ch.hevs.android.demoapplication.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.arch.persistence.room.Update;

import java.util.List;

import ch.hevs.android.demoapplication.db.entity.AccountEntity;

/**
 * https://developer.android.com/topic/libraries/architecture/room.html#no-object-references
 */
@Dao
public abstract class AccountDao {

    @Query("SELECT * FROM accounts WHERE id = :id")
    public abstract LiveData<AccountEntity> getById(Long id);

    @Query("SELECT * FROM accounts WHERE id = :id")
    public abstract AccountEntity getByIdSync(Long id);

    @Insert
    public abstract long insert(AccountEntity account);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAll(List<AccountEntity> accounts);

    @Update
    public abstract void update(AccountEntity account);

    @Delete
    public abstract void delete(AccountEntity account);

    @Query("DELETE FROM accounts")
    public abstract void deleteAll();

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
    @Transaction
    public void transaction(AccountEntity sender, AccountEntity recipient) {
        update(sender);
        update(recipient);
    }

    @Query("SELECT * FROM accounts")
    public abstract LiveData<List<AccountEntity>> getAll();

    @Query("SELECT * FROM accounts WHERE owner=:owner")
    public abstract LiveData<List<AccountEntity>> getOwned(String owner);

    @Query("SELECT * FROM accounts WHERE owner=:owner")
    public abstract List<AccountEntity> getOwnedSync(String owner);
}
