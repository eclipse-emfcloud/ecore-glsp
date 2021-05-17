package org.eclipse.emfcloud.ecore.glsp.model;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emfcloud.ecore.enotation.Diagram;
import org.eclipse.emfcloud.ecore.glsp.EcoreEditorContext;
import org.eclipse.emfcloud.ecore.glsp.EcoreFacade;
import org.eclipse.emfcloud.ecore.glsp.ModelServerClientProvider;
import org.eclipse.emfcloud.modelserver.client.ModelServerClientApi;
import org.eclipse.emfcloud.modelserver.edit.CommandCodec;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GModelRoot;
import org.eclipse.glsp.graph.builder.impl.GGraphBuilder;
import org.eclipse.glsp.server.actions.ActionDispatcher;
import org.eclipse.glsp.server.features.core.model.ModelSourceLoader;
import org.eclipse.glsp.server.features.core.model.RequestModelAction;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.utils.ClientOptions;

import com.google.inject.Inject;

public class EcoreModelSourceLoader implements ModelSourceLoader {

	private static Logger LOGGER = Logger.getLogger(EcoreModelSourceLoader.class);
	private static final String ROOT_ID = "sprotty";

	@Inject
	private ModelServerClientProvider modelServerClientProvider;

	@Inject
	private ActionDispatcher actionDispatcher;

	@Inject
	private CommandCodec commandCodec;

	@Override
	public void loadSourceModel(RequestModelAction action, GModelState gModelState) {

		EcoreModelState modelState = EcoreModelState.getModelState(gModelState);
		modelState.setClientOptions(action.getOptions());

		Optional<String> sourceURI = ClientOptions.getValue(action.getOptions(), ClientOptions.SOURCE_URI);
		if (sourceURI.isEmpty()) {
			LOGGER.error("No source uri given to load model, return empty model.");
			modelState.setRoot(createEmptyRoot());
			return;
		}

		Optional<ModelServerClientApi<EObject>> modelServerClient = modelServerClientProvider.get();
		if (modelServerClient.isEmpty()) {
			LOGGER.error("Connection to modelserver has not been initialized, return empty model");
			modelState.setRoot(createEmptyRoot());
			return;
		}

		EcoreModelServerAccess modelServerAccess = new EcoreModelServerAccess(modelState.getModelUri(),
				modelServerClient.get(), commandCodec);
		modelState.setModelServerAccess(modelServerAccess);
		modelServerAccess
				.subscribe(new EcoreModelServerSubscriptionListener(modelState, modelServerAccess, actionDispatcher));

		EcoreEditorContext editorContext = new EcoreEditorContext(modelState, modelServerAccess);
		modelState.setEditorContext(editorContext);

		EcoreFacade ecoreFacade = editorContext.getEcoreFacade();
		if (ecoreFacade == null) {
			LOGGER.error("EcoreFacade could not be found, return empty model");
			modelState.setRoot(createEmptyRoot());
			return;
		}

		Diagram diagram = ecoreFacade.getDiagram();
		GModelRoot gmodelRoot = editorContext.getGModelFactory().create(ecoreFacade.getEPackage());
		ecoreFacade.initialize(diagram, gmodelRoot);
		modelState.setRoot(gmodelRoot);

	}

	private static GModelRoot createEmptyRoot() {
		return new GGraphBuilder(DefaultTypes.GRAPH)//
				.id(ROOT_ID) //
				.build();
	}

}
