package anna.blocks

import anna.async.NetBuilder
import anna.async.NetBuilderOps._

/**
  * Created by gorywoda on 1/31/16.
  */
case class DelayGate(name: String, delay: Int){
  lazy val data = {
    val builder = NetBuilder()
    chain(builder)
    builder.data
  }

  def chain(builder: NetBuilder, inputWeight: Double = 1.0, inputThreshold: Double = 0.0) = {
    val feedbackWeight = DelayGate.middleThreshold / (delay + 1)
    if(builder.isCurrent) builder.chain(inputId, inputWeight, inputThreshold, delay)
    else builder.addMiddle(id=inputId, threshold=inputThreshold, silenceIterations=delay)

    builder.use(inputId).silence(inputId).chain(middleId, 1.0, 0.01).connect(middleId, 1.0)
           .chain(outputId, feedbackWeight, DelayGate.middleThreshold).silence(middleId)
           .addSilencingNeuron(silencingId).silence(inputId).silence(middleId).silence(outputId)
           .use(outputId) // always end chaining with setting the current neuron at the main output of the block
  }

  val inputId = DelayGate.inputId(name)
  val middleId = DelayGate.middleId(name)
  val outputId = DelayGate.outputId(name)
  val silencingId = DelayGate.silencingId(name)
}

object DelayGate {
  val blockNamePrefix = "DelayGate"
  val nameRegex = s""".*${blockNamePrefix}#([0-9]+)#.*""".r
  val neuronsInBlock = 4

  val middleThreshold = 0.9

  def nextName() = s"${blockNamePrefix}#${firstFreeId}#"

  private var firstFreeId = 1

  def apply(delay: Int):DelayGate = {
    val newName = nextName()
    firstFreeId += 1
    DelayGate(newName, delay)
  }

  def inputId(name: String) = s"${name}1"
  def middleId(name: String) = s"${name}2"
  def outputId(name: String) = s"${name}3"
  def silencingId(name: String) = s"${name}s"

}