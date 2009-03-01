/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 *******************************************************************************/

package org.eclipse.imp.pdb.facts.impl.reference;

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.imp.pdb.facts.IRelation;
import org.eclipse.imp.pdb.facts.IRelationWriter;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISetWriter;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.pdb.facts.exceptions.UnexpectedElementTypeException;
import org.eclipse.imp.pdb.facts.exceptions.UnexpectedResultTypeException;
import org.eclipse.imp.pdb.facts.impl.Value;
import org.eclipse.imp.pdb.facts.impl.Writer;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;

class Set extends Value implements ISet{
	final HashSet<IValue> content;

	/*package*/ Set(Type setType, HashSet<IValue> content){
		super(setType);
		
		this.content = content;
	}
	
	public boolean contains(IValue element) throws FactTypeUseException{
		return content.contains(element);
	}

	public boolean isEmpty(){
		return content.isEmpty();
	}

	public int size(){
		return content.size();
	}

	@SuppressWarnings("unchecked")
	public ISet insert(IValue element) {
		ISetWriter sw = ValueFactory.getInstance().setWriter(getElementType().lub(element.getType()));
		sw.insertAll(this);
		sw.insert(element);
		return sw.done();
	}

	@SuppressWarnings("unchecked")
	public ISet intersect(ISet other) {
		ISetWriter w = ValueFactory.getInstance().setWriter(other.getElementType().lub(getElementType()));
		Set o = (Set) other;
		
		for(IValue v : content){
			if(o.content.contains(v)){
				w.insert(v);
			}
		}
		
		return w.done();
	}

	@SuppressWarnings("unchecked")
	public ISet subtract(ISet other) {
		ISetWriter sw = ValueFactory.getInstance().setWriter(getElementType());
		for (IValue a : content){
			if (!other.contains(a)){
				sw.insert(a);
			}
		}
		return sw.done();
	}
	
	@SuppressWarnings("unchecked")
	public ISet delete(IValue elem) {
		ISetWriter sw = ValueFactory.getInstance().setWriter(getElementType());
		for (IValue a : content){
			if (!a.isEqual(elem)){
				sw.insert(a);
			}
		}
		return sw.done();
	}

	public boolean isSubSet(ISet other) {
		for (IValue elem : this) {
			if (!other.contains(elem)) {
				return false;
			}
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public <SetOrRel extends ISet> SetOrRel union(ISet other){
		ISetWriter w = ValueFactory.getInstance().setWriter(other.getElementType().lub(getElementType()));
		w.insertAll(this);
		w.insertAll(other);
		ISet result = w.done();
		
		try{
			// either a set or a relation is returned. If the caller
			// expected a relation, but the union constructed a set
			// this will throw a ClassCastException
			return (SetOrRel) result;
		}
		catch (ClassCastException e) {
			throw new UnexpectedResultTypeException(result.getType(), e);
		}
	}
	
	

	public Iterator<IValue> iterator(){
		return content.iterator();
	}

	public Type getElementType(){
		return fType.getElementType();
	}
	
	public boolean equals(Object o){
		if (o.getClass() == Set.class || o.getClass() == Relation.class) {
			Set other = (Set) o;
			
			return fType.comparable(other.fType) && content.equals(other.content);
		}
		return false;
	}
	
	@Override
	public int hashCode() { 
		return content.hashCode();
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append("{");
		
		Iterator<IValue> setIterator = iterator();
		if(setIterator.hasNext()){
			sb.append(setIterator.next());
			
			while(setIterator.hasNext()){
				sb.append(",");
				sb.append(setIterator.next());
			}
		}
		
		sb.append("}");
		
		return sb.toString();
	}

	public IRelation product(ISet set){
		Type resultType = TypeFactory.getInstance().tupleType(getElementType(),set.getElementType());
		IRelationWriter w = new Relation.RelationWriter(resultType);

		for(IValue t1 : this){
			for(IValue t2 : set){
				ITuple t3 = new Tuple(t1,t2);
				w.insert(t3);
			}
		}

		return w.done();
	}

	public <T> T accept(IValueVisitor<T> v) throws VisitorException{
		return v.visitSet(this);
	}
	
	private static void checkInsert(IValue elem, Type eltType) throws FactTypeUseException{
		Type type = elem.getType();
		if(!type.isSubtypeOf(eltType)){
			throw new UnexpectedElementTypeException(eltType, type);
		}
	}
	
	static ISetWriter createSetWriter(Type eltType){
		return new SetWriter(eltType);
	}
	
	protected static class SetWriter extends Writer implements ISetWriter{
		protected final Type eltType;
		protected final HashSet<IValue> setContent;
		
		protected Set constructedSet;

		public SetWriter(Type eltType){
			super();
			
			this.eltType = eltType;
			setContent = new HashSet<IValue>();
		}
		
		private void put(IValue elem){
			checkInsert(elem, eltType);
			setContent.add(elem);
		}

		public void insert(IValue... elems) throws FactTypeUseException{
			checkMutation();
			
			for(IValue elem : elems){
				put(elem);
			}
		}

		public void insertAll(Iterable<IValue> collection) throws FactTypeUseException{
			checkMutation();
			
			for(IValue v : collection){
				put(v);
			}
		}
		
		public ISet done(){
			if(constructedSet == null){
				constructedSet = new Set(TypeFactory.getInstance().setType(eltType), setContent);
			}
			return  constructedSet;
		}
		
		private void checkMutation(){
			if(constructedSet != null) throw new UnsupportedOperationException("Mutation of a finalized set is not supported.");
		}
		
		public int size() {
			return constructedSet.size();
		}
	}
}