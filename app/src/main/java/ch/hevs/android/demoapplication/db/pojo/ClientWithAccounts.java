package ch.hevs.android.demoapplication.db.pojo;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

import ch.hevs.android.demoapplication.db.entity.AccountEntity;
import ch.hevs.android.demoapplication.db.entity.ClientEntity;

/**
 * https://developer.android.com/topic/libraries/architecture/room.html#no-object-references
 */
public class ClientWithAccounts {
    @Embedded
    public ClientEntity client;

    @Relation(parentColumn = "email", entityColumn = "owner", entity = AccountEntity.class)
    public List<AccountEntity> accounts;
}