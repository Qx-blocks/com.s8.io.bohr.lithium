package com.s8.io.bohr.lithium.fields.objects;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Queue;

import com.s8.io.bohr.atom.BOHR_Types;
import com.s8.io.bohr.atom.annotations.S8Field;
import com.s8.io.bohr.atom.annotations.S8Getter;
import com.s8.io.bohr.atom.annotations.S8Setter;
import com.s8.io.bohr.lithium.exceptions.LiBuildException;
import com.s8.io.bohr.lithium.exceptions.LiIOException;
import com.s8.io.bohr.lithium.fields.LiField;
import com.s8.io.bohr.lithium.fields.LiFieldBuilder;
import com.s8.io.bohr.lithium.fields.LiFieldComposer;
import com.s8.io.bohr.lithium.fields.LiFieldDelta;
import com.s8.io.bohr.lithium.fields.LiFieldParser;
import com.s8.io.bohr.lithium.fields.LiFieldPrototype;
import com.s8.io.bohr.lithium.handlers.LiHandler;
import com.s8.io.bohr.lithium.object.LiObject;
import com.s8.io.bohr.lithium.properties.LiFieldProperties;
import com.s8.io.bohr.lithium.properties.LiFieldProperties1T;
import com.s8.io.bohr.lithium.type.BuildScope;
import com.s8.io.bohr.lithium.type.GraphCrawler;
import com.s8.io.bohr.lithium.type.ResolveScope;
import com.s8.io.bytes.alpha.ByteInflow;
import com.s8.io.bytes.alpha.ByteOutflow;
import com.s8.io.bytes.alpha.MemoryFootprint;


/**
 * 
 *
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 */
public class InterfaceLiField extends LiField {



	public final static LiFieldPrototype PROTOTYPE = new LiFieldPrototype() {


		@Override
		public LiFieldProperties captureField(Field field) throws LiBuildException {
			Class<?> fieldType = field.getType();
			S8Field annotation = field.getAnnotation(S8Field.class);
			if(annotation != null) {
				LiFieldProperties properties = new LiFieldProperties1T(this, LiFieldProperties.FIELD, fieldType);
				properties.setFieldAnnotation(annotation);
				return properties;	
			}
			else { return null; }
		}


		@Override
		public LiFieldProperties captureSetter(Method method) throws LiBuildException {
			Class<?> baseType = method.getParameterTypes()[0];
			S8Setter annotation = method.getAnnotation(S8Setter.class);
			if(annotation != null) {
				LiFieldProperties properties = new LiFieldProperties1T(this, LiFieldProperties.METHODS, baseType);
				properties.setSetterAnnotation(annotation);
				return properties;
			}
			else { return null; }
		}

		@Override
		public LiFieldProperties captureGetter(Method method) throws LiBuildException {
			Class<?> baseType = method.getReturnType();

			S8Getter annotation = method.getAnnotation(S8Getter.class);
			if(annotation != null) {

				LiFieldProperties properties = new LiFieldProperties1T(this, LiFieldProperties.METHODS, baseType);
				properties.setGetterAnnotation(annotation);
				return properties;

			}
			else { return null; }
		}


		@Override
		public LiFieldBuilder createFieldBuilder(LiFieldProperties properties, LiHandler handler) {
			return new Builder(properties, handler);
		}
	};




	private static class Builder extends LiFieldBuilder {

		public Builder(LiFieldProperties properties, LiHandler handler) {
			super(properties, handler);
		}

		@Override
		public LiFieldPrototype getPrototype() {
			return PROTOTYPE;
		}

		@Override
		public LiField build(int ordinal) {
			return new InterfaceLiField(ordinal, properties, handler);
		}
	}


	public InterfaceLiField(int ordinal, LiFieldProperties properties, LiHandler handler) {
		super(ordinal, properties, handler);
	}





	@Override
	public void sweep(LiObject object, GraphCrawler crawler) {
		try {
			LiObject fieldObject = (LiObject) handler.get(object);
			if(fieldObject!=null) {
				crawler.accept(fieldObject);
			}
		} 
		catch (LiIOException cause) {
			cause.printStackTrace();
		}
	}


	@Override
	public void collectReferencedBlocks(LiObject object, Queue<String> references) {
		// No ext references
	}


	@Override
	public void DEBUG_print(String indent) {
		System.out.println(indent+name+": (S8Object)");
	}

	@Override
	public void computeFootprint(LiObject object, MemoryFootprint weight) throws LiIOException {
		weight.reportReference();
	}


	@Override
	public void deepClone(LiObject origin, ResolveScope resolveScope, LiObject clone, BuildScope scope) throws LiIOException {
		LiObject value = (LiObject) handler.get(origin);
		if(value!=null) {
			String index = resolveScope.resolveId(value);

			scope.appendBinding(new BuildScope.Binding() {

				@Override
				public void resolve(BuildScope scope) throws LiIOException {

					// no need to upcast to S8Object
					LiObject indexedObject = scope.retrieveObject(index);
					if(indexedObject==null) {
						throw new LiIOException("Fialed to retriev vertex");
					}
					handler.set(clone, indexedObject);
				}
			});
		}
		else {
			handler.set(clone, null);
		}
	}


	@Override
	public InterfaceLiFieldDelta produceDiff(LiObject object, ResolveScope scope) throws IOException {
		LiObject value = (LiObject) handler.get(object);
		return new InterfaceLiFieldDelta(this, scope.resolveId(value));
	}



	@Override
	protected void printValue(LiObject object, ResolveScope scope, Writer writer) throws IOException {
		LiObject value = (LiObject) handler.get(object);
		if(value!=null) {
			writer.write("(");
			writer.write(value.getClass().getCanonicalName());
			writer.write("): ");
			writer.write(scope.resolveId(value));	
		}
		else {
			writer.write("null");
		}
	}

	@Override
	public String printType() {
		return "S8Object";
	}

	public void setValue(Object object, LiObject struct) throws LiIOException {
		handler.set(object, struct);
	}





	@Override
	public boolean isValueResolved(LiObject object) {
		return true; // always resolved at resolve step in shell
	}


	/* <IO-inflow-section> */

	@Override
	public LiFieldParser createParser(ByteInflow inflow) throws IOException {
		int code = inflow.getUInt8();
		switch(code){
		case BOHR_Types.S8OBJECT : return new Inflow();
		default: throw new LiIOException("Unsupported code: "+Integer.toHexString(code));
		}
	}


	private class Inflow extends LiFieldParser {

		@Override
		public LiFieldDelta parseValue(ByteInflow inflow) throws IOException {
			String id = inflow.getStringUTF8();
			return new InterfaceLiFieldDelta(getField(), id);
		}


		@Override
		public InterfaceLiField getField() {
			return InterfaceLiField.this;
		}
	}


	/* </IO-inflow-section> */



	/* <IO-outflow-section> */

	@Override
	public LiFieldComposer createComposer(int code) throws LiIOException {
		switch(flow) {

		case DEFAULT_FLOW_TAG: case "obj[]" : return new Outflow(code);

		default : throw new LiIOException("Impossible to match IO type for flow: "+flow);
		}
	}


	private class Outflow extends LiFieldComposer {

		public Outflow(int code) {
			super(code);
		}

		@Override
		public LiField getField() {
			return InterfaceLiField.this;
		}

		@Override
		public void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.S8OBJECT);
		}

		@Override
		public void composeValue(LiFieldDelta delta, ByteOutflow outflow) throws IOException {
			String id = ((InterfaceLiFieldDelta) delta).index;
			outflow.putStringUTF8(id);
		}
	}
	/* </IO-outflow-section> */
}
