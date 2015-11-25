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

import de.doe300.activerecord.proxy.RecordHandler;
import de.doe300.activerecord.proxy.handlers.ProxyHandler;
import de.doe300.activerecord.record.ActiveRecord;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nullable;

/**
 * ProxyHandler for registering and notifying PropertyChangeListeners
 * 
 * @author doe300
 */
public class PropertyChangeProxyHandler implements ProxyHandler
{
	private final Map<ActiveRecord, PropertyChangeSupport> listeners;
	private final Set<PropertyChangeListener> generalListeners;

	public PropertyChangeProxyHandler(@Nullable final Set<PropertyChangeListener> listeners)
	{
		this.listeners = new TreeMap<>();
		this.generalListeners = listeners;
	}

	@Override
	public boolean handlesMethod( ActiveRecord record, Method method, Object[] args ) throws IllegalArgumentException
	{
		return record instanceof JavaBeanRecord && method.getDeclaringClass().equals( JavaBeanRecord.class);
	}

	@Override
	public <T extends ActiveRecord> Object invoke( ActiveRecord record, RecordHandler<T> handler, Method method,
			Object[] args ) throws IllegalArgumentException
	{
		PropertyChangeSupport support = listeners.get( record );
		if(listeners.get( record) == null)
		{
			//register new property-change support for record
			support = new PropertyChangeSupport(record);
			listeners.put( record, support );
			if(this.generalListeners != null)
			{
				//add general listeners to all supports
				for(PropertyChangeListener l : generalListeners)
				{
					support.addPropertyChangeListener( l );
				}
			}
		}
		if(method.getName().equals( "firePropertyChange"))
		{
			//arguments are: attributeName(String), oldValue(Object), newValue(Object)
			support.firePropertyChange( (String)args[0], args[1], args[2]);
			return null;
		}
		if(method.getName().equals( "addPropertyChangeListener"))
		{
			//to make sure, listener is only added once
			support.removePropertyChangeListener( (PropertyChangeListener)args[0] );
			support.addPropertyChangeListener( (PropertyChangeListener)args[0]);
			return null;
		}
		if(method.getName().equals( "removePropertyChangeListener"))
		{
			support.removePropertyChangeListener( (PropertyChangeListener)args[0]);
			return null;
		}
		throw new UnsupportedOperationException("Method '" + method.getName() + "' not implemented!");
	}

	@Override
	public Object setAttributeHook( ActiveRecord record, String attributeName, Object value )
	{
		//hook into setValue-calls to fire listeners
		final Object oldValue = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), attributeName);
		((JavaBeanRecord)record).firePropertyChange( attributeName, oldValue, value);
		return value;
	}
}
