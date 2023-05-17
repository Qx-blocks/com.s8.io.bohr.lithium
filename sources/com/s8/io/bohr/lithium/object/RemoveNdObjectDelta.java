package com.s8.io.bohr.lithium.object;

import java.io.IOException;

import com.s8.io.bohr.atom.BOHR_Keywords;
import com.s8.io.bohr.neodymium.branch.NdGraph;
import com.s8.io.bohr.neodymium.branch.endpoint.NdOutbound;
import com.s8.io.bohr.neodymium.exceptions.NdIOException;
import com.s8.io.bohr.neodymium.type.BuildScope;
import com.s8.io.bytes.alpha.ByteOutflow;
import com.s8.io.bytes.alpha.MemoryFootprint;


/**
 * 
 * @author Pierre Convert
 * Copyright (C) 2022, Pierre Convert. All rights reserved.
 *
 */
public class RemoveNdObjectDelta extends LiObjectDelta {

	public RemoveNdObjectDelta(String index) {
		super(index);
	}

	@Override
	public void consume(NdGraph graph, BuildScope scope) throws NdIOException {
		
	}

	@Override
	public void serialize(NdOutbound outbound, ByteOutflow outflow) throws IOException {
		
		/* remove node */
		outflow.putUInt8(BOHR_Keywords.REMOVE_NODE);

		/* define index */
		outflow.putStringUTF8(index);
	}
	

	@Override
	public void computeFootprint(MemoryFootprint weight) {
		// TODO Auto-generated method stub
		
	}

}