package ch.hevs.android.demoapplication.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * WARNING: This generated code is intended as a sample or starting point for using a
 * Google Cloud Endpoints RESTful API with an Objectify entity. It provides no data access
 * restrictions and no data validation.
 * <p>
 * DO NOT deploy this code unchanged as part of a real application to real users.
 */
@Api(
        name = "bankApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.demoapplication.android.hevs.ch",
                ownerName = "backend.demoapplication.android.hevs.ch",
                packagePath = ""
        )
)
public class AccountEndpoint {

    private static final Logger logger = Logger.getLogger(AccountEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(Account.class);
    }

    /**
     * Returns the {@link Account} with the corresponding ID.
     *
     * @param id the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Account} with the provided ID.
     */
    @ApiMethod(
            name = "getAccount",
            path = "account/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Account getAccount(@Named("id") Long id) throws NotFoundException {
        logger.info("Getting Account with ID: " + id);
        Account account = ofy().load().type(Account.class).id(id).now();
        if (account == null) {
            throw new NotFoundException("Could not find Account with ID: " + id);
        }
        return account;
    }

    /**
     * Inserts a new {@code Account}.
     */
    @ApiMethod(
            name = "insertAccount",
            path = "account",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Account insertAccount(Account account) {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that account.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        ofy().save().entity(account).now();
        logger.info("Created Account.");

        return ofy().load().entity(account).now();
    }

    /**
     * Updates an existing {@code Account}.
     *
     * @param id      the ID of the entity to be updated
     * @param account the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Account}
     */
    @ApiMethod(
            name = "updateAccount",
            path = "account/{id}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Account updateAccount(@Named("id") Long id, Account account) throws NotFoundException {
        if (!account.id.equals(id)) {
            throw new NotFoundException("Cannot update Account due to ID conflict: Entity ID: " + id + " - Resource ID: " + account.id);
        }
        checkExistsAccount(id);
        ofy().save().entity(account).now();
        logger.info("Updated Account: " + account);
        return ofy().load().entity(account).now();
    }

    /**
     * Transfers money from one {@code Account} to another {@code Account}.
     *
     * @param idSend      the ID of the entity to be updated
     * @param idRec      the ID of the entity to be updated
     * @param amount the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code sendId} or {@code recId} do not correspond to an existing
     *                           {@code Account}
     */
    @ApiMethod(
            name = "transaction",
            path = "transaction/from={sendId}&to={recId}&amount={amount}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public void transaction(@Named("sendId") Long idSend, @Named("recId") Long idRec, @Named("amount") Double amount) throws NotFoundException, BadRequestException {
        checkExistsAccount(idSend);
        checkExistsAccount(idRec);

        Account accFrom = ofy().load().type(Account.class).id(idSend).now();
        if (accFrom.balance < amount)
            throw new BadRequestException("Cannot execute transaction because sender does not have enought money.");
        Account accTo = ofy().load().type(Account.class).id(idRec).now();

        accFrom.balance -= amount;
        accTo.balance += amount;

        ofy().save().entity(accFrom).now();
        ofy().save().entity(accTo).now();
    }

    /**
     * Deletes the specified {@code Account}.
     *
     * @param id the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Account}
     */
    @ApiMethod(
            name = "removeAccount",
            path = "account/{id}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void removeAccount(@Named("id") Long id) throws NotFoundException {
        checkExistsAccount(id);
        ofy().delete().type(Account.class).id(id).now();
        logger.info("Deleted Account with ID: " + id);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "listAccounts",
            path = "account",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Account> listAccounts(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Account> query = ofy().load().type(Account.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Account> queryIterator = query.iterator();
        List<Account> accountList = new ArrayList<Account>(limit);
        while (queryIterator.hasNext()) {
            accountList.add(queryIterator.next());
        }
        return CollectionResponse.<Account>builder().setItems(accountList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    /**
     * List all entities.
     *
     * @param ownerEmail the email of owner of the accounts
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "listOwnedAccounts",
            path = "account/list/{ownerEmail}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Account> listOwnedAccounts(@Named("ownerEmail") String ownerEmail, @Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Client owner = ofy().load().type(Client.class).id(ownerEmail).now();
        Query<Account> query = ofy().load().type(Account.class).filter("owner", owner).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Account> queryIterator = query.iterator();
        List<Account> accountList = new ArrayList<Account>(limit);
        while (queryIterator.hasNext()) {
            accountList.add(queryIterator.next());
        }
        return CollectionResponse.<Account>builder().setItems(accountList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExistsAccount(Long id) throws NotFoundException {
        try {
            ofy().load().type(Account.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Account with ID: " + id);
        }
    }
}