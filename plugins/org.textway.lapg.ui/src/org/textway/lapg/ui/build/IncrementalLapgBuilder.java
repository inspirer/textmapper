package org.textway.lapg.ui.build;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.textway.lapg.api.ParserConflict;
import org.textway.lapg.api.ProcessingStatus;
import org.textway.lapg.api.ProcessingStrategy;
import org.textway.lapg.api.Rule;
import org.textway.lapg.api.SourceElement;
import org.textway.lapg.common.GeneratedFile;
import org.textway.lapg.gen.LapgGenerator;
import org.textway.lapg.gen.LapgOptions;
import org.textway.lapg.parser.LapgTree;
import org.textway.lapg.parser.LapgTree.LapgProblem;
import org.textway.lapg.parser.LapgTree.TextSource;
import org.textway.lapg.ui.FileUtil;
import org.textway.lapg.ui.LapgProjectSettings;
import org.textway.lapg.ui.LapgUIActivator;
import org.textway.lapg.ui.WorkspaceResourceLoader;
import org.textway.templates.storage.IResourceLoader;

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

		LapgProjectSettings settings = LapgUIActivator.getDefault().getProjectSettings(getProject());
		if (settings == null) {
			return null;
		}

		monitor.beginTask("Building lapg grammars in " + getProject().getName(), 10);
		Collection<IPath> resources = collectResourcesToBuild(kind == FULL_BUILD, settings);
		monitor.worked(1);
		checkCanceled(monitor);
		if (!resources.isEmpty()) {
			SubProgressMonitor compiling = new SubProgressMonitor(monitor, 9);
			compiling.beginTask("Building " + resources.size() + " file(s)", resources.size());
			for (IPath p : resources) {
				checkCanceled(monitor);
				compileSyntax(p, settings.getSettings().get(p), new SubProgressMonitor(compiling, 1));
			}
			compiling.done();
		}

		monitor.done();
		return settings.getReferencedProjects();
	}

	private void compileSyntax(IPath p, LapgOptions lapgOptions, IProgressMonitor monitor) {
		monitor.beginTask("Building " + p.lastSegment(), 10);
		IFile file = getProject().getFile(p);
		if(file == null || !file.exists()) {
			return;
		}
		
		try {
			String content = FileUtil.getStreamContents(file.getContents(),	file.getCharset());
			TextSource source = new TextSource(file.getName(), content.toCharArray(), 1);
			BuilderStrategy strategy = new BuilderStrategy(file);
			BuilderStatus status = new BuilderStatus(file, lapgOptions);

			deleteMarkers(file);
			boolean result = new LapgGenerator(lapgOptions, status, strategy).compileGrammar(source);

			if(result) {
				// TODO store files				
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
		} catch(CoreException e) {
			LapgUIActivator.log(e);
		}
	}

	private Collection<IPath> collectResourcesToBuild(boolean fullBuild, LapgProjectSettings settings) {
		if (!fullBuild) {
			IResourceDelta delta = getDelta(getProject());
			// TODO optimize
		}

		return settings.getSettings().keySet();
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

	private void createMarker(IFile file, LapgProblem p, TextSource source) {
		createMarker(file, p.getOffset(), p.getEndOffset(), source.lineForOffset(p.getOffset()),
				p.getKind() == LapgTree.KIND_WARN, p.getMessage());
	}

	private void createMarker(IFile file, SourceElement element, boolean isWarning, String message) {
		createMarker(file, element.getOffset(), element.getEndOffset(), element.getLine(), isWarning, message);
	}

	private class BuilderStatus implements ProcessingStatus {
		
		private final IFile myFile;
		private final LapgOptions options;
		
		public BuilderStatus(IFile file, LapgOptions options) {
			myFile = file;
			this.options = options;
		}

		public void report(int kind, String message, SourceElement... anchors) {
			if(kind == KIND_INFO) {
				return;
			}
			for(SourceElement anchor : anchors) {
				createMarker(myFile, anchor, kind == KIND_WARN, message);
			}
		}

		public void report(String message, Throwable th) {
			LapgUIActivator.logError(message, th);
		}

		public void report(ParserConflict conflict) {
			if(conflict.getKind() == ParserConflict.FIXED) {
				return;
			}
			for(Rule r : conflict.getRules()) {
				createMarker(myFile, r, false, conflict.getText());
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
		private Map<String,String> result = new HashMap<String,String>();
		
		public BuilderStrategy(IFile file) {
			super();
			this.file = file;
		}

		public void createFile(String name, String contents, ProcessingStatus status) {
			// TODO Auto-generated method stub
			new GeneratedFile(name, contents, "utf8", false) {
				@Override
				public void create() {
					result.put(getName(), getData());
				}
			}.create();
		}

		public IResourceLoader createResourceLoader(String path) {
			return WorkspaceResourceLoader.create(file.getProject(), path);
		}
	}
}
