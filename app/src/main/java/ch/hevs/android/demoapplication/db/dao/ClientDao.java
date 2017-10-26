package ch.hevs.android.demoapplication.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.database.sqlite.SQLiteConstraintException;

import java.util.List;

import ch.hevs.android.demoapplication.db.entity.ClientEntity;
import ch.hevs.android.demoapplication.db.pojo.ClientWithAccounts;

/**
 * https://developer.android.com/topic/libraries/architecture/room.html#no-object-references
 */
@Dao
public interface ClientDao {

    @Query("SELECT * FROM clients WHERE email = :id")
    LiveData<ClientEntity> getById(String id);

    @Query("SELECT * FROM clients WHERE email = :id")
    ClientEntity getByIdSync(String id);

    @Insert
    long insert(ClientEntity client) throws SQLiteConstraintException;

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ClientEntity> clients);

    @Update
    void update(ClientEntity client);

    @Delete
    void delete(ClientEntity client);

    @Query("DELETE FROM clients")
    void deleteAll();

    @Query("SELECT * FROM clients")
    LiveData<List<ClientEntity>> getAll();

    @Query("SELECT * FROM clients")
    List<ClientEntity> getAllSync();

    @Query("SELECT * FROM clients WHERE email = :id")
    LiveData<ClientWithAccounts> loadClientWithAccounts(String id);

    @Query("SELECT * FROM clients WHERE email = :id")
    List<ClientWithAccounts> loadClientWithAccountsSync(String id);
}
