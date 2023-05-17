package com.s8.io.bohr.lithium.fields.primitives;

import java.io.IOException;
import java.io.Writer;

import com.s8.io.bohr.atom.BOHR_Types;
import com.s8.io.bohr.lithium.exceptions.LiBuildException;
import com.s8.io.bohr.lithium.exceptions.LiIOException;
import com.s8.io.bohr.lithium.fields.LiField;
import com.s8.io.bohr.lithium.fields.LiFieldComposer;
import com.s8.io.bohr.lithium.fields.LiFieldDelta;
import com.s8.io.bohr.lithium.fields.LiFieldParser;
import com.s8.io.bohr.lithium.fields.LiFieldPrototype;
import com.s8.io.bohr.lithium.handlers.LiHandler;
import com.s8.io.bohr.lithium.object.LiS8Object;
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
public class DoubleLiField extends PrimitiveLiField {


	public final static PrimitiveLiField.Prototype PROTOTYPE = new Prototype(double.class){

		@Override
		public PrimitiveLiField.Builder createFieldBuilder(LiFieldProperties properties, LiHandler handler) {
			return new DoubleLiField.Builder(properties, handler);
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
			return new DoubleLiField(ordinal, properties, handler);
		}		
	}

	/**
	 * 
	 * @param outboundTypeName
	 * @param handler
	 * @throws LiBuildException 
	 */
	public DoubleLiField(int ordinal, LiFieldProperties properties, LiHandler handler) throws LiBuildException{
		super(ordinal, properties, handler);
	}

	@Override
	public Prototype getPrototype() {
		return PROTOTYPE;
	}


	@Override
	public void computeFootprint(LiS8Object object, MemoryFootprint weight) {
		weight.reportBytes(8);
	}


	@Override
	public void deepClone(LiS8Object origin, ResolveScope resolveScope, LiS8Object clone, BuildScope scope) throws LiIOException {
		double value = handler.getDouble(origin);
		handler.setDouble(clone, value);
	}


	@Override
	public boolean hasDiff(LiS8Object base, LiS8Object update, ResolveScope resolveScope) throws IOException {
		double baseValue = handler.getDouble(base);
		double updateValue = handler.getDouble(update);
		return baseValue != updateValue;
	}
	

	@Override
	public void DEBUG_print(String indent) {
		System.out.println(indent+name+": (double)");
	}


	@Override
	protected void printValue(LiS8Object object, ResolveScope scope, Writer writer) throws IOException {
		writer.write(Double.toString(handler.getDouble(object)));
	}



	/* <IO-inflow-section> */


	@Override
	public LiFieldParser createParser(ByteInflow inflow) throws IOException {
		int code = inflow.getUInt8();
		switch(code) {

		case BOHR_Types.FLOAT32 : return new Float32_Inflow();
		case BOHR_Types.FLOAT64 : return new Float64_Inflow();

		default : throw new LiIOException("Failed to find field-inflow for code: "+Integer.toHexString(code));
		}
	}



	private abstract class Inflow extends LiFieldParser {

		@Override
		public DoubleLiField getField() {
			return DoubleLiField.this;
		}

		@Override
		public DoubleLiFieldDelta parseValue(ByteInflow inflow, BuildScope scope) throws IOException {
			return new DoubleLiFieldDelta(getField(), deserialize(inflow));
		}
		
		public abstract double deserialize(ByteInflow inflow) throws IOException;

	}

	private class Float32_Inflow extends Inflow {
		public @Override double deserialize(ByteInflow inflow) throws IOException {
			return inflow.getFloat32();
		}
	}

	private class Float64_Inflow extends Inflow {
		public @Override double deserialize(ByteInflow inflow) throws IOException {
			return inflow.getFloat64();
		}
	}
	/* </IO-inflow-section> */


	/* <IO-outflow-section> */

	@Override
	public LiFieldComposer createComposer(int code) throws LiIOException {
		switch(flow) {

		case "float32" : return new Float32_Outflow(code);
		case DEFAULT_FLOW_TAG: case "float64" : return new Float64_Outflow(code);

		default : throw new LiIOException("Failed to find field-outflow for encoding: "+flow);
		}
	}


	private abstract class Outflow extends LiFieldComposer {

		public Outflow(int code) {
			super(code);
		}


		@Override
		public DoubleLiField getField() {
			return DoubleLiField.this;
		}


		@Override
		public void composeValue(LiFieldDelta delta, ByteOutflow outflow, ResolveScope scope) throws IOException {
			serialize(outflow, handler.getDouble(object));
		}

		public abstract void serialize(ByteOutflow outflow, double value) throws IOException;
	}


	private class Float32_Outflow extends Outflow {
		public Float32_Outflow(int code) { super(code); }
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.FLOAT32);
		}
		public @Override void serialize(ByteOutflow outflow, double value) throws IOException {
			outflow.putFloat32((float) value);
		}
	}

	private class Float64_Outflow extends Outflow {
		public Float64_Outflow(int code) { super(code); }
		public @Override void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.FLOAT64);
		}
		public @Override void serialize(ByteOutflow outflow, double value) throws IOException {
			outflow.putFloat64(value);
		}
	}

	/* <IO-outflow-section> */

}
