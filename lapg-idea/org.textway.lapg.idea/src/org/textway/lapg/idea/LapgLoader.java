/**
 * Copyright 2002-2011 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.lapg.idea;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerAdapter;
import org.jetbrains.annotations.NotNull;

public class LapgLoader implements ApplicationComponent {

	public LapgLoader() {
	}

	public void initComponent() {
		ProjectManager.getInstance().addProjectManagerListener(new ProjectManagerAdapter() {
			public void projectOpened(final Project project) {
			}
		});
	}

	public void disposeComponent() {
	}

	@NotNull
	public String getComponentName() {
		return "lapg.support.loader";
	}
}
