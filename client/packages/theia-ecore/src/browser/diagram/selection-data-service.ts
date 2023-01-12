/********************************************************************************
 * Copyright (c) 2021 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
import {
    isSetSemanticUriAction,
    RequestSemanticUriAction,
    SetSemanticUriAction
} from "@eclipse-emfcloud/sprotty-ecore/lib/action-definitions";
import { GLSPActionDispatcher, TYPES } from "@eclipse-glsp/client";
import { GlspSelectionData, GlspSelectionDataService } from "@eclipse-glsp/theia-integration/lib/browser";
import { inject, injectable } from "inversify";

export interface EcoreElementSelectionData {
    modelUri: string;
    semanticUri: string;
    eClass: string;
}

export interface EcoreGlspSelectionData extends GlspSelectionData {
    selectionDataMap: Map<string, EcoreElementSelectionData>;
}

@injectable()
export class EcoreGlspSelectionDataService extends GlspSelectionDataService {

    @inject(TYPES.IActionDispatcher) protected actionDispatcher: GLSPActionDispatcher;

    async getSelectionData(selectedElementIds: string[]): Promise<EcoreGlspSelectionData> {
        const map = new Map<string, EcoreElementSelectionData>();

        // If the graph was selected, no id is available and we send an empty string to fetch its properties
        const requestElementId = (selectedElementIds.length > 0) ? selectedElementIds[0] : "";

        return new Promise(resolve => {
            this.actionDispatcher.request(new RequestSemanticUriAction(requestElementId)).then(response => {
                if (isSetSemanticUriAction(response)) {
                    map.set(
                        requestElementId,
                        {
                            modelUri: (response as SetSemanticUriAction).modelUri,
                            semanticUri: (response as SetSemanticUriAction).semanticUri,
                            eClass: (response as SetSemanticUriAction).elementEClass
                        } as EcoreElementSelectionData
                    );
                    resolve({ selectionDataMap: map });
                }
            });
        });
    }

}
