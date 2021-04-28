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
import { getPort } from "@eclipse-glsp/protocol";
import { JavaSocketServerContribution, JavaSocketServerLaunchOptions } from "@eclipse-glsp/theia-integration/lib/node";
import { injectable } from "inversify";
import * as net from "net";
import { createSocketConnection, IConnection } from "vscode-ws-jsonrpc/lib/server";

import { EcoreLanguage } from "../common/ecore-language";

@injectable()
export class EcoreGLSPServerContribution extends JavaSocketServerContribution {

    readonly id = EcoreLanguage.Id;
    readonly name = EcoreLanguage.Name;
    serverStarted = false;
    readonly description = {
        id: "ecore",
        name: "Ecore",
        documentSelector: ["ecore"],
        fileEvents: [
            "**/*.ecorediagram"
        ]
    };

    createLaunchOptions(): Partial<JavaSocketServerLaunchOptions> {
        return {
            launchedExternally: true,
            serverPort: getPort("ECORE_GLSP")
        };
    }

    connect(clientConnection: IConnection): void {
        const socketPort = this.launchOptions.serverPort;
        if (socketPort) {
            const socket = new net.Socket();
            const serverConnection = createSocketConnection(socket, socket, () => {
                socket.destroy();
            });
            this.forward(clientConnection, serverConnection);
            socket.connect(socketPort);
        } else {
            console.error("Error when trying to connect to Ecore GLSP server");
        }
    }
}
