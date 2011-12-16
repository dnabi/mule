/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.expression;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.api.expression.ExpressionRuntimeException;
import org.mule.tck.AbstractMuleTestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;

public class AttachmentsExpressionEvaluatorTestCase extends AbstractMuleTestCase
{
    private MuleMessage message;

    @Override
    protected void doSetUp() throws Exception
    {
        message = new DefaultMuleMessage("test");

        try
        {
            message.addAttachment("foo", new DataHandler(new StringDataSource("moo")));
            message.addAttachment("bar", new DataHandler(new StringDataSource("mar")));
            message.addAttachment("baz", new DataHandler(new StringDataSource("maz")));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testSingleAttachment() throws Exception
    {
        MessageAttachmentExpressionEvaluator eval = new MessageAttachmentExpressionEvaluator();

        // Value required + found
        Object result = eval.evaluate("foo", message);
        assertNotNull(result);
        assertTrue(result instanceof DataHandler);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
        ((DataHandler)result).writeTo(baos);
        assertEquals("moo", baos.toString());
        
        // Value not required + found
        result = eval.evaluate("foo*", message);
        assertNotNull(result);
        assertTrue(result instanceof DataHandler);
        baos = new ByteArrayOutputStream(4);
        ((DataHandler)result).writeTo(baos);
        assertEquals("moo", baos.toString());
        
        // Value not required + not found
        result = eval.evaluate("fool*", message);
        assertNull(result);

        // Value required + not found (throws exception)
        try
        {
            result = eval.evaluate("fool", message);
            fail("required value");
        }
        catch (Exception e)
        {
            //Expected
        }
    }

    public void testMapAttachments() throws Exception
    {
        MessageAttachmentsExpressionEvaluator eval = new MessageAttachmentsExpressionEvaluator();

        // Value required + found
        Object result = eval.evaluate("foo, baz", message);
        assertNotNull(result);
        assertTrue(result instanceof Map);
        assertEquals(2, ((Map)result).size());

        assertNotNull(((Map)result).get("foo"));
        assertTrue(((Map)result).get("foo") instanceof DataHandler);
        DataHandler dh = (DataHandler)((Map)result).get("foo");
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
        dh.writeTo(baos);
        assertEquals("moo", baos.toString());

        assertNotNull(((Map)result).get("baz"));
        assertTrue(((Map)result).get("baz") instanceof DataHandler);
        dh = (DataHandler)((Map)result).get("baz");
        baos = new ByteArrayOutputStream(4);
        dh.writeTo(baos);
        assertEquals("maz", baos.toString());

        // Value not required + found
        result = eval.evaluate("foo*, baz", message);
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        assertNotNull(((Map)result).get("foo"));
        assertTrue(((Map)result).get("foo") instanceof DataHandler);
        dh = (DataHandler)((Map)result).get("foo");
        baos = new ByteArrayOutputStream(4);
        dh.writeTo(baos);
        assertEquals("moo", baos.toString());

        assertNotNull(((Map)result).get("baz"));
        assertTrue(((Map)result).get("baz") instanceof DataHandler);
        dh = (DataHandler)((Map)result).get("baz");
        baos = new ByteArrayOutputStream(4);
        dh.writeTo(baos);
        assertEquals("maz", baos.toString());        
        
        // Value not required + not found
        result = eval.evaluate("fool*", message);
        assertNull(result);

        // Value required + found
        try
        {
            result = eval.evaluate("fool", message);
            fail("required value");
        }
        catch (Exception e)
        {
            //Expected
        }

        assertEquals(3, eval.evaluate("{count}", message));

    }

    public void testListAttachments() throws Exception
    {
        MessageAttachmentsListExpressionEvaluator eval = new MessageAttachmentsListExpressionEvaluator();

        // Value required + found
        Object result = eval.evaluate("foo, baz", message);
        assertNotNull(result);
        assertTrue(result instanceof List);
        assertEquals(2, ((List)result).size());

        assertTrue(((List)result).get(0) instanceof DataHandler);
        DataHandler dh = (DataHandler)((List)result).get(0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
        dh.writeTo(baos);
        assertEquals("moo", baos.toString());

        assertTrue(((List)result).get(1) instanceof DataHandler);
        dh = (DataHandler)((List)result).get(1);
        baos = new ByteArrayOutputStream(4);
        dh.writeTo(baos);
        assertEquals("maz", baos.toString());

        // Value not required + found
        result = eval.evaluate("foo*, baz", message);
        assertNotNull(result);
        assertTrue(result instanceof List);
        assertEquals(2, ((List)result).size());

        assertTrue(((List)result).get(0) instanceof DataHandler);
        dh = (DataHandler)((List)result).get(0);
        baos = new ByteArrayOutputStream(4);
        dh.writeTo(baos);
        assertEquals("moo", baos.toString());

        assertTrue(((List)result).get(1) instanceof DataHandler);
        dh = (DataHandler)((List)result).get(1);
        baos = new ByteArrayOutputStream(4);
        dh.writeTo(baos);
        assertEquals("maz", baos.toString());
        
        // Value not required + not found
        result = eval.evaluate("fool*", message);
        assertNull(result);

        // Value required + not found (throws exception)
        try
        {
            result = eval.evaluate("fool", message);
            fail("required value");
        }
        catch (Exception e)
        {
            //Expected
        }
    }

    public void testSingleAttachmentUsingManager() throws Exception
    {
        // Value required + found
        Object result = muleContext.getExpressionManager().evaluate("#[attachment:foo]", message);
        assertNotNull(result);
        assertTrue(result instanceof DataHandler);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
        ((DataHandler)result).writeTo(baos);
        assertEquals("moo", baos.toString());

        // Value not required + found
        result = muleContext.getExpressionManager().evaluate("#[attachment:foo*]", message);
        assertNotNull(result);
        assertTrue(result instanceof DataHandler);
        baos = new ByteArrayOutputStream(4);
        ((DataHandler)result).writeTo(baos);
        assertEquals("moo", baos.toString());        
                
        // Value not required + not found
        result = muleContext.getExpressionManager().evaluate("#[attachment:fool*]", message);
        assertNull(result);

        // Value required + not found (throws exception)
        try
        {
            result = muleContext.getExpressionManager().evaluate("#[attachment:fool]", message);
            fail("Required value");
        }
        catch (ExpressionRuntimeException e)
        {
            //expected
        }
    }

    public void testMapAttachmentsUsingManager() throws Exception
    {
        // Value required + found
        Object result = muleContext.getExpressionManager().evaluate("#[attachments:foo, baz]", message);
        assertNotNull(result);
        assertTrue(result instanceof Map);
        assertEquals(2, ((Map)result).size());

        assertNotNull(((Map)result).get("foo"));
        assertTrue(((Map)result).get("foo") instanceof DataHandler);
        DataHandler dh = (DataHandler)((Map)result).get("foo");
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
        dh.writeTo(baos);
        assertEquals("moo", baos.toString());

        assertNotNull(((Map)result).get("baz"));
        assertTrue(((Map)result).get("baz") instanceof DataHandler);
        dh = (DataHandler)((Map)result).get("baz");
        baos = new ByteArrayOutputStream(4);
        dh.writeTo(baos);
        assertEquals("maz", baos.toString());

        // Value not required + found
        result = muleContext.getExpressionManager().evaluate("#[attachments:foo*, baz]", message);
        assertNotNull(result);
        assertTrue(result instanceof Map);
        assertEquals(2, ((Map)result).size());

        assertNotNull(((Map)result).get("foo"));
        assertTrue(((Map)result).get("foo") instanceof DataHandler);
        dh = (DataHandler)((Map)result).get("foo");
        baos = new ByteArrayOutputStream(4);
        dh.writeTo(baos);
        assertEquals("moo", baos.toString());

        assertNotNull(((Map)result).get("baz"));
        assertTrue(((Map)result).get("baz") instanceof DataHandler);
        dh = (DataHandler)((Map)result).get("baz");
        baos = new ByteArrayOutputStream(4);
        dh.writeTo(baos);
        assertEquals("maz", baos.toString());
        
        // Value not required + not found
        result = muleContext.getExpressionManager().evaluate("#[attachments:fool*]", message);
        assertNull(result);

        // Value required + not found (throws exception)
        try
        {
            result = muleContext.getExpressionManager().evaluate("#[attachments:fool]", message);
            fail("Required value");
        }
        catch (ExpressionRuntimeException e)
        {
            //expected
        }
        assertEquals(3, muleContext.getExpressionManager().evaluate("#[attachments:{count}]", message));

    }

    public void testListAttachmentsUsingManager() throws Exception
    {
        // Value required + found
        Object result = muleContext.getExpressionManager().evaluate("#[attachments-list:foo,baz]", message);
        assertNotNull(result);
        assertTrue(result instanceof List);
        assertEquals(2, ((List)result).size());

        assertTrue(((List)result).get(0) instanceof DataHandler);
        DataHandler dh = (DataHandler)((List)result).get(0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
        dh.writeTo(baos);
        assertEquals("moo", baos.toString());

        assertTrue(((List)result).get(1) instanceof DataHandler);
        dh = (DataHandler)((List)result).get(1);
        baos = new ByteArrayOutputStream(4);
        dh.writeTo(baos);
        assertEquals("maz", baos.toString());

        // Value not required + found
        result = muleContext.getExpressionManager().evaluate("#[attachments-list:foo*,baz]", message);
        assertNotNull(result);
        assertTrue(result instanceof List);
        assertEquals(2, ((List)result).size());

        assertTrue(((List)result).get(0) instanceof DataHandler);
        dh = (DataHandler)((List)result).get(0);
        baos = new ByteArrayOutputStream(4);
        dh.writeTo(baos);
        assertEquals("moo", baos.toString());

        assertTrue(((List)result).get(1) instanceof DataHandler);
        dh = (DataHandler)((List)result).get(1);
        baos = new ByteArrayOutputStream(4);
        dh.writeTo(baos);
        assertEquals("maz", baos.toString());
                
        // Value not required + not found
        result = muleContext.getExpressionManager().evaluate("#[attachments-list:fool*]", message);
        assertNull(result);

        // Value required + not found (throws exception)
        try
        {
            result = muleContext.getExpressionManager().evaluate("#[attachments-list:fool]", message);
            fail("Required value");
        }
        catch (ExpressionRuntimeException e)
        {
            //expected
        }
    }

    // silly little fake DataSource so that we don't need to use javamail
    protected static class StringDataSource implements DataSource
    {
        protected String content;

        public StringDataSource(String payload)
        {
            super();
            content = payload;
        }

        public InputStream getInputStream() throws IOException
        {
            return new ByteArrayInputStream(content.getBytes());
        }

        public OutputStream getOutputStream()
        {
            throw new UnsupportedOperationException("Read-only javax.activation.DataSource");
        }

        public String getContentType()
        {
            return "text/plain";
        }

        public String getName()
        {
            return "StringDataSource";
        }
    }
}