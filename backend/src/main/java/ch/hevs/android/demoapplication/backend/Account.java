package ch.hevs.android.demoapplication.backend;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

/**
 * Created by stefan on 25.08.17.
 */
@Entity
public class Account {

    @Id Long id;
    String name;
    Double balance;

    @Load
    @Index
    Ref<Client> owner;

    public Account() { }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Client getOwner() {
        return owner.get();
    }

    public void setOwner(Client owner) {
        this.owner = Ref.create(owner);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Account)) return false;
        Account o = (Account) obj;
        return o.getId() == this.getId();
    }
}
