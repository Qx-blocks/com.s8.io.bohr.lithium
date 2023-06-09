package com.s8.io.bohr.lithium.fields;

import java.io.IOException;
import java.io.Writer;
import java.util.Queue;

import com.s8.io.bohr.atom.S8ShellStructureException;
import com.s8.io.bohr.lithium.exceptions.LiIOException;
import com.s8.io.bohr.lithium.handlers.LiHandler;
import com.s8.io.bohr.lithium.object.LiObject;
import com.s8.io.bohr.lithium.properties.LiFieldProperties;
import com.s8.io.bohr.lithium.type.BuildScope;
import com.s8.io.bohr.lithium.type.GraphCrawler;
import com.s8.io.bohr.lithium.type.ResolveScope;
import com.s8.io.bytes.alpha.ByteInflow;
import com.s8.io.bytes.alpha.MemoryFootprint;

/**
 * 
 *
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 * 
 */
public abstract class LiField {

	public final static String DEFAULT_FLOW_TAG = "(default)";

	
	public final int ordinal;
	
	/**
	 * 
	 */
	public final LiHandler handler;


	/**
	 * 
	 */
	public final String name;

	/**
	 * 
	 */
	public final String flow;

	/**
	 * 
	 */
	public final long mask;

	/**
	 * 
	 */
	public final long flags;
	
	/**
	 * 
	 * @param properties
	 * @param handler
	 */
	public LiField(int ordinal, LiFieldProperties properties, LiHandler handler) {
		super();
		
		this.ordinal = ordinal;
		
		/* <field-properties> */
		this.name = properties.getName();
		this.flow = properties.getFlow();
		this.mask = properties.getMask();
		this.flags = properties.getFlags();
		/* </field-properties> */
		
		/* <handler> */
		this.handler = handler;
		/* </handler> */
	}


	
	
	protected LiHandler getHandler() {
		return handler;
	}

	
	
	
	/**
	 * 
	 * @param flow (null = use flow define in field)
	 * @return
	 * @throws  
	 */
	public abstract LiFieldComposer createComposer(int code) throws LiIOException;
	
	
	public abstract LiFieldParser createParser(ByteInflow inflow) throws IOException;
	
	


	public abstract void computeFootprint(LiObject object, MemoryFootprint weight) throws LiIOException;

	/**
	 * Collect all INTERNAL (belonging to this block) object not already in the
	 * state passed as argument, and switch their state to the state passed as
	 * argument
	 * 
	 * @param object
	 * @param state
	 * @param referenced
	 * @throws IOException
	 * @throws S8ShellStructureException 
	 */
	public abstract void sweep(LiObject object, GraphCrawler crawler) throws LiIOException;

	/**
	 * Collect all external blocks with flag not already set to true
	 * 
	 * @param object
	 */
	public abstract void collectReferencedBlocks(LiObject object, Queue<String> references);

	

	/**
	 * Print field for debugging purposes
	 * 
	 * @param indent
	 */
	public abstract void DEBUG_print(String indent);

	/**
	 * 
	 * @param clone
	 * @param bindings
	 * @throws LthSerialException
	 */
	public abstract void deepClone(LiObject origin, ResolveScope resolveScope, LiObject clone, BuildScope scope) throws LiIOException;

	
	/**
	 * 
	 * @param object
	 * @param scope
	 * @return
	 * @throws IOException
	 */
	public abstract LiFieldDelta produceDiff(LiObject object, ResolveScope scope) throws IOException;

	

	public void print(LiObject object, ResolveScope scope, Writer writer) throws IOException, S8ShellStructureException {
		writer.append("(");
		writer.append(printType());
		writer.append(") ");
		writer.append(name);
		writer.append(": ");
		printValue(object, scope, writer);
	}
	

	/**
	 * print standard name of field type
	 */
	public abstract String printType();


	protected abstract void printValue(LiObject object, ResolveScope scope, Writer writer) throws LiIOException, 
	IOException, S8ShellStructureException;

	

	/**
	 * 
	 * @param object
	 * @return true is the object has been resolved, false otherwise
	 * @throws LthSerialException 
	 */
	public abstract boolean isValueResolved(LiObject object) throws LiIOException;

	
}
