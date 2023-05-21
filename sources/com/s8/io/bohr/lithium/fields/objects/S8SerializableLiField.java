package com.s8.io.bohr.lithium.fields.objects;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Queue;

import com.s8.io.bohr.atom.BOHR_Properties;
import com.s8.io.bohr.atom.BOHR_Types;
import com.s8.io.bohr.atom.BohrSerializable;
import com.s8.io.bohr.atom.S8Exception;
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
import com.s8.io.bohr.lithium.properties.LiFieldProperties0T;
import com.s8.io.bohr.lithium.type.BuildScope;
import com.s8.io.bohr.lithium.type.GraphCrawler;
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
public class S8SerializableLiField extends LiField {
	


	public final static LiFieldPrototype PROTOTYPE = new LiFieldPrototype() {


		@Override
		public LiFieldProperties captureField(Field field) throws LiBuildException {
			Class<?> fieldType = field.getType();
			if(BohrSerializable.class.isAssignableFrom(fieldType)){
				S8Field annotation = field.getAnnotation(S8Field.class);
				if(annotation != null) {
					LiFieldProperties properties = new LiFieldProperties0T(this, LiFieldProperties.FIELD, fieldType);
					properties.setFieldAnnotation(annotation);
					return properties;	
				}
				else { return null; }
			}
			else { return null; }
		}


		@Override
		public LiFieldProperties captureSetter(Method method) throws LiBuildException {
			Class<?> baseType = method.getParameterTypes()[0];
			S8Setter annotation = method.getAnnotation(S8Setter.class);
			if(annotation != null) {
				if(BohrSerializable.class.isAssignableFrom(baseType)) {
					LiFieldProperties properties = new LiFieldProperties0T(this, LiFieldProperties.METHODS, baseType);
					properties.setSetterAnnotation(annotation);
					return properties;
				}
				else {
					throw new LiBuildException("S8Annotated field of type List must have its "
							+"parameterized type inheriting from S8Object", method);
				}
			}
			else { return null; }
		}

		@Override
		public LiFieldProperties captureGetter(Method method) throws LiBuildException {
			Class<?> baseType = method.getReturnType();

			S8Getter annotation = method.getAnnotation(S8Getter.class);
			if(annotation != null) {
				if(BohrSerializable.class.isAssignableFrom(baseType)){
					LiFieldProperties properties = new LiFieldProperties0T(this, LiFieldProperties.METHODS, baseType);
					properties.setGetterAnnotation(annotation);
					return properties;
				}
				else {
					throw new LiBuildException("S8Annotated field of type List must have its "
							+"parameterized type inheriting from S8Object", method);

				}
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
		public LiField build(int ordinal) throws LiBuildException {
			return new S8SerializableLiField(ordinal, properties, handler);
		}
	}



	private BohrSerializable.BohrSerialPrototype<?> deserializer;


	
	/**
	 * 
	 * @param properties
	 * @param handler
	 * @throws LiBuildException 
	 */
	public S8SerializableLiField(int ordinal, LiFieldProperties properties, LiHandler handler) throws LiBuildException {
		super(ordinal, properties, handler);
		Class<?> baseType = properties.getBaseType();
		try {
			deserializer = BohrSerializable.getDeserializer(baseType);
		} 
		catch (S8Exception e) {
			e.printStackTrace();
			throw new LiBuildException("Failed to build the S8Serizalizable GphField due to  "+e.getMessage() , baseType);
		}
	}





	@Override
	public void sweep(LiObject object, GraphCrawler crawler) {
		// no sweep
	}


	@Override
	public void collectReferencedBlocks(LiObject object, Queue<String> references) {
		// No ext references
	}





	@Override
	public void computeFootprint(LiObject object, MemoryFootprint weight) throws LiIOException {
		BohrSerializable value = (BohrSerializable) handler.get(object);
		if(value!=null) {
			weight.reportInstance();
			weight.reportBytes(value.computeFootprint());	
		}
	}

	@Override
	public void deepClone(LiObject origin, ResolveScope resolveScope, LiObject clone, BuildScope scope) throws LiIOException {
		BohrSerializable value = (BohrSerializable) handler.get(origin);
		handler.set(clone, value.deepClone());
	}


	@Override
	public void DEBUG_print(String indent) {
		System.out.println(indent+name+": (ByteSerializableFieldHandler)");
	}


	@Override
	public LiFieldDelta produceDiff(LiObject object, ResolveScope scope) throws LiIOException {
		return new S8SerializableLiFieldDelta(this, (BohrSerializable) handler.get(object));
	}



	@Override
	protected void printValue(LiObject object, ResolveScope scope, Writer writer) throws IOException {
		Object value = handler.get(object);
		if(value!=null) {
			writer.write("(");
			writer.write(value.getClass().getCanonicalName());
			writer.write("): ");
			writer.write(value.toString());	
		}
		else {
			writer.write("null");
		}
	}

	@Override
	public String printType() {
		return "S8Object";
	}



	@Override
	public boolean isValueResolved(LiObject object) {
		return true; // always resolved at resolve step in shell
	}
	
	
	


	/* <IO-inflow-section> */

	@Override
	public LiFieldParser createParser(ByteInflow inflow) throws IOException {
		int code = inflow.getUInt8();
		if(code != BOHR_Types.SERIAL) {
			throw new IOException("only array accepted");
		}
		
		// in fine, create parser
		return new Parser();
	}
	

	private class Parser extends LiFieldParser {

		@Override
		public LiFieldDelta parseValue(ByteInflow inflow) throws IOException {
			return new S8SerializableLiFieldDelta(getField(), deserialize(inflow));
		}


		@Override
		public S8SerializableLiField getField() {
			return S8SerializableLiField.this;
		}
		
		private BohrSerializable deserialize(ByteInflow inflow) throws IOException {
			int props = inflow.getUInt8();
			if(isNonNull(props)) {
				return deserializer.deserialize(inflow);
			}
			else {
				return null;
			}
		}

	}
	
	/* </IO-inflow-section> */

	

	/* <IO-outflow-section> */
		
	@Override
	public LiFieldComposer createComposer(int code) throws LiIOException {
		switch(flow) {

		case DEFAULT_FLOW_TAG: case "serial" : return new Outflow(code);

		default : throw new LiIOException("Impossible to match IO type for flow: "+flow);
		}
	}


	private class Outflow extends LiFieldComposer {

		public Outflow(int code) { super(code); }

		@Override
		public LiField getField() {
			return S8SerializableLiField.this;
		}

		@Override
		public void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.SERIAL);
		}

		@Override
		public void composeValue(LiFieldDelta delta, ByteOutflow outflow) throws IOException {
			BohrSerializable value = ((S8SerializableLiFieldDelta) delta).value;
			if(value != null) {
				outflow.putUInt8(BOHR_Properties.IS_NON_NULL_PROPERTIES_BIT);
				value.serialize(outflow);
			}
			else {
				outflow.putUInt8(0x00);
			}
		}
	}
	/* </IO-outflow-section> */
	

}
