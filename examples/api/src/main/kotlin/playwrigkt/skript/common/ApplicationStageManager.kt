package playwrigkt.skript.common

import playwright.skript.consumer.alpha.QueueMessage
import playwright.skript.performer.QueuePublishPerformer
import playwright.skript.troupe.QueuePublishTroupe
import playwrigkt.skript.Skript
import playwrigkt.skript.performer.PublishPerformer
import playwrigkt.skript.performer.SQLPerformer
import playwrigkt.skript.performer.SerializePerformer
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.troupe.SQLTroupe
import playwrigkt.skript.troupe.SerializeTroupe
import playwrigkt.skript.venue.StageManager

data class ApplicationStageManager(
        val publishProvider: StageManager<QueuePublishTroupe>,
        val sqlProvider: StageManager<SQLTroupe>,
        val serializeProvider: StageManager<SerializeTroupe>
): StageManager<ApplicationTroupe> {
    override fun hireTroupe(): ApplicationTroupe =
        ApplicationTroupe(publishProvider.hireTroupe(), sqlProvider.hireTroupe(), serializeProvider.hireTroupe())

    fun <I, O> runWithTroupe(skript: Skript<I, O, ApplicationTroupe>, i: I): AsyncResult<O> {
        val troupe = hireTroupe()
        println("troupe: $troupe")
        return skript.run(i, troupe)
    }
}

data class ApplicationTroupe(
        private val publishTroupe: QueuePublishTroupe,
        private val sqlTroupe: SQLTroupe,
        private val serializeTroupe: SerializeTroupe):
        QueuePublishTroupe,
        SQLTroupe,
        SerializeTroupe {
    override fun getPublishPerformer(): AsyncResult<out PublishPerformer<QueueMessage>> = publishTroupe.getPublishPerformer()
    override fun getSerializePerformer(): AsyncResult<out SerializePerformer> = serializeTroupe.getSerializePerformer()
    override fun getSQLPerformer(): AsyncResult<out SQLPerformer> = sqlTroupe.getSQLPerformer()
}