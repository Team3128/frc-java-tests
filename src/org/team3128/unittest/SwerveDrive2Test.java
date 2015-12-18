package org.team3128.unittest;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.Ignore;
import org.junit.Test;
import org.team3128.Log;
import org.team3128.drive.SwerveDrive2;
import org.team3128.hardware.motor.MotorGroup;
import org.team3128.listener.ListenerManager;
import org.team3128.listener.controller.ControllerSimXbox;
import org.team3128.util.RobotMath;
import org.team3128.util.Units;

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
		
		//degree offset from straight ahead
		double simulatedHeadingDegrees = 0;
		
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
		
		private static final double WHEEL_CIRCUMFERENCE = 5 * Units.cm * Math.PI;
		
		private static final double TURNING_MOTOR_SPEED_DPS = 180; //degrees per second
		
		//should be executed after the swerve drive updates
		public void update(double timeBetweenUpdates)
		{
			driveWheelSpeed = (RobotMath.getCIMExpectedRPM(driveMotor.get()) / 10) * WHEEL_CIRCUMFERENCE;
			
			driveWheelForce = DRIVE_MOTOR_FORCE * driveMotor.get();
			
			double turningAngularSpeed = TURNING_MOTOR_SPEED_DPS  * turnMotor.get();
			
			double distanceTurned = turningAngularSpeed * timeBetweenUpdates;
			
			simulatedHeadingDegrees += distanceTurned;
			
			fakeEncoder.distanceInDegrees += distanceTurned;
			
			if(Math.abs(simulatedHeadingDegrees) < 1)
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
	
	final static double kP = .001, kI = 0, kD = 0;
	final static double ROBOT_MASS_KG = 100;
	final static double DRIVE_MOTOR_FORCE = 400; //N
	
	SwerveDrive2 drive = new SwerveDrive2();
	
	JFrame frame;
	
	SwerveSimulatorPanel panel;
	
	ListenerManager listenerManager;
	
	BufferedImage robotBaseIcon;
	
	boolean windowOpen;
	
	double robotSpeedX = 0, robotSpeedY = 0; // in m/s
	
	double robotPositionX = WINDOW_WIDTH_M / 2, robotPositionY = WINDOW_HEIGHT_M / 2; //in m from top left
	
	final static int PIXELS_PER_METER = 300;

	final static int WINDOW_WIDTH_PX = 1920;
	final static int WINDOW_HEIGHT_PX = 1080;

	final static double WINDOW_WIDTH_M = WINDOW_WIDTH_PX / PIXELS_PER_METER;
	final static double WINDOW_HEIGHT_M = WINDOW_HEIGHT_PX / PIXELS_PER_METER;
	

	@Test
	public void testSwerveDrive2()
	{
		
        try
		{
			robotBaseIcon = ImageIO.read(getClass().getResource("assets/robot-base.png"));
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
		
        listenerManager = new ListenerManager(new Joystick(0), ControllerSimXbox.instance);
        
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
		drive.drive(listenerManager.getRawAxis(ControllerSimXbox.JOY1X), listenerManager.getRawAxis(ControllerSimXbox.JOY1Y), listenerManager.getRawAxis(ControllerSimXbox.JOY2X));		
		
		//update fake swerve modules
		for(FakeModule module : modules)
		{
			module.update(millisSinceLastTick);
		}
		
		//update physics
		//-----------------------------------
		double netForceX = 0, netForceY = 0; //in Newtons
		
		for(FakeModule module : modules)
		{
			double driveWheelAngleRad = RobotMath.dTR(module.simulatedHeadingDegrees - 90);
			
			netForceX += Math.cos(driveWheelAngleRad) * module.driveWheelForce;
			netForceY += Math.sin(driveWheelAngleRad) * module.driveWheelForce;
		}
		
		double robotAccelerationX = netForceX / ROBOT_MASS_KG;
		double robotAccelerationY = netForceY / ROBOT_MASS_KG;
		
		double secondsSinceLastTick = ((double)millisSinceLastTick) / 1000;
		
		robotSpeedX += robotAccelerationX * secondsSinceLastTick;
		robotSpeedY += robotAccelerationY * secondsSinceLastTick;
		
		Log.debug("SwerveDrive2Test", "X Acceleration: " + robotAccelerationX + ", Y Acceleration: " + robotAccelerationY);
		
		robotPositionX += robotSpeedX * secondsSinceLastTick;
		robotPositionY += robotSpeedY * secondsSinceLastTick;
	}
	
	@Ignore
	class SwerveSimulatorPanel extends JPanel
	{	
		int halfRobotHeight, halfRobotWidth;

		/**
		 * 
		 */
		private static final long serialVersionUID = -4090860471315258373L;
		
		public SwerveSimulatorPanel()
		{
			halfRobotHeight = robotBaseIcon.getHeight() / 2;
			halfRobotWidth = robotBaseIcon.getWidth() / 2;
		}
		
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			
			Graphics2D g2d = (Graphics2D) g;
			
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			//draw robot
			g2d.drawImage(robotBaseIcon, ((int)(robotPositionX * PIXELS_PER_METER)) - halfRobotWidth, ((int)(robotPositionY * PIXELS_PER_METER)) - halfRobotWidth, null);
			
			for(int index = 0; index < modules.size(); ++index)
			{
				FakeModule currentModule = modules.get(index);
				g2d.drawString(String.format("Module %d: Drive Speed (RPM): %f, Heading Offset: %f", index, currentModule.driveWheelSpeed, currentModule.simulatedHeadingDegrees), 0, index * 20);
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
