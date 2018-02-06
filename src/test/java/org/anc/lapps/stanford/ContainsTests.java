/*
 * Copyright (c) 2018 The American National Corpus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.anc.lapps.stanford;

import org.junit.Ignore;
import org.junit.Test;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Contains;
import org.lappsgrid.serialization.lif.View;

/**
 *
 */
@Ignore
public class ContainsTests
{
	@Test
	public void contains()
	{
		View view = new View();
		Contains contains = view.addContains("T", "producer", "type");
		contains.put("namedEntityCategorySet", "stanfordv2");
		System.out.println(Serializer.toPrettyJson(contains));
		System.out.println(Serializer.toPrettyJson(view.getMetadata()));
	}
}
