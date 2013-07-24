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
package org.textmapper.lapg.ui.wizard;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard;
import org.textmapper.lapg.ui.LapgUIActivator;
import org.textmapper.lapg.ui.build.IncrementalLapgBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class CreateSyntaxFileWizard extends BasicNewFileResourceWizard {

	private final class NewSyntaxFileCreationPage extends WizardNewFileCreationPage {
		private NewSyntaxFileCreationPage(String pageName, IStructuredSelection selection) {
			super(pageName, selection);
		}

		@Override
		protected InputStream getInitialContents() {
			String defaultContent =
					"# lapg syntax file\n" +
					"\n" +
					"language syntax(java);\n" +
					"\n" +
					"prefix = \"Lang\"\n" +
					"package = \"org.example.mylang.parser\"\n" +
					"\n" +
					":: lexer\n" +
					"\n" +
					"identifier(String): /[a-zA-Z_][a-zA-Z_0-9]*/   { $lexem = current(); break; }\n" +
					"icon(Integer):  /-?[0-9]+/                     { $lexem = Integer.parseInt(current()); break; }\n" +
					"_skip: /[\\n\\t\\r ]+/ (space)\n" +
					"\n" +
					":: parser\n" +
					"\n" +
					"input ::=\n" +
					"\tidentifier ;\n" +
					"\n" +
					"%%\n";

			try {
				return new ByteArrayInputStream(defaultContent.getBytes("ISO-8859-1"));
			} catch (UnsupportedEncodingException e) {
				return null;
			}
		}
	}

	private NewSyntaxFileCreationPage mainPage;

	public CreateSyntaxFileWizard() {
	}

	@Override
	public void addPages() {
		mainPage = new NewSyntaxFileCreationPage("newFilePage1", getSelection());
		mainPage.setTitle("New Syntax File");
		// FIXME use string from plugin.properties
		mainPage.setDescription("Create a new Textmapper file (.tm)");
		mainPage.setFileName("syntax.tm");
		addPage(mainPage);
	}

	@Override
	public boolean performFinish() {
		IFile file = mainPage.createNewFile();
		if (file == null) {
			return false;
		}

		selectAndReveal(file);

		// Open editor on new file.
		IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
		try {
			if (dw != null) {
				IWorkbenchPage page = dw.getActivePage();
				if (page != null) {
					IDE.openEditor(page, file, true);
				}
			}
		} catch (PartInitException e) {
			// DialogUtil.openError(dw.getShell(),
			// ResourceMessages.FileResource_errorMessage,
			// e.getMessage(), e);
		}

		IFile f = mainPage.createNewFile();
		configureBuilder(f.getProject());
		return true;
	}

	private void configureBuilder(final IProject project) {
		try {
			final IProjectDescription desc = project.getDescription();
			final ICommand[] commands = desc.getBuildSpec();

			for (ICommand element : commands) {
				if (element.getBuilderName().equals(IncrementalLapgBuilder.BUILDER_ID)) {
					return;
				}
			}

			project.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					final ICommand[] newCommands = new ICommand[commands.length + 1];
					System.arraycopy(commands, 0, newCommands, 0, commands.length);
					final ICommand command = desc.newCommand();
					command.setBuilderName(IncrementalLapgBuilder.BUILDER_ID);
					newCommands[commands.length] = command;
					desc.setBuildSpec(newCommands);
					project.setDescription(desc, monitor);
				}

			}, project.getWorkspace().getRoot(), 0, null);
		} catch (CoreException ex) {
			LapgUIActivator.log(ex.getStatus());
		}
	}
}
