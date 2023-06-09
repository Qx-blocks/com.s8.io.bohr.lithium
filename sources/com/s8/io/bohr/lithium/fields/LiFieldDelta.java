package com.s8.io.bohr.lithium.fields;

import com.s8.io.bohr.lithium.exceptions.LiIOException;
import com.s8.io.bohr.lithium.object.LiObject;
import com.s8.io.bohr.lithium.type.BuildScope;


/**
 * <p><code>NdFieldDelta</code> are immutable!</p>
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public abstract class LiFieldDelta {
		
	
	public LiFieldDelta() {
		super();
	}
	
	/**
	 * 
	 * @param object
	 * @throws LthSerialException 
	 */
	public abstract void operate(LiObject object, BuildScope scope) throws LiIOException;
	
	public abstract LiField getField();
}
