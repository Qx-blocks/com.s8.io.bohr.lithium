package com.s8.io.bohr.lithium.fields;

import java.io.IOException;

import com.s8.io.bohr.atom.BOHR_Properties;
import com.s8.io.bohr.lithium.type.BuildScope;
import com.s8.io.bytes.alpha.ByteInflow;


/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public abstract class LiFieldParser {
	
	
	public abstract LiField getField();
	
	
	/**
	 * 
	 * @param map
	 * @param object
	 * @param inflow
	 * @param bindings
	 * @throws BkException
	 */
	public abstract LiFieldDelta parseValue(ByteInflow inflow, BuildScope scope) throws IOException;
	

	
	/**
	 * 
	 * @param props
	 * @return
	 */
	public static boolean isNonNull(int props) {
		return (props & BOHR_Properties.IS_NON_NULL_PROPERTIES_BIT) == BOHR_Properties.IS_NON_NULL_PROPERTIES_BIT;
	}
	
	
}
