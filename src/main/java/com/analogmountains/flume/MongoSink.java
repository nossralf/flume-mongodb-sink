/*
 * Copyright (C) Fredrik Larsson <nossralf@gmail.com>
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the BSD license.  See the LICENSE file for details.
 */
package com.analogmountains.flume;

import static com.analogmountains.flume.MongoSinkConstants.BATCH_SIZE;
import static com.analogmountains.flume.MongoSinkConstants.COLLECTION;
import static com.analogmountains.flume.MongoSinkConstants.DATABASE;
import static com.analogmountains.flume.MongoSinkConstants.DEFAULT_BATCH_SIZE;
import static com.analogmountains.flume.MongoSinkConstants.HOSTNAMES;
import static com.analogmountains.flume.MongoSinkConstants.PASSWORD;
import static com.analogmountains.flume.MongoSinkConstants.USER;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.instrumentation.SinkCounter;
import org.apache.flume.sink.AbstractSink;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

public class MongoSink extends AbstractSink implements Configurable {

  private static final Logger logger = LoggerFactory.getLogger(MongoSink.class);

  private MongoClient client;
  private MongoCollection<Document> collection;
  private List<ServerAddress> seeds;
  private MongoCredential credential;

  private String databaseName;
  private String collectionName;

  private int batchSize = DEFAULT_BATCH_SIZE;

  private SinkCounter sinkCounter;

  @Override
  public Status process() throws EventDeliveryException {
    Status status = Status.READY;

    List<Document> documents = new ArrayList<Document>(batchSize);

    Channel channel = getChannel();
    Transaction transaction = channel.getTransaction();
    try {
      transaction.begin();

      long count;
      for (count = 0; count < batchSize; ++count) {
        Event event = channel.take();

        if (event == null) {
          break;
        }

        String jsonEvent = new String(event.getBody(), StandardCharsets.UTF_8);
        BasicDBObject dbObject = (BasicDBObject) JSON.parse(jsonEvent);
        documents.add(new Document(dbObject));
      }

      if (count <= 0) {
        sinkCounter.incrementBatchEmptyCount();
        status = Status.BACKOFF;
      } else {
        if (count < batchSize) {
          sinkCounter.incrementBatchUnderflowCount();
          status = Status.BACKOFF;
        } else {
          sinkCounter.incrementBatchCompleteCount();
        }

        sinkCounter.addToEventDrainAttemptCount(count);
        collection.insertMany(documents);
      }

      transaction.commit();
      sinkCounter.addToEventDrainSuccessCount(count);

    } catch (Throwable t) {
      try {
        transaction.rollback();
      } catch (Exception e) {
        logger.error("Exception during transaction rollback.", e);
      }

      logger.error("Failed to commit transaction. Transaction rolled back.", t);
      if (t instanceof Error || t instanceof RuntimeException) {
        Throwables.propagate(t);
      } else {
        throw new EventDeliveryException(
            "Failed to commit transaction. Transaction rolled back.", t);
      }
    } finally {
      if (transaction != null) {
        transaction.close();
      }
    }

    return status;
  }

  @Override
  public synchronized void start() {
    logger.info("Starting MongoDB sink");
    sinkCounter.start();
    try {
      client = new MongoClient(seeds, Arrays.asList(credential));
      MongoDatabase database = client.getDatabase(databaseName);
      collection = database.getCollection(collectionName);
      sinkCounter.incrementConnectionCreatedCount();
    } catch (Exception e) {
      logger.error("Exception while connecting to MongoDB", e);
      sinkCounter.incrementConnectionFailedCount();
      if (client != null) {
        client.close();
        sinkCounter.incrementConnectionClosedCount();
      }
    }
    super.start();
    logger.info("MongoDB sink started");
  }

  @Override
  public synchronized void stop() {
    logger.info("Stopping MongoDB sink");
    if (client != null) {
      client.close();
    }
    sinkCounter.incrementConnectionClosedCount();
    sinkCounter.stop();
    super.stop();
    logger.info("MongoDB sink stopped");
  }

  @Override
  public void configure(Context context) {
    seeds = getSeeds(context.getString(HOSTNAMES));
    credential = getCredential(context);
    databaseName = context.getString(DATABASE);
    collectionName = context.getString(COLLECTION);
    batchSize = context.getInteger(BATCH_SIZE, DEFAULT_BATCH_SIZE);

    if (sinkCounter == null) {
      sinkCounter = new SinkCounter(getName());
    }
  }

  private List<ServerAddress> getSeeds(String seedsString) {
    List<ServerAddress> seeds = new LinkedList<ServerAddress>();
    String[] seedStrings = StringUtils.deleteWhitespace(seedsString).split(",");
    for (String seed : seedStrings) {
      String[] hostAndPort = seed.split(":");
      String host = hostAndPort[0];
      int port;
      if (hostAndPort.length == 2) {
        port = Integer.parseInt(hostAndPort[1]);
      } else {
        port = 27017;
      }
      seeds.add(new ServerAddress(host, port));
    }

    return seeds;
  }

  private MongoCredential getCredential(Context context) {
    String user = context.getString(USER);
    String database = context.getString(DATABASE);
    String password = context.getString(PASSWORD);
    return MongoCredential.createCredential(user, database,
        password.toCharArray());
  }
}
