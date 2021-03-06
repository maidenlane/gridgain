/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef TRANSACTION_CONSTS_H
#define TRANSACTION_CONSTS_H

namespace ignite
{
    namespace thin
    {
        namespace transactions
        {
            #define TX_ALREADY_CLOSED "The transaction is already closed."
            #define TX_ALREADY_STARTED "A transaction has already been started by the current thread."
            #define TX_DIFFERENT_THREAD "You can commit transaction only from the thread it was started."

            /**
             * Transaction concurrency control model.
             */
            struct TransactionConcurrency
            {
                enum Type
                {
                    /**
                     * Optimistic concurrency model. In this mode all cache operations
                     * are not distributed to other nodes until Transaction::Commit()
                     * is called. In this mode one @c 'PREPARE' message will be sent to
                     * participating cache nodes to start acquiring per-transaction
                     * locks, and once all nodes reply @c 'OK', a one-way @c 'COMMIT'
                     * message is sent without waiting for reply.
                     *
                     * Note that in this mode, optimistic failures are only possible in
                     * conjunction with ::IGNITE_TX_ISOLATION_SERIALIZABLE isolation
                     * level. In all other cases, optimistic transactions will never
                     * fail optimistically and will always be identically ordered on all
                     * participating grid nodes.
                     */
                    OPTIMISTIC = 0,

                    /**
                     * Pessimistic concurrency model. In this mode a lock is acquired
                     * on all cache operations with exception of read operations in
                     * ::IGNITE_TX_ISOLATION_READ_COMMITTED mode. All optional filters
                     * passed into cache operations will be evaluated after successful
                     * lock acquisition. Whenever Transaction::Commit() is called, a
                     * single one-way @c 'COMMIT' message is sent to participating cache
                     * nodes without waiting for reply. Note that there is no reason for
                     * distributed @c 'PREPARE' step, as all locks have been already
                     * acquired.
                     */
                    PESSIMISTIC = 1
                };
            };

            /**
             * Defines different cache transaction isolation levels.
             */
            struct TransactionIsolation
            {
                enum Type
                {
                    /**
                     * Read committed isolation level. This isolation level means that
                     * always a committed value will be provided for read operations.
                     * With this isolation level values are always read from cache
                     * global memory or persistent store every time a value is accessed.
                     * In other words, if the same key is accessed more than once within
                     * the same transaction, it may have different value every time
                     * since global cache memory may be updated concurrently by other
                     * threads.
                     */
                    READ_COMMITTED = 0,

                    /**
                     * Repeatable read isolation level. This isolation level means that
                     * if a value was read once within transaction, then all consecutive
                     * reads will provide the same in-transaction value. With this
                     * isolation level accessed values are stored within in-transaction
                     * memory, so consecutive access to the same key within the same
                     * transaction will always return the value that was previously read
                     * or updated within this transaction. If concurrency is
                     * ::IGNITE_TX_CONCURRENCY_PESSIMISTIC, then a lock on the key will
                     * be acquired prior to accessing the value.
                     */
                    REPEATABLE_READ = 1,

                    /**
                     * Serializable isolation level. This isolation level means that all
                     * transactions occur in a completely isolated fashion, as if all
                     * transactions in the system had executed serially, one after the
                     * other. Read access with this level happens the same way as with
                     * ::IGNITE_TX_ISOLATION_REPEATABLE_READ level. However, in
                     * ::IGNITE_TX_CONCURRENCY_OPTIMISTIC mode, if some transactions
                     * cannot be serially isolated from each other, then one winner will
                     * be picked and the other transactions in conflict will result in
                     * IgniteError being thrown.
                     */
                    SERIALIZABLE = 2
                };
            };
        }
    }
}

#endif // TRANSACTION_CONSTS_H
