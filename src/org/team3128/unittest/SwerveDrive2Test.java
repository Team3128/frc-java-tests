package org.team3128.unittest;

import java.util.ArrayList;

import org.junit.Test;
import org.team3128.drive.SwerveDrive2;
import org.team3128.hardware.motor.MotorGroup;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Talon;

public class SwerveDrive2Test
{
	ArrayList<Talon> turnMotors = new ArrayList<Talon>();
	ArrayList<Talon> driveMotors = new ArrayList<Talon>();
	ArrayList<DigitalInput> homingSwitches = new ArrayList<DigitalInput>();
	ArrayList<DistanceEncoderMock> fakeEncoders = new ArrayList<DistanceEncoderMock>();

	
	final static double kP = 1, kI = 0, kD = 0;
	
	SwerveDrive2 drive = new SwerveDrive2();

	
	void addModule(int number, double angle)
	{
		// create and store mock objects
		Talon turnMotor = new Talon(1000 + number);
		
		Talon driveMotor = new Talon(number);
		
		DigitalInput homingSwitch = new DigitalInput(number);
		
		DistanceEncoderMock fakeEncoder = new DistanceEncoderMock();
		
		drive.addModule(new MotorGroup(turnMotor), new MotorGroup(driveMotor), fakeEncoder, homingSwitch, 0, angle, 0, kP, kI, kD);

	}
	
	@Test
	public void testSewrveDrive2()
	{
		

	}
}
