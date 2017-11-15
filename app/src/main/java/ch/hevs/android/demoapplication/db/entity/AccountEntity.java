package ch.hevs.android.demoapplication.db.entity;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

import ch.hevs.android.demoapplication.model.Account;

@IgnoreExtraProperties
public class AccountEntity implements Account {
    @NonNull
    private String id;
    private String name;
    private Double balance;
    @Exclude
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
    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("balance", balance);

        return result;
    }
}
