/*******************************************************************************
* Copyright (c) 2009 Centrum Wiskunde en Informatica (CWI)
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Arnold Lankamp - interfaces and implementation
*******************************************************************************/
package org.eclipse.imp.pdb.facts.impl.shared;

import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.impl.fast.SetWriter;
import org.eclipse.imp.pdb.facts.impl.util.collections.ShareableValuesHashSet;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;

/**
 * Set writer for shareable sets.
 * 
 * @author Arnold Lankamp
 */
public class SharedSetWriter extends SetWriter{
	
	protected SharedSetWriter(Type elementType){
		super(elementType);
	}
	
	protected SharedSetWriter(){
		super();
	}

	protected SharedSetWriter(Type elementType, ShareableValuesHashSet data){
		super(elementType, data);
	}
	
	public ISet done(){
		if(constructedSet == null) {
			if (inferred && elementType.isTupleType() || data.isEmpty()) {
				constructedSet = SharedValueFactory.getInstance().buildRelation(new SharedRelation(data.isEmpty() ? TypeFactory.getInstance().voidType() : elementType, data));
			}
			else {
				constructedSet = SharedValueFactory.getInstance().buildSet(new SharedSet(data.isEmpty() ? TypeFactory.getInstance().voidType() : elementType, data));
			}
		}
		
		return constructedSet;
	}
}
