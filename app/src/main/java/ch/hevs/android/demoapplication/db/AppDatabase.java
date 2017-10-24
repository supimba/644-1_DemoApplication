package ch.hevs.android.demoapplication.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import ch.hevs.android.demoapplication.db.dao.AccountDao;
import ch.hevs.android.demoapplication.db.dao.ClientDao;
import ch.hevs.android.demoapplication.db.entity.AccountEntity;
import ch.hevs.android.demoapplication.db.entity.ClientEntity;

@Database(entities = {AccountEntity.class, ClientEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    static final String DATABASE_NAME = "bank-database";

    public abstract AccountDao accountDao();

    public abstract ClientDao clientDao();
}
