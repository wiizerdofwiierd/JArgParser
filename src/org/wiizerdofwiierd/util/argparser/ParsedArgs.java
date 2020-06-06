package org.wiizerdofwiierd.util.argparser;

import java.util.Iterator;

public class ParsedArgs implements Iterable<Object> {

	private Object[] values;

	public ParsedArgs(int size){
		this.values = new Object[size];
	}

	public Object getOrDefault(int index, Object defaultValue){
		return get(index) == null ? defaultValue : get(index);
	}

	public Object get(int index){
		return this.values[index];
	}

	public void set(int index, Object value){
		this.values[index] = value;
	}

	@Override
	public Iterator<Object> iterator(){
		return new Iterator<Object>() {
			private int index = 0;

			@Override
			public boolean hasNext(){
				return index < values.length;
			}

			@Override
			public Object next(){
				return values[index++];
			}
		};
	}
}