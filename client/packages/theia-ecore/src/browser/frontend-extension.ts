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
import { GLSPClientContribution } from "@eclipse-glsp/theia-integration/lib/browser";
import { CommandContribution } from "@theia/core";
import {
    FrontendApplicationContribution,
    OpenHandler,
    WebSocketConnectionProvider,
    WidgetFactory
} from "@theia/core/lib/browser";
import { ContainerModule, interfaces } from "inversify";
import { DiagramConfiguration, DiagramManager, DiagramManagerProvider } from "sprotty-theia/lib";

import { FILEGEN_SERVICE_PATH, FileGenServer } from "../common/generate-protocol";
import { EcoreCommandContribution } from "./command-contribution";
import { EcoreDiagramConfiguration } from "./diagram/ecore-diagram-configuration";
import { EcoreDiagramManager } from "./diagram/ecore-diagram-manager";
import { EcoreGLSPDiagramClient } from "./diagram/ecore-glsp-diagram-client";
import { EcoreGLSPClientContribution } from "./glsp-client-contribution";

export default new ContainerModule((bind: interfaces.Bind, unbind: interfaces.Unbind, isBound: interfaces.IsBound, rebind: interfaces.Rebind) => {
    bind(EcoreGLSPClientContribution).toSelf().inSingletonScope();
    bind(GLSPClientContribution).toService(EcoreGLSPClientContribution);
    bind(EcoreGLSPDiagramClient).toSelf().inSingletonScope();
    bind(DiagramConfiguration).to(EcoreDiagramConfiguration).inSingletonScope();
    bind(EcoreDiagramManager).toSelf().inSingletonScope();
    bind(FrontendApplicationContribution).toService(EcoreDiagramManager);
    bind(OpenHandler).toService(EcoreDiagramManager);
    bind(WidgetFactory).toService(EcoreDiagramManager);
    bind(DiagramManagerProvider).toProvider<DiagramManager>(context => () => new Promise<DiagramManager>(resolve => {
        const diagramManager = context.container.get<EcoreDiagramManager>(EcoreDiagramManager);
        resolve(diagramManager);
    }));
    bind(CommandContribution).to(EcoreCommandContribution);
    bind(FileGenServer).toDynamicValue(ctx => {
        const connection = ctx.container.get(WebSocketConnectionProvider);
        return connection.createProxy<FileGenServer>(FILEGEN_SERVICE_PATH);
    }).inSingletonScope();
});
