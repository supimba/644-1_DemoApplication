package ch.hevs.android.demoapplication.entity;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

import ch.hevs.android.demoapplication.model.Account;

public class AccountEntity implements Account {
    @NonNull
    private String uid;
    private String name;
    private Double balance;
    private String owner;

    public AccountEntity() {
    }

    public AccountEntity(Account account) {
        uid = account.getUid();
        name = account.getName();
        balance = account.getBalance();
        owner = account.getOwner();
    }

    @Exclude
    @Override
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    @Exclude
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
        return o.getUid().equals(this.getUid());
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("balance", balance);

        return result;
    }
}
