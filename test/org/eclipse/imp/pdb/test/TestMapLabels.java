/*******************************************************************************
* Copyright (c) 2012 Centrum Wiskunde en Informatica (CWI)
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Anya Helene Bagge (University of Bergen) - implementation
*    Arnold Lankamp - base implementation (from TestBinaryIO.java)
*******************************************************************************/
package org.eclipse.imp.pdb.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.impl.fast.ValueFactory;
import org.eclipse.imp.pdb.facts.io.binary.BinaryReader;
import org.eclipse.imp.pdb.facts.io.binary.BinaryWriter;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.imp.pdb.facts.type.TypeStore;

/**
 * @author Anya Helene Bagge
 */
public class TestMapLabels extends TestCase {
	private TypeStore ts = new TypeStore();
	private TypeFactory tf = TypeFactory.getInstance();
	private IValueFactory vf = ValueFactory.getInstance();
	private Type routeType = tf.mapType(tf.stringType(), "from", tf.stringType(), "to");
	private Type dictType = tf.mapType(tf.stringType(), "key", tf.stringType(), "value");

	private Type a = tf.abstractDataType(ts, "A");
	private Type b = tf.abstractDataType(ts, "B");
	
	private IValue[] testValues = {
			vf.tuple(vf.string("foo"), vf.string("bar")),
			((IMap)routeType.make(vf)).put(vf.string("Bergen"), vf.string("Amsterdam")),
			vf.map(tf.stringType(), tf.stringType()).put(vf.string("New York"), vf.string("London")),
			((IMap)dictType.make(vf)).put(vf.string("Banana"), vf.string("Fruit")),
	};

	public void testNoLabels() {
		// make a non-labeled map type, and the labels should be null
		Type type = tf.mapType(a, b);
		
		assertNull(type.getKeyLabel());
		assertNull(type.getValueLabel());
	}
	
	public void testLabels() {
		// make a labeled map type, and the labels should match
		Type type = tf.mapType(a, "apple", b, "banana");
		
		assertEquals("apple", type.getKeyLabel());
		assertEquals("banana", type.getValueLabel());
	}

	public void testTwoLabels1() {
		// make two map types with same key/value types but different labels,
		// and the labels should be kept distinct
		Type type1 = tf.mapType(a, "apple", b, "banana");
		Type type2 = tf.mapType(a, "orange", b, "mango");
		Type type3 = tf.mapType(a, b);
		
		assertEquals("apple", type1.getKeyLabel());
		assertEquals("banana", type1.getValueLabel());
		assertEquals("orange", type2.getKeyLabel());
		assertEquals("mango", type2.getValueLabel());
		assertNull(type3.getKeyLabel());
		assertNull(type3.getValueLabel());
	}

	public void testTwoLabels2() {
		Type type1 = tf.mapType(a, "apple", b, "banana");
		Type type2 = tf.mapType(a, "orange", b, "mango");
		
		assertTrue("Two map types with different labels should be equivalent", type1.equivalent(type2));
		assertTrue("Two map types with different labels should be equivalent", type2.equivalent(type1));
		assertFalse("Two map types with different labels should not be equals", type1.equals(type2));
		assertFalse("Two map types with different labels should not be equals", type2.equals(type1));

		Type type3 = tf.mapType(a, b);
		assertTrue("Labeled and unlabeled maps should be equivalent", type1.equivalent(type3));
		assertTrue("Labeled and unlabeled maps should be equivalent", type3.equivalent(type1));
		assertTrue("Labeled and unlabeled maps should be equivalent", type2.equivalent(type3));
		assertTrue("Labeled and unlabeled maps should be equivalent", type3.equivalent(type2));
		assertFalse("Labeled and unlabeled maps should not be equals", type1.equals(type3));
		assertFalse("Labeled and unlabeled maps should not be equals", type3.equals(type1));
		assertFalse("Labeled and unlabeled maps should not be equals", type2.equals(type3));
		assertFalse("Labeled and unlabeled maps should not be equals", type3.equals(type2));
	}
	
	public void testLabelsIO(){
		try{
			for(int i = 0; i < testValues.length; i++){
				IValue value = testValues[i];
				
				System.out.println(value + " : " + value.getType()); // Temp

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				BinaryWriter binaryWriter = new BinaryWriter(value, baos, ts);
				binaryWriter.serialize();
				
				//PBFWriter.writeValueToFile(value, new File("/tmp/testIO"+i+".pbf")); // Temp

				byte[] data = baos.toByteArray();
				ByteArrayInputStream bais = new ByteArrayInputStream(data);
				BinaryReader binaryReader = new BinaryReader(vf, ts, bais);
				printBytes(data); // Temp
				IValue result = binaryReader.deserialize();

				System.out.println(result + " : " + result.getType()); // Temp
				System.out.println(); // Temp
				
				if(!value.isEqual(result)){
					String message = "Not equal: \n\t"+value+" : "+value.getType()+"\n\t"+result+" : "+result.getType();
					System.err.println(message);
					fail(message);
				}
			}
		}catch(IOException ioex){
			ioex.printStackTrace();
			fail(ioex.getMessage());
		}
	}
	
	private final static String[] HEX = new String[]{"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f"};
	
	// May be handy when debugging.
	private static void printBytes(byte[] bytes){
		for(int i = 0; i < bytes.length; i++){
			byte b = bytes[i];
			int higher = (b & 0xf0) >> 4;
			int lower = b & 0xf;
			System.out.print("0x");
			System.out.print(HEX[higher]);
			System.out.print(HEX[lower]);
			System.out.print(" ");
		}
		System.out.println();
	}
}
