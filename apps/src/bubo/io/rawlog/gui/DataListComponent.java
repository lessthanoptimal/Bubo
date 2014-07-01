/*
 * Copyright (c) 2013-2014, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Project BUBO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bubo.io.rawlog.gui;

import bubo.io.UtilReflections;
import bubo.io.logs.LogFileObjectRef;
import bubo.io.logs.LogFileReader;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


/**
 * Displays a list of all the loaded data objects from the log file.  Which object being
 * viewed by the user is selected in this list.
 *
 * @author Peter Abeles
 */
public class DataListComponent extends JComponent
		implements ActionListener, TreeSelectionListener,
		ListSelectionListener {

	// previously selected base ref.
	// by keeping track of this unnecessary reads to the file can be avoided
	LogFileObjectRef prevBaseRef;
	Object prevObject;
	// used to select which view
	private JComboBox viewSelect;
	// shows a list of all the objects
	private JList flatList;
	// shows a list of all the objects and their children
	private JTree treeList;
	// list of log references to the data
	private List<LogFileObjectRef> flatData;
	// listener for when the user selects a new object to view
	private SelectionListener listener;
	// used to switch between the two list views
	private JPanel listPanel = new JPanel(new CardLayout());
	// reads the log file
	private LogFileReader reader;

	public DataListComponent(SelectionListener listener) {

		this.listener = listener;

		setLayout(new BorderLayout());

		JScrollPane listScrollPane = new JScrollPane(listPanel);
		add(BorderLayout.CENTER, listScrollPane);

		configureComboBox();
		configureFlatList();
		configureTreeView();
	}

	private void configureComboBox() {
		String[] petStrings = {"Flat", "Tree"};

		viewSelect = new JComboBox(petStrings);
		viewSelect.setSelectedIndex(0);
		viewSelect.addActionListener(this);

		add(BorderLayout.NORTH, viewSelect);
	}

	private void configureFlatList() {
		flatList = new JList();

		flatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		flatList.getSelectionModel().setSelectionMode
				(TreeSelectionModel.SINGLE_TREE_SELECTION);

		flatList.addListSelectionListener(this);

		listPanel.add(flatList, "Flat");
	}

	private void configureTreeView() {
		treeList = new JTree();
		treeList.setRootVisible(false);
		treeList.getSelectionModel().setSelectionMode
				(TreeSelectionModel.SINGLE_TREE_SELECTION);
		treeList.addTreeSelectionListener(this);

		listPanel.add(treeList, "Tree");
	}

	public void setObjectList(List<LogFileObjectRef> flatData,
							  LogFileReader reader) {
		flatList.clearSelection();
		this.flatData = flatData;
		this.reader = reader;

		this.flatList.setListData(new Vector(flatData));
		flatList.setSelectedIndex(0);
		createTreeView(flatData);
	}

	/**
	 * Adds the base objects to the tree view.  The children are added at a later time
	 * when the user selects the base object for viewing.
	 *
	 * @param data
	 */
	protected void createTreeView(List<LogFileObjectRef> data) {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Root");

		for (LogFileObjectRef d : data) {
			DefaultMutableTreeNode parent = null;
			parent = new DefaultMutableTreeNode(d);
			rootNode.add(parent);
		}
		treeList.setModel(new DefaultTreeModel(rootNode));
	}

	/**
	 * Returns the object which is currently selected.
	 *
	 * @return
	 */
	public Object getSelected() {
		if (isFlatSelected()) {
			int selectedIndex = flatList.getSelectedIndex();
			if (selectedIndex == -1)
				return null;

			return reader.getObject(flatData.get(selectedIndex));
		} else {
			TreePath treePath = treeList.getSelectionPath();

			if (treePath == null)
				return null;

			Object[] path = treePath.getPath();

			LogFileObjectRef ref = (LogFileObjectRef) ((DefaultMutableTreeNode) path[1]).getUserObject();
			Object o = prevBaseRef == ref ? prevObject : reader.getObject(ref);

			// save the current reference to avoid loading it again each time a child is selected
			prevBaseRef = ref;
			prevObject = o;

			try {
				// the base data structure was selected
				if (path.length == 2) {

					updateTreeView(o, treePath);

					return o;
				} else {
					// a child data structure was selected
					return findChild(o, path);
				}
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}

		}
	}

	/**
	 * Updates the tree view for an object dynamically after it has been selected to show
	 * all the child data it references.
	 *
	 * @param o
	 */
	private void updateTreeView(Object o, TreePath treePath) throws InvocationTargetException, IllegalAccessException {
		DefaultMutableTreeNode baseData = (DefaultMutableTreeNode) treePath.getPathComponent(1);

		// the view has already been extracted
		if (baseData.getChildCount() > 0)
			return;

		addChildren(o, baseData);
	}

	/**
	 * Examines the children of the parent data and adds them to the tree view
	 */
	private void addChildren(Object parentData, DefaultMutableTreeNode parentNode) throws InvocationTargetException, IllegalAccessException {

		if (parentData.getClass().isArray()) {
			addChildrenArray(parentData, parentNode);
		} else if (parentData.getClass().isAssignableFrom(List.class)) {
			addChildrenList(parentData, parentNode);
		} else {
			addChildrenGetters(parentData, parentNode);
		}

	}

	/**
	 * Adds all the elements in a list to the tree view
	 */
	private void addChildrenList(Object parentData, DefaultMutableTreeNode parentNode) throws InvocationTargetException, IllegalAccessException {
		List<?> list = (List) parentData;

		for (int i = 0; i < list.size(); i++) {
			Object childData = list.get(i);
			if (childData == null)
				continue;

			ChildInfo info = new ChildInfo();
			info.name = childData.getClass().getSimpleName();
			info.getter = null;
			info.index = i;

			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(info);
			parentNode.add(childNode);
			addChildren(childData, childNode);
		}
	}

	/**
	 * Adds all the elements in an array to the tree view
	 */
	private void addChildrenArray(Object parentData, DefaultMutableTreeNode parentNode) throws InvocationTargetException, IllegalAccessException {
		final int N = Array.getLength(parentData);

		for (int i = 0; i < N; i++) {
			Object childData = Array.get(parentData, i);
			if (childData == null)
				continue;

			ChildInfo info = new ChildInfo();
			info.name = childData.getClass().getSimpleName();
			info.getter = null;
			info.index = i;

			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(info);
			parentNode.add(childNode);
			addChildren(childData, childNode);
		}
	}

	/**
	 * Uses reflections to get a list of getter that are part of a getter/setter pair and
	 * adds children that are referenced by them
	 */
	private void addChildrenGetters(Object parentData, DefaultMutableTreeNode parentNode) throws IllegalAccessException, InvocationTargetException {
		List<String> names = new ArrayList<String>();
		List<Method> getters = new ArrayList<Method>();

		UtilReflections.findAccessors(parentData.getClass(), null, getters, names);

		for (int i = 0; i < names.size(); i++) {
			String n = names.get(i);
			Method g = getters.get(i);

			ChildInfo info = new ChildInfo();
			info.name = n;
			info.getter = g;

			Object childData = g.invoke(parentData);
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(info);
			parentNode.add(childNode);

			if (childData != null) {
				addChildren(childData, childNode);
			}
		}
	}

	/**
	 * Given a parent object and the tree path selected in the GUI, return the object
	 * that it specifies.
	 */
	private Object findChild(Object o, Object[] path) throws InvocationTargetException, IllegalAccessException {
		for (int i = 2; i < path.length; i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path[i];
			ChildInfo info = (ChildInfo) node.getUserObject();

			if (info.getter == null) {
				if (o.getClass().isAssignableFrom(List.class)) {
					List<?> list = (List<?>) o;
					o = list.get(info.index);
				} else if (o.getClass().isArray()) {
					o = Array.get(o, info.index);
				}
			} else {
				o = info.getter.invoke(o);
			}
		}

		return o;
	}

	/**
	 * Returns true if the flat list is selected
	 */
	private boolean isFlatSelected() {
		return viewSelect.getSelectedIndex() == 0;
	}

	/**
	 * Select the next item in the list.  Useful when playing a lot file
	 *
	 * @return If it is NOT at the last element.
	 */
	public boolean selectNextItem() {
		int index = flatList.getSelectedIndex();
		if (index == flatData.size() - 1)
			return false;
		flatList.setSelectedIndex(index + 1);
		Rectangle a = flatList.getCellBounds(index, index);
		flatList.scrollRectToVisible(a);
		return true;
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
//        System.out.println("Tree value changed");
		Object o = getSelected();
//        if( o == null )
//            return;

		listener.selectionChanged(o);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
			return;

		Object o = getSelected();
		if (o == null)
			return;

		listener.selectionChanged(o);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == viewSelect) {
			CardLayout cl = (CardLayout) (listPanel.getLayout());
			if (isFlatSelected()) {
				cl.show(listPanel, "Flat");
			} else {
				cl.show(listPanel, "Tree");
			}
		}
	}

	/**
	 * Used to notify other classes when the user selects a new object for viewing
	 */
	public static interface SelectionListener {
		public void selectionChanged(Object selected);
	}

	/**
	 * Stores information on how to access the child data in an object
	 */
	public static class ChildInfo {
		// the classes name as it is displayed
		public String name;
		// if access through a getter this is not null.  if it is a container it is null
		public Method getter;
		// if it is a container object this is the element being referenced.
		public int index;

		@Override
		public String toString() {
			if (getter != null)
				return name;
			else
				return index + " " + name;
		}
	}
}
