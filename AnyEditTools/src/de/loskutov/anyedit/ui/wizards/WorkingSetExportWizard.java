/*******************************************************************************
 * Copyright (c) 2009 Andrey Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributor:  Andrey Loskutov - initial API and implementation
 *******************************************************************************/
package de.loskutov.anyedit.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

/**
 * @author Andrey
 */
public class WorkingSetExportWizard extends Wizard implements IExportWizard {

    private ExportPage mainPage;

    public WorkingSetExportWizard() {
        super();
    }

    @Override
    public boolean performFinish() {
        return mainPage.finish();
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // noop
    }

    @Override
    public void addPages() {
        super.addPages();
        mainPage = new ExportPage("Working Set Export");
        addPage(mainPage);
        setWindowTitle(mainPage.getName());
    }
}
