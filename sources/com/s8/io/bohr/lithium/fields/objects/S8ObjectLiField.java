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
import com.s8.io.bohr.lithium.fields.LiFieldParser;
import com.s8.io.bohr.lithium.fields.LiFieldPrototype;
import com.s8.io.bohr.lithium.handlers.LiHandler;
import com.s8.io.bohr.lithium.object.LiS8Object;
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
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 *
 */
public class S8ObjectLiField extends LiField {



	public final static LiFieldPrototype PROTOTYPE = new LiFieldPrototype() {


		@Override
		public LiFieldProperties captureField(Field field) throws LiBuildException {
			Class<?> fieldType = field.getType();
			if(LiS8Object.class.isAssignableFrom(fieldType)){
				S8Field annotation = field.getAnnotation(S8Field.class);
				if(annotation != null) {
					LiFieldProperties properties = new LiFieldProperties1T(this, LiFieldProperties.FIELD, fieldType);
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
				if(LiS8Object.class.isAssignableFrom(baseType)) {
					LiFieldProperties properties = new LiFieldProperties1T(this, LiFieldProperties.METHODS, baseType);
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
				if(LiS8Object.class.isAssignableFrom(baseType)){
					LiFieldProperties properties = new LiFieldProperties1T(this, LiFieldProperties.METHODS, baseType);
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
		public LiField build(int ordinal) {
			return new S8ObjectLiField(ordinal, properties, handler);
		}
	}


	public S8ObjectLiField(int ordinal, LiFieldProperties properties, LiHandler handler) {
		super(ordinal, properties, handler);
	}





	@Override
	public void sweep(LiS8Object object, GraphCrawler crawler) {
		try {
			LiS8Object fieldObject = (LiS8Object) handler.get(object);
			if(fieldObject!=null) {
				crawler.accept(fieldObject);
			}
		} 
		catch (LiIOException cause) {
			cause.printStackTrace();
		}
	}


	@Override
	public void collectReferencedBlocks(LiS8Object object, Queue<String> references) {
		// No ext references
	}


	@Override
	public void DEBUG_print(String indent) {
		System.out.println(indent+name+": (S8Object)");
	}

	@Override
	public void computeFootprint(LiS8Object object, MemoryFootprint weight) throws LiIOException {
		weight.reportReference();
	}


	@Override
	public void deepClone(LiS8Object origin, ResolveScope resolveScope, LiS8Object clone, BuildScope scope) throws LiIOException {
		LiS8Object value = (LiS8Object) handler.get(origin);
		if(value!=null) {
			String index = resolveScope.resolveId(value);

			scope.appendBinding(new BuildScope.Binding() {

				@Override
				public void resolve(BuildScope scope) throws LiIOException {

					// no need to upcast to S8Object
					LiS8Object indexedObject = scope.retrieveObject(index);
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
	public boolean hasDiff(LiS8Object base, LiS8Object update, ResolveScope scope) throws LiIOException {
		LiS8Object baseValue = (LiS8Object) handler.get(base);
		LiS8Object updateValue = (LiS8Object) handler.get(update);
		if(baseValue == null && updateValue == null) {
			return false;
		}
		else if ((baseValue != null && updateValue == null) || (baseValue == null && updateValue != null)) {
			return true;
		}
		else {
			return !scope.resolveId(baseValue).equals(scope.resolveId(updateValue));
		}
	}


	@Override
	protected void printValue(LiS8Object object, ResolveScope scope, Writer writer) throws IOException {
		LiS8Object value = (LiS8Object) handler.get(object);
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

	public void setValue(Object object, LiS8Object struct) throws LiIOException {
		handler.set(object, struct);
	}





	@Override
	public boolean isValueResolved(LiS8Object object) {
		return true; // always resolved at resolve step in shell
	}



	/**
	 * 
	 * @author pierreconvert
	 *
	 * @param <T>
	 */
	private class ObjectBinding implements BuildScope.Binding {

		private Object object;

		private String id;

		public ObjectBinding(Object object, String id) {
			super();
			this.object = object;
			this.id = id;
		}

		@Override
		public void resolve(BuildScope scope) throws LiIOException {
			handler.set(object, scope.retrieveObject(id));
		}
	}

	/* <delta> */





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
		public void parseValue(LiS8Object object, ByteInflow inflow, BuildScope scope) throws IOException {
			String id = inflow.getStringUTF8();
			if(id != null) {
				/* append bindings */
				scope.appendBinding(new ObjectBinding(object, id));
			}
			else {
				// set list
				handler.set(object, null);	
			}
		}


		@Override
		public S8ObjectLiField getField() {
			return S8ObjectLiField.this;
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
			return S8ObjectLiField.this;
		}

		@Override
		public void publishFlowEncoding(ByteOutflow outflow) throws IOException {
			outflow.putUInt8(BOHR_Types.S8OBJECT);
		}

		@Override
		public void composeValue(LiS8Object object, ByteOutflow outflow, ResolveScope scope) throws IOException {
			LiS8Object value = (LiS8Object) handler.get(object);
			if(value != null) {
				String index = scope.resolveId(value);
				outflow.putStringUTF8(index);
			}
			else {
				outflow.putStringUTF8(null);
			}
		}
	}
	/* </IO-outflow-section> */
}
