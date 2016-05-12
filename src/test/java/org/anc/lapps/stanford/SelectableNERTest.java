package org.anc.lapps.stanford;

import org.junit.Test;
import org.lappsgrid.api.WebService;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;

import static org.lappsgrid.discriminator.Discriminators.Uri;

/**
 * @author Keith Suderman
 */
public class SelectableNERTest
{
	public SelectableNERTest()
	{

	}

	@Test
	public void testAll3()
	{
		WebService tokenizer = new Tokenizer();
		WebService tagger = new Tagger();
		WebService ner = new NamedEntityRecognizer();
		Data data = new Data<>();
		data.setDiscriminator(Uri.LAPPS);
		Container container = new Container();
		container.setText("Goodbye cruel world I am leaving you today.");
		data.setPayload(container);
		String json = tokenizer.execute(data.asJson());
		json = tagger.execute(json);
		data = Serializer.parse(json, Data.class);
		data.setParameter("classifier", SelectableNamedEntityRecognizer.CONLL);
		json = ner.execute(data.asJson());
		data = Serializer.parse(json, Data.class);
		System.out.println(data.asPrettyJson());
	}
}
