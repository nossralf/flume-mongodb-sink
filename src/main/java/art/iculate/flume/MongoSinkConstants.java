/*
 * Copyright (C) Fredrik Larsson <nossralf@gmail.com>
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the BSD license.  See the LICENSE file for details.
 */
package art.iculate.flume;

final class MongoSinkConstants {

  private MongoSinkConstants() {}

  /**
   * Comma-separated list of hostname:port. If the port is not present the default port 27017 will
   * be used.
   */
  static final String HOSTNAMES = "hostNames";

  /** Database name. */
  static final String DATABASE = "database";

  /** Collection name. */
  static final String COLLECTION = "collection";

  /** User name. */
  static final String USER = "user";

  /** Password. */
  static final String PASSWORD = "password";

  /**
   * Maximum number of events the sink should take from the channel per transaction, if available.
   * Defaults to 100.
   */
  static final String BATCH_SIZE = "batchSize";

  /** The default batch size. */
  static final int DEFAULT_BATCH_SIZE = 100;
}
