package org.team3128.unittest;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.team3128.Log;
import org.team3128.util.MotorDir;
import org.team3128.util.RobotMath;


public class RobotMathTest
{
	   @Test
	   public void testsgn() 
	   {
		   //test the three different values it can output for double and int inputs
		   Log.info("RobotMathTest", "Testing sgn()");
		   
	       assertEquals(1, RobotMath.sgn(1));
	       assertEquals(0, RobotMath.sgn(0));
	       assertEquals(-1.0, RobotMath.sgn(-1.0), .001);
	   }
	   
	   @Test
	   public void testNormalizeAngle() 
	   {
	       assertEquals(75, RobotMath.normalizeAngle(75), .001);
	       assertEquals(356, RobotMath.normalizeAngle(-4), .001);
	       assertEquals(0, RobotMath.normalizeAngle(360), .001);
	       assertEquals(73, RobotMath.normalizeAngle(793), .001);
	   }
	   
	   @Test
	   public void testGetMotorDirToTarget() 
	   {
	       assertEquals(MotorDir.CW, RobotMath.getMotorDirToTarget(0, 30, false));
	       assertEquals(MotorDir.CW, RobotMath.getMotorDirToTarget(0, 200, false));
	       assertEquals(MotorDir.CCW, RobotMath.getMotorDirToTarget(0, 200, true));
	       assertEquals(MotorDir.CW, RobotMath.getMotorDirToTarget(0, 400, false));
	       
	       assertEquals(MotorDir.CCW, RobotMath.getMotorDirToTarget(100, 23, false));
	       assertEquals(MotorDir.CCW, RobotMath.getMotorDirToTarget(300, 0, false));
	       assertEquals(MotorDir.CW, RobotMath.getMotorDirToTarget(300, 0, true));

	       assertEquals(MotorDir.CW, RobotMath.getMotorDirToTarget(10, 190, false));
	       assertEquals(MotorDir.CCW, RobotMath.getMotorDirToTarget(190, 10, true));
	       assertEquals(MotorDir.NONE, RobotMath.getMotorDirToTarget(35, 35, false));
	       assertEquals(MotorDir.NONE, RobotMath.getMotorDirToTarget(197, 197, true));

	   }
}
