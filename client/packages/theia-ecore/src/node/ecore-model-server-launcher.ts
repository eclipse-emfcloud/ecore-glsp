/********************************************************************************
 * Copyright (c) 2021 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
import { DefaultModelServerLauncher } from "@eclipse-emfcloud/modelserver-theia/lib/node";
import { injectable } from "inversify";

import { findEquinoxLauncher } from "./equinox";

@injectable()
export class EcoreModelServerLauncher extends DefaultModelServerLauncher {

    startServer(): boolean {
        if (this.launchOptions.jarPath) {
            let args = ["-jar", findEquinoxLauncher(this.launchOptions.jarPath), "--port", `${this.launchOptions.serverPort}`];
            if (this.launchOptions.additionalArgs) {
                args = [...args, ...this.launchOptions.additionalArgs];
            }
            this.spawnProcessAsync("java", args);
        } else {
            this.logError("Could not start model server. No path to executable is specified");
        }
        return true;
    }

}
