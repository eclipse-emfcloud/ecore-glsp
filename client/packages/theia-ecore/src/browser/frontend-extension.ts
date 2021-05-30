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
import { ModelServerClient } from "@eclipse-emfcloud/modelserver-theia";
import { GLSPClientContribution, registerDiagramManager } from "@eclipse-glsp/theia-integration/lib/browser";
import { CommandContribution, MenuContribution } from "@theia/core";
import { WebSocketConnectionProvider } from "@theia/core/lib/browser";
import { ContainerModule } from "inversify";
import { DiagramConfiguration } from "sprotty-theia/lib";

import { EcoreModelServerClient } from "../common/ecore-model-server-client";
import { FILEGEN_SERVICE_PATH, FileGenServer } from "../common/generate-protocol";
import { EcoreCommandContribution } from "./command-contribution";
import { EcoreDiagramConfiguration } from "./diagram/ecore-diagram-configuration";
import { EcoreDiagramManager } from "./diagram/ecore-diagram-manager";
import { EcoreGLSPDiagramClient } from "./diagram/ecore-glsp-diagram-client";
import { EcoreGLSPClientContribution } from "./glsp-client-contribution";

export default new ContainerModule(bind => {
    bind(EcoreGLSPClientContribution).toSelf().inSingletonScope();
    bind(GLSPClientContribution).toService(EcoreGLSPClientContribution);
    bind(EcoreGLSPDiagramClient).toSelf().inSingletonScope();
    bind(DiagramConfiguration).to(EcoreDiagramConfiguration).inSingletonScope();
    registerDiagramManager(bind, EcoreDiagramManager);

    bind(CommandContribution).to(EcoreCommandContribution);
    bind(MenuContribution).to(EcoreCommandContribution);

    bind(FileGenServer).toDynamicValue(ctx => {
        const connection = ctx.container.get(WebSocketConnectionProvider);
        return connection.createProxy<FileGenServer>(FILEGEN_SERVICE_PATH);
    }).inSingletonScope();

    bind(EcoreModelServerClient).toService(ModelServerClient);
});
