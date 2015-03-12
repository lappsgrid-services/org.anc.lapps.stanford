package org.anc.lapps.stanford;

import org.lappsgrid.discriminator.Constants;
import org.lappsgrid.serialization.Data;

import static org.junit.Assert.fail;

/**
 * @author Keith Suderman
 */
public class TestUtils
{
	private TestUtils()
	{

	}

	public static void check(String expected, String actual)
	{
		if (!actual.equals(expected))
		{
			String message = String.format("Expected: %s Found %s", expected, actual);
			fail(message);
		}
	}

	public static boolean isError(Data<?> data)
	{
		return isError(data.getDiscriminator());
	}

	public static boolean isError(String url)
	{
		return Constants.Uri.ERROR.equals(url);
	}

	public static boolean isa(Data<?> data, String type)
	{
		return isa(data.getDiscriminator(), type);
	}

	public static boolean isa(String candidate, String type)
	{
		return type.equals(candidate);
	}

}
