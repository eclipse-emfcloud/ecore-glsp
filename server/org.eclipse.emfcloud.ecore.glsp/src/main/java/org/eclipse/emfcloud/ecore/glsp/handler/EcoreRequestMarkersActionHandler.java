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
import org.eclipse.emfcloud.GenericValidation;
import org.eclipse.emfcloud.ModelServerValidationResult;
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
        GenericValidation validation;
        String modeluri = modelState.getClientOptions().get(ClientOptions.SOURCE_URI);
        List<Marker> markers = new ArrayList<>();
        try {
            ModelServerClient modelServerClient = new ModelServerClient("http://localhost:8081/api/v1/");
            validation = new GenericValidation(modelServerClient);
            EcoreModelState ecoreModelState = EcoreModelState.getModelState(modelState);
            EcoreModelServerAccess access = new EcoreModelServerAccess(modeluri, modelServerClient, ecoreModelState.getIndex());
            access.setEcoreFacade(EcoreModelState.getEcoreFacade(modelState));
            access.update();
            Thread.sleep(1000);
            validation.validate(modeluri);
            Thread.sleep(1000);
            markers = createMarkers(validation.recentValidationResultMap.get(modeluri), ecoreModelState);
            System.out.println("Test");
            
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       return listOf(new SetMarkersAction(markers));
   }

   public List<Marker> createMarkers(List<ModelServerValidationResult> validationResult, EcoreModelState ecoreModelState){
        EcoreModelIndex index = ecoreModelState.getIndex();
        List<Marker> markers = new ArrayList();
        for(ModelServerValidationResult r: validationResult){
            Optional<GModelElement> gElement = index.getGModelElement(r.getIdentifier());
            BasicDiagnostic diagnostic = r.getDiagnostic();
            //Filter out non error messages
            if(diagnostic.getData().size() > 1){
                if(gElement.isPresent()){
                    markers.add(new Marker(diagnostic.getMessage(), diagnostic.getMessage(), gElement.get().getId(), getMarkerKind(r.getDiagnostic().getSeverity())));
                }else{
                    markers.add(new Marker(diagnostic.getMessage(), diagnostic.getMessage(), ecoreModelState.getRoot().getId(), getMarkerKind(r.getDiagnostic().getSeverity())));
                }
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