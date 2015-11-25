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

import de.doe300.activerecord.pojo.POJOBase;
import de.doe300.activerecord.record.RecordType;

/**
 *
 * @author doe300
 */
@RecordType(defaultColumns = {"id", "name", "age"}, typeName = "javaBeans")
public class TestJavaBeanPOJO extends AbstractJavaBeanRecord implements TestJavaBeanRecord
{

	public TestJavaBeanPOJO( int primaryKey,POJOBase<?> base )
	{
		super( primaryKey, base );
	}

	@Override
	public String getName()
	{
		return getProperty( "name", String.class);
	}

	@Override
	public void setName( String name )
	{
		setProperty( "name", name);
	}

	@Override
	public int getAge()
	{
		return getProperty( "age", Integer.class);
	}

	@Override
	public void setAge( int age )
	{
		setProperty( "age", age);
	}

}
