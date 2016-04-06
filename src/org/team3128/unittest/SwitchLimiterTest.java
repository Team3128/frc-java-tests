package org.team3128.unittest;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.team3128.common.hardware.motor.limiter.SwitchLimiter;

import frctest.mock.MockDigitalInput;

public class SwitchLimiterTest
{
	
	SwitchLimiter testLimiter;
	MockDigitalInput minSwitch;
	MockDigitalInput maxSwitch;
	
	@Before
	public void setup()
	{
		minSwitch = new MockDigitalInput();
		maxSwitch = new MockDigitalInput();
		
		//construct testLimiter with mock objects
		testLimiter = new SwitchLimiter(minSwitch, maxSwitch, false);
	}
	
	@Test
	/**
	 * Test that, with both limit switches inactive, the limit is not activated
	 */
	public void testBasic()
	{
		minSwitch.state = false;
		maxSwitch.state = false;
		
		assertEquals(testLimiter.canMove(1), true);
		assertEquals(testLimiter.canMove(-1), true);
	}
	
	@Test
	/**
	 * Test that the minimum limit switch works
	 */
	public void testMinLimit()
	{
		minSwitch.state = true;
		maxSwitch.state = false;
		
		assertEquals(testLimiter.canMove(1), true);
		assertEquals(testLimiter.canMove(-1), false);
	}
	
	@Test
	/**
	 * Test that the maximum limit switch works
	 */
	public void testMaxLimit()
	{
		minSwitch.state = false;
		maxSwitch.state = true;
		
		assertEquals(testLimiter.canMove(1), false);
		assertEquals(testLimiter.canMove(-1), true);
	}
}
