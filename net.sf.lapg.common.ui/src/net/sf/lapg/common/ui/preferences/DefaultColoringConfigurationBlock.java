/**
 * This file is part of Lapg.UI project.
 * 
 * Copyright (c) 2010 Evgeny Gryaznov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Gryaznov - initial API and implementation
 */
package net.sf.lapg.common.ui.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.lapg.common.ui.editor.colorer.ColorDefinition;
import net.sf.lapg.common.ui.editor.colorer.ColorGroupDefinition;
import net.sf.lapg.common.ui.editor.colorer.DefaultColorManager;
import net.sf.lapg.common.ui.editor.colorer.DefaultHighlightingManager;
import net.sf.lapg.common.ui.editor.colorer.DefaultHighlightingManager.ColorDescriptor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;

public abstract class DefaultColoringConfigurationBlock {

	private class ColorListLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof ColorGroupDefinition) {
				return ((ColorGroupDefinition) element).getLabel();
			}
			return ((ColorDescriptor) element).getDisplayName();
		}
	}

	private class ColorListContentProvider implements ITreeContentProvider {

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof List<?>) {
				return ((List<?>)parentElement).toArray();
			}
			if(parentElement instanceof ColorGroupDefinition) {
				List<ColorDescriptor> list = fGroupColors.get(((ColorGroupDefinition) parentElement).getId());
				if(list != null) {
					return list.toArray();
				}
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			if (element instanceof ColorGroupDefinition) {
				return fGroups;
			}
			if (element instanceof ColorDescriptor) {
				return fColorGroup.get(element);
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			return element instanceof ColorGroupDefinition;
		}
	}

	private ColorSelector fSyntaxForegroundColorEditor;
	private Label fColorEditorLabel;
	private Button fBoldCheckBox;
	private Button fEnableCheckbox;
	private Button fItalicCheckBox;
	private Button fStrikethroughCheckBox;
	private Button fUnderlineCheckBox;
	private TreeViewer fTreeViewer;

	private final IPreferenceStore fStore;
	private final DefaultColorManager fColorManager;
	private final DefaultHighlightingManager fLapgColorManager;

	private final List<ColorGroupDefinition> fGroups;
	private final List<ColorDescriptor> fColorList = new ArrayList<ColorDescriptor>();
	private final Map<String, List<ColorDescriptor>> fGroupColors = new HashMap<String, List<ColorDescriptor>>();
	private final Map<ColorDescriptor, ColorGroupDefinition> fColorGroup = new HashMap<ColorDescriptor, ColorGroupDefinition>();

	public DefaultColoringConfigurationBlock(OverlayPreferenceStore store) {
		fStore = store;
		fColorManager = new DefaultColorManager(false);
		fLapgColorManager = createHighlightingManager(store, fColorManager);

		// load groups
		fGroups = fLapgColorManager.getGroups();
		Map<String, ColorGroupDefinition> idToGroup = new HashMap<String,ColorGroupDefinition>(fGroups.size());
		for(ColorGroupDefinition group : fGroups) {
			idToGroup.put(group.getId(), group);
		}

		// load colors
		for(ColorDefinition def : fLapgColorManager.getColors()) {
			ColorDescriptor color = fLapgColorManager.getColor(def.getId());
			fColorList.add(color);
			ColorGroupDefinition group = idToGroup.get(def.getCategory());
			if(group == null) {
				group = fGroups.get(0);
			}
			fColorGroup.put(color, group);
			List<ColorDescriptor> list = fGroupColors.get(group.getId());
			if(list == null) {
				list = new ArrayList<ColorDescriptor>();
				fGroupColors.put(group.getId(), list);
			}
			list.add(color);
		}

		store.addKeys(createOverlayStoreKeys());
	}

	protected abstract DefaultHighlightingManager createHighlightingManager(OverlayPreferenceStore store, DefaultColorManager colorManager);

	private OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys() {

		ArrayList<OverlayPreferenceStore.OverlayKey> overlayKeys = new ArrayList<OverlayPreferenceStore.OverlayKey>();

		for (int i = 0, n = fColorList.size(); i < n; i++) {
			ColorDescriptor item = fColorList.get(i);
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, item.getColorKey()));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, item.getBoldKey()));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, item.getItalicKey()));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, item
					.getStrikethroughKey()));
			overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, item
					.getUnderlineKey()));

			if (item.canBeDisabled()) {
				overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, item
						.getEnabledKey()));
			}
		}

		OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return keys;
	}

	/**
	 * Creates page for hover preferences
	 */
	public Control createControl(Composite parent) {
		return createSyntaxPage(parent);
	}

	public void initialize() {
		fTreeViewer.setInput(fGroups);
		fTreeViewer.setSelection(new StructuredSelection(fGroups.get(0)));
	}

	public void performDefaults() {
		handleSyntaxColorListSelection();
	}

	public void dispose() {
		fLapgColorManager.dispose();
		fColorManager.dispose();
	}

	private void handleSyntaxColorListSelection() {
		ColorDescriptor item = getColorDescriptor();
		if (item == null) {
			fEnableCheckbox.setEnabled(false);
			fSyntaxForegroundColorEditor.getButton().setEnabled(false);
			fColorEditorLabel.setEnabled(false);
			fBoldCheckBox.setEnabled(false);
			fItalicCheckBox.setEnabled(false);
			fStrikethroughCheckBox.setEnabled(false);
			fUnderlineCheckBox.setEnabled(false);
			return;
		}
		RGB rgb = PreferenceConverter.getColor(getPreferenceStore(), item.getColorKey());
		fSyntaxForegroundColorEditor.setColorValue(rgb);
		fBoldCheckBox.setSelection(getPreferenceStore().getBoolean(item.getBoldKey()));
		fItalicCheckBox.setSelection(getPreferenceStore().getBoolean(item.getItalicKey()));
		fStrikethroughCheckBox.setSelection(getPreferenceStore().getBoolean(item.getStrikethroughKey()));
		fUnderlineCheckBox.setSelection(getPreferenceStore().getBoolean(item.getUnderlineKey()));
		if (item.canBeDisabled()) {
			fEnableCheckbox.setEnabled(true);
			boolean enable = getPreferenceStore().getBoolean(item.getEnabledKey());
			fEnableCheckbox.setSelection(enable);
			fSyntaxForegroundColorEditor.getButton().setEnabled(enable);
			fColorEditorLabel.setEnabled(enable);
			fBoldCheckBox.setEnabled(enable);
			fItalicCheckBox.setEnabled(enable);
			fStrikethroughCheckBox.setEnabled(enable);
			fUnderlineCheckBox.setEnabled(enable);
		} else {
			fSyntaxForegroundColorEditor.getButton().setEnabled(true);
			fColorEditorLabel.setEnabled(true);
			fBoldCheckBox.setEnabled(true);
			fItalicCheckBox.setEnabled(true);
			fStrikethroughCheckBox.setEnabled(true);
			fUnderlineCheckBox.setEnabled(true);
			fEnableCheckbox.setEnabled(false);
			fEnableCheckbox.setSelection(true);
		}
	}

	private Button createCheckBox(Composite composite, String text) {
		Button result = new Button(composite, SWT.CHECK);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).indent(20, 0).span(2, 1).applyTo(result);
		result.setText(text);
		return result;
	}

	private Control createSyntaxPage(final Composite parent) {
		final Composite colorComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().hint(100, 100).applyTo(colorComposite);
		GridLayoutFactory.fillDefaults().applyTo(colorComposite);

		createHeader(colorComposite);

		Label label = new Label(colorComposite, SWT.LEFT);
		label.setText("&Element:");
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createEditorControls(colorComposite);
		return colorComposite;
	}

	private void createEditorControls(final Composite colorComposite) {
		Composite editorComposite = new Composite(colorComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(editorComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(editorComposite);

		createViewer(editorComposite);
		createSylesControls(editorComposite);
	}

	private void createViewer(Composite editorComposite) {
		fTreeViewer = new TreeViewer(editorComposite, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(fTreeViewer.getControl());
		fTreeViewer.setLabelProvider(new ColorListLabelProvider());
		fTreeViewer.setContentProvider(new ColorListContentProvider());
		fTreeViewer.setAutoExpandLevel(2);
		fTreeViewer.setComparator(new ViewerComparator() {
			@Override
			public int category(Object element) {
				if (element instanceof ColorGroupDefinition) {
					return fGroups.indexOf(element);
				}
				return 0;
			}
		});
		installDoubleClickListener();

		fTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSyntaxColorListSelection();
			}
		});
	}

	private void createSylesControls(Composite editorComposite) {
		Composite stylesComposite = new Composite(editorComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(stylesComposite);
		GridDataFactory.fillDefaults().align(SWT.END, SWT.BEGINNING).applyTo(stylesComposite);

		fEnableCheckbox = new Button(stylesComposite, SWT.CHECK);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).span(2, 1).applyTo(fEnableCheckbox);
		fEnableCheckbox.setText("Enab&le");

		fColorEditorLabel = new Label(stylesComposite, SWT.LEAD);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).indent(20, 1).applyTo(fColorEditorLabel);
		fColorEditorLabel.setText("C&olor:");

		fSyntaxForegroundColorEditor = new ColorSelector(stylesComposite);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(
				fSyntaxForegroundColorEditor.getButton());

		fBoldCheckBox = createCheckBox(stylesComposite, "&Bold");
		fItalicCheckBox = createCheckBox(stylesComposite, "&Italic");
		fStrikethroughCheckBox = createCheckBox(stylesComposite, "&Strikethrough");
		fUnderlineCheckBox = createCheckBox(stylesComposite, "&Underline");

		fSyntaxForegroundColorEditor.getButton().addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				ColorDescriptor item = getColorDescriptor();
				PreferenceConverter.setValue(getPreferenceStore(), item.getColorKey(), fSyntaxForegroundColorEditor
						.getColorValue());
			}
		});

		fEnableCheckbox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				ColorDescriptor item = getColorDescriptor();
				if (item.canBeDisabled()) {
					boolean enable = fEnableCheckbox.getSelection();
					getPreferenceStore().setValue(item.getEnabledKey(), enable);
					fEnableCheckbox.setSelection(enable);
					fSyntaxForegroundColorEditor.getButton().setEnabled(enable);
					fColorEditorLabel.setEnabled(enable);
					fBoldCheckBox.setEnabled(enable);
					fItalicCheckBox.setEnabled(enable);
					fStrikethroughCheckBox.setEnabled(enable);
					fUnderlineCheckBox.setEnabled(enable);
				}
			}
		});

		fBoldCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				ColorDescriptor item = getColorDescriptor();
				getPreferenceStore().setValue(item.getBoldKey(), fBoldCheckBox.getSelection());
			}
		});

		fItalicCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				ColorDescriptor item = getColorDescriptor();
				getPreferenceStore().setValue(item.getItalicKey(), fItalicCheckBox.getSelection());
			}
		});

		fStrikethroughCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				ColorDescriptor item = getColorDescriptor();
				getPreferenceStore().setValue(item.getStrikethroughKey(), fStrikethroughCheckBox.getSelection());
			}
		});

		fUnderlineCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}

			public void widgetSelected(SelectionEvent e) {
				ColorDescriptor item = getColorDescriptor();
				getPreferenceStore().setValue(item.getUnderlineKey(), fUnderlineCheckBox.getSelection());
			}
		});
	}

	private void createHeader(final Composite colorComposite) {
		Link link = new Link(colorComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).hint(150, SWT.DEFAULT).applyTo(link);
		link
		.setText("Default colors and font can be configured on the <a href=\"org.eclipse.ui.preferencePages.GeneralTextEditor\">Text Editors</a> and on the <a href=\"org.eclipse.ui.preferencePages.ColorsAndFonts\">Colors and Fonts</a> preference page.");
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(colorComposite.getShell(), e.text, null, null);
			}
		});

		addFiller(colorComposite, 1);
	}

	private void installDoubleClickListener() {
		fTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection s = (IStructuredSelection) event.getSelection();
				Object element = s.getFirstElement();
				if (fTreeViewer.isExpandable(element)) {
					fTreeViewer.setExpandedState(element, !fTreeViewer.getExpandedState(element));
				}
			}
		});
	}

	private void addFiller(Composite composite, int horizontalSpan) {
		PixelConverter pixelConverter = new PixelConverter(composite);
		Label filler = new Label(composite, SWT.LEFT);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = horizontalSpan;
		gd.heightHint = pixelConverter.convertHeightInCharsToPixels(1) / 2;
		filler.setLayoutData(gd);
	}

	private ColorDescriptor getColorDescriptor() {
		IStructuredSelection selection = (IStructuredSelection) fTreeViewer.getSelection();
		Object element = selection.getFirstElement();
		if (element instanceof ColorGroupDefinition) {
			return null;
		}
		return (ColorDescriptor) element;
	}

	protected final IPreferenceStore getPreferenceStore() {
		return fStore;
	}
}
