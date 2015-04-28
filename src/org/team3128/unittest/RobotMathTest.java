package org.team3128.unittest;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.team3128.Log;
import org.team3128.util.MotorDir;
import org.team3128.util.RobotMath;

/**
 * This class tests the functions of RobotMath.
 * @author Jamie
 *
 */
public class RobotMathTest
{
	   @Test
	   public void testsgn() 
	   {
		   //test the three different values it can output for double and int inputs
		   Log.info("RobotMathTest", "Testing sgn()");
		   
	       assertEquals(1, RobotMath.sgn(1)); //positive values should return 1
	       assertEquals(0, RobotMath.sgn(0)); //0 should return 0	       
	       assertEquals(-1.0, RobotMath.sgn(-1.0), .001); //negative values should return -1
	       
	   }
	   
	   @Test
	   public void testNormalizeAngle() 
	   {
	       assertEquals(75, RobotMath.normalizeAngle(75), .001); //angles between 0 and 359 should not be messed with
	       assertEquals(356, RobotMath.normalizeAngle(-4), .001); //negative values should get 360 added to them
	       assertEquals(0, RobotMath.normalizeAngle(360), .001); //360 should be normalized to 0
	       assertEquals(73, RobotMath.normalizeAngle(793), .001); //values greater than 359 should be reduced
	   }
	   
	   @Test
	   public void testGetMotorDirToTarget() 
	   {
		   //test basic functionality
	       assertEquals(MotorDir.CW, RobotMath.getMotorDirToTarget(0, 30, false));
	       assertEquals(MotorDir.CW, RobotMath.getMotorDirToTarget(0, 200, false));
	       
	       //test that shortWay is honored
	       assertEquals(MotorDir.CCW, RobotMath.getMotorDirToTarget(0, 200, true));
	       assertEquals(MotorDir.CW, RobotMath.getMotorDirToTarget(0, 400, false));
	       
	       //test going the other way
	       assertEquals(MotorDir.CCW, RobotMath.getMotorDirToTarget(100, 23, false));
	       assertEquals(MotorDir.CCW, RobotMath.getMotorDirToTarget(300, 0, false));
	       assertEquals(MotorDir.CW, RobotMath.getMotorDirToTarget(300, 0, true));

	       //test having a nonzero endpoint
	       assertEquals(MotorDir.CW, RobotMath.getMotorDirToTarget(10, 190, false));
	       assertEquals(MotorDir.CCW, RobotMath.getMotorDirToTarget(190, 10, true));
	       
	       //test that going to the current position does nothing
	       assertEquals(MotorDir.NONE, RobotMath.getMotorDirToTarget(35, 35, false));
	       assertEquals(MotorDir.NONE, RobotMath.getMotorDirToTarget(197, 197, true));

	   }
}
