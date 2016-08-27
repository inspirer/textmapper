/**
 * Copyright (c) 2010-2016 Evgeny Gryaznov
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
package org.textmapper.jps.build;

import com.intellij.openapi.util.io.FileUtil;

import java.util.*;

/**
 * evgeny, 11/28/12
 */
public class TmIdeaRefreshComponent {

	private final Object LOCK = new Object();
	private List<String> created = new ArrayList<>();
	private List<String> removed = new ArrayList<>();
	private List<String> genRoots = new ArrayList<>();

	public void refresh(String path) {
		synchronized (LOCK) {
			created.add(path);
		}
	}

	public void filesRemoved(Collection<String> paths) {
		synchronized (LOCK) {
			removed.addAll(paths);
		}
	}

	public void addOutputRoot(String path) {
		path = FileUtil.toCanonicalPath(path);
		synchronized (LOCK) {
			genRoots.add(path);
		}
	}

	Collection<String> getFilesToRefresh() {
		Set<String> result = new HashSet<>();
		synchronized (LOCK) {
			result.addAll(created);
			for (String r : removed) {
				for (String root : genRoots) {
					if (FileUtil.isAncestor(root, r, false)) {
						result.add(r);
						break;
					}
				}
			}
		}
		return result;
	}
}
