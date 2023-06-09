package com.s8.io.bohr.lithium.fields.primitives;

import java.io.IOException;
import java.io.Writer;

import com.s8.io.bohr.atom.BOHR_Types;
import com.s8.io.bohr.atom.S8BuildException;
import com.s8.io.bohr.lithium.exceptions.LiBuildException;
import com.s8.io.bohr.lithium.exceptions.LiIOException;
import com.s8.io.bohr.lithium.fields.LiField;
import com.s8.io.bohr.lithium.fields.LiFieldComposer;
import com.s8.io.bohr.lithium.fields.LiFieldDelta;
import com.s8.io.bohr.lithium.fields.LiFieldParser;
import com.s8.io.bohr.lithium.fields.LiFieldPrototype;
import com.s8.io.bohr.lithium.handlers.LiHandler;
import com.s8.io.bohr.lithium.object.LiObject;
import com.s8.io.bohr.lithium.properties.LiFieldProperties;
import com.s8.io.bohr.lithium.type.BuildScope;
import com.s8.io.bohr.lithium.type.ResolveScope;
import com.s8.io.bytes.alpha.ByteInflow;
import com.s8.io.bytes.alpha.ByteOutflow;
import com.s8.io.bytes.alpha.MemoryFootprint;

/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class BooleanLiField extends PrimitiveLiField {

	public final static PrimitiveLiField.Prototype PROTOTYPE = new Prototype(boolean.class){

		@Override
		public PrimitiveLiField.Builder createFieldBuilder(LiFieldProperties properties, LiHandler handler) {
			return new BooleanLiField.Builder(properties, handler);
		}
	};


	private static class Builder extends PrimitiveLiField.Builder {

		public Builder(LiFieldProperties properties, LiHandler handler) {
			super(properties, handler);
		}

		@Override
		public LiFieldPrototype getPrototype() {
			return PROTOTYPE;
		}

		@Override
		public LiField build(int ordinal) throws LiBuildException {
			return new BooleanLiField(ordinal, properties, handler);
		}
	}



	/**
	 * 
	 * @param outboundTypeName
	 * @param handler
	 * @throws S8BuildException 
	 */
	public BooleanLiField(int ordinal, LiFieldProperties properties, LiHandler handler) throws LiBuildException {
		super(ordinal, properties, handler);
	}

	@Override
	public Prototype getPrototype() {
		return PROTOTYPE;
	}


	@Override
	public void computeFootprint(LiObject object, MemoryFootprint weight) {
		weight.reportBytes(1);
	}

	@Override
	public void deepClone(LiObject origin, ResolveScope resolveScope, LiObject clone, BuildScope scope) throws LiIOException {
		boolean value = handler.getBoolean(origin);
		handler.setBoolean(clone, value);
	}

	@Override
	public void DEBUG_print(String indent) {
		System.out.println(indent + name + ": (boolean)");
	}


	@Override
	public BooleanLiFieldDelta produceDiff(LiObject object, ResolveScope scope) throws IOException {
		return new BooleanLiFieldDelta(this, handler.getBoolean(object));
	}
	

	
	@Override
	protected void printValue(LiObject object, ResolveScope scope, Writer writer) throws IOException {
		writer.write(Boolean.toString(handler.getBoolean(object)));
	}

	

	

	/* <IO-inflow-section> */


	@Override
	public LiFieldParser createParser(ByteInflow inflow) throws IOException {
		int code = inflow.getUInt8();
		switch(code) {

		case BOHR_Types.BOOL8 : return new Bool8_Inflow();

		default : throw new LiIOException("Failed to find field-inflow for code: "+Integer.toHexString(code));
		}
	}



	private class Bool8_Inflow extends LiFieldParser {

		@Override
		public BooleanLiField getField() {
			return BooleanLiField.this;
		}

		@Override
		public BooleanLiFieldDelta parseValue(ByteInflow inflow) throws IOException {
			return new BooleanLiFieldDelta(BooleanLiField.this, inflow.getBool8());
		}
	}

	/* </IO-inflow-section> */


	/* <IO-outflow-section> */



	@Override
	public LiFieldComposer createComposer(int code) throws LiIOException {
		switch(flow) {
		case DEFAULT_FLOW_TAG: case "bool8" : return new DefaultComposer(code);
		default : throw new LiIOException("Failed to find field-outflow for encoding: "+flow);
		}	
	}
	
	
	private class DefaultComposer extends LiFieldComposer {

		public DefaultComposer(int code) { super(code); }

		@Override
		public BooleanLiField getField() {
			return BooleanLiField.this;
		}

		@Override
		public void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.BOOL8);
		}

		@Override
		public void composeValue(LiFieldDelta delta, ByteOutflow outflow) throws IOException {
			outflow.putBool8(((BooleanLiFieldDelta) delta).value);
		}
	}



	/* <IO-outflow-section> */	


}
