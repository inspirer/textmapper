/**
 * Copyright (c) 2010-2014 Evgeny Gryaznov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.textmapper.idea.java;

import com.intellij.compiler.impl.CompilerUtil;
import com.intellij.compiler.server.CustomBuilderMessageHandler;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.compiler.TmBuilderMessages;
import org.textmapper.idea.compiler.TmCompilerUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GeneratedFilesRefresher implements ProjectComponent {

	private Project project;

	public GeneratedFilesRefresher(Project project) {
		this.project = project;
	}

	public void projectOpened() {
		project.getMessageBus().connect().subscribe(CustomBuilderMessageHandler.TOPIC, new RefreshJavaCompilationStatusListener());
	}

	public void projectClosed() {
	}

	public void initComponent() {
	}

	public void disposeComponent() {
	}

	@NotNull
	public String getComponentName() {
		return "Generated Files Refresher";
	}

	private class RefreshJavaCompilationStatusListener implements CustomBuilderMessageHandler {

		private final AtomicReference<List<File>>
				myAffectedFiles = new AtomicReference<>(new ArrayList<>());

		@Override
		public void messageReceived(String builderId, String messageType, String messageText) {
			if (!TmCompilerUtil.BUILDER_ID.equals(builderId)) {
				return;
			}

			if (messageType.equals(TmBuilderMessages.MSG_CHANGED)) {
				myAffectedFiles.get().add(new File(messageText));

			} else if (messageType.equals(TmBuilderMessages.MSG_REFRESH)) {
				final List<File> generatedJava = myAffectedFiles.getAndSet(new ArrayList<>());
				if (project.isDisposed() || generatedJava.isEmpty()) {
					return;
				}

				// refresh affected files
				CompilerUtil.refreshIOFiles(generatedJava);
			}
		}
	}
}
