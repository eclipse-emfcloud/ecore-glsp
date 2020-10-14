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
import { LaunchOptions } from "@eclipse-emfcloud/modelserver-theia";
import { FILEGEN_SERVICE_PATH, FileGenServer } from "@eclipse-emfcloud/theia-ecore/lib/common/generate-protocol";
import { GLSPServerContribution } from "@eclipse-glsp/theia-integration/lib/node";
import { ConnectionHandler, JsonRpcConnectionHandler } from "@theia/core";
import { BackendApplicationContribution } from "@theia/core/lib/node";
import { ContainerModule, injectable } from "inversify";
import { join, resolve } from "path";

import { EcoreFileGenServer } from "./ecore-file-generation";
import { EcoreGLServerContribution } from "./ecore-glsp-server-contribution";
import { GLSPLaunchOptions, GLSPServerLauncher } from "./glsp-server-launcher";

@injectable()
export class EcoreGlspLaunchOptions implements GLSPLaunchOptions {
    isRunning = false;
    hostname = "localhost";
    jarPath = resolve(join(__dirname, "..", "..", "build", "org.eclipse.emfcloud.ecore.glsp-0.0.2-SNAPSHOT-glsp.jar"));
    serverPort = 5007;
}

@injectable()
export class EcoreModelServerLaunchOptions implements LaunchOptions {
    isRunning = false;
    baseURL = "api/v1/";
    serverPort = 8081;
    hostname = "localhost";
    jarPath = resolve(join(__dirname, "..", "..", "build", "org.eclipse.emfcloud.ecore.modelserver-0.0.1-SNAPSHOT-standalone.jar"));
    additionalArgs = [
        "--errorsOnly",
        `-r=${resolve(join(__dirname, "..", "..", "..", "workspace"))}`
    ];
}

export default new ContainerModule((bind, _unbind, isBound, rebind) => {
    if (isBound(LaunchOptions)) {
        rebind(LaunchOptions).to(EcoreModelServerLaunchOptions).inSingletonScope();
    } else {
        bind(LaunchOptions).to(EcoreModelServerLaunchOptions).inSingletonScope();
    }

    bind(GLSPLaunchOptions).to(EcoreGlspLaunchOptions).inSingletonScope();

    bind(GLSPServerContribution).to(EcoreGLServerContribution).inSingletonScope();

    bind(EcoreFileGenServer).toSelf().inSingletonScope();
    bind(BackendApplicationContribution).toService(EcoreFileGenServer);
    bind(ConnectionHandler).toDynamicValue(ctx =>
        new JsonRpcConnectionHandler(FILEGEN_SERVICE_PATH, () =>
            ctx.container.get<FileGenServer>(EcoreFileGenServer))
    ).inSingletonScope();

    bind(BackendApplicationContribution).to(GLSPServerLauncher);
});
