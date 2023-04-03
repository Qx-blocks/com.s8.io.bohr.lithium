    package com.s8.io.bohr.lithium.demos.repo2;

import com.s8.io.bohr.atom.annotations.S8Field;
import com.s8.io.bohr.atom.annotations.S8ObjectType;
import com.s8.io.bohr.lithium.object.LiS8Object;



/**
 * 
 * 
 * 
 * 
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 *
 */
@S8ObjectType(name = "my-floor", sub= {
		MyCommercialFloor.class,
		MyEmptyFloor.class
})
public abstract class MyFloor extends LiS8Object {

	public final static long HAS_CHANGED = 0x02;


	public @S8Field(name = "x0", mask = HAS_CHANGED) double x0;

	public @S8Field(name = "x1", mask = HAS_CHANGED) double x1;

	public @S8Field(name = "y0", mask = HAS_CHANGED) double y0;

	public @S8Field(name = "y1", mask = HAS_CHANGED) double y1;

	public @S8Field(name = "ceiling-height", mask = HAS_CHANGED) double ceilingHeight;

	
	public enum Face {
		
		POS_X, NEG_X, POS_Y, NEG_Y;
		
	}
	

	

	public MyFloor() {
		super();
	}

	

	public static MyFloor create() {
		MyFloor element = null;
		if(Math.random()<0.3){
			element = MyEmptyFloor.create();
		}
		else {
			element = MyCommercialFloor.create();
		}
		return element;
	}



	
	protected abstract void init();
	
	protected abstract void variate();



}