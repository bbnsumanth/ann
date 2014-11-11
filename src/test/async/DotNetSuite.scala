package test.async

import org.scalatest.junit.JUnitSuite
import main.async.NetBuilder
import org.junit.Test
import org.junit.Assert._
import main.async.Context.sleepTime
import main.logger.LOG.debug
import main.logger.LOG

class DotNetSuite extends MySuite {
  private def dotNet() = {
    builder.defSlope = 5.0
    builder.addInput("in1")
    // dots
    builder.use("in1").chainMiddle("mi11",0.6,0.5).loop("loop1",1.0,0.5,1.0).chainMiddle("mi12",1.0,0.75).chainOutput("out1",1.0)
    builder.use("out1").connect("mi11", -0.49)
    builder.use("out1").connect("mi12", -1.0)
    
    build()
    debug("----------")
    val sb = StringBuilder.newBuilder
    in.tickInterval = sleepTime * 2
    out.ids.foreach( debug(this, _) )
    out.addAfterFireTrigger("out1", () => {
      println("KROPA!")
      sb.append('.')
     })
    
    sb
  }
  
  @Test def shouldDotThenNothing(){
    val sb = dotNet
    
    in += "1"
    init()
    in.tickUntilCalm()
    assertEquals(".",sb.toString)
  }
  
  @Test def shouldDot3Times(){
    val sb = dotNet
    
    in += "1,0,0,1,0,0,1,0,0" // is there a way to just send 1s? after all in this implementations 0s are just lack of signal
    init()
    in.tickUntilCalm()
    assertEquals("...",sb.toString)
  }
  
  private def dotNetRes4() = {
    builder.defSlope = 5.0
    builder.resolution = 4
    builder.addInput("in1")
    // dots
    builder.use("in1").chainMiddle("mi11",0.24,0.5).loop("loop1",0.75,0.5,1.0).chainMiddle("mi12",1.0,0.75).chainOutput("out1",1.0)
    builder.use("out1").hush("mi11")
    builder.use("out1").hush("mi12")
    builder.use("out1").hush("loop1")
    
    build()
    debug("----------")
    val sb = StringBuilder.newBuilder
    in.tickInterval = sleepTime * 2
    out.addAfterFireTrigger("out1", () => {
      println("KROPA!")
      sb.append('.') 
    })
    
    sb
  }
  
  @Test def shouldDotThenNothingRes4(){
    val sb = dotNetRes4()
    //LOG.allow("mi11")
    in += "1"
    init()
    LOG.timer()
    in.tickUntilCalm()
    assertEquals(".",sb.toString)
    LOG.clearAllowedIds()
  }
  
  @Test def shouldDot3TimesRes4(){
    val sb = dotNetRes4()
    LOG.allow("mi11")
    in += "1,0,0,1,0,0,1,0,0"
    init()
    LOG.timer()  
    in.tickUntilCalm()
    assertEquals("...",sb.toString)
    LOG.clearAllowedIds()
  }
}