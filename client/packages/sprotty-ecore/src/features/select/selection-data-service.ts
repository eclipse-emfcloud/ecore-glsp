/********************************************************************************
 * Copyright (c) 2021 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ********************************************************************************/
import { GLSPActionDispatcher, TYPES } from "@eclipse-glsp/client";
import { inject, injectable } from "inversify";

import { isSetSemanticUriAction, RequestSemanticUriAction, SetSemanticUriAction } from "../../action-definitions";

export interface SelectionData {
    selectionDataMap: Map<string, any>;
}

@injectable()
export abstract class GlspSelectionDataService {
    abstract getSelectionData(selectedElementIds: string[]): Promise<SelectionData>;
}

export interface EcoreElementSelectionData {
    modelUri: string;
    semanticUri: string;
    eClass: string;
}

export interface EcoreGlspSelectionData extends SelectionData {
    selectionDataMap: Map<string, EcoreElementSelectionData>;
}

@injectable()
export class EcoreGlspSelectionDataService extends GlspSelectionDataService {

    @inject(TYPES.IActionDispatcher) protected actionDispatcher: GLSPActionDispatcher;

    async getSelectionData(selectedElementIds: string[]): Promise<EcoreGlspSelectionData> {
        const map = new Map<string, EcoreElementSelectionData>();

        if (selectedElementIds.length === 0) {
            selectedElementIds[0] = "";
        }

        return new Promise(resolve => {
            // selectedElementIds.forEach(id => {
            this.actionDispatcher.request(new RequestSemanticUriAction(selectedElementIds[0])).then(response => {
                if (isSetSemanticUriAction(response)) {
                    map.set(
                        selectedElementIds[0],
                        {
                            modelUri: (response as SetSemanticUriAction).modelUri,
                            semanticUri: (response as SetSemanticUriAction).semanticUri,
                            eClass: (response as SetSemanticUriAction).elementEClass
                        } as EcoreElementSelectionData
                    );
                    resolve({ selectionDataMap: map });
                }
            });
            // });
        });
    }

}
