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
    GLSPWidgetOpenerOptions,
    GLSPWidgetOptions
} from "@eclipse-glsp/theia-integration/lib/browser";
import URI from "@theia/core/lib/common/uri";
import { WorkspaceService } from "@theia/workspace/lib/browser";
import { inject, injectable, postConstruct } from "inversify";

import { EcoreLanguage } from "../../common/ecore-language";

export interface EcoreDiagramWidgetOptions extends DiagramWidgetOptions, GLSPWidgetOptions {
    workspaceRoot: string;
}

@injectable()
export class EcoreDiagramManager extends GLSPDiagramManager {

    @inject(WorkspaceService) workspaceService: WorkspaceService;

    readonly diagramType = EcoreLanguage.diagramType;
    readonly label = EcoreLanguage.label + " Editor";

    private workspaceRoot: string;

    @postConstruct()
    protected async initialize(): Promise<void> {
        super.initialize();
        this.workspaceService.roots.then(roots => this.workspaceRoot = roots[0].resource.toString());
    }

    get fileExtensions(): string[] {
        return EcoreLanguage.fileExtensions;
    }

    protected createWidgetOptions(uri: URI, options?: GLSPWidgetOpenerOptions): EcoreDiagramWidgetOptions {
        return {
            ...super.createWidgetOptions(uri, options),
            workspaceRoot: this.workspaceRoot
        } as EcoreDiagramWidgetOptions;
    }

    get iconClass(): string {
        return EcoreLanguage.iconClass!;
    }
}
