package ch.hevs.android.demoapplication.db;

import java.util.ArrayList;
import java.util.List;

import ch.hevs.android.demoapplication.db.entity.AccountEntity;
import ch.hevs.android.demoapplication.db.entity.ClientEntity;

/**
 * Generates dummy data and inserts them into the database
 */
class DatabaseInitUtil {

    static void initializeDb(AppDatabase db) {
        List<ClientEntity> clients = new ArrayList<>();
        List<AccountEntity> accounts = new ArrayList<>();

        generateData(clients, accounts);
        insertData(db, clients, accounts);
    }

    private static void generateData(List<ClientEntity> clients, List<AccountEntity> accounts) {
        ClientEntity client1 = new ClientEntity();
        ClientEntity client2 = new ClientEntity();
        ClientEntity client3 = new ClientEntity();
        ClientEntity client4 = new ClientEntity();

        client1.setFirstName("Michel");
        client1.setLastName("Platini");
        client1.setId("m.p@fifa.com");
        client1.setPassword("michel1");
        client1.setAdmin(false);
        clients.add(client1);

        client2.setFirstName("Sepp");
        client2.setLastName("Blatter");
        client2.setId("s.b@fifa.com");
        client2.setPassword("sepp1");
        client2.setAdmin(true);
        clients.add(client2);

        client3.setFirstName("Ebbe");
        client3.setLastName("Schwartz");
        client3.setId("e.s@fifa.com");
        client3.setPassword("ebbe1");
        client3.setAdmin(false);
        clients.add(client3);

        client4.setFirstName("Aleksander");
        client4.setLastName("Ceferin");
        client4.setId("a.c@fifa.com");
        client4.setPassword("aleksander1");
        client4.setAdmin(false);
        clients.add(client4);

        AccountEntity account1 = new AccountEntity();
        AccountEntity account2 = new AccountEntity();
        AccountEntity account3 = new AccountEntity();
        AccountEntity account4 = new AccountEntity();
        AccountEntity account5 = new AccountEntity();
        AccountEntity account6 = new AccountEntity();
        AccountEntity account7 = new AccountEntity();
        AccountEntity account8 = new AccountEntity();

        account1.setBalance(20000d);
        account1.setName("Savings");
        account1.setOwner(clients.get(0).getId());
        accounts.add(account1 );

        account2.setBalance(1840000d);
        account2.setName("Secret");
        account2.setOwner(clients.get(0).getId());
        accounts.add(account2);

        account3.setBalance(21000d);
        account3.setName("Savings");
        account3.setOwner(clients.get(1).getId());
        accounts.add(account3);

        account4.setBalance(1820000d);
        account4.setName("Secret");
        account4.setOwner(clients.get(1).getId());
        accounts.add(account4);

        account5.setBalance(18500d);
        account5.setName("Savings");
        account5.setOwner(clients.get(2).getId());
        accounts.add(account5);

        account6.setBalance(1810000d);
        account6.setName("Secret");
        account6.setOwner(clients.get(2).getId());
        accounts.add(account6);

        account7.setBalance(19000d);
        account7.setName("Savings");
        account7.setOwner(clients.get(3).getId());
        accounts.add(account7);

        account8.setBalance(1902360d);
        account8.setName("Secret");
        account8.setOwner(clients.get(3).getId());
        accounts.add(account8);
    }

    private static void insertData(AppDatabase db, List<ClientEntity> clients, List<AccountEntity> accounts) {
        db.beginTransaction();
        try {
            db.clientDao().insertAll(clients);
            db.accountDao().insertAll(accounts);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
