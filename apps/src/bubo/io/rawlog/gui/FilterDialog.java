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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * A dialog that queries the user about how and if the data being displayed should be filtered.
 *
 * @author Peter Abeles
 */
public class FilterDialog extends JDialog implements ActionListener {

	Type selectedType;
	// list of all the data types
	List<Class<?>> listTypes = new ArrayList<Class<?>>();
	// list of all the input sources
	List<String> listSources = new ArrayList<String>();

	// selected stuff
	int[] selectedTypes = new int[0];
	int[] selectedSources = new int[0];

	volatile boolean decisionMade;
	boolean theDecision;


	DefaultListModel listModel;
	JList listSelect;


	JButton buttonOK;
	JButton buttonCancel;

	// buttons that select which type of filtering will happen
	JRadioButton radioNoFilter;
	JRadioButton radioData;
	JRadioButton radioSource;
	// the previous selected radio button.
	// used to determine which selected list needs to be updated
	JRadioButton prevRadioSelected;

	public FilterDialog(Frame owner) {
		super(owner, "Filter", true);
		selectedType = Type.NO_FILTERING;

		setupGUI();

		setPreferredSize(new Dimension(300, 400));
		setMinimumSize(getPreferredSize());
	}

	/**
	 * Make the dialog box visible and wait for the person to select what is to be filtered.
	 *
	 * @param locationComp The component that the dialog box is to be displayed relative to.
	 * @return If the user decided to change the filter or not.
	 */
	public boolean performSelection(Component locationComp) {
		decisionMade = false;

		// save the list of what was selected before so it can undo the changes
		// if the user canceled
		int[] origTypes = selectedTypes.clone();
		int[] origSources = selectedSources.clone();

		setLocationRelativeTo(locationComp);
		setVisible(true);


		if (!theDecision) {
			selectedTypes = origTypes.clone();
			selectedSources = origSources.clone();

			// change the list so that the correct list shows next time
			if (prevRadioSelected == radioData) {
				updateSelected(listTypes, selectedTypes);
			} else if (prevRadioSelected == radioSource) {
				updateSelected(listSources, selectedSources);
			}
		}

		return theDecision;
	}

	/**
	 * Creates and adds all the GUI elements
	 */
	private void setupGUI() {
		getRootPane().setLayout(new BorderLayout());

		addRadioButtons();
		addList();
		addButtons();
	}

	/**
	 * Change the list of types and sources that can be filtered.
	 *
	 * @param listTypes   List of all the different data types.
	 * @param listSources List of all the different data sources.
	 */
	public void setData(List<Class<?>> listTypes, List<String> listSources) {
		this.listTypes.clear();
		this.listSources.clear();

		this.listTypes.addAll(listTypes);
		this.listSources.addAll(listSources);

		this.selectedTypes = new int[listTypes.size()];
		this.selectedSources = new int[listSources.size()];

		for (int i = 0; i < selectedTypes.length; i++) {
			selectedTypes[i] = i;
		}
		for (int i = 0; i < selectedSources.length; i++) {
			selectedSources[i] = i;
		}

		// update the display
		radioNoFilter.setSelected(true);
		listModel.clear();
		prevRadioSelected = radioNoFilter;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == radioData) {
			saveSelected(radioData);
			updateSelected(listTypes, selectedTypes);
		} else if (e.getSource() == radioSource) {
			saveSelected(radioSource);
			updateSelected(listSources, selectedSources);
		} else if (e.getSource() == radioNoFilter) {
			saveSelected(radioNoFilter);
			listModel.clear();
		} else if (e.getSource() == buttonOK) {
			saveSelected(prevRadioSelected);
			decisionMade = true;
			theDecision = true;
			setVisible(false);
		} else if (e.getSource() == buttonCancel) {
			saveSelected(prevRadioSelected);
			decisionMade = true;
			theDecision = false;
			setVisible(false);
		}
	}

	private void saveSelected(JRadioButton current) {
		if (radioData == prevRadioSelected) {
			int[] selected = listSelect.getSelectedIndices();

			selectedTypes = selected.clone();

		} else if (radioSource == prevRadioSelected) {
			int[] selected = listSelect.getSelectedIndices();

			selectedSources = selected.clone();
		}
		prevRadioSelected = current;
	}

	private void updateSelected(List<?> listItems,
								int[] listSelected) {
		listModel.clear();
		for (Object s : listItems) {
			if (s instanceof Class)
				listModel.addElement(((Class) s).getSimpleName());
			else
				listModel.addElement(s);
		}

		listSelect.clearSelection();

		for (int i : listSelected) {
			listSelect.addSelectionInterval(i, i);
		}
	}

	private void addButtons() {
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));

		buttonOK = new JButton("OK");
		buttonOK.addActionListener(this);

		buttonCancel = new JButton("Cancel");
		buttonCancel.addActionListener(this);


		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(buttonCancel);
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(buttonOK);

		getRootPane().setDefaultButton(buttonOK);

		getRootPane().add(buttonPane, BorderLayout.SOUTH);
	}

	private void addList() {
		listModel = new DefaultListModel();
		listSelect = new JList(listModel);

		listSelect.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		listSelect.setLayoutOrientation(JList.VERTICAL);

		JScrollPane listScroller = new JScrollPane(listSelect);
		listScroller.setPreferredSize(new Dimension(180, 300));

		getRootPane().add(listScroller, BorderLayout.CENTER);
	}

	private void addRadioButtons() {
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.PAGE_AXIS));

		radioNoFilter = new JRadioButton("No Filtering");
		radioData = new JRadioButton("Data Type");
		radioSource = new JRadioButton("Input Source");

		radioNoFilter.setSelected(true);
		prevRadioSelected = radioNoFilter;

		ButtonGroup group = new ButtonGroup();
		group.add(radioNoFilter);
		group.add(radioData);
		group.add(radioSource);

		radioNoFilter.addActionListener(this);
		radioData.addActionListener(this);
		radioSource.addActionListener(this);

		buttonPane.add(radioNoFilter);
		buttonPane.add(radioData);
		buttonPane.add(radioSource);

		getRootPane().add(buttonPane, BorderLayout.NORTH);
	}

	public Type getFilterType() {

		if (radioNoFilter.isSelected()) {
			return Type.NO_FILTERING;
		} else if (radioData.isSelected()) {
			return Type.DATA;
		} else if (radioSource.isSelected()) {
			return Type.SOURCE;
		}

		throw new RuntimeException("Impossible.  Nothing is selected");
	}

	public List<Class<?>> getSelectedTypes() {

		List<Class<?>> ret = new ArrayList<Class<?>>();

		for (int s : selectedTypes) {
			ret.add(listTypes.get(s));
		}

		return ret;
	}

	public List<String> getSelectedSources() {
		List<String> ret = new ArrayList<String>();

		for (int s : selectedSources) {
			ret.add(listSources.get(s));
		}

		return ret;
	}

	public static enum Type {
		NO_FILTERING,
		DATA,
		SOURCE
	}
}
