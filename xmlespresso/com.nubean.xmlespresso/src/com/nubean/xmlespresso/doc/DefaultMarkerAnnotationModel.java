/*
The MIT License (MIT)

Copyright (c) 2015 NuBean LLC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.nubean.xmlespresso.doc;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

public class DefaultMarkerAnnotationModel extends ResourceMarkerAnnotationModel {

	private IWorkspace workspace;
	private IResourceChangeListener fResourceChangeListener; 


	public DefaultMarkerAnnotationModel(IResource resource) {
		super(resource);
		this.workspace = resource.getWorkspace();
		this.fResourceChangeListener= new ResourceChangeListener();
	}

	@Override
	protected Position createPositionFromMarker(IMarker marker) {
		Position p = super.createPositionFromMarker(marker);
		if (p == null) {
			p = new Position(0, 0);
		}

		return p;
	}

	@Override
	protected void deleteMarkers(final IMarker[] markers) throws CoreException {
		workspace.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (int i = 0; i < markers.length; ++i) {
					markers[i].delete();
				}
			}
		}, null, IWorkspace.AVOID_UPDATE, null);

	}

	@Override
	protected boolean isAcceptable(IMarker marker) {
		return marker != null && getResource().equals(marker.getResource());
	}

	@Override
	protected void listenToMarkerChanges(boolean listen) {
		if (listen)
			workspace.addResourceChangeListener(fResourceChangeListener);
		else
			workspace.removeResourceChangeListener(fResourceChangeListener);

	}

	@Override
	protected IMarker[] retrieveMarkers() throws CoreException {
		IMarker[] markers = getResource()
				.findMarkers(IMarker.MARKER, true, IResource.DEPTH_ZERO);
		return markers;
	}

	protected void update(IMarkerDelta[] markerDeltas) {
		if (markerDeltas.length == 0)
			return;

		for (int i = 0; i < markerDeltas.length; i++) {
			IMarkerDelta delta = markerDeltas[i];
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				addMarkerAnnotation(delta.getMarker());
				break;
			case IResourceDelta.REMOVED:
				removeMarkerAnnotation(delta.getMarker());
				break;
			case IResourceDelta.CHANGED:
				modifyMarkerAnnotation(delta.getMarker());
				break;
			}
		}

		fireModelChanged();
	}
	

	private class ResourceChangeListener implements IResourceChangeListener {
		public void resourceChanged(IResourceChangeEvent e) {
			IResourceDelta delta = e.getDelta();
			if (delta != null && getResource() != null) {
				IResourceDelta child = delta
						.findMember(getResource().getFullPath());
				if (child != null)
					update(child.getMarkerDeltas());
			}
		}
	}

}
