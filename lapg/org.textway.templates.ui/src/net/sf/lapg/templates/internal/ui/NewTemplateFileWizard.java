package net.sf.lapg.templates.internal.ui;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard;


public class NewTemplateFileWizard extends BasicNewFileResourceWizard {

    public NewTemplateFileWizard() {
    }

    @Override
    public void addPages() {
    	super.addPages();
    	WizardNewFileCreationPage page = (WizardNewFileCreationPage) getPage("newFilePage1");
    	page.setTitle("New Template File");
    	page.setDescription("Creates a new Lapg Templates File");
    	page.setFileName("TemplateFile.ltp");
    }

    @Override
    public boolean performFinish() {
    	if (super.performFinish()) {
    		IFile f = ((WizardNewFileCreationPage) getPage("newFilePage1")).createNewFile();
    		configureBuilder(f.getProject());
    		return true;
    	} else {
    		return false;
    	}
    }

    private void configureBuilder(final IProject project) {
        try {
            final IProjectDescription desc = project.getDescription();
            final ICommand[] commands = desc.getBuildSpec();

            for (ICommand element : commands) {
                if (element.getBuilderName().equals("ltp_builder")) {
    				return;
    			}
            }

//            project.getWorkspace().run(new IWorkspaceRunnable() {
//
//		        public void run(IProgressMonitor monitor) throws CoreException {
//			        final ICommand[] newCommands = new ICommand[commands.length + 1];
//			        System.arraycopy(commands, 0, newCommands, 0, commands.length);
//			        final ICommand command = desc.newCommand();
//			        command.setBuilderName("ltp_builder");
//			        newCommands[commands.length] = command;
//			        desc.setBuildSpec(newCommands);
//			        project.setDescription(desc, monitor);
//				}
//
//	    	}, project.getWorkspace().getRoot(), 0, null);
        } catch (CoreException ex) {
        	/* ignore */
        }
    }
}
