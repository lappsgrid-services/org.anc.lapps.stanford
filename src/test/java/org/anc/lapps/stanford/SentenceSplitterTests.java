package org.anc.lapps.stanford;

import org.anc.resource.ResourceLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import org.lappsgrid.api.WebService;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Constants;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Keith Suderman
 */
public class SentenceSplitterTests
{
	private WebService service;

	public SentenceSplitterTests()
	{

	}

	@Before
	public void setup()
	{
		service = new SentenceSplitter();
	}

	@After
	public void cleanup()
	{
		service = null;
	}

	@Ignore
	public void sentenceSplitterTest() throws IOException
	{
		String text = ResourceLoader.loadString("Bartok.txt");
		String input = DataFactory.text(text);
		String json = service.execute(input);

		assertNotNull("Service returned null", json);
		Data data = Serializer.parse(json, Data.class);
		assertNotNull("Unable to parse response.", data);
		assertFalse(data.getPayload().toString(), TestUtils.isError(data));
		assertTrue("Invalid return type: " + data.getDiscriminator(), TestUtils.isa(data, Constants.Uri.JSON_LD));

		Container container = new Container((Map)data.getPayload());
		List<View> views = container.getViews();
		// There should be two views: one view with tokens and a second with sentences.
		assertTrue("Wrong number of views. Expected 1 found " + views.size(), views.size() == 1);

		views = container.findViewsThatContain(Constants.Uri.TOKEN);
		assertTrue("Wrong number of token views. Expected = 0 found " + views.size(), views.size() == 0);

		views = container.findViewsThatContain(Constants.Uri.SENTENCE);
		assertTrue("Wrong number of sentence views. Expected 1 found " + views.size(), views.size() == 1);

		View view = views.get(0);
		List<Annotation> annotations = view.getAnnotations();
		assertTrue("No sentence annotations.", annotations.size() > 0);
	}

	@Test
	public void testMetadata()
	{
		Data<Void> command = new Data<Void>(Constants.Uri.GETMETADATA);
		String json = service.execute(command.asJson());
		assertNotNull("No metadata returned.", json);

		Data data = Serializer.parse(json, Data.class);
		assertNotNull("Unable to parse response.", data);

		assertFalse(data.getPayload().toString(), TestUtils.isError(data));
		TestUtils.check(Constants.Uri.META, data.getDiscriminator());

		ServiceMetadata metadata = Serializer.parse(data.getPayload().toString(), ServiceMetadata.class);
		assertNotNull("Unable to parse metadata.", metadata);

		TestUtils.check("http://www.anc.org", metadata.getVendor());
		TestUtils.check(Version.getVersion(), metadata.getVersion());
		TestUtils.check(SentenceSplitter.class.getName(), metadata.getName());
//		System.out.println(data.getPayload().getClass().getName());
		//ServiceMetadata metadata = new ServiceMetadata((Map)data.getPayload());
	}
}
