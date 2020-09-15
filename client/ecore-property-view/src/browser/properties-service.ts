/********************************************************************************
 * Copyright (c) 2020 EclipseSource and others.
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
import {
    isSetEcorePropertiesAction,
    RequestEcorePropertiesAction
} from "@eclipse-emfcloud/theia-ecore/lib/browser/action-definitions";
import { EcoreDiagramWidget } from "@eclipse-emfcloud/theia-ecore/lib/browser/diagram/ecore-diagram-manager";
import { GLSPActionDispatcher } from "@eclipse-glsp/client";
import { JsonSchema, UISchemaElement } from "@jsonforms/core";
import { JsonFormsPropertiesService } from "@ndoschek/jsonforms-property-view";
import { ApplicationShell } from "@theia/core/lib/browser";
import { inject, injectable } from "inversify";
import { isSprottySelection } from "sprotty-theia";

@injectable()
export class EcoreGlspPropertiesService implements JsonFormsPropertiesService {

    @inject(ApplicationShell) protected shell: ApplicationShell;

    canHandleSelection(selection: any): number {
        if (isSprottySelection(selection)) {
            return 15;
        }
        return 0;
    }

    async getProperties(selection: any): Promise<any> {
        if (this.actionDispatcher && selection.selectedElementsIDs) {
            const requestId = selection.selectedElementsIDs.length < 1
                ? ""
                : selection.selectedElementsIDs[0];
            const response = await this.actionDispatcher.requestUntil(new RequestEcorePropertiesAction(requestId));
            if (isSetEcorePropertiesAction(response)) {
                return response.ecoreProperties;
            }
        }
        return undefined;
    }

    protected get actionDispatcher(): GLSPActionDispatcher | undefined {
        const ecoreDiagramWidget = (this.shell.activeWidget || this.shell.currentWidget);
        if (ecoreDiagramWidget instanceof EcoreDiagramWidget) {
            return ecoreDiagramWidget.actionDispatcher as GLSPActionDispatcher;
        }
        return undefined;
    }

    async getSchema(selection: any): Promise<JsonSchema | undefined> {
        // let JsonFormsWidget generate JsonSchema
        return undefined;
    }

    async getUiSchema(selection: any): Promise<UISchemaElement | undefined> {
        // let JsonFormsWidget generate UISchema
        return undefined;
    }

}
