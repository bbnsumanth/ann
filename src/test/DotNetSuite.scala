package test

import org.scalatest.junit.JUnitSuite
import org.junit.{Test, Before}
import org.junit.Assert._
import main._

class DotNetSuite extends JUnitSuite {
  private def dotNet() = {
    val builder = NetBuilder()
    builder.middleNeuronType = NeuronType.DELAY
    builder.defSlope = 5.0
    builder.addInput("in1")
    // dots
    builder.use("in1").chainMiddle("mi11",0.6,0.5).loop("loop1",1.0,0.5,1.0).chainMiddle("mi12",1.0,0.75).chainOutput("out1",1.0)
    builder.use("out1").connect("mi11", -0.49)
    builder.use("out1").connect("mi12", -1.0)
    
    val (in, net, out) = builder.build("in","out")
    val out1 = builder.get("out1")
    val sb = StringBuilder.newBuilder
    out.addAfterFireTrigger(out1, (n:Neuron) => {
      println("KROPA!")
      sb.append('.'); 
    })
    
    (in, sb)
  }
  
  @Test
  def shouldDotThenNothing(){
    val (in, sb) = dotNet
    
    in += "1,0,0,0,0,0"
    in.tickUntilCalm()
    assertEquals(".",sb.toString)
  }
  
  @Test
  def shouldDot3Times(){
    val (in, sb) = dotNet
    
    in += "1,0,0,1,0,0,1,0,0"
    in.tickUntilCalm()
    assertEquals("...",sb.toString)
  }
  
  private def dotNetRes4() = {
    val builder = NetBuilder()
    builder.middleNeuronType = NeuronType.DELAY
    builder.defSlope = 5.0
    builder.defForgetting = 0.1
    
    builder.addInput("in1")
    // dots
    builder.use("in1").chainMiddle("mi11",0.6,0.5).loop("loop1",1.0,0.5,1.0).chainMiddle("mi12",1.0,0.75).chainOutput("out1",1.0)
    builder.use("out1").connect("mi11", -0.49)
    builder.use("out1").connect("mi12", -1.0)
    
    val (in, net, out) = builder.build("in","out", 4)
    val out1 = builder.get("out1")
    val sb = StringBuilder.newBuilder
    out.addAfterFireTrigger(out1, (n:Neuron) => {
      println("KROPA!")
      sb.append('.'); 
    })
    
    (in, sb)
  }
  
  @Test
  def shouldDotThenNothingRes4(){
    val (in, sb) = dotNetRes4
    LOG.allow("mi11")
    in += "1,0,0,0,0,0"
    in.tickUntilCalm()
    assertEquals(".",sb.toString)
    LOG.clearAllowedIds()
  }
  
  @Test
  def shouldDot3TimesRes4(){
    val (in, sb) = dotNetRes4
    LOG.allow("mi11")
    in += "1,0,0,1,0,0,1,0,0"
    in.tickUntilCalm()
    assertEquals("...",sb.toString)
    LOG.clearAllowedIds()
  }
}