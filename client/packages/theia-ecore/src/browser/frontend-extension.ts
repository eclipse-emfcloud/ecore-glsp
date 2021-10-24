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
import {
    ContainerContext,
    GLSPClientContribution,
    GLSPTheiaFrontendModule,
    registerDiagramManager,
    TheiaGLSPConnector
} from "@eclipse-glsp/theia-integration/lib/browser";
import { CommandContribution, MenuContribution } from "@theia/core";
import { WebSocketConnectionProvider } from "@theia/core/lib/browser";
import { WorkspaceDeleteHandler } from "@theia/workspace/lib/browser/workspace-delete-handler";
import { DiagramConfiguration } from "sprotty-theia/lib";

import { EcoreLanguage } from "../common/ecore-language";
import { EcoreModelServerClient } from "../common/ecore-model-server-client";
import { FILEGEN_SERVICE_PATH, FileGenServer } from "../common/generate-protocol";
import { EcoreCommandContribution } from "./command-contribution";
import { EcoreDiagramConfiguration } from "./diagram/ecore-diagram-configuration";
import { EcoreDiagramManager } from "./diagram/ecore-diagram-manager";
import { EcoreTheiaGLSPConnector } from "./diagram/ecore-theia-glsp-connector";
import { EcoreGLSPClientContribution } from "./glsp-client-contribution";
import { EcoreWorkspaceDeleteHandler } from "./workspace-delete-handler";

export class EcoreTheiaFrontendModule extends GLSPTheiaFrontendModule {

    readonly diagramLanguage = EcoreLanguage;

    bindTheiaGLSPConnector(context: ContainerContext): void {
        context.bind(TheiaGLSPConnector).toDynamicValue(dynamicContext => {
            const connector = dynamicContext.container.resolve(EcoreTheiaGLSPConnector);
            connector.doConfigure(this.diagramLanguage);
            return connector;
        });
    }

    bindDiagramConfiguration(context: ContainerContext): void {
        context.bind(DiagramConfiguration).to(EcoreDiagramConfiguration);
    }

    bindGLSPClientContribution(context: ContainerContext): void {
        context.bind(GLSPClientContribution).to(EcoreGLSPClientContribution);
    }

    configure(context: ContainerContext): void {
        context.bind(CommandContribution).to(EcoreCommandContribution);
        context.bind(MenuContribution).to(EcoreCommandContribution);
        context.bind(EcoreWorkspaceDeleteHandler).toSelf().inSingletonScope();
        context.rebind(WorkspaceDeleteHandler).toService(EcoreWorkspaceDeleteHandler);

        context.bind(FileGenServer).toDynamicValue(ctx => {
            const connection = ctx.container.get(WebSocketConnectionProvider);
            return connection.createProxy<FileGenServer>(FILEGEN_SERVICE_PATH);
        }).inSingletonScope();

        context.bind(EcoreModelServerClient).toService(ModelServerClient);
    }

    configureDiagramManager(context: ContainerContext): void {
        registerDiagramManager(context.bind, EcoreDiagramManager);
    }

}

export default new EcoreTheiaFrontendModule();

