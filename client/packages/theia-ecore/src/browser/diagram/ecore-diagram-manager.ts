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
import {
    DiagramWidgetOptions,
    GLSPDiagramManager,
    GLSPNotificationManager,
    GLSPTheiaSprottyConnector,
    GLSPWidgetOpenerOptions,
    GLSPWidgetOptions,
    TheiaFileSaver
} from "@eclipse-glsp/theia-integration/lib/browser";
import { MessageService } from "@theia/core";
import { WidgetManager } from "@theia/core/lib/browser";
import URI from "@theia/core/lib/common/uri";
import { EditorManager } from "@theia/editor/lib/browser";
import { WorkspaceService } from "@theia/workspace/lib/browser";
import { inject, injectable } from "inversify";

import { EcoreLanguage } from "../../common/ecore-language";
import { EcoreGLSPDiagramClient } from "./ecore-glsp-diagram-client";

export const ECORE_DIAGRAM_ICON_CLASS = "fa fa-project-diagram";

export interface EcoreDiagramWidgetOptions extends DiagramWidgetOptions, GLSPWidgetOptions {
    workspaceRoot: string;
}

@injectable()
export class EcoreDiagramManager extends GLSPDiagramManager {
    readonly diagramType = EcoreLanguage.DiagramType;
    readonly iconClass = ECORE_DIAGRAM_ICON_CLASS;
    readonly label = EcoreLanguage.Label + " Editor";

    private _diagramConnector: GLSPTheiaSprottyConnector;
    private workspaceRoot: string;

    constructor(
        @inject(EcoreGLSPDiagramClient) diagramClient: EcoreGLSPDiagramClient,
        @inject(TheiaFileSaver) fileSaver: TheiaFileSaver,
        @inject(WidgetManager) widgetManager: WidgetManager,
        @inject(EditorManager) editorManager: EditorManager,
        @inject(MessageService) messageService: MessageService,
        @inject(GLSPNotificationManager) notificationManager: GLSPNotificationManager,
        @inject(WorkspaceService) workspaceService: WorkspaceService) {
        super();
        this._diagramConnector = new GLSPTheiaSprottyConnector({
            diagramClient,
            fileSaver, editorManager, widgetManager, diagramManager: this, messageService, notificationManager
        });
        workspaceService.roots.then(roots => this.workspaceRoot = roots[0].resource.toString());
    }

    get fileExtensions(): string[] {
        return [EcoreLanguage.FileExtension];
    }

    get diagramConnector(): GLSPTheiaSprottyConnector {
        return this._diagramConnector;
    }

    protected createWidgetOptions(uri: URI, options?: GLSPWidgetOpenerOptions): EcoreDiagramWidgetOptions {
        return {
            ...super.createWidgetOptions(uri, options),
            workspaceRoot: this.workspaceRoot
        } as EcoreDiagramWidgetOptions;
    }
}
