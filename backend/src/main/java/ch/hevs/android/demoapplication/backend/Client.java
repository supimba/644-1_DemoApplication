package ch.hevs.android.demoapplication.backend;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.OnLoad;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stefan on 28.08.17.
 */
@Entity
public class Client {

    @Id String email;
    String firstName;
    String lastName;
    String password;
    Boolean isAdmin;

    @Load
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    List<Ref<Account>> accountsRefs;

    @Ignore
    List<Account> accounts;

    public Client() {
        accountsRefs = new ArrayList<>();
    }

    @OnLoad
    public void deRef() {
        if (accountsRefs != null) {
            accounts = new ArrayList<>();
            for (Ref<Account> passengerLoaded : accountsRefs) {
                if (passengerLoaded.isLoaded()) {
                    accounts.add(passengerLoaded.get());
                }
            }
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public List<Ref<Account>> getAccountsRefs() {
        return accountsRefs;
    }

    public void setAccountsRefs(List<Ref<Account>> accountsRefs) {
        this.accountsRefs = accountsRefs;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Client)) return false;
        Client o = (Client) obj;
        return o.getEmail().equals(this.getEmail());
    }
}
