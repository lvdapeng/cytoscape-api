/* vim: set ts=2: */
/* %% Ignore-License */
/*
 * Copyright (c) 2006 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.cytoscape.view.layout;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;


/**
 * The LayoutNode class.  This class is used as a container for information
 * about the nodes in a layout.  In particular, it provides a convenient handle
 * to information about position, the node itself, the node view.  Many of
 * the methods of this class are wrappers for Node or View<CyNode> methods, and
 * these are often wrapped by LayoutPartition methods.
 * @CyAPI.Final.Class
 * @CyAPI.InModule layout-api
 */
public final class LayoutNode implements Comparable<LayoutNode> {
	// static (class) variables
	private static final double EPSILON = 0.0000001D;

	// instance variables
	private double x;
	private double y;
	private double z;
	private double dispX;
	private double dispY;
	private double dispZ;
	private CyNode node;
	private CyRow row;
	private View<CyNode> nodeView;
	private int index;
	private boolean isLocked = false;

	private List<LayoutNode> neighbors = null;

	/**
	 * The main constructor for a LayoutNode.
	 *
	 * @param nodeView The View<CyNode> of this node
	 * @param index The index (usually in a node array) of this node
	 */
	public LayoutNode(View<CyNode> nodeView, int index, CyRow row) {
		this.nodeView = nodeView;
		this.node = nodeView.getModel();
		this.row = row;
		this.index = index;
		this.x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
		this.y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
		this.z = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION);
		this.neighbors = new ArrayList<LayoutNode>();
	}

	/**
	 * Accessor function to return the Node associated with
	 * this LayoutNode.
	 *
	 * @return    Node that is associated with this LayoutNode
	 */
	public CyNode getNode() {
		return this.node;
	}

	/**
	 * Accessor function to return the CyRow associated with
	 * this LayoutNode.
	 *
	 * @return    CyRow that is associated with this LayoutNode
	 */
	public CyRow getRow() {
		return this.row;
	}

	/**
	 * Accessor function to return the View<CyNode> associated with
	 * this LayoutNode.
	 *
	 * @return    View<CyNode> that is associated with this LayoutNode
	 */
	public View<CyNode> getNodeView() {
		return this.nodeView;
	}

	/**
	 * Set the 2D location of this LayoutNode.  Note that this only
	 * sets the location -- it does not actually move the node to
	 * that location.  Users should call moveToLocation to actually
	 * accomplish the move.
	 *
	 * @param    x    Double representing the new X coordinate of this node
	 * @param    y    Double representing the new Y coordinate of this node
	 */
	public void setLocation(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Set the 3D location of this LayoutNode.  Note that this only
	 * sets the location -- it does not actually move the node to
	 * that location.  Users should call moveToLocation to actually
	 * accomplish the move.
	 *
	 * @param    x    Double representing the new X coordinate of this node
	 * @param    y    Double representing the new Y coordinate of this node
	 * @param    z    Double representing the new Z coordinate of this node
	 */
	public void setLocation(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Set the X location of this LayoutNode.  Note that this only
	 * sets the location -- it does not actually move the node to
	 * that location.  Users should call moveToLocation to actually
	 * accomplish the move.
	 *
	 * @param    x    Double representing the new X coordinate of this node
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * Set the Y location of this LayoutNode.  Note that this only
	 * sets the location -- it does not actually move the node to
	 * that location.  Users should call moveToLocation to actually
	 * accomplish the move.
	 *
	 * @param    y    Double representing the new Y coordinate of this node
	 */
	public void setY(double y) {
		this.y = y;
	}
	
	/**
	 * Set the Z location of this LayoutNode.  Note that this only
	 * sets the location -- it does not actually move the node to
	 * that location.  Users should call moveToLocation to actually
	 * accomplish the move.
	 *
	 * @param    z    Double representing the new Z coordinate of this node
	 */
	public void setZ(double z) {
		this.z = z;
	}

	/**
	 * Set the 2D displacement of this LayoutNode.  The displacement is a
	 * little different than the location in that it records an offset from
	 * the current location.  This is useful for algorithms such as Kamada Kawai
	 * and Fructerman Rheingold, which update positions iteratively.
	 *
	 * @param    x    Double representing the amount to offset in the x direction
	 * @param    y    Double representing the amount to offset in the y direction
	 */
	public void setDisp(double x, double y) {
		this.dispX = x;
		this.dispY = y;
	}
	
	/**
	 * Set the 3D displacement of this LayoutNode.  The displacement is a
	 * little different than the location in that it records an offset from
	 * the current location.  This is useful for algorithms such as Kamada Kawai
	 * and Fructerman Rheingold, which update positions iteratively.
	 *
	 * @param    x    Double representing the amount to offset in the x direction
	 * @param    y    Double representing the amount to offset in the y direction
	 * @param    z    Double representing the amount to offset in the y direction
	 */
	public void setDisp(double x, double y, double z) {
		this.dispX = x;
		this.dispY = y;
		this.dispZ = z;
	}

	/**
	 * Convenience function to keep track of neighbors of this node.  This can be
	 * used to improve the performance of algorithms that try to determine the edge
	 * partners of nodes.
	 *
	 * @param    v    LayoutNode that is a neighbor of this LayoutNode
	 */
	public void addNeighbor(LayoutNode v) {
		this.neighbors.add(v);
	}

	/**
	 * Convenience function to return the list of neighbors (connected nodes) of this node.
	 *
	 * @return        List of all of the neighbors (nodes with shared edges) of this node.
	 */
	public List<LayoutNode> getNeighbors() {
		return this.neighbors;
	}

	/**
	 * Returns the index of this LayoutNode.  This is <em>not</em> the same as the
	 * rootGraphIndex of the node.  Its primarily used by LayoutPartition to keep
	 * track of the offset in the node array that holds this LayoutNode.
	 *
	 * @return        The index of this node
	 * @see    LayoutPartition
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * Register this node as being "locked".  Locked nodes are exempt from being moved
	 * during layout.  Usually, these are the unselected nodes when a selected-only
	 * layout is being executed.
	 */
	public void lock() {
		this.isLocked = true;
	}

	/**
	 * Register this node as being "unlocked".  Locked nodes are exempt from being moved
	 * during layout.  Usually, these are the unselected nodes when a selected-only
	 * layout is being executed.  The "unlocked" state is the default.
	 */
	public void unLock() {
		this.isLocked = false;
	}

	/**
	 * Returns "true" if this node is locked, false otherwise.
	 *
	 * @return        true if locked, false if unlocked.
	 */
	public boolean isLocked() {
		return isLocked;
	}

	/**
	 * Increment the displacement recorded for this node by (x,y).
	 *
	 * @param    x    the additional amount to displace in the x direction
	 * @param    y    the additional amount to displace in the y direction
	 */
	public void incrementDisp(double x, double y) {
		this.dispX += x;
		this.dispY += y;
	}
	
	/**
	 * Increment the displacement recorded for this node by (x,y,z).
	 *
	 * @param    x    the additional amount to displace in the x direction
	 * @param    y    the additional amount to displace in the y direction
	 * @param    z    the additional amount to displace in the z direction
	 */
	public void incrementDisp(double x, double y, double z) {
		this.dispX += x;
		this.dispY += y;
		this.dispZ += z;
	}

	/**
	 * Increment the location of this node by (x,y).  Note that location
	 * values are merely recorded until moveToLocation is called.
	 *
	 * @param    x    the amount to move in the x direction
	 * @param    y    the amount to move in the y direction
	 */
	public void increment(double x, double y) {
		this.x += x;
		this.y += y;
	}
	
	/**
	 * Increment the location of this node by (x,y,z).  Note that location
	 * values are merely recorded until moveToLocation is called.
	 *
	 * @param    x    the amount to move in the x direction
	 * @param    y    the amount to move in the y direction
	 * @param    z    the amount to move in the z direction
	 */
	public void increment(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
	}

	/**
	 * Decrement the displacement recorded for this node by (x,y).
	 *
	 * @param    x    the additional amount to displace in the -x direction
	 * @param    y    the additional amount to displace in the -y direction
	 */
	public void decrementDisp(double x, double y) {
		this.dispX -= x;
		this.dispY -= y;
	}
	
	/**
	 * Decrement the displacement recorded for this node by (x,y,z).
	 *
	 * @param    x    the additional amount to displace in the -x direction
	 * @param    y    the additional amount to displace in the -y direction
	 * @param    z    the additional amount to displace in the -z direction
	 */
	public void decrementDisp(double x, double y, double z) {
		this.dispX -= x;
		this.dispY -= y;
		this.dispZ -= z;
	}

	/**
	 * Decrement the location of this node by (x,y).  Note that location
	 * values are merely recorded until moveToLocation is called.
	 *
	 * @param    x    the amount to move in the -x direction
	 * @param    y    the amount to move in the -y direction
	 */
	public void decrement(double x, double y) {
		this.x -= x;
		this.y -= y;
	}
	
	/**
	 * Decrement the location of this node by (x,y,z).  Note that location
	 * values are merely recorded until moveToLocation is called.
	 *
	 * @param    x    the amount to move in the -x direction
	 * @param    y    the amount to move in the -y direction
	 * @param    z    the amount to move in the -z direction
	 */
	public void decrement(double x, double y, double z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
	}

	/**
	 * Return the current X value for this LayoutNode.
	 *
	 * @return        current X value
	 */
	public double getX() {
		return this.x;
	}

	/**
	 * Return the current Y value for this LayoutNode.
	 *
	 * @return        current Y value
	 */
	public double getY() {
		return this.y;
	}
	
	/**
	 * Return the current Z value for this LayoutNode.
	 *
	 * @return        current Z value
	 */
	public double getZ() {
		return this.z;
	}

	/**
	 * Return the current X displacement value for this LayoutNode.
	 *
	 * @return        current X displacement value
	 */
	public double getXDisp() {
		return this.dispX;
	}

	/**
	 * Return the current Y displacement value for this LayoutNode.
	 *
	 * @return        current Y displacement value
	 */
	public double getYDisp() {
		return this.dispY;
	}

	/**
	 * Return the current Z displacement value for this LayoutNode.
	 *
	 * @return        current Z displacement value
	 */
	public double getZDisp() {
		return this.dispZ;
	}
	
	/**
	 * Return the width of this node
	 *
	 * @return        width of this node
	 */
	public double getWidth() {
		return this.nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
	}

	/**
	 * Return the height of this node
	 *
	 * @return        height of this node
	 */
	public double getHeight() {
		return this.nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT);
	}

	/**
	 * Return the height of this node
	 *
	 * @return        height of this node
	 */
	public double getDepth() {
		return this.nodeView.getVisualProperty(BasicVisualLexicon.NODE_DEPTH);
	}
	
	/**
	 * Return the Euclidean distance (in 2D) between this node and another node.
	 * This method only considers the X and Y coordinates of the nodes.
	 *
	 * @param    otherNode    the node to measure the distance to
	 * @return        the Euclidean distance from this node to otherNode
	 */
	public double distance(LayoutNode otherNode) {
		double deltaX = this.x - otherNode.getX();
		double deltaY = this.y - otherNode.getY();

		return Math.max(EPSILON, Math.sqrt((deltaX * deltaX) + (deltaY * deltaY)));
	}
	
	/**
	 * Return the Euclidean distance (in 3D) between this node and another node.
	 * This method considers the X, Y and Z coordinates of the nodes.
	 *
	 * @param    otherNode    the node to measure the distance to
	 * @return        the Euclidean distance from this node to otherNode
	 */
	public double distance3D(LayoutNode otherNode) {
		double deltaX = this.x - otherNode.getX();
		double deltaY = this.y - otherNode.getY();
		double deltaZ = this.z - otherNode.getZ();

		return Math.max(EPSILON, Math.sqrt((deltaX * deltaX) + (deltaY * deltaY) + (deltaZ * deltaZ)));
	}

	/**
	 * Return the Euclidean distance (in 2D) between this node and a location.
	 * This method only considers the X and Y coordinates of the nodes.
	 *
	 * @param    uX    the X location to measure the distance to
	 * @param    uY    the Y location to measure the distance to
	 * @return        the Euclidean distance from this node to (uX,uY)
	 */
	public double distance(double uX, double uY) {
		double deltaX = this.x - uX;
		double deltaY = this.y - uY;

		return Math.max(EPSILON, Math.sqrt((deltaX * deltaX) + (deltaY * deltaY)));
	}
	
	/**
	 * Return the Euclidean distance (in 3D) between this node and a location.
	 * This method considers the X, Y and Z coordinates of the nodes.
	 *
	 * @param    uX    the X location to measure the distance to
	 * @param    uY    the Y location to measure the distance to
	 * @param    uZ    the Z location to measure the distance to
	 * @return        the Euclidean distance from this node to (uX,uY,uZ)
	 */
	public double distance3D(double uX, double uY, double uZ) {
		double deltaX = this.x - uX;
		double deltaY = this.y - uY;
		double deltaZ = this.z - uZ;

		return Math.max(EPSILON, Math.sqrt((deltaX * deltaX) + (deltaY * deltaY) + (deltaZ * deltaZ)));
	}

	/**
	 * Move the node to its current x,y coordinate (ignores the value of the z coordinate).
	 */
	public void moveToLocation() {
		if (isLocked) {
			this.x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			this.y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
		} else {
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, this.x);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, this.y);
		}
	}
	
	/**
	 * Move the node to its current x,y,z coordinate.
	 */
	public void moveToLocation3D() {
		if (isLocked) {
			this.x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			this.y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			this.z = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION);
		} else {
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, this.x);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, this.y);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_Z_LOCATION, this.z);
		}
	}

	/**
	 * Return the node's identifier.
	 *
	 * @return        String containing the node's identifier
	 */
	public String getIdentifier() {
		return node.getSUID().toString();
	}

	/**
	 * Return the node's degree (i.e. number of nodes it's connected to).
	 *
	 * @return        Degree of this node
	 */
	public int getDegree() {
		return neighbors.size();
	}

	/**
	 * Return a string representation of the node
	 *
	 * @return        String containing the node's identifier and location
	 */
	public String toString() {
		return "Node " + getIdentifier() + " at " + printLocation();
	}

	/**
	 * Return a string representation of the node's displacement
	 *
	 * @return        String containing the node's X,Y displacement
	 */
	public String printDisp() {
		return "" + dispX + ", " + dispY;
	}

	/**
	 * Return a string representation of the node's location
	 *
	 * @return        String containing the node's X,Y location
	 */
	public String printLocation() {
		return "" + x + ", " + y;
	}

	@Override
	public int compareTo(LayoutNode nodeView) {
		return getIdentifier().compareTo(nodeView.getIdentifier());
	}
}
