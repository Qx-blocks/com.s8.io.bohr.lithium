package com.s8.io.bohr.lithium.type;


import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import com.s8.io.bohr.atom.S8ShellStructureException;
import com.s8.io.bohr.lithium.exceptions.LiIOException;
import com.s8.io.bohr.lithium.fields.LiField;
import com.s8.io.bohr.lithium.object.LiObject;
import com.s8.io.bytes.alpha.MemoryFootprint;


/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public class LiType {


	/**
	 * 
	 */
	protected final Class<?> baseType;



	/**
	 * 
	 */
	Constructor<?> constructor;





	/**
	 * <p><b>/!\ WARNING </b></p>
	 * code for serialization / deserialization
	 */
	String name;




	/**
	 * fields
	 */
	final Map<String, LiField> fieldsByName = new HashMap<>();

	
	/**
	 * fields by ordinal number
	 */
	LiField[] fields;




	private final DebugModule debugModule;



	/**
	 * The number of references to be stored in vertex
	 */
	int nVertexReferences;


	public LiType(Class<?> baseType) {
		super();
		this.baseType = baseType;

		nVertexReferences = 0;

		// modules
		debugModule = new DebugModule(this);
	}
	
	
	
	
	public int getNumberOfFields() {
		return fields.length;
	}
	



	public Class<?> getBaseType(){
		return baseType;
	}

	public String getSerialName() {
		return name;
	}

	public String getRuntimeName() {
		return baseType.getName();
	}



	/**
	 * 
	 * @param object
	 * @param footprint
	 */
	public void computeFootprint(LiObject object, MemoryFootprint footprint) {
		footprint.reportInstance();
		fieldsByName.forEach((name, handler) -> {  
			try {
				footprint.reportEntry();

				handler.computeFootprint(object, footprint);
			} catch (LiIOException e) {
				e.printStackTrace();
			} 
		});
	}



	/**
	 * 
	 * @return
	 * @throws LthSerialException
	 */
	public LiObject createNewInstance() throws LiIOException {
		try {
			return (LiObject) constructor.newInstance(new Object[]{});
		}
		catch (InstantiationException 
				| IllegalAccessException 
				| IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
			throw new LiIOException("instance creation failed due to constructor call error", baseType, e);
		}
	}

	public final static byte[] CODEBASE_ENTRY = "<e/>".getBytes(StandardCharsets.US_ASCII);




	/**
	 * Collect all INTERNAL (belonging to this block) object whose state is matching the 
	 * state passed as argument, and switch their state to the state passed as
	 * argument.
	 * 
	 * @param object
	 * @param flagState
	 * @param queue
	 * @throws IOException 
	 * @throws S8ShellStructureException 
	 */
	public void sweep(LiObject object, GraphCrawler crawler) throws LiIOException {
		int nFields = fields.length;
		for(int i = 0; i<nFields; i++) { fields[i].sweep(object, crawler); }
	}




	/**
	 * Field accessor
	 * 
	 * @param name
	 * @return
	 */
	public LiField getFieldByName(String name) {
		return fieldsByName.get(name);
	}


	/**
	 * Collect all EXTERNAL blocks effectively (meaning ref is resolved) referenced by field of this objects
	 * @param object
	 * @param references
	 */
	public void collectReferencedBlocks(LiObject object, Queue<String> references) {
		for(LiField entryHandler : fieldsByName.values()) {
			entryHandler.collectReferencedBlocks(object, references);	
		}
	}


	/**
	 * 
	 * @param origin
	 * @param context
	 * @return
	 * @throws LthSerialException
	 */
	public LiObject deepClone(LiObject origin, ResolveScope resolveScope, BuildScope scope) throws LiIOException {
		LiObject clone = createNewInstance();
		for(LiField field : fields) {
			field.deepClone(origin, resolveScope, clone, scope);
		}
		return clone;
	}

	
	
	public LiField getField(int ordinal) {
		return fields[ordinal];
	}
	
	




	/**
	 * 
	 * @param indent
	 */
	public void DEBUG_print(String indent) {
		System.out.println(indent+"S8Object Type, name = "+getSerialName());
		System.out.println(indent+" fields:{");
		fieldsByName.forEach((name, field) -> { field.DEBUG_print(indent+"   "); });
		System.out.println(indent+" }\n");
	}

	/**
	 * 
	 * @param object
	 * @param inflow
	 * @param context
	 * @throws IOException
	 * @throws S8ShellStructureException 
	 */
	public void print(LiObject object, ResolveScope scope, Writer writer) throws IOException, S8ShellStructureException {
		debugModule.print(object, scope, writer);
	}

	
	public boolean equals(LiType right) {
		return getSerialName().equals(right.getSerialName());
	}
	
	public int getNumberOfVertexReferences() {
		return nVertexReferences;
	}




	
}
