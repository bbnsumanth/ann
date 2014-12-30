package anna.async

import anna.async.NeuronTriggers.Trigger
import anna.data.{NeuronData, ForgetTrait, HushValue, SynapseTrait}

object Messages {
  // signals
  case class Signal(s: Double)
  case class SignalSeq(input: Seq[Double])

  // commands
  case object HushNow // become silent
  case object WakeUp
  case object WakeFromHush
  case class Connect(destinationRef: NeuronRef, weight: SynapseTrait)
  case class Disconnect(destinationId: String)
  case class CreateNeuron(data: NeuronData)
  case class SetInputLayer(ids: Seq[String])
  case object NeuronShutdown
  case object Shutdown // net shutdown
  case object ResetBuffer
  case class SetSynapses(synapses: Seq[Synapse])
  
  // questions
  case object GetId
  case object GetInput
  case object GetLastOutput
  case class FindSynapse(destinationId: String)
  case object GetSynapses
  case class GetNeuron(id: String)
  case object GetNeurons
  case object GetInputLayer
  case object GetMiddleLayer

  // answers
  abstract class Answer
  case class Success(id: String) extends Answer // successful execution of the command
  case class Failure(error: String) extends Answer // error while executing the command
  case class NeuronShutdownDone(id: String) extends Answer // a special case - successful shutdown of a neuron
  case class NetShutdownDone(id: String) extends Answer // a special case - successful shutdown if the whole net
  case class Msg(d: Double, str: String) extends Answer // general answer to a question about a number or an id
  case class MsgSynapse(synapseOpt: Option[Synapse]) extends Answer // sends back a synapse
  case class MsgSynapses(synapses: Seq[Synapse]) extends Answer // sends back all synapses of the neuron
  case class MsgNeuron(neuronOpt: Option[NeuronRef]) extends Answer
  case class MsgNeurons(neurons: Seq[NeuronRef]) extends Answer
	
  // triggers
  case class AddAfterFireTrigger(id: String, f: Trigger)
  case class RemoveAfterFireTrigger(id: String)
  case class AddHushRequestedTrigger(id: String, f: Trigger)
  case class RemoveHushRequestedTrigger(id: String)

}