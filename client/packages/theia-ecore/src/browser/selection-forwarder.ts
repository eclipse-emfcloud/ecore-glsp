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
import {
    GlspSelectionDataService,
    SelectionData
} from "@eclipse-emfcloud/sprotty-ecore/lib/features/select/selection-data-service";
import { EditorContextService } from "@eclipse-glsp/client";
import { inject, injectable } from "inversify";
import {
    Action,
    ActionDispatcher,
    ActionHandlerRegistry,
    GetSelectionAction,
    SelectAction,
    SelectAllAction,
    TYPES
} from "sprotty";
import { isSprottySelection, SprottySelection, TheiaSprottySelectionForwarder } from "sprotty-theia";

export interface GlspSelection extends SprottySelection {
    additionalSelectionData?: SelectionData;
}

export function isGlspSelection(object?: any): object is GlspSelection {
    return isSprottySelection(object);
}

@injectable()
export class TheiaGlspSelectionForwarder extends TheiaSprottySelectionForwarder {

    @inject(TYPES.IActionDispatcher) protected actionDispatcher: ActionDispatcher; // editor context service ersetzen
    @inject(EditorContextService) protected editorContextService: EditorContextService;
    @inject(GlspSelectionDataService) protected readonly selectionDataService: GlspSelectionDataService;

    initialize(registry: ActionHandlerRegistry): any {
        super.initialize(registry);
        registry.register(SelectAllAction.KIND, this);
        registry.register(GetSelectionAction.KIND, this);
    }

    handle(action: Action): void {
        if (action instanceof SelectAction) { // select all editor context fragen, root get all element fitler by selected
            this.selectionDataService.getSelectionData(action.selectedElementsIDs).then((additionalSelectionData: any) =>
                this.selectionService.selection = { // map mit uri für elementid, erweitern der sprotty selection
                    selectedElementsIDs: action.selectedElementsIDs,
                    additionalSelectionData: additionalSelectionData,
                    widgetId: this.viewerOptions.baseDiv,
                    sourceUri: this.sourceUri
                } as GlspSelection
            );
        } else if (action instanceof SelectAllAction) { // editorcontext is updated after the updateSelection() of the glsp selection service....
            console.log(action.kind + "" + JSON.stringify(this.editorContextService.get().selectedElementIds));
        } else {
            super.handle(action);
        }
    }
}
