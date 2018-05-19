package playwrigkt.skript.sql.skript

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import playwrigkt.skript.Skript
import playwrigkt.skript.sql.transaction.SqlTransactionSkript
import playwrigkt.skript.troupe.SqlTroupe

class SqlTransactionSkriptSpec : StringSpec() {
    init {
        "A transactional skript should wrap a SqlSkript in a skript" {
            val action = Skript.map<Int, String, SqlTroupe>({ it.toString() })
            val skript = SqlTransactionSkript.transaction(action)
            skript shouldBe SqlTransactionSkript.TransactionalSqlTransactionSkript(action)
        }

        "An autocommit skript should wrap a SqlSkript in a skript" {
            val action = Skript.map<Int, String, SqlTroupe>({ it.toString() })
            val skript = SqlTransactionSkript.autoCommit(action)
            skript shouldBe SqlTransactionSkript.AutoCommitSQlTransactionSkript(action)
        }

        "A transactional SqlTransactionSkript should map a sqlAction within the transaction" {
            val action1 = Skript.map<Int, String, SqlTroupe>({ it.toString() })
            val action2 = Skript.map<String, Long, SqlTroupe>({ it.toLong() })
            val skript = SqlTransactionSkript
                    .transaction(action1)
                    .mapInsideTransaction(action2)
            skript shouldBe SqlTransactionSkript.TransactionalSqlTransactionSkript(
                    Skript.SkriptLink(action1, action2))
        }

        "An autocommit SqlTransactionSkript should map a sqlAction within the transaction" {
            val action1 = Skript.map<Int, String, SqlTroupe>({ it.toString() })
            val action2 = Skript.map<String, Long, SqlTroupe>({ it.toLong() })
            val skript = SqlTransactionSkript
                    .autoCommit(action1)
                    .mapInsideTransaction(action2)
            skript shouldBe SqlTransactionSkript.autoCommit(
                    Skript.SkriptLink(
                            action1,
                            action2))
        }

        "A transactional SqlTransactionSkript should map a skript within the transaction" {
            val action1 = Skript.map<Int, String, SqlTroupe>({ it.toString() })
            val mappedSkript = Skript.map<String, Long, SqlTroupe> { it.toLong() }
            val skript = SqlTransactionSkript
                    .transaction(action1)
                    .mapInsideTransaction(mappedSkript)
            skript shouldBe SqlTransactionSkript.transaction(
                    Skript.SkriptLink(
                            action1,
                            mappedSkript))
        }

        "An autocommit SqlTransactionSkript should map a skript within the transaction" {
            val action1 = Skript.map<Int, String, SqlTroupe>({ it.toString() })
            val mappedSkript = Skript.map<String, Long, SqlTroupe> { it.toLong() }
            val skript = SqlTransactionSkript
                    .autoCommit(action1)
                    .mapInsideTransaction(mappedSkript)
            skript shouldBe SqlTransactionSkript.autoCommit(
                    Skript.SkriptLink(
                            action1,
                            mappedSkript))
        }

        "A transactional SqlTransactionSkript should unwrap a SqlTransactionSkript mapped within a transaction" {
            val action1 = Skript.map<Int, String, SqlTroupe>({ it.toString() })
            val action2 = Skript.map<String, Long, SqlTroupe>({ it.toLong() })
            val skript = SqlTransactionSkript
                    .transaction(action1)
                    .mapInsideTransaction(SqlTransactionSkript.transaction(action2))
            skript shouldBe SqlTransactionSkript.transaction(
                    Skript.SkriptLink(
                            action1,
                            action2))
        }

        "An autocommit SqlTransactionSkript should unwrap a SqlTransactionSkript mapped within a transaction" {
            val action1 = Skript.map<Int, String, SqlTroupe>({ it.toString() })
            val action2 = Skript.map<String, Long, SqlTroupe>({ it.toLong() })
            val skript = SqlTransactionSkript
                    .autoCommit(action1)
                    .mapInsideTransaction(SqlTransactionSkript.transaction(action2))
            skript shouldBe SqlTransactionSkript.autoCommit(
                    Skript.SkriptLink(
                            action1,
                            action2))
        }

        "A transactional SQlSkript should create a transaction from a skript" {
            val transaction = Skript.map<Int, String, SqlTroupe>({ it.toString() })
            val skript = SqlTransactionSkript.transaction(transaction)
            skript shouldBe SqlTransactionSkript.transaction(transaction)
        }

        "An autocommit SQlSkript should create a transaction from a skript" {
            val transaction = Skript.map<Int, String, SqlTroupe>({ it.toString() })
            val skript = SqlTransactionSkript.autoCommit(transaction)
            skript shouldBe SqlTransactionSkript.autoCommit(transaction)
        }

        "A transactional SqlTransactionSkript should create a transaction from a skript and unwrap a SqlTransactionSkript" {
            val transaction = Skript.map<Int, String, SqlTroupe>({ it.toString() })
            val skript = SqlTransactionSkript.transaction(SqlTransactionSkript.transaction(transaction))
            val skript2 = SqlTransactionSkript.transaction(SqlTransactionSkript.autoCommit(transaction))
            skript shouldBe SqlTransactionSkript.transaction(transaction)
            skript2 shouldBe SqlTransactionSkript.transaction(transaction)
        }

        "An autocommit SqlTransactionSkript should create a transaction from a skript and unwrap a SqlTransactionSkript" {
            val transaction = Skript.map<Int, String, SqlTroupe>({ it.toString() })
            val skript = SqlTransactionSkript.autoCommit(SqlTransactionSkript.transaction(transaction))
            val skript2 = SqlTransactionSkript.autoCommit(SqlTransactionSkript.autoCommit(transaction))
            skript shouldBe SqlTransactionSkript.autoCommit(transaction)
            skript2 shouldBe SqlTransactionSkript.autoCommit(transaction)
        }
    }
}
