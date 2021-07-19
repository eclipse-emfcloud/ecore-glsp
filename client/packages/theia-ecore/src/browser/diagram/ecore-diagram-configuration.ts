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
import { GlspSelectionDataService } from "@eclipse-glsp/theia-integration/lib/browser";
import {
    GLSPTheiaDiagramConfiguration
} from "@eclipse-glsp/theia-integration/lib/browser/diagram/glsp-theia-diagram-configuration";
import { Container, injectable } from "inversify";

import { EcoreLanguage } from "../../common/ecore-language";
import { EcoreGLSPTheiaDiagramServer } from "./ecore-glsp-theia-diagram-server";
import { EcoreGlspSelectionDataService } from "./selection-data-service";

@injectable()
export class EcoreDiagramConfiguration extends GLSPTheiaDiagramConfiguration {
    diagramType: string = EcoreLanguage.DiagramType;

    doCreateContainer(widgetId: string): Container {
        const container = createEcoreDiagramContainer(widgetId);
        this.configureDiagramServer(container, EcoreGLSPTheiaDiagramServer);
        container.bind(GlspSelectionDataService).to(EcoreGlspSelectionDataService);
        return container;
    }

}
