package org.team3128.unittest;

import org.junit.Ignore;
import org.team3128.common.hardware.encoder.distance.IDistanceEncoder;

@Ignore
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
