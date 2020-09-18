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
import { JsonRpcServer } from "@theia/core/lib/common/messaging";

export const FileGenServer = Symbol("FileGenServer");
export const FILEGEN_SERVICE_PATH = "/services/codegen";

export interface FileGenServer extends JsonRpcServer<undefined> {
    generateEcore(name: string, prefix: string, uri: string, path: string): Promise<string>;
    generateCode(genmodelPath: string, workspacePath: string): Promise<string>;
    generateGenModel(workspacePath: string, ecorePath: string, customPackageName: string, folderName: string): Promise<string>;
}
