/*
 * The MIT License
 *
 * Copyright 2015 doe300.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.doe300.activerecord.record.bean;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestSuite;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author doe300
 */
@RunWith(Parameterized.class)
public class JavaBeanRecordTest extends Assert
{
	private RecordBase<? extends TestJavaBeanRecord> base;
	private static TestPropertyChangeListener listener;
	private static RecordCore core;
	
	
	public JavaBeanRecordTest(Class<? extends TestJavaBeanRecord> type)
	{
		base = core.getBase( type, new PropertyChangeProxyHandler(Collections.singleton( listener)));
	}
	
	@BeforeClass
	public static void setUpClass() throws Exception
	{
		listener = new TestPropertyChangeListener();
		core = RecordCore.fromDatabase( TestSuite.con, true);
		core.createTable( TestJavaBeanRecord.class);
	}
	
	@Parameterized.Parameters
	public static Collection<Object[]> getParameters() throws Exception
	{
		return Arrays.asList(
			new Object[]{TestJavaBeanRecord.class},
			new Object[]{TestJavaBeanPOJO.class}
		);
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception
	{
		core.dropTable( TestJavaBeanRecord.class);
	}

	@Test
	public void testAddRemoveListeners()
	{
		int count = listener.getCount();
		TestJavaBeanRecord r = base.createRecord();
		r.removePropertyChangeListener( listener );
		r.setAge( 23);
		assertEquals( count, listener.getCount());
		r.addPropertyChangeListener( listener );
		r.setAge( 24);
		assertEquals( count+1, listener.getCount());
	}

	@Test
	public void testSetAttributeHook()
	{
		int count = listener.getCount();
		TestJavaBeanRecord r = base.createRecord();
		r.addPropertyChangeListener( listener );
		r.setName( "Adam");
		r.setAge( 112);
		assertEquals(count + 2, listener.getCount());
	}
	
}
