/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.construct.builder;

import org.mule.MessageExchangePattern;
import org.mule.construct.Bridge;
import org.mule.exception.DefaultServiceExceptionStrategy;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.transformer.compression.GZipCompressTransformer;
import org.mule.transformer.simple.ObjectToByteArray;
import org.mule.transformer.simple.StringAppendTransformer;

public class BridgeBuilderTestCase extends AbstractMuleTestCase
{
    public void testFullConfiguration() throws Exception
    {
        Bridge bridge = new BridgeBuilder().name("test-bridge-full")
            .inboundAddress("test://foo.in")
            .transformers(new StringAppendTransformer("bar"))
            .responseTransformers(new ObjectToByteArray(), new GZipCompressTransformer())
            .outboundAddress("test://foo.out")
            .exchangePattern(MessageExchangePattern.REQUEST_RESPONSE)
            .transacted(false)
            .exceptionStrategy(new DefaultServiceExceptionStrategy(muleContext))
            .build(muleContext);

        assertEquals("test-bridge-full", bridge.getName());
    }

    public void testTransacted() throws Exception
    {
        Bridge bridge = new BridgeBuilder().name("test-bridge-transacted")
            .inboundAddress("test://foo.in")
            .outboundAddress("test2://foo.out")
            .transacted(true)
            .build(muleContext);

        assertEquals("test-bridge-transacted", bridge.getName());
    }
}