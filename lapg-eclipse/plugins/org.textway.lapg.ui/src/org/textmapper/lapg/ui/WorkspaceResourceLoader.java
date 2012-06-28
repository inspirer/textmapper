package org.textmapper.lapg.ui;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.textmapper.templates.storage.IResourceLoader;
import org.textmapper.templates.storage.Resource;

public class WorkspaceResourceLoader implements IResourceLoader {
	
	private IFolder myFolder; 
	
	public WorkspaceResourceLoader(IFolder folder) {
		myFolder = folder;
	}
	
	public IProject getProject() {
		IProject p = myFolder.getProject();
		if(p != null && p.isAccessible()) {
			return p;
		}
		return null;
	}

	public Resource loadResource(String qualifiedName, String kind) {
		Path p = new Path(qualifiedName.replaceAll("\\.", "/") + "." + kind);
		IFile f = myFolder.getFile(p);
		if(f.exists()) {
			try {
				String content = FileUtil.getStreamContents(f.getContents(), f.getCharset());
				return new Resource(f.getLocationURI(), content);
			} catch (IOException e) {
			} catch (CoreException e) {
			}
		}
		return null;
	}

	public static WorkspaceResourceLoader create(IProject p, String location) {
		Path path = new Path(location);
		IResource resource = null;
		if(path.isAbsolute()) {
			resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
		} else {
			resource = p.findMember(path);
		}
		if(resource instanceof IFolder && resource.exists()) {
			return new WorkspaceResourceLoader((IFolder) resource);
		}
		return null;
	}
}
