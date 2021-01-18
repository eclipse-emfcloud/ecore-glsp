/********************************************************************************
 * Copyright (c) 2019-2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
import { createEcoreDiagramContainer } from "@eclipse-emfcloud/sprotty-ecore/lib";
import { TYPES } from "@eclipse-glsp/client/lib";
import { SelectionService } from "@theia/core";
import { Container, inject, injectable } from "inversify";
import { DiagramConfiguration, TheiaDiagramServer } from "sprotty-theia/lib";

import { EcoreLanguage } from "../../common/ecore-language";
import { TheiaGlspSelectionForwarder } from "../selection-forwarder";
import { EcoreGLSPTheiaDiagramServer } from "./ecore-glsp-theia-diagram-server";

@injectable()
export class EcoreDiagramConfiguration implements DiagramConfiguration {
    @inject(SelectionService) protected selectionService: SelectionService;
    diagramType: string = EcoreLanguage.DiagramType;

    createContainer(widgetId: string): Container {
        const container = createEcoreDiagramContainer(widgetId);
        container.bind(TYPES.ModelSource).to(EcoreGLSPTheiaDiagramServer).inSingletonScope();
        container.bind(TheiaDiagramServer).toService(EcoreGLSPTheiaDiagramServer);
        // container.rebind(KeyTool).to(TheiaKeyTool).inSingletonScope()
        container.bind(TYPES.IActionHandlerInitializer).to(TheiaGlspSelectionForwarder);
        container.bind(SelectionService).toConstantValue(this.selectionService);

        return container;
    }
}
