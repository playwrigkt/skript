package dev.yn.playground.sql.task

import dev.yn.playground.Skript
import dev.yn.playground.context.SQLTaskContext
import dev.yn.playground.sql.transaction.SQLTransactionSkript
import io.kotlintest.matchers.shouldBe
import io.kotlintest.mock.mock
import io.kotlintest.specs.StringSpec

class SQLTransactionSkriptSpec : StringSpec() {
    init {
        "A transactional skript should wrap a SQLSkript in a skript" {
            val action = Skript.map<Int, String, SQLTaskContext<*>>({ it.toString() })
            val task = SQLTransactionSkript.transaction(action)
            task shouldBe SQLTransactionSkript.TransactionalSQLTransactionSkript(action)
        }

        "An autocommit skript should wrap a SQLSkript in a skript" {
            val action = Skript.map<Int, String, SQLTaskContext<*>>({ it.toString() })
            val task = SQLTransactionSkript.autoCommit(action)
            task shouldBe SQLTransactionSkript.AutoCommitSQlTransactionSkript(action)
        }

        "A transactional SQLTransactionSkript should map a sqlAction within the transaction" {
            val action1 = Skript.map<Int, String, SQLTaskContext<*>>({ it.toString() })
            val action2 = Skript.map<String, Long, SQLTaskContext<*>>({ it.toLong() })
            val task = SQLTransactionSkript
                    .transaction(action1)
                    .mapInsideTransaction(action2)
            task shouldBe SQLTransactionSkript.TransactionalSQLTransactionSkript(
                    Skript.SkriptLink(action1, action2))
        }

        "An autocommit SQLTransactionSkript should map a sqlAction within the transaction" {
            val action1 = Skript.map<Int, String, SQLTaskContext<*>>({ it.toString() })
            val action2 = Skript.map<String, Long, SQLTaskContext<*>>({ it.toLong() })
            val task = SQLTransactionSkript
                    .autoCommit(action1)
                    .mapInsideTransaction(action2)
            task shouldBe SQLTransactionSkript.autoCommit(
                    Skript.SkriptLink(
                            action1,
                            action2))
        }

        "A transactional SQLTransactionSkript should map a skript within the transaction" {
            val action1 = Skript.map<Int, String, SQLTaskContext<*>>({ it.toString() })
            val mappedTask = mock<Skript<String, Long, SQLTaskContext<*>>>()
            val task = SQLTransactionSkript
                    .transaction(action1)
                    .mapInsideTransaction(mappedTask)
            task shouldBe SQLTransactionSkript.transaction(
                    Skript.SkriptLink(
                            action1,
                            mappedTask))
        }

        "An autocommit SQLTransactionSkript should map a skript within the transaction" {
            val action1 = Skript.map<Int, String, SQLTaskContext<*>>({ it.toString() })
            val mappedTask = mock<Skript<String, Long, SQLTaskContext<*>>>()
            val task = SQLTransactionSkript
                    .autoCommit(action1)
                    .mapInsideTransaction(mappedTask)
            task shouldBe SQLTransactionSkript.autoCommit(
                    Skript.SkriptLink(
                            action1,
                            mappedTask))
        }

        "A transactional SQLTransactionSkript should unwrap a SQLTransactionSkript mapped within a transaction" {
            val action1 = Skript.map<Int, String, SQLTaskContext<*>>({ it.toString() })
            val action2 = Skript.map<String, Long, SQLTaskContext<*>>({ it.toLong() })
            val task = SQLTransactionSkript
                    .transaction(action1)
                    .mapInsideTransaction(SQLTransactionSkript.transaction(action2))
            task shouldBe SQLTransactionSkript.transaction(
                    Skript.SkriptLink(
                            action1,
                            action2))
        }

        "An autocommit SQLTransactionSkript should unwrap a SQLTransactionSkript mapped within a transaction" {
            val action1 = Skript.map<Int, String, SQLTaskContext<*>>({ it.toString() })
            val action2 = Skript.map<String, Long, SQLTaskContext<*>>({ it.toLong() })
            val task = SQLTransactionSkript
                    .autoCommit(action1)
                    .mapInsideTransaction(SQLTransactionSkript.transaction(action2))
            task shouldBe SQLTransactionSkript.autoCommit(
                    Skript.SkriptLink(
                            action1,
                            action2))
        }

        "A transactional SQlTask should create a transaction from a skript" {
            val transaction = Skript.map<Int, String, SQLTaskContext<*>>({ it.toString() })
            val task = SQLTransactionSkript.transaction(transaction)
            task shouldBe SQLTransactionSkript.transaction(transaction)
        }

        "An autocommit SQlTask should create a transaction from a skript" {
            val transaction = Skript.map<Int, String, SQLTaskContext<*>>({ it.toString() })
            val task = SQLTransactionSkript.autoCommit(transaction)
            task shouldBe SQLTransactionSkript.autoCommit(transaction)
        }

        "A transactional SQLTransactionSkript should create a transaction from a skript and unwrap a SQLTransactionSkript" {
            val transaction = Skript.map<Int, String, SQLTaskContext<*>>({ it.toString() })
            val task = SQLTransactionSkript.transaction(SQLTransactionSkript.transaction(transaction))
            val task2 = SQLTransactionSkript.transaction(SQLTransactionSkript.autoCommit(transaction))
            task shouldBe SQLTransactionSkript.transaction(transaction)
            task2 shouldBe SQLTransactionSkript.transaction(transaction)
        }

        "An autocommit SQLTransactionSkript should create a transaction from a skript and unwrap a SQLTransactionSkript" {
            val transaction = Skript.map<Int, String, SQLTaskContext<*>>({ it.toString() })
            val task = SQLTransactionSkript.autoCommit(SQLTransactionSkript.transaction(transaction))
            val task2 = SQLTransactionSkript.autoCommit(SQLTransactionSkript.autoCommit(transaction))
            task shouldBe SQLTransactionSkript.autoCommit(transaction)
            task2 shouldBe SQLTransactionSkript.autoCommit(transaction)
        }
    }
}
