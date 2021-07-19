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
import { GLSPDiagramClient } from "@eclipse-glsp/theia-integration/lib/browser";
import { EditorManager } from "@theia/editor/lib/browser";
import { inject, injectable } from "inversify";

import { EcoreGLSPClientContribution } from "../glsp-client-contribution";

@injectable()
export class EcoreGLSPDiagramClient extends GLSPDiagramClient {
    constructor(
        @inject(EcoreGLSPClientContribution) ecoreClientContribution: EcoreGLSPClientContribution,
        @inject(EditorManager) editorManager: EditorManager) {
        super(ecoreClientContribution, editorManager);
    }
}
