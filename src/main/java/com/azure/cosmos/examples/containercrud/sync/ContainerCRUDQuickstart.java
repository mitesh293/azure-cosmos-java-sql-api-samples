// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.examples.containercrud.sync;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosPagedIterable;
import com.azure.cosmos.examples.changefeed.SampleChangeFeedProcessor;
import com.azure.cosmos.examples.common.AccountSettings;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseRequestOptions;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.FeedOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerCRUDQuickstart {

    private CosmosClient client;

    private final String databaseName = "AzureSampleFamilyDB";
    private final String containerName = "FamilyContainer";

    private CosmosDatabase database;
    private CosmosContainer container;

    protected static Logger logger = LoggerFactory.getLogger(SampleChangeFeedProcessor.class.getSimpleName());

    public void close() {
        client.close();
    }

    /**
     * Sample to demonstrate the following container CRUD operations:
     * -Create
     * -Update throughput
     * -Read by ID
     * -Read all
     * -Delete
     */
    public static void main(String[] args) {
        ContainerCRUDQuickstart p = new ContainerCRUDQuickstart();

        try {
            logger.info("Starting SYNC main");
            p.containerCRUDDemo();
            logger.info("Demo complete, please hold while resources are released");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(String.format("Cosmos getStarted failed with %s", e));
        } finally {
            logger.info("Closing the client");
            p.shutdown();
        }
    }

    private void containerCRUDDemo() throws Exception {

        logger.info("Using Azure Cosmos DB endpoint: " + AccountSettings.HOST);

        //  Create sync client
        client = new CosmosClientBuilder()
                .setEndpoint(AccountSettings.HOST)
                .setKey(AccountSettings.MASTER_KEY)
                .setConsistencyLevel(ConsistencyLevel.EVENTUAL)
                .buildClient();


        createDatabaseIfNotExists();
        createContainerIfNotExists();

        readContainerById();
        readAllContainers();
        // deleteAContainer() is called at shutdown()

    }

    // Database Create
    private void createDatabaseIfNotExists() throws Exception {
        logger.info("Create database " + databaseName + " if not exists...");

        //  Create database if not exists
        database = client.createDatabaseIfNotExists(databaseName).getDatabase();

        logger.info("Done.");
    }

    // Container create
    private void createContainerIfNotExists() throws Exception {
        logger.info("Create container " + containerName + " if not exists.");

        //  Create container if not exists
        CosmosContainerProperties containerProperties =
                new CosmosContainerProperties(containerName, "/lastName");

        //  Create container with 200 RU/s
        container = database.createContainerIfNotExists(containerProperties, 200).getContainer();

        logger.info("Done.");
    }

    // Update container throughput
    private void updateContainerThroughput() throws Exception {
        logger.info("Update throughput for container " + containerName + ".");

        // Specify new throughput value
        container.replaceProvisionedThroughput(400);

        logger.info("Done.");
    }

    // Container read
    private void readContainerById() throws Exception {
        logger.info("Read container " + containerName + " by ID.");

        //  Read container by ID
        container = database.getContainer(containerName);

        logger.info("Done.");
    }

    // Container read all
    private void readAllContainers() throws Exception {
        logger.info("Read all containers in database " + databaseName + ".");

        //  Read all containers in the account
        CosmosPagedIterable<CosmosContainerProperties> containers = database.readAllContainers(new FeedOptions());

        // Print
        String msg="Listing containers in database:\n";
        for(CosmosContainerProperties containerProps : containers) {
            msg += String.format("-Container ID: %s\n",containerProps.getId());
        }
        logger.info(msg + "\n");

        logger.info("Done.");
    }

    // Container delete
    private void deleteAContainer() throws Exception {
        logger.info("Delete container " + containerName + " by ID.");

        // Delete container
        CosmosContainerResponse containerResp = database.getContainer(containerName).delete(new CosmosContainerRequestOptions());
        logger.info("Status code for container delete: {}",containerResp.getStatusCode());

        logger.info("Done.");
    }

    // Database delete
    private void deleteADatabase() throws Exception {
        logger.info("Last step: delete database " + databaseName + " by ID.");

        // Delete database
        CosmosDatabaseResponse dbResp = client.getDatabase(databaseName).delete(new CosmosDatabaseRequestOptions());
        logger.info("Status code for database delete: {}",dbResp.getStatusCode());

        logger.info("Done.");
    }

    // Cleanup before close
    private void shutdown() {
        try {
            //Clean shutdown
            deleteAContainer();
            deleteADatabase();
        } catch (Exception err) {
            logger.error("Deleting Cosmos DB resources failed, will still attempt to close the client. See stack trace below.");
            err.printStackTrace();
        }
        client.close();
        logger.info("Done with sample.");
    }

}
