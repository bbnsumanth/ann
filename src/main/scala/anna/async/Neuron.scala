package anna.async

import akka.actor._
import anna.Context
import anna.async.Messages._
import anna.data._
import anna.logger.LOG
import anna.utils.Utils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class Neuron(
              val id: String,
              val netId: String,
              val threshold: Double,
              val silenceIterations: Int,
              protected var synapses: List[Synapse] = List[Synapse]()
) extends Actor with NeuronTriggers {
  implicit val that = this

  protected var buffer = 0.0

  private val schedulerBuffer = new SchedulerBuffer(context)

  private var isSleeping = false

  override def preStart():Unit = {
    NeuronCounter.reg(netId, id, self)
  }

  override def postStop():Unit = {
    schedulerBuffer.clear()
    NeuronCounter.unreg(netId, id)
  }

  private def becomeSilent(){
    LOG += s"$id becomeSilent, silenceIterations is ${silenceIterations}"
    buffer = 0.0
    LOG += s"$id: buffer is now $buffer"

    val t = Context().iterationTime * silenceIterations
    if(t > 0){
      LOG += s"$id becoming silent for ${silenceIterations} iterations ($t millis)"
      context.become(silence)
      schedulerBuffer.schedule(t millis){ wakeFromSilence() }
    }

    triggerSilenceRequested()
  }

  private def makeSleep() = {
    LOG += s"$id going to sleep for ${Context().iterationTime} ms"
    isSleeping = true
    context.system.scheduler.scheduleOnce(Context().iterationTime millis){ wakeUp() } //@todo: change to schedulerBuffer
  }

  private def wakeFromSilence() = {
    LOG += s"$id waking up from a silence request"
    isSleeping = false
    context.become(receive)
  }

  protected def +=(signal: Double){
    LOG += s"$id adding signal $signal to buffer $buffer, threshold is $threshold"
    buffer += signal
    LOG += s"$id: buffer is now $buffer"
    if(!isSleeping){
      tick()
    }
  }

  private def wakeUp(){
    LOG += s"$id waking up"
    isSleeping = false
    tick()
  }

  private def biggerOrCloseEnough(x: Double, y: Double) = { x > 0.0 && x + 0.001 > y }

  private def tick():Unit = this.synchronized {
    buffer = Utils.minmax(-1.0, buffer, 1.0)

    if (biggerOrCloseEnough(buffer, threshold)) {
      triggerThresholdPassed()
      run()
    }
    LOG += s"after the tick: $id: buffer is now $buffer"
  }

  protected def run(): Unit = {
    val output = 1.0
    buffer = 0.0

    LOG += s"$id trigger output $output, synapses size: ${synapses.size}"
    makeSleep()
    synapses.foreach( _.send(output, id) )

    triggerAfterFire(output)
  }

  protected def findSynapse(destinationId: String):Option[Synapse] =
    if(synapses.nonEmpty) synapses.find(_.dest.id == destinationId) else None

  protected def findSynapse(destination: Neuron):Option[Synapse] = findSynapse(destination.id)

  protected def answer(msg: Answer) = NetRef.get match {
    case Some(netref) => netref ! msg
    case None =>
  }

  private def reset(): Unit ={
    buffer = 0.0
    LOG += s"reset; $id: buffer is now $buffer"

    context.become(receive)
    schedulerBuffer.clear()
    answer(Success(id))
  }

  private def removeTriggers(): Unit ={
    removeAllTriggers()
    answer(Success(id))
  }

  def receive = activeBehaviour orElse commonBehaviour orElse otherBehaviour(s"$id, active")

  def silence = silentBehaviour orElse commonBehaviour orElse otherBehaviour(s"$id, silence")

  val activeBehaviour: Receive = {
    case Signal(s, _) => this += s
    case SilenceRequest => becomeSilent()
  }

  val silentBehaviour: Receive = {
    case Signal(s, senderId) =>
        LOG += s"$id, signal ignored: $s"
  }

  val commonBehaviour: Receive = {
      case GetId => sender ! Msg(0.0, id)
      case GetData => sender ! info
      case FindSynapse(destinationId) => sender ! MsgSynapse(findSynapse(destinationId))
      case GetSynapses => sender ! MsgSynapses(synapses)
      case SetSynapses(synapses) => this.synapses = synapses.toList
      case AddAfterFireTrigger(triggerId, trigger) =>
        addAfterFire(triggerId, trigger)
        sender ! Success(triggerId)
      case RemoveAfterFireTrigger(triggerId) =>
        removeAfterFire(triggerId)
        sender ! Success(triggerId)
      case AddSilenceRequestedTrigger(triggerId, trigger) =>
        addSilenceRequested(triggerId, trigger)
        sender ! Success(triggerId)
      case RemoveSilenceRequestedTrigger(triggerId) =>
        removeSilenceRequested(triggerId)
        sender ! Success(triggerId)
      case RemoveAllTriggers => removeTriggers()
      case Reset => reset()
  }

  def otherBehaviour(state: String): Receive = {
    case other => LOG += s"$state, unrecognized message: $other"
  }

  def info = NeuronInfo(id, netId, threshold, silenceIterations, synapses.map(_.info), buffer)
}
