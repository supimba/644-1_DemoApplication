package ch.hevs.android.demoapplication.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import ch.hevs.android.demoapplication.model.Account;

/**
 * https://developer.android.com/reference/android/arch/persistence/room/Entity.html
 */
@Entity(tableName = "accounts",
        foreignKeys =
        @ForeignKey(
                entity = ClientEntity.class,
                parentColumns = "email",
                childColumns = "owner",
                onDelete = ForeignKey.CASCADE
        )
)
public class AccountEntity implements Account {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    private String name;
    private Double balance;
    private String owner;

    public AccountEntity() {
    }

    public AccountEntity(Account account) {
        id = account.getId();
        name = account.getName();
        balance = account.getBalance();
        owner = account.getOwner();
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof AccountEntity)) return false;
        AccountEntity o = (AccountEntity) obj;
        return o.getId().equals(this.getId());
    }
}
