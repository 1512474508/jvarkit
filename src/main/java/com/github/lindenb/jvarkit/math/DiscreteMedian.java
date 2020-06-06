/*
The MIT License (MIT)

Copyright (c) 2020 Pierre Lindenbaum

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.


*/
package com.github.lindenb.jvarkit.math;

import java.util.Iterator;
import java.util.OptionalDouble;
import java.util.TreeMap;

/*
 * see https://stackoverflow.com/questions/3052924
 * @author lindenb
 *
 */
public class DiscreteMedian<T extends Number>
	{
	private final TreeMap<T,Long> counter = new TreeMap<>();
	private long size = 0L;
	public DiscreteMedian() {
		}
	
	public long size() {
		return this.size;
		}
	
	public boolean isEmpty() {
		return counter.isEmpty();
		}
	
	public void clear() {
		this.size = 0L;
		this.counter.clear();
	}
	
	public void add(final T value) {
		long n = this.counter.getOrDefault(value, 0L);
		this.counter.put(value, n+1L);
		this.size++;
		}
	
	public OptionalDouble getAverage() {
		if(isEmpty()) return OptionalDouble.empty();
		return OptionalDouble.of(
			counter.entrySet().
			stream().
			mapToDouble(KV->KV.getKey().doubleValue()* KV.getValue()).
			sum()/this.size
			);
		}
	
	public OptionalDouble getMedian() {
		if(isEmpty()) return OptionalDouble.empty();
		final long mid_x = this.size/2L;
		if (this.size%2==1) {
			long n=0;
			for(T key : this.counter.keySet())   {
				final long c = this.counter.get(key);
				if(mid_x< n+c) return OptionalDouble.of(key.doubleValue());
				n+=c;
				}
			}
		else {
			long n=0;
		 	final Iterator<T> iter = this.counter.keySet().iterator();
		 	while(iter.hasNext()) {
		 		T key =  iter.next();
		 		final long count = this.counter.get(key);
		 		if((mid_x-1)>= n+count)
		 			{
		 			n+=count;
		 			continue;
		 			}
		 		double v1= key.doubleValue();
		 		if((mid_x)< n+count) return OptionalDouble.of(v1);
		 		if(!iter.hasNext()) throw new IllegalStateException();
		 		key = iter.next();
				double v2= key.doubleValue();
				return OptionalDouble.of((v1+v2)/2.0);	
		 		}
		  }
		throw new IllegalStateException();
		}
	}
