package org.textmapper.lapg.ui.build;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.textmapper.lapg.api.DerivedSourceElement;
import org.textmapper.lapg.api.ParserConflict;
import org.textmapper.lapg.api.ProcessingStatus;
import org.textmapper.lapg.api.Rule;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.TextSourceElement;
import org.textmapper.lapg.ui.FileUtil;
import org.textmapper.lapg.ui.LapgProjectSettings;
import org.textmapper.lapg.ui.LapgUIActivator;
import org.textmapper.lapg.ui.WorkspaceResourceLoader;
import org.textmapper.templates.storage.IResourceLoader;
import org.textmapper.tool.common.GeneratedFile;
import org.textmapper.tool.gen.TMGenerator;
import org.textmapper.tool.gen.TMOptions;
import org.textmapper.tool.gen.ProcessingStrategy;
import org.textmapper.tool.parser.TMTree;
import org.textmapper.tool.parser.TMTree.TMProblem;
import org.textmapper.tool.parser.TMTree.TextSource;

public class IncrementalLapgBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = LapgUIActivator.PLUGIN_ID + ".lapgBuilder";

	public IncrementalLapgBuilder() {
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask("Building lapg grammars in " + getProject().getName(), 10);
		LapgProjectSettings settings = LapgUIActivator.getDefault().getProjectSettings(getProject());
		if (settings == null) {
			return null;
		}

		Collection<IPath> resources = collectResourcesToBuild(kind == FULL_BUILD, settings);
		monitor.worked(1);
		checkCanceled(monitor);
		if (!resources.isEmpty()) {
			buildResources(resources, settings, new SubProgressMonitor(monitor, 9));
		}

		monitor.done();
		return settings.getReferencedProjects();
	}

	private void buildResources(Collection<IPath> resources, LapgProjectSettings settings, IProgressMonitor monitor) {
		monitor.beginTask("Building " + resources.size() + " file(s)", resources.size());
		for (IPath p : resources) {
			checkCanceled(monitor);
			buildSyntax(p, settings.getSettings().get(p), new SubProgressMonitor(monitor, 1));
		}
		monitor.done();
	}

	private void buildSyntax(IPath p, TMOptions TMOptions, IProgressMonitor monitor) {
		monitor.beginTask("Building " + p.lastSegment(), 12);
		IFile file = getProject().getFile(p);
		if (file == null || !file.exists()) {
			return;
		}

		try {
			String content = FileUtil.getStreamContents(file.getContents(), file.getCharset());
			monitor.worked(1);
			TextSource source = new TextSource(file.getName(), content, 1);
			BuilderStrategy strategy = new BuilderStrategy(file);
			BuilderStatus status = new BuilderStatus(file, TMOptions);

			deleteMarkers(file);
			monitor.worked(1);
			boolean result = new TMGenerator(TMOptions, status, strategy).compileGrammar(source, false);
			monitor.worked(9);

			if (result) {
				// TODO store files
				monitor.worked(1);
			}
		} catch (IOException e) {
		} catch (CoreException e) {
			LapgUIActivator.log(e);
		}
		monitor.done();
	}

	protected void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(LapgUIActivator.PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			LapgUIActivator.log(e);
		}
	}

	private Collection<IPath> collectResourcesToBuild(boolean fullBuild, LapgProjectSettings settings) {
		IResourceDelta delta = fullBuild ? null : getDelta(getProject());
		if (delta == null) {
			return settings.getSettings().keySet();
		}

		// settings changed, rebuild
		IResourceDelta settingsDelta = delta.findMember(LapgProjectSettings.SETTINGS_FILE);
		if (settingsDelta != null && FileUtil.affectsFile(settingsDelta)) {
			return settings.getSettings().keySet();
		}

		Collection<IPath> changedFiles = new ArrayList<IPath>();
		for (Entry<IPath, TMOptions> entry : settings.getSettings().entrySet()) {
			IPath p = entry.getKey();
			IResourceDelta fileDelta = delta.findMember(p);
			if (fileDelta != null && FileUtil.affectsFile(fileDelta)) {
				changedFiles.add(p);
				continue;
			}

			List<String> folders = entry.getValue().getIncludeFolders();
			if (folders == null) {
				continue;
			}
			for (String folder : folders) {
				IPath path = new Path(folder);
				IResourceDelta projectDelta = delta;
				if (path.isAbsolute()) {
					projectDelta = getDelta(ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0)));
					path = path.removeFirstSegments(1);
				}
				if (projectDelta == null || isChanged(path, projectDelta)) {
					changedFiles.add(p);
					break;
				}
			}
		}

		return changedFiles;
	}

	private boolean isChanged(IPath pathInProject, IResourceDelta projectDelta) {
		IResourceDelta delta = projectDelta.findMember(pathInProject);
		if (delta == null) {
			return false;
		}

		final boolean[] clean = new boolean[]{true};
		try {
			delta.accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) throws CoreException {
					IResource resource = delta.getResource();
					if (resource.isDerived()) {
						return false;
					}
					if (resource instanceof IFile) {
						if (!FileUtil.isTemplateFile((IFile) resource)) {
							return false;
						}
						if (FileUtil.affectsFile(delta)) {
							clean[0] = false;
						}
					}
					return clean[0];
				}
			});
		} catch (CoreException e) {
			LapgUIActivator.logError("exception while analyzing delta", e);
			return true;
		}
		return !clean[0];
	}

	@Override
	protected void clean(final IProgressMonitor monitor) throws CoreException {
		getProject().accept(new IResourceProxyVisitor() {
			public boolean visit(IResourceProxy proxy) throws CoreException {
				if (proxy.getType() == IResource.FILE) {
					if ("s".equals(proxy.requestFullPath().getFileExtension())) {
						proxy.requestResource().deleteMarkers(LapgUIActivator.PROBLEM_MARKER, true,
								IResource.DEPTH_INFINITE);
					}
					return false;
				}
				return true;
			}

		}, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
	}

	private void checkCanceled(final IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}

	private void createMarker(IFile file, int start, int end, int line, boolean isWarning, String message) {
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put(IMarker.CHAR_START, start);
		attributes.put(IMarker.CHAR_END, end);
		attributes.put(IMarker.MESSAGE, message);
		int severity = isWarning ? IMarker.SEVERITY_WARNING : IMarker.SEVERITY_ERROR;
		attributes.put(IMarker.SEVERITY, severity);
		attributes.put(IMarker.LINE_NUMBER, line);
		try {
			IMarker marker = file.createMarker(LapgUIActivator.PROBLEM_MARKER);
			marker.setAttributes(attributes);
		} catch (CoreException ex) {
			LapgUIActivator.logWarning("lapgBuilder: cannot create marker", ex);
		}
	}

	private void createMarker(IFile file, TMProblem p, TextSource source) {
		createMarker(file, p.getOffset(), p.getEndoffset(), source.lineForOffset(p.getOffset()),
				p.getKind() == TMTree.KIND_WARN, p.getMessage());
	}

	private void createMarker(IFile file, TextSourceElement element, boolean isWarning, String message) {
		createMarker(file, element.getOffset(), element.getEndoffset(), element.getLine(), isWarning, message);
	}

	private class BuilderStatus implements ProcessingStatus {

		private final IFile myFile;
		private final TMOptions options;

		public BuilderStatus(IFile file, TMOptions options) {
			myFile = file;
			this.options = options;
		}

		public void report(int kind, String message, SourceElement... anchors) {
			if (kind == KIND_INFO) {
				return;
			}
			for (SourceElement anchor : anchors) {
				while (anchor instanceof DerivedSourceElement) {
					anchor = ((DerivedSourceElement) anchor).getOrigin();
				}
				if (anchor instanceof TextSourceElement) {
					createMarker(myFile, (TextSourceElement) anchor, kind == KIND_WARN, message);
				} else {
					createMarker(myFile, 0, 1, 1, kind == KIND_WARN, message);
				}
			}
		}

		public void report(String message, Throwable th) {
			LapgUIActivator.logError(message, th);
		}

		public void report(ParserConflict conflict) {
			if (conflict.getKind() == ParserConflict.FIXED) {
				return;
			}
			for (Rule r : conflict.getRules()) {
				SourceElement anchor = r;
				while (anchor instanceof DerivedSourceElement) {
					anchor = ((DerivedSourceElement) anchor).getOrigin();
				}
				if (anchor instanceof TextSourceElement) {
					createMarker(myFile, (TextSourceElement) anchor, false, conflict.getText());
				} else {
					createMarker(myFile, 0, 1, 1, false, conflict.getText());
				}
			}
		}

		public void debug(String info) {
		}

		public boolean isDebugMode() {
			return options.getDebug() >= 2;
		}

		public boolean isAnalysisMode() {
			return options.getDebug() >= 1;
		}
	}

	private class BuilderStrategy implements ProcessingStrategy {

		private final IFile file;
		private Map<String, String> result = new HashMap<String, String>();

		public BuilderStrategy(IFile file) {
			super();
			this.file = file;
		}

		public void createFile(String name, String contents, Map<String, Object> options, ProcessingStatus status) {
			// TODO Auto-generated method stub
			new GeneratedFile(null, name, contents, options) {
				@Override
				public void create() {
					result.put(name, getData());
				}
			}.create();
		}

		public IResourceLoader createResourceLoader(String path) {
			return WorkspaceResourceLoader.create(file.getProject(), path);
		}
	}
}
