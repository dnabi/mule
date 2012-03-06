/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.mule.api.MuleContext;
import org.mule.api.config.MuleProperties;
import org.mule.api.registry.MuleRegistry;
import org.mule.api.transformer.TransformerException;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;
import org.mule.transformer.types.DataTypeFactory;

import org.junit.Test;
import org.mockito.Mockito;

@SmallTest
public class TransformerSourceTypeEnforcementTestCase extends AbstractMuleTestCase
{

    private MuleContext muleContext = mock(MuleContext.class);
    private MuleRegistry registry = mock(MuleRegistry.class);

    @Test
    public void ignoresBadInputIfEnforcementOff() throws TransformerException
    {
        AbstractTransformer transformer = createDummyTransformer(true);

        setTransformationEnforcement(false);

        Object result = transformer.transform("TEST");
        assertEquals("TEST", result);

        Mockito.verify(registry, times(1)).get(MuleProperties.TRANSFORMATION_ENFORCE);
    }

    @Test
    public void rejectsBadInputIfEnforcementOn() throws TransformerException
    {
        AbstractTransformer transformer = createDummyTransformer(true);

        setTransformationEnforcement(true);

        try
        {
            transformer.transform("TEST");
            fail("Transformation should fail because source type is not supported");
        }
        catch (TransformerException expected)
        {
        }

        Mockito.verify(registry, times(1)).get(MuleProperties.TRANSFORMATION_ENFORCE);
    }

    @Test
    public void rejectsBadInputUsingDefaultEnforcement() throws TransformerException
    {
        AbstractTransformer transformer = createDummyTransformer(true);
        when(muleContext.getRegistry()).thenReturn(registry);

        try
        {
            transformer.transform("TEST");
            fail("Transformation should fail because source type is not supported");
        }
        catch (TransformerException expected)
        {
        }

        Mockito.verify(registry, times(1)).get(MuleProperties.TRANSFORMATION_ENFORCE);
    }

    @Test
    public void transformsValidSourceTypeWithNoCheckForEnforcement() throws TransformerException
    {
        AbstractTransformer transformer = createDummyTransformer(true);
        transformer.sourceTypes.add(DataTypeFactory.STRING);
        transformer.returnType = DataTypeFactory.STRING;

        when(muleContext.getRegistry()).thenReturn(registry);

        Object result = transformer.transform("TEST");
        assertEquals("TRANSFORMED", result);

        Mockito.verify(registry, times(0)).get(MuleProperties.TRANSFORMATION_ENFORCE);
    }

    private void setTransformationEnforcement(boolean enforce)
    {
        when(registry.get(MuleProperties.TRANSFORMATION_ENFORCE)).thenReturn(Boolean.toString(enforce));
        when(muleContext.getRegistry()).thenReturn(registry);
    }

    private AbstractTransformer createDummyTransformer(boolean ignoreBadInput)
    {
        AbstractTransformer result = new AbstractTransformer()
        {

            @Override
            protected Object doTransform(Object src, String enc) throws TransformerException
            {
                return "TRANSFORMED";
            }
        };

        result.sourceTypes.add(DataTypeFactory.BYTE_ARRAY);
        result.setMuleContext(muleContext);
        result.setIgnoreBadInput(ignoreBadInput);

        return result;
    }
}