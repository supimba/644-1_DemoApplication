package ch.hevs.android.demoapplication.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.Key;
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
public class ClientEndpoint {

    private static final Logger logger = Logger.getLogger(ClientEndpoint.class.getName());

    private static final int DEFAULT_LIST_LIMIT = 20;

    static {
        // Typically you would register this inside an OfyServive wrapper. See: https://code.google.com/p/objectify-appengine/wiki/BestPractices
        ObjectifyService.register(Client.class);
    }

    /**
     * Returns the {@link Client} with the corresponding ID.
     *
     * @param email the ID of the entity to be retrieved
     * @return the entity with the corresponding ID
     * @throws NotFoundException if there is no {@code Client} with the provided ID.
     */
    @ApiMethod(
            name = "getClient",
            path = "client/{id}",
            httpMethod = ApiMethod.HttpMethod.GET)
    public Client getClient(@Named("id") String email) throws NotFoundException {
        logger.info("Getting Client with ID: " + email);
        Client client = ofy().load().type(Client.class).id(email).now();
        if (client == null) {
            throw new NotFoundException("Could not find Client with ID: " + email);
        }
        return client;
    }

    /**
     * Inserts a new {@code Client}.
     */
    @ApiMethod(
            name = "insertClient",
            path = "client",
            httpMethod = ApiMethod.HttpMethod.POST)
    public Client insertClient(Client client) throws ConflictException, NotFoundException {
        // Typically in a RESTful API a POST does not have a known ID (assuming the ID is used in the resource path).
        // You should validate that client.id has not been set. If the ID type is not supported by the
        // Objectify ID generator, e.g. long or String, then you should generate the unique ID yourself prior to saving.
        if (ofy().load().type(Client.class).id(client.email).now() != null) {
            throw new ConflictException("Email is already in use");
        }
        ofy().save().entity(client).now();
        logger.info("Created Client.");

        return ofy().load().entity(client).now();
    }

    /**
     * Updates an existing {@code Client}.
     *
     * @param email  the ID of the entity to be updated
     * @param client the desired state of the entity
     * @return the updated version of the entity
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Client}
     */
    @ApiMethod(
            name = "updateClient",
            path = "client/{email}",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Client updateClient(@Named("email") String email, Client client) throws NotFoundException {
        if (!client.email.equals(email)) {
            throw new NotFoundException("Cannot update Client due to ID conflict: Entity ID: " + email + " - Resource ID: " + client.email);
        }
        checkExistsClient(email);
        ofy().save().entity(client).now();
        logger.info("Updated Client: " + client);
        return ofy().load().entity(client).now();
    }

    /**
     * Deletes the specified {@code Client}.
     *
     * @param email the ID of the entity to delete
     * @throws NotFoundException if the {@code id} does not correspond to an existing
     *                           {@code Client}
     */
    @ApiMethod(
            name = "removeClient",
            path = "client/{email}",
            httpMethod = ApiMethod.HttpMethod.DELETE)
    public void removeClient(@Named("email") String email) throws NotFoundException {
        checkExistsClient(email);
        Client client = ofy().load().type(Client.class).id(email).now();
        List<Long> accountIds = new ArrayList<>();
        for (Account acc : ofy().load().type(Account.class).filter("owner", client).list()) {
            accountIds.add(acc.getId());
        }
        ofy().delete().type(Account.class).ids(accountIds);
        ofy().delete().type(Client.class).id(email).now();
        logger.info("Deleted Client with ID: " + email);
    }

    /**
     * List all entities.
     *
     * @param cursor used for pagination to determine which page to return
     * @param limit  the maximum number of entries to return
     * @return a response that encapsulates the result list and the next page token/cursor
     */
    @ApiMethod(
            name = "listClients",
            path = "client",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Client> listClients(@Nullable @Named("cursor") String cursor, @Nullable @Named("limit") Integer limit) {
        limit = limit == null ? DEFAULT_LIST_LIMIT : limit;
        Query<Client> query = ofy().load().type(Client.class).limit(limit);
        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor));
        }
        QueryResultIterator<Client> queryIterator = query.iterator();
        List<Client> clientList = new ArrayList<Client>(limit);
        while (queryIterator.hasNext()) {
            clientList.add(queryIterator.next());
        }
        return CollectionResponse.<Client>builder().setItems(clientList).setNextPageToken(queryIterator.getCursor().toWebSafeString()).build();
    }

    private void checkExistsClient(String id) throws NotFoundException {
        try {
            ofy().load().type(Client.class).id(id).safe();
        } catch (com.googlecode.objectify.NotFoundException e) {
            throw new NotFoundException("Could not find Client with ID: " + id);
        }
    }
}