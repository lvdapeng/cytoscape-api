package org.cytoscape.view.presentation.property;

/*
 * #%L
 * Cytoscape Presentation API (presentation-api)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.ContinuousRange;
import org.cytoscape.view.model.DiscreteRange;
import org.cytoscape.view.model.NullDataType;
import org.cytoscape.view.model.Range;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.Visualizable;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic Implementation of VisualLexicon.
 * 
 * @CyAPI.Abstract.Class
 * @CyAPI.InModule presentation-api
 */
public class BasicVisualLexicon implements VisualLexicon {

	private static final Logger logger = LoggerFactory.getLogger("org.cytoscape.application.userlog");

	private static final double DEF_BORDER_WIDTH = 2.0d;
	private static final int DEF_FONT_SIZE = 12;

	private final Map<Class<?>, Map<String, VisualProperty<?>>> identifierLookup;
	private final Map<VisualProperty<?>, VisualLexiconNode> visualPropertyMap;

	/**
	 * The Root of this tree.
	 */
	protected final VisualProperty<NullDataType> rootVisualProperty;

	// TODO move these!
	protected static final Color MIN_COLOR = new Color(0, 0, 0);
	protected static final Color MAX_COLOR = new Color(0xFF, 0xFF, 0xFF);
	protected static final Range<Paint> PAINT_RANGE = new ContinuousRange<>(Paint.class, MIN_COLOR, MAX_COLOR,
			true, true);

	protected static final Set<String> STRING_SET = new HashSet<String>();
	// This will be used to for String VP which accepts any string values.
	protected static final Range<String> ARBITRARY_STRING_RANGE = new DiscreteRange<String>(String.class, STRING_SET) {
		// Takes any String as valid value.
		@Override
		public boolean inRange(String value) {
			return true;
		}
	};

	protected static final Range<Double> ARBITRARY_DOUBLE_RANGE = new ContinuousRange<>(Double.class,
			Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true, true);
	protected static final Range<Double> NONE_ZERO_POSITIVE_DOUBLE_RANGE = new ContinuousRange<>(Double.class,
			0d, Double.POSITIVE_INFINITY, false, true);

	// Top level nodes has null as parent, and will be pointed by parent node.
	// This is because all VPs are static objects.

	// //// Top level objects ///////
	public static final VisualProperty<Visualizable> NETWORK = new DefaultVisualizableVisualProperty("NETWORK",
			"Network Visual Property", CyNetwork.class);
	public static final VisualProperty<Visualizable> NODE = new DefaultVisualizableVisualProperty("NODE",
			"Node Visual Property", CyNode.class);
	public static final VisualProperty<Visualizable> EDGE = new DefaultVisualizableVisualProperty("EDGE",
			"Edge Visual Property", CyEdge.class);

	// //// Node VP /////////

	/**
	 * The color of the whole node, including its border, label and selected paint. 
	 */
	public static final VisualProperty<Paint> NODE_PAINT = new PaintVisualProperty(new Color(120, 120, 120),
			PAINT_RANGE, "NODE_PAINT", "Node Paint", CyNode.class);

	/**
	 * The color of the node.
	 */
	public static final VisualProperty<Paint> NODE_FILL_COLOR = new PaintVisualProperty(new Color(200, 0, 0),
			PAINT_RANGE, "NODE_FILL_COLOR", "Node Fill Color", CyNode.class);

	/**
	 * The color of the node label.
	 */
	public static final VisualProperty<Paint> NODE_LABEL_COLOR = new PaintVisualProperty(Color.BLACK, PAINT_RANGE,
			"NODE_LABEL_COLOR", "Node Label Color", CyNode.class);

	/**
	 * The text used for the node label.
	 */
	public static final VisualProperty<String> NODE_LABEL = new StringVisualProperty("", ARBITRARY_STRING_RANGE,
			"NODE_LABEL", "Node Label", CyNode.class);

	/**
	 * X location of the node. Default value of this will be ignored. The value will be used only when mapping function is defined.
	 */
	public static final VisualProperty<Double> NODE_X_LOCATION = new DoubleVisualProperty(0.0, ARBITRARY_DOUBLE_RANGE,
			"NODE_X_LOCATION", "Node X Location", true, CyNode.class);

	/**
	 * X location of the node. Default value of this will be ignored. The value will be used only when mapping function is defined.
	 */
	public static final VisualProperty<Double> NODE_Y_LOCATION = new DoubleVisualProperty(0.0, ARBITRARY_DOUBLE_RANGE,
			"NODE_Y_LOCATION", "Node Y Location", true, CyNode.class);

	/**
	 * The size of the node. Width and height will be equal. This property is mutually exclusive of Node Height and Node Width.
	 */
	public static final VisualProperty<Double> NODE_SIZE = new DoubleVisualProperty(50.0,
			NONE_ZERO_POSITIVE_DOUBLE_RANGE, "NODE_SIZE", "Node Size", CyNode.class);

	/**
	 * The width of the node. Width will be independent of height. This property is mutually exclusive of Node Size.
	 */
	public static final VisualProperty<Double> NODE_WIDTH = new DoubleVisualProperty(60.0,
			NONE_ZERO_POSITIVE_DOUBLE_RANGE, "NODE_WIDTH", "Node Width", CyNode.class);

	/**
	 * The height of the node. Height will be independent of width. This property is mutually exclusive of Node Size.
	 */	
	public static final VisualProperty<Double> NODE_HEIGHT = new DoubleVisualProperty(40.0,
			NONE_ZERO_POSITIVE_DOUBLE_RANGE, "NODE_HEIGHT", "Node Height", CyNode.class);
	/**
	 * Hides the node if set to false. By default, this value is set to true.
	 */	
	public static final VisualProperty<Boolean> NODE_VISIBLE = new BooleanVisualProperty(true, "NODE_VISIBLE",
			"Node Visible", CyNode.class);
	/**
	 * TODO Documentation
	 */
	public static final VisualProperty<Boolean> NODE_SELECTED = new BooleanVisualProperty(false, "NODE_SELECTED",
			"Node Selected", true, CyNode.class);
	/**
	 * A boolean value that indicates whether a nested network should be visualized (assuming 
	 * a nested network is present for the specified node).
	 */
	public static final VisualProperty<Boolean> NODE_NESTED_NETWORK_IMAGE_VISIBLE = new BooleanVisualProperty(true,
			"NODE_NESTED_NETWORK_IMAGE_VISIBLE", "Nested Network Image Visible", CyNode.class);

	/**
	 * The maximum width of the node label. If the node label is wider than the specified 
	 * width, Cytoscape will automatically wrap the label on space characters. Cytoscape 
	 * will not hyphenate words, meaning that if a single word (i.e. no spaces) is longer 
	 * than maximum width, the word will be displayed beyond the maximum width.
	 */
	public static final VisualProperty<Double> NODE_LABEL_WIDTH = new DoubleVisualProperty(200d, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"NODE_LABEL_WIDTH", "Node Label Width", CyNode.class);

	// Moved from DING rendering engine.
	/**
	 * TODO Documentation
	 */
	public static final VisualProperty<Paint> NODE_SELECTED_PAINT = new PaintVisualProperty(Color.YELLOW,
			PAINT_RANGE, "NODE_SELECTED_PAINT", "Node Selected Paint", CyNode.class);

	/**
	 * The color of the border of the node.
	 */
	public static final VisualProperty<Paint> NODE_BORDER_PAINT = new PaintVisualProperty(Color.BLACK,
			PAINT_RANGE, "NODE_BORDER_PAINT", "Node Border Paint", CyNode.class);

	/**
	 * TODO Documentation
	 */
	public static final VisualProperty<Double> NODE_BORDER_WIDTH = new DoubleVisualProperty(DEF_BORDER_WIDTH,
			new ContinuousRange<Double>(Double.class, 0d, Double.POSITIVE_INFINITY, true, true), "NODE_BORDER_WIDTH",
			"Node Border Width", CyNode.class);

	/**
	 * The text of the tooltip that appears when a mouse hovers over the node.
	 */
	public static final VisualProperty<String> NODE_TOOLTIP = new StringVisualProperty("",
			ARBITRARY_STRING_RANGE, "NODE_TOOLTIP", "Node Tooltip", CyNode.class);

	/**
	 * The font used for the node label.
	 */
	public static final VisualProperty<Font> NODE_LABEL_FONT_FACE = new FontVisualProperty(new Font("SansSerif",
			Font.PLAIN, DEF_FONT_SIZE), "NODE_LABEL_FONT_FACE", "Node Label Font Face", CyNode.class);

	/**
	 * The size of the font used for the node label.
	 */
	public static final VisualProperty<Integer> NODE_LABEL_FONT_SIZE = new IntegerVisualProperty(DEF_FONT_SIZE,
			new ContinuousRange<Integer>(Integer.class, 1, Integer.MAX_VALUE, true, true), "NODE_LABEL_FONT_SIZE",
			"Node Label Font Size", CyNode.class);

	/**
	 * The opacity of the color of the node. Zero means totally transparent, and 255 means
	 *  totally opaque.
	 */
	public static final VisualProperty<Integer> NODE_TRANSPARENCY = new IntegerVisualProperty(255,
			new ContinuousRange<Integer>(Integer.class, 0, 255, true, true), "NODE_TRANSPARENCY", "Node Transparency",
			CyNode.class);
	/**
	 * See {@see BasicVisualLexicon#NODE_TRANSPARENCY}
	 */
	public static final VisualProperty<Integer> NODE_OPACITY = NODE_TRANSPARENCY;

	/**
	 * TODO Documentation
	 */
	public static final VisualProperty<Integer> NODE_BORDER_TRANSPARENCY = new IntegerVisualProperty(255,
			new ContinuousRange<Integer>(Integer.class, 0, 255, true, true), "NODE_BORDER_TRANSPARENCY",
			"Node Border Transparency", CyNode.class);

	/**
	 * TODO Documentation
	 */
	public static final VisualProperty<Integer> NODE_LABEL_TRANSPARENCY = new IntegerVisualProperty(255,
			new ContinuousRange<Integer>(Integer.class, 0, 255, true, true), "NODE_LABEL_TRANSPARENCY",
			"Node Label Transparency", CyNode.class);
	/**
	 * The shape of the node.
	 */
	public static final VisualProperty<NodeShape> NODE_SHAPE = new NodeShapeVisualProperty(
			NodeShapeVisualProperty.ELLIPSE, "NODE_SHAPE", "Node Shape", CyNode.class);

	/**
	 * The type of line used for the border of the node.
	 */
	public static final VisualProperty<LineType> NODE_BORDER_LINE_TYPE = new LineTypeVisualProperty(
			LineTypeVisualProperty.SOLID, "NODE_BORDER_STROKE", "Node Border Line Type", CyNode.class);


	////////////////////////////////// Edge VP ////////////////////////////////////////
	/**
	 * The color of the whole edge (including the stroke and arrows) when it is selected or
	 *  unselected.
	 */
	public static final VisualProperty<Paint> EDGE_PAINT = new PaintVisualProperty(Color.gray, PAINT_RANGE,
			"EDGE_PAINT", "Edge Paint", CyEdge.class);

	/**
	 * TODO Documentation
	 */
	public static final VisualProperty<Paint> EDGE_LABEL_COLOR = new PaintVisualProperty(Color.BLACK, PAINT_RANGE,
			"EDGE_LABEL_COLOR", "Edge Label Color", CyEdge.class);

	/**
	 * The text used for the edge label.
	 */
	public static final VisualProperty<String> EDGE_LABEL = new StringVisualProperty("", ARBITRARY_STRING_RANGE,
			"EDGE_LABEL", "Edge Label", CyEdge.class);

	/**
	 * 	The width of the edge line.
	 */
	public static final VisualProperty<Double> EDGE_WIDTH = new DoubleVisualProperty(1d,
			NONE_ZERO_POSITIVE_DOUBLE_RANGE, "EDGE_WIDTH", "Edge Width", CyEdge.class);

	/**
	 * Hides the edge if set to false. By default, this value is set to true.
	 */
	public static final VisualProperty<Boolean> EDGE_VISIBLE = new BooleanVisualProperty(true, "EDGE_VISIBLE",
			"Edge Visible", CyEdge.class);

	/**
	 * TODO Documentation
	 */
	public static final VisualProperty<Boolean> EDGE_SELECTED = new BooleanVisualProperty(false, "EDGE_SELECTED",
			"Edge Selected", true, CyEdge.class);

	// Line Types
	/**
	 * The type of stoke used to render the line (solid, dashed, etc.)
	 */
	public static final VisualProperty<LineType> EDGE_LINE_TYPE = new LineTypeVisualProperty(
			LineTypeVisualProperty.SOLID, "EDGE_LINE_TYPE", "Edge Line Type", CyEdge.class);

	/**
	 * The text of the tooltip that appears when a mouse hovers over the edge.
	 */
	public static final VisualProperty<String> EDGE_TOOLTIP = new StringVisualProperty("",
			ARBITRARY_STRING_RANGE, "EDGE_TOOLTIP", "Edge Tooltip", CyEdge.class);

	/**
	 * The font used for the edge label.
	 */
	public static final VisualProperty<Font> EDGE_LABEL_FONT_FACE = new FontVisualProperty(new Font("SansSerif",
			Font.PLAIN, 10), "EDGE_LABEL_FONT_FACE", "Edge Label Font Face", CyEdge.class);
	
	/**
	 * The size of the font used for the edge label.
	 */
	public static final VisualProperty<Integer> EDGE_LABEL_FONT_SIZE = new IntegerVisualProperty(10,
			new ContinuousRange<Integer>(Integer.class, 1, Integer.MAX_VALUE, true, true), "EDGE_LABEL_FONT_SIZE",
			"Edge Label Font Size", CyEdge.class);

	/**
	 * The opacity of the color of the edge label. Zero means totally transparent, and 255 means totally opaque.
	 */
	public static final VisualProperty<Integer> EDGE_LABEL_TRANSPARENCY = new IntegerVisualProperty(255,
			new ContinuousRange<Integer>(Integer.class, 0, 255, true, true), "EDGE_LABEL_TRANSPARENCY",
			"Edge Label Transparency", CyEdge.class);
	/**
	 * The color of the whole edge (stroke and arrows) when selected. 
	 */
	public static final VisualProperty<Paint> EDGE_SELECTED_PAINT = new PaintVisualProperty(Color.RED,
			PAINT_RANGE, "EDGE_SELECTED_PAINT", "Edge Color (Selected)", CyEdge.class);
	
	/**
	 * The color of the whole edge (stroke and arrows) when it is not selected.
	 */
	public static final VisualProperty<Paint> EDGE_UNSELECTED_PAINT = new PaintVisualProperty(Color.DARK_GRAY,
			PAINT_RANGE, "EDGE_UNSELECTED_PAINT", "Edge Color (Unselected)", CyEdge.class);
	
	/**
	 * The color of the edge line when selected. 
	 */
	public static final VisualProperty<Paint> EDGE_STROKE_SELECTED_PAINT = new PaintVisualProperty(Color.RED,
			PAINT_RANGE, "EDGE_STROKE_SELECTED_PAINT", "Edge Stroke Color (Selected)",
			CyEdge.class);
	
	/**
	 * The color of the edge line.
	 */
	public static final VisualProperty<Paint> EDGE_STROKE_UNSELECTED_PAINT = new PaintVisualProperty(Color.DARK_GRAY,
			PAINT_RANGE, "EDGE_STROKE_UNSELECTED_PAINT", "Edge Stroke Color (Unselected)",
			CyEdge.class);

	/**
	 * The opacity of the of the edge. Zero means totally transparent, and 255 means totally 
	 * opaque.
	 */
	public static final VisualProperty<Integer> EDGE_TRANSPARENCY = new IntegerVisualProperty(255,
			new ContinuousRange<Integer>(Integer.class, 0, 255, true, true), "EDGE_TRANSPARENCY", "Edge Transparency",
			CyEdge.class);

	/**
	 * The shape of the arrow on the source node end of the edge.
	 */
	public static final VisualProperty<ArrowShape> EDGE_SOURCE_ARROW_SHAPE = new ArrowShapeVisualProperty(
			ArrowShapeVisualProperty.NONE, "EDGE_SOURCE_ARROW_SHAPE", "Edge Source Arrow Shape", CyEdge.class);
	/**
	 *The shape of the arrow on the target node end of the edge.
	 */
	public static final VisualProperty<ArrowShape> EDGE_TARGET_ARROW_SHAPE = new ArrowShapeVisualProperty(
			ArrowShapeVisualProperty.NONE, "EDGE_TARGET_ARROW_SHAPE", "Edge Target Arrow Shape", CyEdge.class);
	/* 
	 * TODO Documentation
	 */
	public static final VisualProperty<Double> EDGE_SOURCE_ARROW_SIZE = new DoubleVisualProperty(6d, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"EDGE_SOURCE_ARROW_SIZE", "Edge Source Arrow Size", CyEdge.class);
	
	/**
	 * TODO Documentation
	 */
	public static final VisualProperty<Double> EDGE_TARGET_ARROW_SIZE = new DoubleVisualProperty(6d, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"EDGE_TARGET_ARROW_SIZE", "Edge Target Arrow Size", CyEdge.class);

	/**
	 * If Edge Bend is defined, edges will be rendered as straight or curved lines. If this 
	 * value is set to true, edges will be drawn as curved lines.
	 */
	public static final EdgeBendVisualProperty EDGE_BEND = new EdgeBendVisualProperty(
			EdgeBendVisualProperty.DEFAULT_EDGE_BEND, "EDGE_BEND", "Edge Bend");

	/**
	 * TODO Documentation
	 */
	public static final VisualProperty<Double> EDGE_LABEL_WIDTH = new DoubleVisualProperty(200d, NONE_ZERO_POSITIVE_DOUBLE_RANGE,
			"EDGE_LABEL_WIDTH", "Edge Label Width", CyEdge.class);


	// ////// Network VP ////////
	/**
	 * The zoom level of the network view.
	 */
	public static final VisualProperty<Double> NETWORK_SCALE_FACTOR = new DoubleVisualProperty(1.0,
			NONE_ZERO_POSITIVE_DOUBLE_RANGE, "NETWORK_SCALE_FACTOR", "Network Scale Factor", true, CyNetwork.class);
	/**
	 * The X location of network view center.
	 */
	public static final VisualProperty<Double> NETWORK_CENTER_X_LOCATION = new DoubleVisualProperty(0.0,
			ARBITRARY_DOUBLE_RANGE, "NETWORK_CENTER_X_LOCATION", "Network Center X Location", true, CyNetwork.class);
	/**
	 * The Y location of network view center.
	 */
	public static final VisualProperty<Double> NETWORK_CENTER_Y_LOCATION = new DoubleVisualProperty(0.0,
			ARBITRARY_DOUBLE_RANGE, "NETWORK_CENTER_Y_LOCATION", "Network Center Y Location", true, CyNetwork.class);

	/**
	 * The size (width and height) of the network view.
	 */
	public static final VisualProperty<Double> NETWORK_SIZE = new DoubleVisualProperty(550.0,
			NONE_ZERO_POSITIVE_DOUBLE_RANGE, "NETWORK_SIZE", "Network Size", true, CyNetwork.class);
	
	/**
	 * The width of the network view.
	 */
	public static final VisualProperty<Double> NETWORK_WIDTH = new DoubleVisualProperty(550.0,
			NONE_ZERO_POSITIVE_DOUBLE_RANGE, "NETWORK_WIDTH", "Network Width", true, CyNetwork.class);
	/**
	 * The height of the network view.
	 */
	public static final VisualProperty<Double> NETWORK_HEIGHT = new DoubleVisualProperty(400.0,
			NONE_ZERO_POSITIVE_DOUBLE_RANGE, "NETWORK_HEIGHT", "Network Height", true, CyNetwork.class);

	/**
	 * The title of the network view.
	 */
	public static final VisualProperty<String> NETWORK_TITLE = new StringVisualProperty("", ARBITRARY_STRING_RANGE,
			"NETWORK_TITLE", "Network Title", true, CyNetwork.class);
	/**
	 * The background color of the network view.
	 */
	public static final VisualProperty<Paint> NETWORK_BACKGROUND_PAINT = new PaintVisualProperty(Color.WHITE,
			PAINT_RANGE, "NETWORK_BACKGROUND_PAINT", "Network Background Paint", CyNetwork.class);

	// 3D-related props
	/**
	 * Z location of the node. Default value of this will be ignored. The value will be used
	 *  only when mapping function is defined.
	 */
	public static final VisualProperty<Double> NODE_Z_LOCATION = new DoubleVisualProperty(0.0, ARBITRARY_DOUBLE_RANGE,
			"NODE_Z_LOCATION", "Node Z Location", true, CyNode.class);

	/**
	 * TODO Documentation
	 */
	public static final VisualProperty<Double> NODE_DEPTH = new DoubleVisualProperty(0.0,
			NONE_ZERO_POSITIVE_DOUBLE_RANGE, "NODE_DEPTH", "Node Depth", CyNode.class);

	/**
	 * TODO Documentation
	 */
	public static final VisualProperty<Double> NETWORK_CENTER_Z_LOCATION = new DoubleVisualProperty(0.0,
			ARBITRARY_DOUBLE_RANGE, "NETWORK_CENTER_Z_LOCATION", "Network Center Z Location", true, CyNetwork.class);

	/**
	 * TODO Documentation
	 */
	public static final VisualProperty<Double> NETWORK_DEPTH = new DoubleVisualProperty(0.0,
			NONE_ZERO_POSITIVE_DOUBLE_RANGE, "NETWORK_DEPTH", "Network Depth", CyNetwork.class);



	/**
	 * Constructor for VisualLexicon. The parameters are required for all
	 * lexicons.
	 * 
	 * @param rootVisualProperty
	 *            Root of the visual property tree.
	 */
	public BasicVisualLexicon(final VisualProperty<NullDataType> rootVisualProperty) {
		this.visualPropertyMap = new HashMap<>();
		this.rootVisualProperty = rootVisualProperty;
		final VisualLexiconNode rootNode = new VisualLexiconNode(rootVisualProperty, null);

		visualPropertyMap.put(rootVisualProperty, rootNode);

		this.identifierLookup = new HashMap<>();
		this.identifierLookup.put(CyNode.class, new HashMap<String, VisualProperty<?>>());
		this.identifierLookup.put(CyEdge.class, new HashMap<String, VisualProperty<?>>());
		this.identifierLookup.put(CyNetwork.class, new HashMap<String, VisualProperty<?>>());

		addVisualProperties(rootVisualProperty);
	}

	public final void addBendFactory(final BendFactory bendFactory, final Map<?, ?> props) {
		EDGE_BEND.setBendFactory(bendFactory);
	}

	private void addVisualProperties(final VisualProperty<NullDataType> root) {
		addVisualProperty(NETWORK, root);

		// Level 1: Direct children of network VP
		addVisualProperty(NODE, NETWORK);
		addVisualProperty(EDGE, NETWORK);
		addVisualProperty(NETWORK_SIZE, NETWORK);
		addVisualProperty(NETWORK_SCALE_FACTOR, NETWORK);
		addVisualProperty(NETWORK_TITLE, NETWORK);
		addVisualProperty(NETWORK_BACKGROUND_PAINT, NETWORK);
		addVisualProperty(NETWORK_CENTER_X_LOCATION, NETWORK);
		addVisualProperty(NETWORK_CENTER_Y_LOCATION, NETWORK);
		addVisualProperty(NETWORK_CENTER_Z_LOCATION, NETWORK);

		// Level 2: Network-related VP
		addVisualProperty(NETWORK_WIDTH, NETWORK_SIZE);
		addVisualProperty(NETWORK_HEIGHT, NETWORK_SIZE);
		addVisualProperty(NETWORK_DEPTH, NETWORK_SIZE);

		// Level 2: Children of node VP
		addVisualProperty(NODE_PAINT, NODE);
		addVisualProperty(NODE_SIZE, NODE);
		addVisualProperty(NODE_SHAPE, NODE);
		addVisualProperty(NODE_VISIBLE, NODE);
		addVisualProperty(NODE_SELECTED, NODE);
		addVisualProperty(NODE_NESTED_NETWORK_IMAGE_VISIBLE, NODE);
		addVisualProperty(NODE_X_LOCATION, NODE);
		addVisualProperty(NODE_Y_LOCATION, NODE);
		addVisualProperty(NODE_Z_LOCATION, NODE);
		addVisualProperty(NODE_LABEL, NODE);
		addVisualProperty(NODE_BORDER_WIDTH, NODE);
		addVisualProperty(NODE_BORDER_LINE_TYPE, NODE);
		addVisualProperty(NODE_TRANSPARENCY, NODE);
		addVisualProperty(NODE_BORDER_TRANSPARENCY, NODE);
		addVisualProperty(NODE_LABEL_FONT_FACE, NODE);
		addVisualProperty(NODE_LABEL_TRANSPARENCY, NODE);
		addVisualProperty(NODE_TOOLTIP, NODE);
		addVisualProperty(NODE_LABEL_WIDTH, NODE);

		// Level 2: Children of edge Visual Property
		addVisualProperty(EDGE_PAINT, EDGE);
		addVisualProperty(EDGE_VISIBLE, EDGE);
		addVisualProperty(EDGE_SELECTED, EDGE);
		addVisualProperty(EDGE_WIDTH, EDGE);
		addVisualProperty(EDGE_LABEL, EDGE);
		addVisualProperty(EDGE_LINE_TYPE, EDGE);
		addVisualProperty(EDGE_TOOLTIP, EDGE);
		addVisualProperty(EDGE_LABEL_FONT_FACE, EDGE);
		addVisualProperty(EDGE_LABEL_FONT_SIZE, EDGE);
		addVisualProperty(EDGE_LABEL_TRANSPARENCY, EDGE);
		addVisualProperty(EDGE_TRANSPARENCY, EDGE);
		addVisualProperty(EDGE_SOURCE_ARROW_SHAPE, EDGE);
		addVisualProperty(EDGE_TARGET_ARROW_SHAPE, EDGE);
		addVisualProperty(EDGE_SOURCE_ARROW_SIZE, EDGE);
		addVisualProperty(EDGE_TARGET_ARROW_SIZE, EDGE);
		addVisualProperty(EDGE_BEND, EDGE);
		addVisualProperty(EDGE_LABEL_WIDTH, EDGE);

		// Level 3 - 4: Node-related VP
		addVisualProperty(NODE_FILL_COLOR, NODE_PAINT);
		addVisualProperty(NODE_SELECTED_PAINT, NODE_PAINT);
		addVisualProperty(NODE_BORDER_PAINT, NODE_PAINT);
		addVisualProperty(NODE_LABEL_COLOR, NODE_PAINT);
		addVisualProperty(NODE_LABEL_FONT_SIZE, NODE);
		addVisualProperty(NODE_WIDTH, NODE_SIZE);
		addVisualProperty(NODE_HEIGHT, NODE_SIZE);
		addVisualProperty(NODE_DEPTH, NODE_SIZE);

		// Level 3: Edge-related VP
		addVisualProperty(EDGE_LABEL_COLOR, EDGE_PAINT);
		addVisualProperty(EDGE_SELECTED_PAINT, EDGE_PAINT);
		addVisualProperty(EDGE_UNSELECTED_PAINT, EDGE_PAINT);
		addVisualProperty(EDGE_STROKE_SELECTED_PAINT, EDGE_SELECTED_PAINT);
		addVisualProperty(EDGE_STROKE_UNSELECTED_PAINT, EDGE_UNSELECTED_PAINT);

		createLookupMap();
	}

	private void createLookupMap() {
		// XGMML:
		addIdentifierMapping(CyNetwork.class, "color", NETWORK_BACKGROUND_PAINT);
		addIdentifierMapping(CyNetwork.class, "GRAPH_VIEW_CENTER_X", NETWORK_CENTER_X_LOCATION);
		addIdentifierMapping(CyNetwork.class, "GRAPH_VIEW_CENTER_Y", NETWORK_CENTER_Y_LOCATION);
		addIdentifierMapping(CyNetwork.class, "GRAPH_VIEW_ZOOM", NETWORK_SCALE_FACTOR);
		addIdentifierMapping(CyNetwork.class, "label", NETWORK_TITLE);

		addIdentifierMapping(CyNode.class, "fill", NODE_FILL_COLOR);
		addIdentifierMapping(CyNode.class, "x", NODE_X_LOCATION);
		addIdentifierMapping(CyNode.class, "y", NODE_Y_LOCATION);
		addIdentifierMapping(CyNode.class, "z", NODE_Z_LOCATION);
		addIdentifierMapping(CyNode.class, "w", NODE_WIDTH);
		addIdentifierMapping(CyNode.class, "h", NODE_HEIGHT);
		addIdentifierMapping(CyNode.class, "size", NODE_SIZE);
		addIdentifierMapping(CyNode.class, "type", NODE_SHAPE);
		addIdentifierMapping(CyNode.class, "outline", NODE_BORDER_PAINT);
		addIdentifierMapping(CyNode.class, "width", NODE_BORDER_WIDTH);
		addIdentifierMapping(CyNode.class, "borderLineType", NODE_BORDER_LINE_TYPE);
		addIdentifierMapping(CyNode.class, "nodeLabelFont", NODE_LABEL_FONT_FACE);
		addIdentifierMapping(CyNode.class, "nodeTransparency", NODE_TRANSPARENCY);
		addIdentifierMapping(CyNode.class, "nodeLabelTransparency", NODE_LABEL_TRANSPARENCY);
		addIdentifierMapping(CyNode.class, "nodeBorderTransparency", NODE_BORDER_TRANSPARENCY);

		addIdentifierMapping(CyEdge.class, "width", EDGE_WIDTH);
		addIdentifierMapping(CyEdge.class, "fill", EDGE_STROKE_UNSELECTED_PAINT);
		addIdentifierMapping(CyEdge.class, "edgeLineType", EDGE_LINE_TYPE);
		addIdentifierMapping(CyEdge.class, "edgeLabelFont", EDGE_LABEL_FONT_FACE);

		// 2.x VizMap Properties:
		addIdentifierMapping(CyNetwork.class, "backgroundColor", NETWORK_BACKGROUND_PAINT);

		addIdentifierMapping(CyNode.class, "nodeFillColor", NODE_FILL_COLOR);
		addIdentifierMapping(CyNode.class, "nodeSelectionColor", NODE_SELECTED_PAINT);
		addIdentifierMapping(CyNode.class, "nodeSize", NODE_SIZE);
		addIdentifierMapping(CyNode.class, "nodeWidth", NODE_WIDTH);
		addIdentifierMapping(CyNode.class, "nodeHight", NODE_HEIGHT); // We have to be nice with this 2.8 typo!
		addIdentifierMapping(CyNode.class, "nodeHeight", NODE_HEIGHT);
		addIdentifierMapping(CyNode.class, "nodeOpacity", NODE_TRANSPARENCY);
		addIdentifierMapping(CyNode.class, "nodeBorderColor", NODE_BORDER_PAINT);
		addIdentifierMapping(CyNode.class, "nodeLineWidth", NODE_BORDER_WIDTH);
		addIdentifierMapping(CyNode.class, "nodeLineStyle", NODE_BORDER_LINE_TYPE);
		addIdentifierMapping(CyNode.class, "nodeBorderOpacity", NODE_BORDER_TRANSPARENCY);
		addIdentifierMapping(CyNode.class, "nodeShape", NODE_SHAPE);
		addIdentifierMapping(CyNode.class, "nodeFont", NODE_LABEL_FONT_FACE);
		addIdentifierMapping(CyNode.class, "nodeFontSize", NODE_LABEL_FONT_SIZE);
		addIdentifierMapping(CyNode.class, "nodeShowNestedNetwork", NODE_NESTED_NETWORK_IMAGE_VISIBLE);
		addIdentifierMapping(CyNode.class, "nodeLabel", NODE_LABEL);
		addIdentifierMapping(CyNode.class, "nodeLabelColor", NODE_LABEL_COLOR);
		addIdentifierMapping(CyNode.class, "nodeLabelWidth", NODE_LABEL_WIDTH);
		addIdentifierMapping(CyNode.class, "nodeLabelOpacity", NODE_LABEL_TRANSPARENCY);
		addIdentifierMapping(CyNode.class, "nodeToolTip", NODE_TOOLTIP);

		addIdentifierMapping(CyEdge.class, "edgeLineWidth", EDGE_WIDTH);
		addIdentifierMapping(CyEdge.class, "edgeColor", EDGE_STROKE_UNSELECTED_PAINT);
		addIdentifierMapping(CyEdge.class, "edgeSelectionColor", EDGE_STROKE_SELECTED_PAINT);
		addIdentifierMapping(CyEdge.class, "edgeLineStyle", EDGE_LINE_TYPE);
		addIdentifierMapping(CyEdge.class, "edgeOpacity", EDGE_TRANSPARENCY);
		addIdentifierMapping(CyEdge.class, "sourceArrow", EDGE_SOURCE_ARROW_SHAPE);
		addIdentifierMapping(CyEdge.class, "targetArrow", EDGE_TARGET_ARROW_SHAPE);
		addIdentifierMapping(CyEdge.class, "edgeSourceArrowShape", EDGE_SOURCE_ARROW_SHAPE);
		addIdentifierMapping(CyEdge.class, "edgeTargetArrowShape", EDGE_TARGET_ARROW_SHAPE);
		addIdentifierMapping(CyEdge.class, "edgeBend", EDGE_BEND);
		addIdentifierMapping(CyEdge.class, "edgeLabel", EDGE_LABEL);
		addIdentifierMapping(CyEdge.class, "edgeFont", EDGE_LABEL_FONT_FACE);
		addIdentifierMapping(CyEdge.class, "edgeLabelColor", EDGE_LABEL_COLOR);
		addIdentifierMapping(CyEdge.class, "edgeLabelOpacity", EDGE_LABEL_TRANSPARENCY);
		addIdentifierMapping(CyEdge.class, "edgeFontSize", EDGE_LABEL_FONT_SIZE);
		addIdentifierMapping(CyEdge.class, "edgeLabelWidth", EDGE_LABEL_WIDTH);
		addIdentifierMapping(CyEdge.class, "edgeToolTip", EDGE_TOOLTIP);
		addIdentifierMapping(CyEdge.class, "edgeHandleList", EDGE_BEND);
	}

	@Override
	public final Set<VisualProperty<?>> getAllVisualProperties() {
		return new HashSet<VisualProperty<?>>(visualPropertyMap.keySet());
	}

	@Override
	public final Collection<VisualProperty<?>> getAllDescendants(final VisualProperty<?> prop) {
		if (prop == null)
			throw new NullPointerException("Target visual property cannot be null.");

		if (!this.visualPropertyMap.containsKey(prop))
			throw new IllegalArgumentException("No such Visual Property in the Lexicon.");

		return getChildNodes(prop);
	}

	@Override
	public final VisualProperty<NullDataType> getRootVisualProperty() {
		return this.rootVisualProperty;
	}

	private Set<VisualProperty<?>> getChildNodes(VisualProperty<?> prop) {
		final VisualLexiconNode node = visualPropertyMap.get(prop);
		final Set<VisualProperty<?>> children = new HashSet<>();

		// if this is a leaf node, return empty set
		if (node.getChildren().size() == 0)
			return children;

		Collection<VisualLexiconNode> currentChildren = node.getChildren();
		for (VisualLexiconNode nd : currentChildren)
			children.add(nd.getVisualProperty());

		for (VisualLexiconNode nd : currentChildren)
			children.addAll(getChildNodes(nd.getVisualProperty()));

		return children;
	}

	/**
	 * Insert a {@link VisualProperty} to the tree.
	 * 
	 * @param vp
	 *            the VisualProperty to insert in the tree.
	 * @param parent
	 *            the parent of the VisualProperty to insert.
	 */
	protected final void addVisualProperty(final VisualProperty<?> vp, final VisualProperty<?> parent) {
		if (this.visualPropertyMap.containsKey(vp))
			throw new IllegalStateException("The key " + vp.getIdString() + " already exists in the lexicon.");

		if (parent == null)
			throw new NullPointerException("Parent cannot be null.");

		final VisualLexiconNode parentNode = this.visualPropertyMap.get(parent);

		if (parentNode == null)
			throw new IllegalArgumentException("Parent does not exist in the lexicon: " + parent.getDisplayName());

		final VisualLexiconNode newNode = new VisualLexiconNode(vp, parentNode);
		this.visualPropertyMap.put(vp, newNode);

		addIdentifierMapping(vp.getTargetDataType(), vp.getIdString(), vp);
	}

	@Override
	public final VisualLexiconNode getVisualLexiconNode(final VisualProperty<?> vp) {
		return this.visualPropertyMap.get(vp);
	}

	@Override
	public final VisualProperty<?> lookup(final Class<?> type, final String id) {
		if (id == null || type == null)
			return null;

		Map<String, VisualProperty<?>> map = identifierLookup.get(type);
		if (map == null)
			return null;

		return map.get(id.toLowerCase());
	}

	@Override
	public boolean isSupported(VisualProperty<?> vp) {
		return visualPropertyMap.containsKey(vp);
	}

	@Override
	public <T> Set<T> getSupportedValueRange(VisualProperty<T> vp) {
		final Range<T> range = vp.getRange();

		if (range.isDiscrete())
			return ((DiscreteRange<T>)range).values();

		return Collections.emptySet();
	}

	/**
	 * @param type
	 * @param id
	 * @param vp
	 */
	protected final void addIdentifierMapping(final Class<?> type, final String id, final VisualProperty<?> vp) {
		if (type == null) {
			logger.warn("attempting to add VisualLexicon identifier lookup mapping with null type");
			return;
		}
		if (id == null) {
			logger.warn("attempting to add VisualLexicon identifier lookup mapping with null id");
			return;
		}
		if (vp == null) {
			logger.warn("attempting to add VisualLexicon identifier lookup mapping with null visual property");
			return;
		}
		Map<String, VisualProperty<?>> map = identifierLookup.get(type);
		if (map == null) {
			logger.warn("attempting to add VisualLexicon identifier lookup mapping with unrecognized type: "
					+ type.getClass().getName() + "(expect: " + identifierLookup.keySet().toString() + ")");
			return;
		}
		map.put(id.toLowerCase(), vp);
	}
}
