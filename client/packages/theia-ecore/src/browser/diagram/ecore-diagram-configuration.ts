/********************************************************************************
 * Copyright (c) 2019-2021 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
import { createEcoreDiagramContainer } from "@eclipse-emfcloud/sprotty-ecore/lib";
import {
    configureDiagramServer,
    GLSPDiagramConfiguration,
    GlspSelectionDataService,
    TheiaDiagramServer
} from "@eclipse-glsp/theia-integration/lib/browser";
import { Container, injectable } from "inversify";

import { EcoreLanguage } from "../../common/ecore-language";
import { EcoreGLSPTheiaDiagramServer } from "./ecore-glsp-theia-diagram-server";
import { EcoreGlspSelectionDataService } from "./selection-data-service";

@injectable()
export class EcoreDiagramConfiguration extends GLSPDiagramConfiguration {

    diagramType: string = EcoreLanguage.diagramType;

    doCreateContainer(widgetId: string): Container {
        const container = createEcoreDiagramContainer(widgetId);
        configureDiagramServer(container, EcoreGLSPTheiaDiagramServer);
        container.bind(TheiaDiagramServer).toService(EcoreGLSPTheiaDiagramServer);
        container.bind(GlspSelectionDataService).to(EcoreGlspSelectionDataService);
        return container;
    }

}
