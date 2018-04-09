package playwrigkt.skript.sql.skript

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import playwrigkt.skript.Skript
import playwrigkt.skript.sql.transaction.SQLTransactionSkript
import playwrigkt.skript.troupe.SQLTroupe

class SQLTransactionSkriptSpec : StringSpec() {
    init {
        "A transactional skript should wrap a SQLSkript in a skript" {
            val action = Skript.map<Int, String, SQLTroupe>({ it.toString() })
            val skript = SQLTransactionSkript.transaction(action)
            skript shouldBe SQLTransactionSkript.TransactionalSQLTransactionSkript(action)
        }

        "An autocommit skript should wrap a SQLSkript in a skript" {
            val action = Skript.map<Int, String, SQLTroupe>({ it.toString() })
            val skript = SQLTransactionSkript.autoCommit(action)
            skript shouldBe SQLTransactionSkript.AutoCommitSQlTransactionSkript(action)
        }

        "A transactional SQLTransactionSkript should map a sqlAction within the transaction" {
            val action1 = Skript.map<Int, String, SQLTroupe>({ it.toString() })
            val action2 = Skript.map<String, Long, SQLTroupe>({ it.toLong() })
            val skript = SQLTransactionSkript
                    .transaction(action1)
                    .mapInsideTransaction(action2)
            skript shouldBe SQLTransactionSkript.TransactionalSQLTransactionSkript(
                    Skript.SkriptLink(action1, action2))
        }

        "An autocommit SQLTransactionSkript should map a sqlAction within the transaction" {
            val action1 = Skript.map<Int, String, SQLTroupe>({ it.toString() })
            val action2 = Skript.map<String, Long, SQLTroupe>({ it.toLong() })
            val skript = SQLTransactionSkript
                    .autoCommit(action1)
                    .mapInsideTransaction(action2)
            skript shouldBe SQLTransactionSkript.autoCommit(
                    Skript.SkriptLink(
                            action1,
                            action2))
        }

        "A transactional SQLTransactionSkript should map a skript within the transaction" {
            val action1 = Skript.map<Int, String, SQLTroupe>({ it.toString() })
            val mappedSkript = Skript.map<String, Long, SQLTroupe> { it.toLong() }
            val skript = SQLTransactionSkript
                    .transaction(action1)
                    .mapInsideTransaction(mappedSkript)
            skript shouldBe SQLTransactionSkript.transaction(
                    Skript.SkriptLink(
                            action1,
                            mappedSkript))
        }

        "An autocommit SQLTransactionSkript should map a skript within the transaction" {
            val action1 = Skript.map<Int, String, SQLTroupe>({ it.toString() })
            val mappedSkript = Skript.map<String, Long, SQLTroupe> { it.toLong() }
            val skript = SQLTransactionSkript
                    .autoCommit(action1)
                    .mapInsideTransaction(mappedSkript)
            skript shouldBe SQLTransactionSkript.autoCommit(
                    Skript.SkriptLink(
                            action1,
                            mappedSkript))
        }

        "A transactional SQLTransactionSkript should unwrap a SQLTransactionSkript mapped within a transaction" {
            val action1 = Skript.map<Int, String, SQLTroupe>({ it.toString() })
            val action2 = Skript.map<String, Long, SQLTroupe>({ it.toLong() })
            val skript = SQLTransactionSkript
                    .transaction(action1)
                    .mapInsideTransaction(SQLTransactionSkript.transaction(action2))
            skript shouldBe SQLTransactionSkript.transaction(
                    Skript.SkriptLink(
                            action1,
                            action2))
        }

        "An autocommit SQLTransactionSkript should unwrap a SQLTransactionSkript mapped within a transaction" {
            val action1 = Skript.map<Int, String, SQLTroupe>({ it.toString() })
            val action2 = Skript.map<String, Long, SQLTroupe>({ it.toLong() })
            val skript = SQLTransactionSkript
                    .autoCommit(action1)
                    .mapInsideTransaction(SQLTransactionSkript.transaction(action2))
            skript shouldBe SQLTransactionSkript.autoCommit(
                    Skript.SkriptLink(
                            action1,
                            action2))
        }

        "A transactional SQlSkript should create a transaction from a skript" {
            val transaction = Skript.map<Int, String, SQLTroupe>({ it.toString() })
            val skript = SQLTransactionSkript.transaction(transaction)
            skript shouldBe SQLTransactionSkript.transaction(transaction)
        }

        "An autocommit SQlSkript should create a transaction from a skript" {
            val transaction = Skript.map<Int, String, SQLTroupe>({ it.toString() })
            val skript = SQLTransactionSkript.autoCommit(transaction)
            skript shouldBe SQLTransactionSkript.autoCommit(transaction)
        }

        "A transactional SQLTransactionSkript should create a transaction from a skript and unwrap a SQLTransactionSkript" {
            val transaction = Skript.map<Int, String, SQLTroupe>({ it.toString() })
            val skript = SQLTransactionSkript.transaction(SQLTransactionSkript.transaction(transaction))
            val skript2 = SQLTransactionSkript.transaction(SQLTransactionSkript.autoCommit(transaction))
            skript shouldBe SQLTransactionSkript.transaction(transaction)
            skript2 shouldBe SQLTransactionSkript.transaction(transaction)
        }

        "An autocommit SQLTransactionSkript should create a transaction from a skript and unwrap a SQLTransactionSkript" {
            val transaction = Skript.map<Int, String, SQLTroupe>({ it.toString() })
            val skript = SQLTransactionSkript.autoCommit(SQLTransactionSkript.transaction(transaction))
            val skript2 = SQLTransactionSkript.autoCommit(SQLTransactionSkript.autoCommit(transaction))
            skript shouldBe SQLTransactionSkript.autoCommit(transaction)
            skript2 shouldBe SQLTransactionSkript.autoCommit(transaction)
        }
    }
}
