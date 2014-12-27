package anna.async.epengine

import anna.async.NeuronType
import anna.data.{SynapseData, HushValue, ForgetTrait, NeuronData}

/**
 * Created by gorywoda on 28.12.14.
 */
case class NeuronChromosome(data: NeuronData) {
  lazy val id = data.id
  lazy val threshold = data.threshold
  lazy val slope = data.slope
  lazy val hushValue = data.hushValue
  lazy val forgetting = data.forgetting
  lazy val synapses = data.synapses
  lazy val neuronType = data.neuronType
}

object NeuronChromosome {
  def apply(id: String, threshold: Double, slope: Double, hushValue: HushValue, forgetting: ForgetTrait, synapses: List[SynapseData], neuronType: NeuronType.Value) =
    NeuronChromosome(NeuronData(id, threshold, slope, hushValue, forgetting, synapses, neuronType))
}
