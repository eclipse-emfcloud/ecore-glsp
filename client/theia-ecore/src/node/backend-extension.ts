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
import { ConnectionHandler, JsonRpcConnectionHandler } from "@theia/core";
import { BackendApplicationContribution } from "@theia/core/lib/node";
import { LanguageServerContribution } from "@theia/languages/lib/node";
import { ContainerModule, injectable } from "inversify";

import { FILEGEN_SERVICE_PATH, FileGenServer } from "../common/generate-protocol";
import { EcoreFileGenServer } from "./ecore-file-generation";
import { EcoreGLServerContribution } from "./ecore-glsp-server-contribution";
import { GLSPLaunchOptions, GLSPLaunchOptionsSymb, GLSPServerLauncher } from "./glsp-server-launcher";

@injectable()
export class EcoreGlspLaunchOptions implements GLSPLaunchOptions {
    isRunning = true;
    hostname = "localhost";
    // jarPath = resolve(join(__dirname, "..", "..", "build", "org.eclipse.emfcloud.ecore.glsp-0.0.2-SNAPSHOT-glsp.jar"));
    serverPort = 5007;
}

export default new ContainerModule(bind => {
    bind(GLSPLaunchOptionsSymb).to(EcoreGlspLaunchOptions).inSingletonScope();
    bind(LanguageServerContribution).to(EcoreGLServerContribution).inSingletonScope();
    bind(EcoreFileGenServer).toSelf().inSingletonScope();
    bind(BackendApplicationContribution).toService(EcoreFileGenServer);
    bind(ConnectionHandler).toDynamicValue(ctx =>
        new JsonRpcConnectionHandler(FILEGEN_SERVICE_PATH, () =>
            ctx.container.get<FileGenServer>(EcoreFileGenServer))
    ).inSingletonScope();
    bind(BackendApplicationContribution).to(GLSPServerLauncher);
});
