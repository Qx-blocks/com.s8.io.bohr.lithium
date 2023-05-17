package com.s8.io.bohr.lithium.demos;

import java.io.OutputStreamWriter;

import com.s8.io.bohr.atom.S8BuildException;
import com.s8.io.bohr.lithium.branches.LiBranch;
import com.s8.io.bohr.lithium.codebase.LiCodebase;
import com.s8.io.bohr.lithium.codebase.LiCodebaseBuilder;
import com.s8.io.bohr.lithium.codebase.LiCodebaseBuilder.UpperLevel;
import com.s8.io.bohr.lithium.demos.repo2.MyBuilding;
import com.s8.io.bytes.linked.LinkedByteInflow;
import com.s8.io.bytes.linked.LinkedByteOutflow;

public class LiTest03 {

	public static void main(String[] args) throws Exception {
		

		OutputStreamWriter writer = new OutputStreamWriter(System.out);
		
		UpperLevel upperLevel = new UpperLevel() {
			
			@Override
			public void pushRowType(Class<?> type) throws S8BuildException {
				// TODO Auto-generated method stub
				
			}
		};
		LiCodebaseBuilder codebaseBuilder = new LiCodebaseBuilder(upperLevel, false);
		codebaseBuilder.pushObjectType(MyBuilding.class);
		LiCodebase codebase = codebaseBuilder.build();
		
		MyBuilding building = MyBuilding.create();
		LiBranch branch = new LiBranch("com.toto.123.098", "master", codebase);
		branch.resolveId(building).expose(2);
		
		
		LinkedByteOutflow outflow = new LinkedByteOutflow(1024);
		branch.pushSequence(outflow);
		System.out.println(outflow);
		
		LinkedByteInflow inflow = new LinkedByteInflow(outflow.getHead());
		LiBranch rBranch = new LiBranch("com.toto.123.098", "master", codebase);
		rBranch.pullSequence(inflow);
		System.out.println(rBranch.toString());
		
		//rBranch.print(new OutputStreamWriter(System.out));
		
		System.out.println("Test01");
		rBranch.deepCompare(branch, writer);
		
		
		
		// update object
		building.variate();
		
		// transmit
		outflow = new LinkedByteOutflow(1024);
		branch.pushSequence(outflow);
		inflow = new LinkedByteInflow(outflow.getHead());
		rBranch.pullSequence(inflow);
		
		System.out.println("Test02");
		rBranch.deepCompare(branch, writer);
		
		writer.close();
		
		
	}

}
