/*
 * dk.brics.automaton
 * Copyright (C) 2001-2004 Anders Moeller
 * All rights reserved.
 */

package dk.brics.automaton;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * Finite-state automaton with fast run operation.
 * @author Anders M&oslash;ller &lt;<a href="mailto:amoeller@brics.dk">amoeller@brics.dk</a>&gt;
 */
public class RunAutomaton implements Serializable
{
    static final long serialVersionUID = 20001;

    int size;
    boolean[] accept;
    int initial;
    int[] transitions; // delta(state,c) = transitions[state*points.length + getCharClass(c)]

    char[] points; // char interval start points
    int[] classmap; // map from char number to class class

    /** Sets alphabet table for optimal run performance. */
    void setAlphabet()
    {
	classmap = new int[Character.MAX_VALUE - Character.MIN_VALUE + 1];
	int i = 0;
	for (int j = 0; j<=Character.MAX_VALUE - Character.MIN_VALUE; j++) {
	    if (i+1<points.length && j==points[i+1]) 
		i++;
	    classmap[j] = i;
	}
    }
    
    /** Returns a string representation of this automaton. */
    public String toString()
    {
	StringBuffer b = new StringBuffer();
	b.append("initial state: ").append(initial).append("\n");
	for (int i = 0; i<size; i++) {
	    b.append("state " + i);
	    if (accept[i])
		b.append(" [accept]:\n");
	    else
		b.append(" [reject]:\n");
	    for (int j = 0; j<points.length; j++) {
		int k = transitions[i*points.length + j];
		if (k!=-1) {
		    char min = points[j];
		    char max;
		    if (j+1<points.length)
			max = (char) (points[j+1]-1);
		    else
			max = Character.MAX_VALUE;
		    b.append(" ");
		    Transition.appendCharString(min, b);
		    if (min!=max) {
			b.append("-");
			Transition.appendCharString(max, b);
		    }
		    b.append(" -> ").append(k).append("\n");
		}
	    }
	}
	return b.toString();
    }

    /** Returns number of states in automaton. */
    public int getSize()
    {
	return size;
    }

    /** Returns acceptance status for given state. */
    public boolean isAccept(int state)
    {
	return accept[state];
    }

    /** Returns initial state. */
    public int getInitialState()
    {
	return initial;
    }

    /** 
     * Returns array of character class interval start points. 
     * The array should not be modified by the caller.
     */
    public char[] getCharIntervals()
    {
	return points;
    }

    /** gets character class of given char */
    int getCharClass(char c)
    {
	return Automaton.findIndex(c, points);
    }
    
    private RunAutomaton() {}

    /** 
     * Constructs a new <code>RunAutomaton</code> from a deterministic <code>Automaton</code>.
     * Same as <code>RunAutomaton(a, true)</code>.
     * @param a an automaton
    */
    public RunAutomaton(Automaton a)
    {
	this(a, true);
    }

    /** 
     * Retrieves a serialized <code>RunAutomaton</code> located by a URL.
     * @param url URL of serialized automaton
     * @exception IOException if input/output related exception occurs
     * @exception OptionalDataException if the data is not a serialized object
     * @exception InvalidClassException if the class serial number does not match
     * @exception ClassCastException if the data is not a serialized <code>RunAutomaton</code>
     * @exception ClassNotFoundException if the class of the serialized object cannot be found
     */
    public static RunAutomaton load(URL url)
	throws IOException, OptionalDataException, ClassCastException, 
	       ClassNotFoundException, InvalidClassException
    {
	return load(url.openStream());
    }

    /** 
     * Retrieves a serialized <code>RunAutomaton</code> from a stream.
     * @param stream input stream with serialized automaton
     * @exception IOException if input/output related exception occurs
     * @exception OptionalDataException if the data is not a serialized object
     * @exception InvalidClassException if the class serial number does not match
     * @exception ClassCastException if the data is not a serialized <code>RunAutomaton</code>
     * @exception ClassNotFoundException if the class of the serialized object cannot be found
     */
    public static RunAutomaton load(InputStream stream)
	throws IOException, OptionalDataException, ClassCastException, 
	       ClassNotFoundException, InvalidClassException
    {
	ObjectInputStream s = new ObjectInputStream(stream);
	return (RunAutomaton) s.readObject();
    }

    /**
     * Writes this <code>RunAutomaton</code> to the given stream.
     * @param stream output stream for serialized automaton
     * @exception IOException if input/output related exception occurs
     */
    public void store(OutputStream stream)
	throws IOException 
    {
	ObjectOutputStream s = new ObjectOutputStream(stream);
	s.writeObject(this);
	s.flush();
    }

    /** 
     * Constructs a new <code>RunAutomaton</code> from a deterministic <code>Automaton</code>.
     * If the given automaton is not deterministic, it is determinized first. 
     * @param a an automaton
     * @param tableize if true, a transition table is created which makes
     *   the <code>run</code> method faster in return of a higher memory usage
    */
    public RunAutomaton(Automaton a, boolean tableize)
    {
	a.determinize();
	points = a.getStartPoints();
	Set states = a.getStates();
	a.setStateNumbers(states);
	initial = a.initial.number;
	size = states.size();
	accept = new boolean[size];
	transitions = new int[size*points.length];
	for (int n = 0; n<size*points.length; n++)
	    transitions[n] = -1;
	Iterator i = states.iterator();
	while (i.hasNext()) {
	    State s = (State) i.next();
	    int n = s.number;
	    accept[n] = s.accept;
	    for (int c = 0; c<points.length; c++) {
		State q = s.step(points[c]);
		if (q!=null)
		    transitions[n*points.length+c] = q.number;
	    }
	}
	if (tableize)
	    setAlphabet();
    }

    /** Returns the state obtained by reading the given char from the given state.
	Returns -1 if not obtaining any such state. (If the original <code>Automaton</code>
	had no dead states, -1 is returned here if and only if a dead state is entered
	in an equivalent automaton with a total transition function.) */
    public int step(int state, char c)
    {
	if (classmap==null)
	    return transitions[state*points.length + getCharClass(c)];
	else
	    return transitions[state*points.length + classmap[c - Character.MIN_VALUE]];
    }

    /** Returns true if the given string is accepted by this automaton. */
    public boolean run(String s)
    {
	int p = initial;
	int l = s.length();
	for (int i = 0; i<l; i++) {
	    p = step(p, s.charAt(i));
	    if (p==-1)
		return false;
	}
	return accept[p];
    }

    /** 
     * Returns the length of the longest accepted run of the given string
     * starting at the given offset.
     * @param s the string
     * @param offset offset into <code>s</code> where the run starts
     * @return length of the longest accepted run, -1 if no run is accepted
     */
    public int run(String s, int offset) {
        int p = initial;
        int l = s.length();
        int max = -1;
        for (int r = 0; offset <= l; offset++, r++) {
            if (accept[p])
                max = r;
            if (offset == l)
                break;
            p = step(p, s.charAt(offset));
            if (p == -1)
                break;
        }
        return max;
    }
}

