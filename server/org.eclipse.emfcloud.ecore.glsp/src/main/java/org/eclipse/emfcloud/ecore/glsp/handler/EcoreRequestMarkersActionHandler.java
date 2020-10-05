package org.eclipse.emfcloud.ecore.glsp.handler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emfcloud.ecore.glsp.EcoreModelIndex;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelServerAccess;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.emfcloud.modelserver.client.ModelServerClient;
import org.eclipse.glsp.api.action.Action;
import org.eclipse.glsp.api.action.kind.RequestMarkersAction;
import org.eclipse.glsp.api.action.kind.SetMarkersAction;
import org.eclipse.glsp.api.markers.Marker;
import org.eclipse.glsp.api.markers.MarkerKind;
import org.eclipse.glsp.api.model.GraphicalModelState;
import org.eclipse.glsp.api.utils.ClientOptions;
import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.server.actionhandler.RequestMarkersHandler;

public class EcoreRequestMarkersActionHandler extends RequestMarkersHandler {

    @Override
    public List<Action> executeAction(final RequestMarkersAction action, final GraphicalModelState modelState) {
        String modeluri = modelState.getClientOptions().get(ClientOptions.SOURCE_URI);
        List<Marker> markers = new ArrayList<>();
        EcoreModelState ecoreModelState = EcoreModelState.getModelState(modelState);
        markers = createMarkers(ecoreModelState);
        return listOf(new SetMarkersAction(markers));
   }

   public List<Marker> createMarkers(EcoreModelState ecoreModelState){
        EcoreModelIndex index = ecoreModelState.getIndex();
        List<Marker> markers = new ArrayList();
        for(String id: index.allIds() ){
            Optional<GModelElement> gElement = index.getGModelElement(id);
            if(gElement.isPresent()) {
            	markers.add(new Marker("Error", "Error message", gElement.get().getId(), MarkerKind.ERROR));
            } else {
            	markers.add(new Marker("Error", "Error message", index.getRoot().getId(), MarkerKind.ERROR));
            }
   
        }
        return markers;
   }

   public String getMarkerKind(int severity){
        switch(severity){
            case Diagnostic.ERROR:
                return MarkerKind.ERROR;
            case Diagnostic.WARNING:
                return MarkerKind.WARNING;
            default:
                return MarkerKind.INFO;
        }
   }
}