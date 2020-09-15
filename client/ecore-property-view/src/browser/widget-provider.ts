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
import { EditEcorePropertiesOperation } from "@eclipse-emfcloud/theia-ecore/lib/browser/action-definitions";
import { EcoreDiagramWidget } from "@eclipse-emfcloud/theia-ecore/lib/browser/diagram/ecore-diagram-manager";
import { EcoreProperties } from "@eclipse-emfcloud/theia-ecore/lib/browser/ecore-api";
import { GLSPActionDispatcher } from "@eclipse-glsp/client";
import { JsonFormsPropertyViewWidgetProvider } from "@ndoschek/jsonforms-property-view";
import { ApplicationShell } from "@theia/core/lib/browser";
import { inject, injectable, postConstruct } from "inversify";
import { isSprottySelection } from "sprotty-theia";

@injectable()
export class EcoreGlspPropertyViewWidgetProvider extends JsonFormsPropertyViewWidgetProvider {

    @inject(ApplicationShell) protected shell: ApplicationShell;

    private _actionDispatcher: GLSPActionDispatcher | undefined;

    @postConstruct()
    init(): void {
        this._actionDispatcher = this.getActionDispatcher();
        this.jsonFormsWidget.onChange((data: EcoreProperties) => {
            if (!this._actionDispatcher) {
                if (this.getActionDispatcher()) {
                    this._actionDispatcher = this.getActionDispatcher();
                }
            }
            console.log("change name " + data.name);
            this.handleChanges(data);
        });
    }

    canHandle(selection: any): number {
        if (isSprottySelection(selection)) {
            return 15;
        }
        return super.canHandle(selection);
    }

    handleChanges(data: EcoreProperties): void {
        if (this._actionDispatcher) {
            this._actionDispatcher.dispatch(new EditEcorePropertiesOperation(data));
        }
    }

    protected getActionDispatcher(): GLSPActionDispatcher | undefined {
        const ecoreDiagramWidget = (this.shell.activeWidget || this.shell.currentWidget);
        if (ecoreDiagramWidget instanceof EcoreDiagramWidget) {
            return ecoreDiagramWidget.actionDispatcher as GLSPActionDispatcher;
        }
        return undefined;
    }

}
