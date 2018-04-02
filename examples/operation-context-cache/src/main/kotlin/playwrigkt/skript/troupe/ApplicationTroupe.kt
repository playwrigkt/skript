package playwrigkt.skript.troupe

import playwrigkt.skript.performer.QueuePublishPerformer
import playwrigkt.skript.troupe.QueuePublishTroupe
import playwrigkt.skript.performer.SQLPerformer
import playwrigkt.skript.performer.SerializePerformer
import playwrigkt.skript.result.AsyncResult


interface TroupeProps<R> {
    fun getTroupeProps(): R
}

class ApplicationTroupe<R>(
        private val publishTroupe: QueuePublishTroupe,
        private val sqlTroupe: SQLTroupe,
        private val serializeTroupe: SerializeTroupe,
        private val cache: R):
        QueuePublishTroupe,
        SQLTroupe,
        SerializeTroupe,
        TroupeProps<R>
{
    override fun getSerializePerformer(): AsyncResult<out SerializePerformer> = serializeTroupe.getSerializePerformer()
    override fun getTroupeProps(): R = cache
    override fun getPublishPerformer(): AsyncResult<out QueuePublishPerformer> = publishTroupe.getPublishPerformer()
    override fun getSQLPerformer(): AsyncResult<out SQLPerformer> = sqlTroupe.getSQLPerformer()
}