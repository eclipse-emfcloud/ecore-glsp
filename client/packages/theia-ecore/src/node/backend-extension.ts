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
import { LaunchOptions, ModelServerClient } from "@eclipse-emfcloud/modelserver-theia";
import { DefaultModelServerLauncher } from "@eclipse-emfcloud/modelserver-theia/lib/node";
import { GLSPServerContribution } from "@eclipse-glsp/theia-integration/lib/node";
import { ConnectionHandler, JsonRpcConnectionHandler } from "@theia/core";
import { BackendApplicationContribution } from "@theia/core/lib/node";
import { ContainerModule, injectable } from "inversify";
import { join, resolve } from "path";

import { FILEGEN_SERVICE_PATH, FileGenServer } from "../common/generate-protocol";
import { EcoreFileGenServer } from "./ecore-file-generation";
import { EcoreGLSPServerContribution } from "./ecore-glsp-server-contribution";
import { EcoreModelServerLauncher } from "./ecore-model-server-launcher";
import { GLSPLaunchOptions, GLSPServerLauncher } from "./glsp-server-launcher";
import { EcoreModelServerClientImpl } from "./model-server-client";

@injectable()
export class EcoreGlspLaunchOptions implements GLSPLaunchOptions {
    hostname = "localhost";
    jarPath = join(__dirname, "..", "..", "build", "org.eclipse.emfcloud.ecore.glsp.product-1.0.0");
    serverPort = 5007;
}

@injectable()
export class EcoreModelServerLaunchOptions implements LaunchOptions {
    baseURL = "api/v1/";
    serverPort = 8081;
    hostname = "localhost";
    jarPath = join(__dirname, "..", "..", "build", "org.eclipse.emfcloud.ecore.modelserver.product-1.0.0");
    additionalArgs = [
        "--errorsOnly",
        `-r=${resolve(join(__dirname, "..", "..", "..", "..", "workspace"))}`
    ];
}

export default new ContainerModule((bind, unbind, isBound, rebind) => {
    if (isBound(DefaultModelServerLauncher)) {
        bind(EcoreModelServerLauncher).toSelf().inSingletonScope();
        rebind(DefaultModelServerLauncher).toService(EcoreModelServerLauncher);
    }

    if (isBound(LaunchOptions)) {
        rebind(LaunchOptions).to(EcoreModelServerLaunchOptions).inSingletonScope();
    } else {
        bind(LaunchOptions).to(EcoreModelServerLaunchOptions).inSingletonScope();
    }

    bind(GLSPLaunchOptions).to(EcoreGlspLaunchOptions).inSingletonScope();
    bind(GLSPServerContribution).to(EcoreGLSPServerContribution).inSingletonScope();
    bind(EcoreFileGenServer).toSelf().inSingletonScope();
    bind(BackendApplicationContribution).toService(EcoreFileGenServer);
    bind(ConnectionHandler).toDynamicValue(ctx =>
        new JsonRpcConnectionHandler(FILEGEN_SERVICE_PATH, () =>
            ctx.container.get<FileGenServer>(EcoreFileGenServer))
    ).inSingletonScope();
    bind(BackendApplicationContribution).to(GLSPServerLauncher);

    rebind(ModelServerClient).to(EcoreModelServerClientImpl).inSingletonScope();
});
