/* ---------------------------------------------------------------------
 * Numenta Platform for Intelligent Computing (NuPIC)
 * Copyright (C) 2014, Numenta, Inc.  Unless you have an agreement
 * with Numenta, Inc., for a separate license for this software code, the
 * following terms and conditions apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * http://numenta.org/licenses/
 * ---------------------------------------------------------------------
 */
package org.numenta.nupic.encoders;

import gnu.trove.list.array.TDoubleArrayList;
import java.util.*;
import org.numenta.nupic.util.*;

/**
 * Pass an encoded SDR straight to the model.
 * Each encoding is an SDR in which w out of n bits are turned on.
 * @author wilsondy (from Python original)
 *
 */
public class PassThroughEncoder extends Encoder<int[]> {
	
	/**
	 * This is used to check that there are exactly outputBitsOn in the outgoing bits
	 * The Python claims to do more, but I don't think it actually does anything other than throw an error
	 * as we do here also. (This is w in the Python code)
	 */
	private Integer outputBitsOnCount;

	public PassThroughEncoder(int outputWidth, Integer outputBitsOnCount) {
		super.setW(outputWidth);
		super.setN(outputWidth);
		super.setForced(false);
		this.outputBitsOnCount = outputBitsOnCount;
	}
	
	@Override
	/**
	 * Does a bitwise compare of the two bitmaps and returns a fractional 
	 * value between 0 and 1 of how similar they are.
	 * 1 => identical
	 * 0 => no overlapping bits
	 * IGNORES difference in length (only compares bits of shorter list)  e..g 11 and 1100101010 are "identical"
	 * @see org.numenta.nupic.encoders.Encoder#closenessScores(gnu.trove.list.TDoubleList, gnu.trove.list.TDoubleList, boolean)
	 */
	public gnu.trove.list.TDoubleList closenessScores(gnu.trove.list.TDoubleList expValues, gnu.trove.list.TDoubleList actValues, boolean fractional) {
		TDoubleArrayList result = new TDoubleArrayList();

		double ratio = 1.0d;
		double expectedSum = expValues.sum();
		double actualSum = actValues.sum();	

		if (actualSum > expectedSum) {
			double diff = actualSum - expectedSum;
			if (diff < expectedSum)
				ratio = 1 - diff / expectedSum;
			else
				ratio = 1 / diff;
		}

		int[] expectedInts = ArrayUtils.toIntArray(expValues.toArray());
		int[] actualInts = ArrayUtils.toIntArray(actValues.toArray());

		int[] overlap = ArrayUtils.and(expectedInts, actualInts);

		int overlapSum = ArrayUtils.sum(overlap);
		double r = 0.0;
		if (expectedSum == 0)
			r = 0.0;
		else
			r = overlapSum / expectedSum;
		r = r * ratio;

		result.add(r);
		return result;
	}
	
	@Override
	public int getWidth() {
		return w;
	}

	@Override
	public boolean isDelta() {
		return false;
	}

	

	/**
	 * Check for length the same and copy input into output
	 * If outputBitsOnCount (w) set, throw error if not true
	 * @param input 
	 * @param output
	 */
	public void encodeIntoArray(int[] input, int[] output){

		if( input.length != output.length)
			throw new IllegalArgumentException(String.format("Different input (%i) and output (%i) sizes", input.length, output.length));
		if(this.outputBitsOnCount != null && ArrayUtils.sum(input) != outputBitsOnCount)
			throw new IllegalArgumentException(String.format("Input has %i bits but w was set to %i.",  ArrayUtils.sum(input), outputBitsOnCount));
		
		System.arraycopy(input, 0, output, 0, input.length);

	}
	
	/**
	 * Not much real work to do here as this concept doesn't really apply.
	 */
	public Tuple decode(int[] encoded, String parentFieldName) {
	    //TODO: these methods should be properly implemented (this comment in Python)		  
		String fieldName = this.name;
	    if (parentFieldName != null && parentFieldName.length() >0)
	    	String.format("%s.%s", parentFieldName, this.name);

		List<MinMax> ranges = new ArrayList<MinMax>();
		ranges.add(new MinMax(0,0));
	    RangeList inner = new RangeList(ranges, "input");
		Map<String, RangeList> fieldsDict = new HashMap<String, RangeList>();
		fieldsDict.put(fieldName, inner);
		
	    //return ({fieldName: ([[0, 0]], "input")}, [fieldName])
		return new DecodeResult(fieldsDict, Arrays.asList(new String[] { fieldName }));


	}

	@Override
	public void setLearning(boolean learningEnabled) {
		//NOOP		
	}

	@Override
	public List<Tuple> getDescription() {
		return null;
	}

	@Override
	public <T> List<T> getBucketValues(Class<T> returnType) { 
		return null;
	}

}
