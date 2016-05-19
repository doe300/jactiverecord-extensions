/*
 * The MIT License
 *
 * Copyright 2016 doe300.
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
package de.doe300.activerecord.record.javafx;

import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.TestSuite;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author doe300
 */
public class AttributePropertyTest extends Assert implements ChangeListener<String>
{
	private static RecordCore core;
	private static TestPropertyRecord record;
	private static AttributeProperty<String> nameProperty;
	private static AttributeProperty<Integer> ageProperty;
	
	private String changedValue;
	
	public AttributePropertyTest()
	{
		
	}
	
	@BeforeClass
	public static void setUpClass() throws Exception
	{
		core = RecordCore.fromDatabase( TestSuite.con, true);
		core.createTable( TestPropertyRecord.class);
		
		record = core.getBase( TestPropertyRecord.class).createRecord();
		
		nameProperty = new AttributeProperty<>(record, "name", String.class);
		ageProperty = new AttributeProperty<>(record, "age", Integer.class);
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception
	{
		core.dropTable( TestPropertyRecord.class);
	}

	@Test
	public void testBind()
	{
		record.setName( "Adam");
		assertEquals( "Adam", nameProperty.get());
		
		final Property<String> boundProp = new SimpleStringProperty();
		nameProperty.bind( boundProp );
		
		boundProp.setValue( "Eve");
		assertEquals( "Eve", nameProperty.get());
		assertEquals( "Eve", record.getName());
		
		nameProperty.unbind();
	}

	@Test
	public void testUnbind()
	{
		record.setAge( 23);
		assertEquals( 23, record.getAge().intValue());
		
		final Property<Integer>boundProp = new SimpleObjectProperty<>();
		ageProperty.bind( boundProp );
		
		boundProp.setValue( 12);
		assertEquals( 12, ageProperty.get().intValue());
		assertEquals( 12, record.getAge().intValue() );
		
		ageProperty.unbind();
		
		boundProp.setValue( 112);
		
		assertNotEquals( 112, ageProperty.get().intValue());
		assertNotEquals( 112, record.getAge().intValue());
	}

	@Test
	public void testIsBound()
	{
		assertFalse( ageProperty.isBound());
		
		final Property<Integer>boundProp = new SimpleObjectProperty<>();
		ageProperty.bind( boundProp );
		
		assertTrue( ageProperty.isBound());
		
		ageProperty.unbind();
		
		assertFalse( ageProperty.isBound());
	}

	@Test
	public void testGetBean()
	{
		assertEquals( record, ageProperty.getBean());
	}

	@Test
	public void testGetName()
	{
		assertEquals( "name", nameProperty.getName());
		assertEquals( "age", ageProperty.getName());
	}

	@Test
	public void testChangeListener()
	{
		changedValue = null;
		nameProperty.addListener( this);
		assertNull( changedValue);
		nameProperty.set( "Steve");
		assertEquals( "Steve", changedValue);
		nameProperty.removeListener( this);
		nameProperty.set( "George");
		assertNotEquals( "George", changedValue);
	}

	@Test
	public void testInvalidationListener()
	{
		//TODO
	}

	@Test
	public void testGet()
	{
		ageProperty.set( Integer.MIN_VALUE );
		assertEquals( Integer.MIN_VALUE, ageProperty.get().intValue());
	}

	@Test
	public void testSet()
	{
		nameProperty.set( "Alex");
		assertEquals( "Alex", nameProperty.getValue());
	}

	@Override
	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue )
	{
		changedValue = newValue;
	}
	
}
