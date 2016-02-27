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

package de.doe300.activerecord.record.javafx;

import de.doe300.activerecord.record.ActiveRecord;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javax.annotation.Nonnull;

/**
 * Property to map a single attribute of an {@link ActiveRecord}
 * @author doe300
 * @param <T>
 */
public class AttributeProperty<T> extends ObjectProperty<T> implements Property<T>, ChangeListener<T>
{
	private final ActiveRecord record;
	private final String attributeName;
	private final Class<T> attributeType;
	private final List<ChangeListener<? super T>> changeListeners;
	private final List<InvalidationListener> invalidationListeners;
	
	//for bindings
	private ObservableValue<? extends T> ov;
	private final Set<Property<T>> bindings;

	public AttributeProperty(@Nonnull final ActiveRecord record, @Nonnull final String attributeName, @Nonnull final Class<T> attributeType )
	{
		this.record = record;
		this.attributeName = attributeName;
		this.attributeType = attributeType;
		changeListeners = new ArrayList<>(5);
		invalidationListeners = new ArrayList<>(5);
		
		bindings = new HashSet<>(5);
	}

	@Override
	public void bind(ObservableValue<? extends T> ov )
	{
		if(ov == null)
		{
			throw new NullPointerException();
		}
		unbind();
		this.ov = ov;
		ov.addListener( this);
	}

	@Override
	public void unbind()
	{
		if(ov != null)
		{
			ov.removeListener( this);
		}
		ov = null;
	}

	@Override
	public boolean isBound()
	{
		return ov!= null;
	}

	@Override
	public Object getBean()
	{
		return record;
	}

	@Override
	public String getName()
	{
		return attributeName;
	}

	@Override
	public void addListener(ChangeListener<? super T> cl )
	{
		changeListeners.add( cl );
	}

	@Override
	public void removeListener(ChangeListener<? super T> cl )
	{
		changeListeners.remove( cl );
	}

	@Override
	public void addListener( InvalidationListener il )
	{
		invalidationListeners.add( il );
	}

	@Override
	public void removeListener( InvalidationListener il )
	{
		invalidationListeners.remove( il );
	}

	@Override
	public void changed(ObservableValue<? extends T> ov, T t, T t1 )
	{
		record.getBase().getStore().setValue( record.getBase(), record.getPrimaryKey(), attributeName, t);
	}

	@Override
	public T get()
	{
		return attributeType.cast( record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), attributeName));
	}

	@Override
	public void set( T newValue )
	{
		T oldValue = getValue();
		record.getBase().getStore().setValue( record.getBase(), record.getPrimaryKey(), attributeName, newValue);
		for(ChangeListener<? super T> l: changeListeners)
		{
			l.changed( this, oldValue, newValue);
		}
	}

}
