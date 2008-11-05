/*******************************************************************************
* Copyright (c) 2007, 2008 IBM Corporation and CWI
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) 
*    Jurgen Vinju  (jurgen@vinju.org)       
*******************************************************************************/

package org.eclipse.imp.pdb.facts.type;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;

public class TypeFactory {
	private static class InstanceHolder {
      public static TypeFactory sInstance = new TypeFactory();
	}

    /**
     * Caches all types to implement canonicalization
     */
    private Map<Type,Type> fCache = new WeakHashMap<Type,Type>();

    /**
     * Keeps administration of declared type aliases (NamedTypes)
     */
    private Map<String, NamedType> fNamedTypes= new HashMap<String, NamedType>();
    
    /**
     * Keeps administration of declared tree node types
     */
    private Map<TreeSortType, List<TreeNodeType>> fSignatures = new HashMap<TreeSortType, List<TreeNodeType>>();

    /**
     * Keeps administration of declared annotations 
     */
    private Map<Type, Map<String, Type>> fAnnotations = new HashMap<Type, Map<String, Type>>();

    private ValueType sValueType= ValueType.getInstance();
    
    private VoidType sVoidType = VoidType.getInstance();
    
    @SuppressWarnings("unchecked")
	private ObjectType sProtoObjectType = new ObjectType(null);

    private IntegerType sIntegerType= IntegerType.getInstance();

    private DoubleType sDoubleType= DoubleType.getInstance();
    
    private StringType sStringType= StringType.getInstance();

    private SourceRangeType sSourceRangeType= SourceRangeType.getInstance();

    private SourceLocationType sSourceLocationType= SourceLocationType.getInstance();

    private SetType sProtoSet = new SetType(null);
    
    private MapType sProtoMap = new MapType(null, null);

    private RelationType sProtoRelation= new RelationType((TupleType) null);

    private TuplePrototype sProtoTuple= TuplePrototype.getInstance();
    
    private NamedType sProtoNamedType = new NamedType(null, null);
    
    private TreeSortType sProtoTreeSortType = new TreeSortType(null);

    private ListType sProtoListType = new ListType(null);
    
    private TreeNodeType sProtoTreeType = new TreeNodeType(null, null, null);

    public static TypeFactory getInstance() {
        return InstanceHolder.sInstance;
    }

    private TypeFactory() { }

    /**
     * construct the value type, which is the top type of the type hierarchy
     * representing all possible values.
     * @return a unique reference to the value type
     */
    public Type valueType() {
        return sValueType;
    }
    
    /**
     * construct the void type, which is the bottom of the type hierarchy,
     * representing no values at all.
     * 
     * @return a unique reference to the void type
     */
    public Type voidType() {
    	return sVoidType;
    }

    /**
     * Construct a new type. An object type imports a Java class into the PDB type hierarchy.
     * An ObjectType is a sub type of value. The Java type hierarchy is of no influence in the
     * PDB type hierarchy, so if class A is a subclass of class B, then Object[A] is not 
     * a sub type of Object[B] in the PDB class hierarchy.
     * 
     * @param <T>   The type of object that this objecttype wraps
     * @param clazz A class literal for <T>
     * @return a unique reference to a type that represents Java classes.
     */
    @SuppressWarnings("unchecked")
	public <T> ObjectType<T> objectType(Class<T> clazz) {
    	synchronized(fCache){
	    	sProtoObjectType.fClass = (Class<T>) clazz;
			Type result = fCache.get(sProtoObjectType);
	
			if (result == null) {
				result = new ObjectType(clazz);
				fCache.put(result, result);
			}
			return (ObjectType<T>) result;
    	}
    }
    
    
    /**
     * Construct a new type. 
     * @return a reference to the unique integer type of the PDB.
     */
    public IntegerType integerType() {
        return sIntegerType;
    }

    /**
     * Construct a new type. 
     * @return a reference to the unique double type of the PDB.
     */
    public DoubleType doubleType() {
        return sDoubleType;
    }

    /**
     * Construct a new type. 
     * @return a reference to the unique string type of the PDB.
     */
    public StringType stringType() {
        return sStringType;
    }

    /**
     * Construct a new type. 
     * @return a reference to the unique sourceRange type of the PDB.
     */
    public SourceRangeType sourceRangeType() {
        return sSourceRangeType;
    }

    /**
     * Construct a new type. 
     * @return a reference to the unique sourceLocation type of the PDB.
     */
    public SourceLocationType sourceLocationType() {
        return sSourceLocationType;
    }

    /**
     * A special subclass of TupleType to construct a prototype
     * with resizeable arrays for the field types and names.
     *
     */
    private static class TuplePrototype extends TupleType {
        private static final TuplePrototype sInstance= new TuplePrototype();

        public static TuplePrototype getInstance() {
            return sInstance;
        }

        private int fAllocatedWidth;
        private int fWidth;
        private int fStart = 0;

        private TuplePrototype() {
            super(7, 0, new Type[7], new String[7]);
        }

        /**
         * Sets the number of fields in this prototype tuple type. This forces the
         * recomputation of the (otherwise cached) hashcode.
         */
        private void setWidth(int N) {
            if ((fStart + N) > fAllocatedWidth) {
            	fAllocatedWidth= (fStart + N)*2;
                Type[] newFieldTypes = new Type[fAllocatedWidth];
                System.arraycopy(fFieldTypes, 0, newFieldTypes, 0, fStart);
                fFieldTypes = newFieldTypes;
                
                String[] newFieldNames = new String[fAllocatedWidth];
                System.arraycopy(fFieldNames, 0, newFieldNames, 0, fStart);
                fFieldNames = newFieldNames;
            }
            fWidth= N;
            fHashcode= -1;
        }

        /*package*/ Type[] getFieldTypes(int N) {
            setWidth(N);
            return fFieldTypes;
        }
        
        /*package*/ String[] getFieldNames(int N) {
        	setWidth(N);
        	return fFieldNames;
        }
        
        @Override
        public int hashCode() {
            if (fHashcode == -1) {
                fHashcode= 55501;
                for(int i= fStart, end = fStart + fWidth; i < end; i++) {
                    fHashcode= fHashcode * 44927 + fFieldTypes[i].hashCode();
                }
            }
            return fHashcode;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TupleType)) {
                return false;
            }
            TupleType other= (TupleType) obj;
            if (fWidth != other.fFieldTypes.length) {
                return false;
            }
            for(int i=fStart, j = 0, end = fStart + fWidth; i < end; i++, j++) {
                // N.B.: The field types must have been created and canonicalized before any
                // attempt to manipulate the outer type (i.e. SetType), so we can use object
                // identity here for the fFieldTypes.
                if (fFieldTypes[i] != other.fFieldTypes[j]) {
                    return false;
                }
                if (fFieldNames[i] != null && !fFieldNames[i].equals(other.fFieldNames[i])) {
                	return false;
                }
            }
            return true;
        }
    }

   

    private TupleType getOrCreateTuple(int size, Type[] fieldTypes) {
    	Type result= fCache.get(sProtoTuple);

        if (result == null) {
            result= new TupleType(size, sProtoTuple.fStart, fieldTypes);
            fCache.put(result, result);
        }
        return (TupleType) result;
    }
    

    private TupleType getOrCreateTuple(int size, Type[] fieldTypes, String[] fieldNames) {
    	Type result= fCache.get(sProtoTuple);

        if (result == null) {
            result= new TupleType(size, sProtoTuple.fStart, fieldTypes, fieldNames);
            fCache.put(result, result);
        }
        return (TupleType) result;
    }

    /**
     * This method is not for public use. Computes a product of a tuple
     * by concatenating the arrays of tuple types.
     * @param t1
     * @param t2
     * @return
     */
    /*package */ TupleType tupleProduct(TupleType t1, TupleType t2) {
    	int N = t1.getArity() + t2.getArity();
    	Type[] fieldTypes = sProtoTuple.getFieldTypes(N);
    	
    	for(int i = 0; i < t1.getArity(); i++) {
    		fieldTypes[i] = t1.getFieldType(i);
    	}
    	for (int i = t1.getArity(), j = 0; i < N; i++, j++) {
    		fieldTypes[i] = t2.getFieldType(j);
    	}
    	return getOrCreateTuple(N, fieldTypes);
    }
    
    /**
     * Construct a tuple type. 
     * @return a reference to the unique empty tuple type.
     */
    public TupleType tupleEmpty() {
    	Type[] fieldTypes = sProtoTuple.getFieldTypes(0);
    	return getOrCreateTuple(0, fieldTypes);
    }
    
    /**
     * Construct a tuple type.
     * @param fieldTypes a list of field types in order of appearance. The list is copied.
     * @return a tuple type
     */
    public TupleType tupleType(List<Type> fieldTypes) {
        int N= fieldTypes.size();
        Type[] protoFieldTypes= sProtoTuple.getFieldTypes(N);
        for(int i=0; i < N; i++) {
            protoFieldTypes[i]= fieldTypes.get(i);
        }
        return getOrCreateTuple(N, protoFieldTypes);
    }
    
    /**
     * Construct a tuple type.
     * @param fieldTypes a list of field types in order of appearance.
     * @return a tuple type
     */
    public TupleType tupleType(Type... fieldTypes) {
        int N= fieldTypes.length;
        Type[] protoFieldTypes= sProtoTuple.getFieldTypes(N);
        for(int i=0; i < N; i++) {
            protoFieldTypes[i]= fieldTypes[i];
        }
        return getOrCreateTuple(N, protoFieldTypes);
    }
    
    public TupleType tupleType(Object... fieldTypesAndLabels) {
    	int N= fieldTypesAndLabels.length;
        int arity = N / 2;
		Type[] protoFieldTypes= sProtoTuple.getFieldTypes(arity);
        String[] protoFieldNames = sProtoTuple.getFieldNames(arity);
        for(int i=0; i < N; i+=2) {
            protoFieldTypes[i / 2]= (Type) fieldTypesAndLabels[i];
            protoFieldNames[i / 2] = (String) fieldTypesAndLabels[i+1];
        }
        return getOrCreateTuple(arity, protoFieldTypes, protoFieldNames);
    }
    
    /**
     * Construct a tuple type.
     * @param fieldTypes an array of field types in order of appearance. The array is copied.
     * @return a tuple type
     */
    public TupleType tupleType(IValue... elements) {
        int N= elements.length;
        Type[] fieldTypes= sProtoTuple.getFieldTypes(N);
        for(int i=0; i < N; i++) {
            fieldTypes[i]= elements[i].getType();
        }
        return getOrCreateTuple(N, fieldTypes);
    }
    
    /**
     * Compute a new tupletype that is the lub of t1 and t2. 
     * Precondition: t1 and t2 have the same arity.
     * @param t1
     * @param t2
     * @return a TupleType which is the lub of t1 and t2
     */
    /* package */ TupleType lubTupleTypes(TupleType t1, TupleType t2) {
    	int N = t1.getArity();
    	
    	
    	// Note that this function may eventually be recursive (via lub) in the case of nested tuples.
    	// This poses a problem since we would overwrite sPrototuple.fFieldTypes
    	// Therefore fFieldTypes in the prototype is used as a stack, and fStart points to the
    	// bottom of the current stack frame.
    	// The goal is to prevent any kind of memory allocation when computing lub (for efficiency).
    	Type[] fieldTypes = sProtoTuple.getFieldTypes(N);
    	
    	// push the current frame to make room for the nested calls
    	int safeStart = sProtoTuple.fStart;
    	int safeWidth = sProtoTuple.fWidth;
    	sProtoTuple.fStart += N;
        
    	for (int i = safeStart, j = 0, end = safeStart + N; i < end; i++, j++) {
    		fieldTypes[i] = t1.getFieldType(j).lub(t2.getFieldType(j));
    	}
    	
    	// restore the current frame for creation of the tuple
    	sProtoTuple.fStart = safeStart;
    	sProtoTuple.fWidth = safeWidth;
    	sProtoTuple.fHashcode = -1;
    	TupleType result = getOrCreateTuple(N, fieldTypes);
    	
    	return result;
    }
    

    /**
     * Construct a set type
     * @param eltType the type of elements in the set
     * @return a set type
     */
    public SetType setType(Type eltType) {
        sProtoSet.fEltType= eltType;
        Type result= fCache.get(sProtoSet);

        if (result == null) {
            result= new SetType(eltType);
            fCache.put(result, result);
        }
        return (SetType) result;
    }

    /**
     * Constructs a new relation type from a tuple type. The tuple type
     * may be a named type (when type.getBaseType is a tuple type).
     * @param type a NamedType <= TupleType, or a TupleType
     * @return a relation type with the same field types that the tuple type has
     * @throws FactTypeError when type is not a tuple
     */
    public RelationType relType(NamedType type) throws FactTypeError {
    	try {
    	  return relType((TupleType) type.getBaseType());
    	}
    	catch (ClassCastException e) {
    		throw new FactTypeError("Type " + type + " is not a tuple type.", e);
    	}
    }
    
    /**
     * Construct a new relation type from a tuple type.
     * @param namedType the tuple type used to construct the field types
     * @return 
     * @throws FactTypeError
     */
    public RelationType relType(TupleType tupleType) {
        sProtoRelation.fTupleType= tupleType;

        Type result= fCache.get(sProtoRelation);

        if (result == null) {
            result= new RelationType(tupleType);
            fCache.put(result, result);
        }
        return (RelationType) result;
    }

    /**
     * Construct a relation type.
     * @param fieldTypes the types of the fields of the relation
     * @return a relation type
     */
    public RelationType relType(Type... fieldTypes) {
        return relType(tupleType(fieldTypes));
    }
    
    /**
     * Construct a relation type.
     * @param fieldTypes the types of the fields of the relation
     * @return a relation type
     */
    public RelationType relType(Object... fieldTypesAndLabels) {
        return relType(tupleType(fieldTypesAndLabels));
    }

    /** 
     * Construct a named type. Named types are subtypes of types. Note that in the near future
     * they will be type aliases.
     * @param name      the name of the type
     * @param superType the type it should be a subtype of (alias)
     * @return a named type
     * @throws TypeDeclarationException if a type with the same name but a different supertype was defined earlier as a named type of a TreeSortType.
     */
    public NamedType namedType(String name, Type superType) throws TypeDeclarationException {
    	sProtoNamedType.fName = name;
    	sProtoNamedType.fSuperType = superType;
    	
    	Type result= fCache.get(sProtoNamedType);

        if (result == null) {
        	NamedType old = fNamedTypes.get(name);
            if (old != null && !old.equals(result)) {
             	throw new TypeDeclarationException("Can not redeclare type " + old + " with a different type: " + superType);
            }
            
            sProtoTreeSortType.fName = name;
        	Type sort= fCache.get(sProtoTreeSortType);
        	if (result != null) {
        		throw new TypeDeclarationException("Can not redeclare tree sort type " + sort + " with a named type");
        	}
        	 
            if (!isIdentifier(name)) {
            	throw new TypeDeclarationException("This is not a valid identifier: " + name);
            }
            
            NamedType nt= new NamedType(name, superType);
            fCache.put(nt, nt);
            
           
            fNamedTypes.put(name, nt);
            result= nt;
        }
        return (NamedType) result;
    }
    
    /**
     * Construct a TreeSortType. A Tree sort is a kind of tree node. Each kind of tree node
     * may have different alternatives, which are TreeNodeTypes. A TreeNodeType is always a
     * sub type of a TreeSortType. A TreeSortType is always a sub type of value.
     * @param name the name of the tree sort
     * @return a TreeSortType
     * @throws TypeDeclarationException when a NamedType with the same name was already declared. Redeclaration of a TreeSortType is ignored.
     */
    public TreeSortType treeSortType(String name) throws TypeDeclarationException {
    	sProtoTreeSortType.fName = name;
    	
    	Type result= fCache.get(sProtoTreeSortType);

        if (result == null) {
            if (!isIdentifier(name)) {
            	throw new TypeDeclarationException("This is not a valid identifier: " + name);
            }
            
            NamedType old = fNamedTypes.get(name);
            if (old != null) {
             	throw new TypeDeclarationException("Can not redeclare a named type " + old + " with a tree sort type.");
            }
            
            TreeSortType nt= new TreeSortType(name);
            fCache.put(nt, nt);
            
            fSignatures.put(nt, new LinkedList<TreeNodeType>());
            result= nt;
        }
        
        // re-declaration of tree sort types is harmless
        
        return (TreeSortType) result;
    }
    
    /**
     * Construct a new node type. A node type is always a subtype of a TreeSortType. It
     * represents an alternative constructor for a specific TreeSortType. 
     * @param nodeType the type of node this constructor builds
     * @param name     the name of the node type
     * @param children the types of the children of the tree node type
     * @return a tree node type
     */
    public TreeNodeType treeNodeType(TreeSortType nodeType, String name, TupleType children) {
    	sProtoTreeType.fName = name;
    	sProtoTreeType.fChildrenTypes = children;
    	sProtoTreeType.fNodeType = nodeType;
    	
    	Type result = fCache.get(sProtoTreeType);
    	
    	if (result == null) {
			result = new TreeNodeType(name, children, nodeType);
			fCache.put(result, result);

			List<TreeNodeType> signature = fSignatures.get(nodeType);
			if (signature == null) {
				throw new TypeDeclarationException("Unknown tree sort type: " + nodeType);
			}
			signature.add((TreeNodeType) result);
			fSignatures.put(nodeType, signature);
		}
    	
    	return (TreeNodeType) result;
    }
    
    /**
     * Construct a new node type. A node type is always a subtype of a TreeSortType. It
     * represents an alternative constructor for a specific TreeSortType. 
     * @param nodeType the type of node this constructor builds
     * @param name     the name of the node type
     * @param children the types of the children of the tree node type
     * @return a tree node type
     */
    public TreeNodeType treeNodeType(TreeSortType nodeType, String name, Type... children ) { 
    	return treeNodeType(nodeType, name, tupleType(children));
    }
    
    /**
     * Construct a new node type. A node type is always a subtype of a TreeSortType. It
     * represents an alternative constructor for a specific TreeSortType. 
     * @param nodeType the type of node this constructor builds
     * @param name     the name of the node type
     * @param children the types of the children of the tree node type
     * @return a tree node type
     */
    public TreeNodeType treeNodeType(TreeSortType nodeType, String name, Object... childrenAndLabels ) { 
    	return treeNodeType(nodeType, name, tupleType(childrenAndLabels));
    }
    
    /**
     * Construct a special kind of tree node. This tree node does not have
     * a name, always has exactly one child. It is used for serialized values
     * where one alternative for a TreeSortType does not have a wrapping node name.
     * Each TreeSortType may maximally have one anonymous tree node type.
     * 
     * @param sort        the sort this constructor builds      
     * @param string      the name of the alternative (even though it will not be used)
     * @param argType     the type of the single child
     * @param label       the label of the single child
     * @return
     */
    public TreeNodeType anonymousTreeType(TreeSortType sort, String string,
			Type argType, String label) {
    	return treeNodeType(sort, null, TypeFactory.getInstance().tupleType(argType, label));
	}

    /**
     * Lookup a NamedType that was declared before by name
     * @param name the name of the type to lookup
     * @return
     */
    public NamedType lookupNamedType(String name) {
        return fNamedTypes.get(name);
    }
    
    /**
     * Returns all alternative ways of constructing a certain type name using
     * a tree type.
     * 
     * @param type
     * @return all tree node types that construct the given type
     */
    public List<TreeNodeType> lookupTreeNodeTypes(TreeSortType type) {
    	return fSignatures.get(type);
    }
    
    /**
     * Lookup a TreeNodeType by name, and in the context of a certain TreeSortType
     * @param type             the TreeSortType context
     * @param constructorName  the name of the TreeNodeType
     * @return a TreeNodeType if it was declared before
     * @throws a FactTypeError if the type was not declared before
     */
    public List<TreeNodeType> lookupTreeNodeType(TreeSortType type, String constructorName) throws FactTypeError {
    	List<TreeNodeType> result = new LinkedList<TreeNodeType>();
    	
    	for (TreeNodeType node : fSignatures.get(type)) {
    		String name = node.getName();
			if (name != null && name.equals(constructorName)) {
    			result.add(node);
    		}
			else if (name == null && constructorName == null) {
				result.add(node);
			}
    	}

    	return result;
    }
    
    /**
     * Retrieve the type for an anonymous constructor.  
     * See @link {@link TypeFactory#anonymousTreeType(TreeSortType, String, Type, String)})
     * for more information.
     * @param type TreeSortType to lookup the constructor for
     * @return an anonymous tree node type
     * @throws FactTypeError if the type does not have an anonymous constructor
     */
    public TreeNodeType lookupAnonymousTreeNodeType(TreeSortType type) throws FactTypeError {
    	for (TreeNodeType node : fSignatures.get(type)) {
    		if (node.getName() == null) {
    			return node;
    		}
    	}
    	
    	throw new FactTypeError("Type does not have an anonymous constructor: " + type);
    }
    
    /** 
     * Retrieve all tree node types for a given constructor name, 
     * regardless of tree sort type
     * @param constructName the name of the tree node
     */
    public List<TreeNodeType> lookupTreeNodeType(String constructorName) {
    	List<TreeNodeType> result = new LinkedList<TreeNodeType>();
    	for (TreeSortType sort : fSignatures.keySet()) {
    		for (TreeNodeType node : fSignatures.get(sort)) {
        		String name = node.getName();
    			if (name != null && name.equals(constructorName)) {
        			result.add(node);
        		}
        	}	
    	}
    	
    	return result;
    }

    /**
     * Construct a list type
     * @param elementType the type of the elements in the list
     * @return a list type
     */
    public ListType listType(Type elementType) {
		sProtoListType.fEltType = elementType;
		Type result= fCache.get(sProtoListType);

        if (result == null) {
            result= new ListType(elementType);
            fCache.put(result, result);
        }
        return (ListType) result;
	}
    
    /**
     * Construct a map type
     * @param key    the type of the keys in the map
     * @param value  the type of the values in the map
     * @return a map type
     */
    public MapType mapType(Type key, Type value) {
    	sProtoMap.fKeyType = key;
    	sProtoMap.fValueType = value;
		Type result= fCache.get(sProtoMap);

        if (result == null) {
            result= new MapType(key, value);
            fCache.put(result, result);
        }
        
        return (MapType) result;
	}
    
    /**
     * Declare that certain types of values may have an annotation with a certain
     * label. The annotation with that label will have a specific type.
     * 
     * @param onType the type of values that carry this annotation
     * @param key    the label of the annotation
     * @param valueType the type of values that represent the annotation
     */
    public void declareAnnotation(Type onType, String key, Type valueType) {
    	Map<String, Type> annotationsForType = fAnnotations.get(onType);
    	
    	if (!isIdentifier(key)) {
    		throw new FactTypeError("Key " + key + " is not an identifier.");
    	}
    	
    	if (annotationsForType == null) {
    		annotationsForType = new HashMap<String, Type>();
    		fAnnotations.put(onType, annotationsForType);
    	}
    	
    	Map<String, Type> declaredEarlier = getAnnotations(onType);
    	
    	if (!declaredEarlier.containsKey(key)) {
    	  annotationsForType.put(key, valueType);
    	}
    	else if (!declaredEarlier.get(key).equals(valueType)) {
    		throw new FactTypeError("Annotation was declared previously with different type: " + declaredEarlier.get(key));
    	}
    	
    	// otherwise its a safe re-declaration and we do nothing
    }
    
    /**
     * Locates all declared annotations for a type, including the annotations declared
     * for all of its super types.
     * 
     * @param onType
     * @return
     */
    public Map<String, Type> getAnnotations(Type onType) {
    	Map<String, Type> result = new HashMap<String,Type>();
    	
    	Map<String, Type> valueAnnotations = fAnnotations.get(sValueType);
    	if (valueAnnotations != null) {
    		result.putAll(valueAnnotations);
    	}
    	
    	Map<String, Type> localAnnotations = fAnnotations.get(onType);
    	if (localAnnotations != null) {
    	  result.putAll(localAnnotations);
    	}
    	
    	while (onType.isNamedType()) {
    		onType = ((NamedType) onType).getSuperType();
    		localAnnotations = fAnnotations.get(onType);
    		if (localAnnotations != null) {
    		  result.putAll(localAnnotations);
    		}
    	}
    	
    	if (onType.isTreeNodeType()) {
    		localAnnotations = fAnnotations.get(((TreeNodeType) onType).getTreeSortType());
    		if (localAnnotations != null) {
    		  result.putAll(localAnnotations);
    		}
    	}
    	
    	if (onType.isSetType() && ((SetType) onType).getElementType().isTupleType()) {
    		RelationType tmp = relType(((SetType) onType).getElementType());
    		localAnnotations = fAnnotations.get(tmp);
    		if (localAnnotations != null) {
    		  result.putAll(localAnnotations);
    		}
    	}
    	
    	if (onType.isRelationType()) {
    		SetType tmp = setType(((RelationType) onType).getFieldTypes());
    		localAnnotations = fAnnotations.get(tmp);
    		if (localAnnotations != null) {
    		  result.putAll(localAnnotations);
    		}
    	}
    	
    	return result;
    }
    
    /**
     * Retrieve the type of values that are declared to be valid for a certain kind of 
     * annotations on certain kinds of values
     * @param onType the type of values that this annotation can be found on
     * @param key    the label of the annotation to find the corresponding type of
     * @return the type of the requested annotation value
     */
    public Type getAnnotationType(Type onType, String key) {
    	Map<String, Type> annotationsFor = getAnnotations(onType);
    	Type result = annotationsFor.get(key);
    	
    	if (result != null) {
    		return result;
    	}
    	
    	return null;
    }

	/*package*/ TupleType tupleCompose(TupleType type, TupleType other) {
		int N = type.getArity() + other.getArity() - 2;
		Type[] fieldTypes = sProtoTuple.getFieldTypes(N);
		
		for (int i = 0; i < type.getArity() - 1; i++) {
			fieldTypes[i] = type.getFieldType(i);
		}
		
		for (int i = type.getArity() - 1, j = 1; i < N; i++, j++) {
			fieldTypes[i] = other.getFieldType(j);
		}
		
		return getOrCreateTuple(N, fieldTypes);
	}

	/*package*/ RelationType relationProduct(RelationType type1, RelationType type2) {
		int N = type1.getArity() + type2.getArity();
		Type[] fieldTypes = sProtoTuple.getFieldTypes(N);
		
		for (int i = 0; i < type1.getArity(); i++) {
			fieldTypes[i] = type1.getFieldType(i);
		}
		
		for (int i = type1.getArity(), j = 0; i < N; i++, j++) {
			fieldTypes[i] = type2.getFieldType(j);
		}
		
		return relType(getOrCreateTuple(N, fieldTypes));
	}
	
	/**
	 * Checks to see if a string is a valid PDB identifier
	 * 
	 * @param str
	 * @return
	 */
	/* package */ static boolean isIdentifier(String str) {
		byte[] contents = str.getBytes();

		if (str.length() == 0) {
			return false;
		}

		if (!Character.isJavaIdentifierStart(contents[0])) {
			return false;
		}

		if (str.length() > 1) {
			for (int i = 1; i < contents.length; i++) {
				if (!Character.isJavaIdentifierPart(contents[i]) &&
					contents[i] != '.') {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Construct a type that is represented by this value. Will only work for values
	 * that have been constructed using {@link TypeDescriptorFactory#toTypeDescriptor(IValueFactory, Type)},
	 * or something that exactly mimicked it.
	 * 
	 * @param descriptor a value that represents a type
	 * @return a type that was represented by the descriptor
	 * @throws TypeDeclarationException if the descriptor is not a valid type descriptor
	 */
	Type fromDescriptor(IValue typeDescriptor) throws TypeDeclarationException {
		return TypeDescriptorFactory.getInstance().fromTypeDescriptor(typeDescriptor);
	}
}
