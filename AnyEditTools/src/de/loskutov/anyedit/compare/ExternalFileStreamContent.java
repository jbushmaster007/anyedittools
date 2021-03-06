/*******************************************************************************
 * Copyright (c) 2009 Andrey Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributor:  Andrey Loskutov - initial API and implementation
 *******************************************************************************/
package de.loskutov.anyedit.compare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.compare.BufferedContent;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IEditableContentExtension;
import org.eclipse.compare.IModificationDate;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import de.loskutov.anyedit.AnyEditToolsPlugin;

/**
 * Content for external files without document support.
 * @author Andrey
 */
public class ExternalFileStreamContent extends BufferedContent implements StreamContent,
IEditableContent, IModificationDate, IEditableContentExtension {

    protected boolean dirty;
    private final ContentWrapper content;


    public ExternalFileStreamContent(ContentWrapper content) {
        super();
        this.content = content;
    }

    @Override
    public void setContent(byte[] contents) {
        dirty = true;
        super.setContent(contents);
    }

    @Override
    public Image getImage() {
        return CompareUI.getImage(content.getFileExtension());
    }

    @Override
    public boolean commitChanges(IProgressMonitor pm) throws CoreException {
        if (!dirty) {
            return true;
        }

        byte[] bytes = getContent();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(content.getFile());
            fos.write(bytes);
            return true;
        } catch (IOException e) {
            AnyEditToolsPlugin.errorDialog(
                    "Can't store compare buffer to external file: " + content.getFile(), e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return false;
    }

    @Override
    protected InputStream createStream() throws CoreException {
        FileInputStream fis;
        try {
            fis = new FileInputStream(content.getFile());
            return fis;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public String getName() {
        return content.getName();
    }

    @Override
    public String getFullName() {
        return content.getFullName();
    }

    @Override
    public String getType() {
        return content.getFileExtension();
    }

    @Override
    public Object[] getChildren() {
        return new StreamContent[0];
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public ITypedElement replace(ITypedElement dest, ITypedElement src) {
        return null;
    }

    @Override
    public long getModificationDate() {
        return content.getFile().lastModified();
    }

    @Override
    public boolean isReadOnly() {
        return !content.getFile().canWrite();
    }

    @Override
    public IStatus validateEdit(Shell shell) {
        File file = content.getFile();
        if(file.canWrite()) {
            return Status.OK_STATUS;
        }
        FileInfo fi = new FileInfo(file.getAbsolutePath());
        fi.setAttribute(EFS.ATTRIBUTE_READ_ONLY, false);
        try {
            IFileStore store = EFS.getStore(URIUtil.toURI(file.getAbsolutePath()));
            store.putInfo(fi, EFS.SET_ATTRIBUTES, null);
        } catch (CoreException e) {
            AnyEditToolsPlugin.logError("Can't make file writable: " + file, e);
        }
        if(file.canWrite()) {
            return Status.OK_STATUS;
        }
        return Status.CANCEL_STATUS;
    }

    @Override
    public void dispose() {
        discardBuffer();
    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    @Override
    public void init(AnyeditCompareInput input) {
        getContent();
    }

    @Override
    public StreamContent recreate() {
        return new ExternalFileStreamContent(content);
    }

    @Override
    public Object getAdapter(Class adapter) {
        return null;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
