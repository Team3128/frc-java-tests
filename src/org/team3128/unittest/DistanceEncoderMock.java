package org.team3128.unittest;

import org.team3128.hardware.encoder.distance.IDistanceEncoder;

public class DistanceEncoderMock implements IDistanceEncoder
{
	public double distanceInDegrees  = 0;
	
	@Override
	public void clear()
	{
		distanceInDegrees = 0;
	}

	@Override
	public double getDistanceInDegrees()
	{
		return distanceInDegrees;
	}

}
