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

package org.eclipse.imp.pdb.facts.impl.hash;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IListWriter;
import org.eclipse.imp.pdb.facts.IRelation;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.impl.Value;
import org.eclipse.imp.pdb.facts.type.FactTypeError;
import org.eclipse.imp.pdb.facts.type.ListType;
import org.eclipse.imp.pdb.facts.type.RelationType;
import org.eclipse.imp.pdb.facts.type.SetType;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;

public class List extends Value  implements IList {
	/* package */LinkedList<IValue> fList = new LinkedList<IValue>();

	/* package */ static class ListWriter implements IListWriter {
		private List fValue;

		public ListWriter(Type eltType) {
			fValue = new List(eltType);
		}

		public IList getList() {
			return fValue;
		}

		public void insert(IValue... elems) throws FactTypeError {
			for (IValue e : elems) {
			  fValue.checkInsert(e);
			  fValue.fList.add(0, e);
			}
		}

		public void insertAll(IList other) throws FactTypeError {
		    fValue.checkInsert(other.getElementType());
			fValue.fList.addAll(0, ((List) other).fList);
		}

		public void insertAll(IRelation other) throws FactTypeError {
			SetType st = ((RelationType) other.getType().getBaseType()).toSet();
			fValue.checkInsert(st.getElementType());
			for (ITuple t : other) {
				fValue.fList.add(0, t);
			}
		}

		public void insertAll(ISet set) throws FactTypeError {
			fValue.checkInsert(set.getElementType());
			for (IValue t : set) {
				fValue.fList.add(0, t);
			}
		}
		
		public void insertAll(Iterable<? extends IValue> collection) throws FactTypeError {
			for (IValue v : collection) {
				insert(v);
			}
		}

		public void append(IValue... elems) throws FactTypeError {
			for (IValue e : elems) {
			  fValue.checkInsert(e);
			  fValue.fList.add(e);
			}
		}
		
		public IList done() {
			return fValue;
		}

	}

	private Type fEltType;

	/* package */List(Type eltType) {
		super(TypeFactory.getInstance().listType(eltType));
		this.fEltType = eltType;
	}
	
	public Type getElementType() {
		return fEltType;
	}

	public IList append(IValue e) throws FactTypeError {
		IListWriter w = new ListWriter(checkInsert(e).getElementType());
		w.insertAll(this);
		w.append(e);
		return w.done();
	}

	public IValue get(int i) {
		return fList.get(i);
	}

	public IList insert(IValue e) throws FactTypeError {
		IListWriter w = new ListWriter(checkInsert(e).getElementType());
		
		w.insertAll(this);
		w.insert(e);
		return w.done();
	}

	public int length() {
		return fList.size();
	}

	public IList reverse() {
		IListWriter w = new ListWriter(getElementType());
		try {
			for (IValue v : this) {
				w.insert(v);
			}
		} catch (FactTypeError e) {
			// this will never happen
		} 
		return w.done();
	}

	public Iterator<IValue> iterator() {
		return fList.iterator();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		int idx = 0;
		for (IValue a : this) {
			if (idx++ > 0)
				sb.append(", ");
			sb.append(a.toString());
		}
		sb.append(" ]");
		return sb.toString();
	}

	private ListType checkInsert(IValue e) throws FactTypeError {
		checkInsert(e.getType());
		return (ListType) getType().getBaseType();
	}
	
	private void checkInsert(Type eltType) throws FactTypeError {
		if (!eltType.isSubtypeOf(fEltType)) {
			throw new FactTypeError("Element type " + eltType + " is not compatible with " + fEltType);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		// TODO: should we allow IList here?
		// TODO: should the types be equal?
		if (!(o instanceof List)) {
			return false;
		}
		List other = (List) o;
		return fList.equals(other.fList);
	}
	
	public <T> T accept(IValueVisitor<T> v) throws VisitorException {
		return v.visitList(this);
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		List tmp = new List(getElementType());
		tmp.fList = fList;
		return tmp;
	}

	public boolean isEmpty() {
		return fList.isEmpty();
	}
}
