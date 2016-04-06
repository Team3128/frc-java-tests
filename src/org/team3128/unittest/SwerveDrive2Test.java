package org.team3128.unittest;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.Ignore;
import org.junit.Test;
import org.team3128.common.drive.SwerveDrive2;
import org.team3128.common.hardware.motor.MotorGroup;
import org.team3128.common.listener.ListenerManager;
import org.team3128.common.listener.controller.ControllerSimXbox;
import org.team3128.common.util.RobotMath;
import org.team3128.common.util.units.Length;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Talon;
import frctest.EmulatorMain;

public class SwerveDrive2Test
{
	// class to represent an emulated swerve module
	@Ignore
	private class FakeModule
	{
		Talon turnMotor;
		
		Talon driveMotor;
		
		DigitalInput homingSwitch;
		
		DistanceEncoderMock fakeEncoder;
		
		double driveWheelSpeed = 0;
		
		double driveWheelForce = 0; //in N
		
		//90 degrees is straight ahead
		double simulatedAngleDegrees = 0;
		
		double wheelbaseAngle;
		
		/**
		 * 
		 * @param number
		 * @param angle The angle of the module on the wheelbase in degrees.
		 */
		public FakeModule(int number, double angle)
		{
			wheelbaseAngle = angle;
			
			// create mock objects
			turnMotor = new Talon(1000 + number);
			
			driveMotor = new Talon(number);
			
			homingSwitch = new DigitalInput(number);
			
			fakeEncoder = new DistanceEncoderMock();
			
			//add module to swerve drive class
			drive.addModule(new MotorGroup(turnMotor), new MotorGroup(driveMotor), fakeEncoder, homingSwitch, 0, angle, 0, kP, kI, kD);
		}
		
		private static final double WHEEL_CIRCUMFERENCE = 5 * Length.cm * Math.PI;
		
		private static final double TURNING_MOTOR_SPEED_DPS = 180; //degrees per second
		
		//should be executed after the swerve drive updates
		public void update(int millisecondsBetweenUpdates)
		{
			driveWheelSpeed = (RobotMath.getCIMExpectedRPM(driveMotor.get()) / 10) * WHEEL_CIRCUMFERENCE;
			
			driveWheelForce = DRIVE_MOTOR_FORCE * driveMotor.get();
			
			double turningAngularSpeed = TURNING_MOTOR_SPEED_DPS  * turnMotor.get();
			
			double distanceTurned = turningAngularSpeed * (((double)millisecondsBetweenUpdates) / 1000);
			
			simulatedAngleDegrees += distanceTurned;
			
			fakeEncoder.distanceInDegrees += distanceTurned;
			
			if(Math.abs(simulatedAngleDegrees - 90) < 1)
			{
				homingSwitch.state = true;
			}
			else
			{
				homingSwitch.state = false;
			}
		}
	}
	ArrayList<FakeModule> modules = new ArrayList<FakeModule>();
	
	final static double kP = .1, kI = 0, kD = 0;
	final static double ROBOT_MASS_KG = 100;
	final static double DRIVE_MOTOR_FORCE = 200; //N
	final static double INTERNAL_FRICTION = 50; // N/(m/s)^2
	final static double INTERNAL_ROTATIONAL_FRICTION = 3; // Nm/(deg/s)^2
	final static double WHEEL_NON_TURNING_FRICTION_FACTOR = 50; // ratio of frictional force to speed when a wheel is being pushed sideways (N/(m/s))

	
	SwerveDrive2 drive = new SwerveDrive2();
	
	JFrame frame;
	
	SwerveSimulatorPanel panel;
	
	ListenerManager listenerManager;
	
	BufferedImage robotBaseIcon;
	BufferedImage wheelIcon;
	
	double wheelbaseRadiusM;
	double momentOfInertia; // in kgm^2
	
	boolean windowOpen;
	
	double robotSpeedX = 0, robotSpeedY = 0; // in m/s
	
	double robotPositionX = WINDOW_WIDTH_M / 2.0, robotPositionY = WINDOW_HEIGHT_M / 2.0; //in m from top left
	
	double robotRotationDegrees = 0; // 0 degrees is up in the window
	double robotRotationalSpeed = 0; //in deg/s 
	double netTorque; //in Nm
	
	final static int PIXELS_PER_METER = 300;

	final static int WINDOW_WIDTH_PX = 1920;
	final static int WINDOW_HEIGHT_PX = 1080;

	final static double WINDOW_WIDTH_M = ((double)WINDOW_WIDTH_PX) / PIXELS_PER_METER;
	final static double WINDOW_HEIGHT_M = ((double)WINDOW_HEIGHT_PX) / PIXELS_PER_METER;
	

	@Test
	public void testSwerveDrive2()
	{
		
        try
		{
			robotBaseIcon = ImageIO.read(getClass().getResource("assets/robot-base.png")); //must be square
			wheelIcon = ImageIO.read(getClass().getResource("assets/wheel.png")); 
			wheelbaseRadiusM = (robotBaseIcon.getWidth() / 2.0) / PIXELS_PER_METER; 
			momentOfInertia = 3 * ROBOT_MASS_KG * RobotMath.square(wheelbaseRadiusM) / 2;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
        
        // ----------------------------------------------------
		
		frame = new JFrame();
		
        frame.setLayout(new BorderLayout());
        frame.setPreferredSize(new Dimension(WINDOW_WIDTH_PX, WINDOW_HEIGHT_PX));
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        panel = new SwerveSimulatorPanel();
        frame.add(panel, BorderLayout.CENTER);
            	        
        frame.pack();
        frame.setVisible(true);
        
        windowOpen = true;
        
        frame.addWindowListener(new WindowCloseListener());
        
        //we want the simulation window for the Joystick, but not the motors
        EmulatorMain.enableGUI = true;
		
        listenerManager = new ListenerManager(new Joystick(0));
        
        EmulatorMain.enableGUI = false;
        // ----------------------------------------------------
        
		modules.add(new FakeModule(0, 30));
		modules.add(new FakeModule(1, 150));
		modules.add(new FakeModule(2, 270));
		
        while(windowOpen)
        {
        	tickSimulation(20);
        	
        	panel.repaint();
        	
        	try
			{
				Thread.sleep(20);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
        }

	}
	
	void tickSimulation(int millisSinceLastTick)
	{
		listenerManager.tick();
		
		//update swerve drive class
		drive.drive(listenerManager.getRawAxis(ControllerSimXbox.JOY1X), -listenerManager.getRawAxis(ControllerSimXbox.JOY1Y), listenerManager.getRawAxis(ControllerSimXbox.SIM_WINDOW_Z));		
		
		//update fake swerve modules
		for(FakeModule module : modules)
		{
			module.update(millisSinceLastTick);
		}
		
		//update physics
		//-----------------------------------
		double netForceX = 0, netForceY = 0; //in Newtons
		
		netTorque = 0; // in Nm
		
		//detect wheel direction to motion direction mismatch
		double motionDirectionDeg;
		if(robotSpeedX == 0)
		{
			motionDirectionDeg = 0;
		}
		else	
		{
			motionDirectionDeg = RobotMath.rTD(Math.tan(robotSpeedY/robotSpeedX));
		}
		
		double secondsSinceLastTick = ((double)millisSinceLastTick) / 1000;
		
		for(FakeModule module : modules)
		{
			double driveWheelAngle = RobotMath.normalizeAngle(module.simulatedAngleDegrees + robotRotationDegrees);

			double driveWheelAngleRad = RobotMath.dTR(driveWheelAngle);
			
			netForceX += Math.cos(driveWheelAngleRad) * module.driveWheelForce;
			netForceY += Math.sin(driveWheelAngleRad) * module.driveWheelForce;
			
			//not sure what the actual equation for this is
			netForceX -= WHEEL_NON_TURNING_FRICTION_FACTOR * robotSpeedX * Math.abs(Math.sin(RobotMath.dTR(Math.abs(driveWheelAngle - motionDirectionDeg))));
			netForceY -= WHEEL_NON_TURNING_FRICTION_FACTOR * robotSpeedY * Math.abs(Math.sin(RobotMath.dTR(Math.abs(driveWheelAngle - motionDirectionDeg))));
			
			netTorque += -1 * Math.sin(driveWheelAngleRad - RobotMath.dTR(module.wheelbaseAngle)) * module.driveWheelForce;
		}
		
		
		double robotAccelerationX = netForceX / ROBOT_MASS_KG;
		double robotAccelerationY = netForceY / ROBOT_MASS_KG;
		
		netForceX += -1 * INTERNAL_FRICTION * RobotMath.square(robotSpeedX) * RobotMath.sgn(robotSpeedX);
		netForceY += -1 * INTERNAL_FRICTION * RobotMath.square(robotSpeedY) * RobotMath.sgn(robotSpeedY);
		
		//netTorque += -1 * INTERNAL_ROTATIONAL_FRICTION * RobotMath.square(robotRotationalSpeed) * RobotMath.sgn(robotRotationalSpeed);
		
		robotSpeedX += robotAccelerationX * secondsSinceLastTick;
		robotSpeedY += robotAccelerationY * secondsSinceLastTick;
		
		double robotRotationalAcceleration = netTorque / momentOfInertia;
		robotRotationalSpeed += robotRotationalAcceleration * secondsSinceLastTick;
		robotRotationDegrees += robotRotationalSpeed * secondsSinceLastTick;
		
		//Log.debug("SwerveDrive2Test", "X Acceleration: " + robotAccelerationX + ", Y Acceleration: " + robotAccelerationY);
		
		double newPositionX = robotPositionX + robotSpeedX * secondsSinceLastTick;
		double newPositionY = robotPositionY + robotSpeedY * secondsSinceLastTick;
		
		//detect collision with wall
		if((newPositionX - wheelbaseRadiusM) < 0 || (newPositionX + wheelbaseRadiusM) > WINDOW_WIDTH_M)
		{
			robotSpeedX *= -.5;
		}
		else
		{
			robotPositionX = newPositionX;
		}
		
		if((newPositionY - wheelbaseRadiusM) < 0 || (newPositionY + wheelbaseRadiusM) > WINDOW_HEIGHT_M)
		{
			robotSpeedY *= -.5;
		}
		else
		{
			robotPositionY = newPositionY;
		}
		


	}
	
	@Ignore
	class SwerveSimulatorPanel extends JPanel
	{	
		int halfRobotHeight, halfRobotWidth;
		
		int wheelbaseRadiusPx;
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -4090860471315258373L;
		
		public SwerveSimulatorPanel()
		{
			halfRobotHeight = robotBaseIcon.getHeight() / 2;
			halfRobotWidth = robotBaseIcon.getWidth() / 2;
			
			wheelbaseRadiusPx = robotBaseIcon.getWidth() / 2 - 20;
		}
		
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			
			int robotCenterPxX = (int)(robotPositionX * PIXELS_PER_METER);
			int robotCenterPxY = (int)(robotPositionY * PIXELS_PER_METER);

			Graphics2D g2d = (Graphics2D) g;
			
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			//draw robot
			
			AffineTransform robotTransform = new AffineTransform();
			robotTransform.rotate(RobotMath.dTR(robotRotationDegrees), robotCenterPxX, robotCenterPxY);
			AffineTransform oldTransform = g2d.getTransform();
			g2d.transform(robotTransform);

			g2d.drawImage(robotBaseIcon, robotCenterPxX - halfRobotWidth, robotCenterPxY - halfRobotHeight, null);

			g2d.setTransform(oldTransform);
			
			for(int index = 0; index < modules.size(); ++index)
			{
				FakeModule currentModule = modules.get(index);
				g2d.drawString(String.format("Module %d: Drive Speed (RPM): %f, Heading Offset (deg): %f", index, currentModule.driveWheelSpeed, currentModule.simulatedAngleDegrees + 90), 0, index * 20 + 20);
			}
			
			g2d.drawString("Robot rotation: " + robotRotationDegrees + " deg Net Torque: " + netTorque + " Nm", 0, 80);
			g2d.drawString("X Speed: " + robotSpeedX + " Y Speed: " + netTorque, 0, 100);

			
			final int wheelWidth = 25;
			final int wheelHeight = 6;
			
			//draw modules
			for(FakeModule module : modules)
			{

							
				//from http://stackoverflow.com/questions/8807717/java-rotate-rectangle-around-the-center
				AffineTransform transform = new AffineTransform();
				transform.rotate(RobotMath.dTR(robotRotationDegrees), robotCenterPxX, robotCenterPxY);
				
				int pixelOffsetX = RobotMath.floor_double_int(wheelbaseRadiusPx * Math.cos(RobotMath.dTR(module.wheelbaseAngle + robotRotationDegrees)));
				int pixelOffsetY = RobotMath.floor_double_int(wheelbaseRadiusPx * Math.sin(RobotMath.dTR(module.wheelbaseAngle + robotRotationDegrees)));
				
				transform.rotate(RobotMath.dTR(module.simulatedAngleDegrees + robotRotationDegrees), robotCenterPxX + pixelOffsetX, robotCenterPxY + pixelOffsetY);
				oldTransform = g2d.getTransform();
				g2d.transform(transform);

				g2d.drawImage(wheelIcon, robotCenterPxX + pixelOffsetX - (wheelWidth/2), robotCenterPxY + pixelOffsetY - (wheelHeight/2), null);
				
				g2d.setTransform(oldTransform);
			}
		}
	}
	
	@Ignore
	class WindowCloseListener extends WindowAdapter
	{
		
		@Override
		public void windowClosed(WindowEvent e)
		{
			windowOpen = false;	
		}
	}
}
