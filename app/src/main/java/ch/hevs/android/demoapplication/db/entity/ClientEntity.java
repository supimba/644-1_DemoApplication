package ch.hevs.android.demoapplication.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

import ch.hevs.android.demoapplication.model.Client;

/**
 * https://developer.android.com/reference/android/arch/persistence/room/Entity.html
 */
@Entity(tableName = "clients", primaryKeys = {"email"})
public class ClientEntity implements Client {

    @NonNull
    private String email;

    @ColumnInfo(name = "first_name")
    private String firstName;

    @ColumnInfo(name = "last_name")
    private String lastName;

    private String password;

    @ColumnInfo(name = "admin")
    private Boolean admin;

    public ClientEntity() {
    }

    public ClientEntity(Client client) {
        email = client.getEmail();
        firstName = client.getFirstName();
        lastName = client.getLastName();
        password = client.getPassword();
        admin = client.isAdmin();
    }

    @Override
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Boolean isAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof ClientEntity)) return false;
        ClientEntity o = (ClientEntity) obj;
        return o.getEmail().equals(this.getEmail());
    }
}
