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
import { BaseTreeEditorContribution, MasterTreeWidget, TreeEditor } from "@eclipse-emfcloud/theia-tree-editor";
import { ApplicationShell, NavigatableWidgetOptions, OpenerService, WidgetOpenerOptions } from "@theia/core/lib/browser";
import URI from "@theia/core/lib/common/uri";
import { inject, injectable } from "inversify";

import { TreeEditorWidget } from "./tree/tree-editor-widget";
import { TreeLabelProvider } from "./tree/tree-label-provider";
import { TreeModelService } from "./tree/tree-model-service";

@injectable()
export class TreeContribution extends BaseTreeEditorContribution {
    @inject(ApplicationShell) protected shell: ApplicationShell;
    @inject(OpenerService) protected opener: OpenerService;

    constructor(
        @inject(TreeModelService) modelService: TreeEditor.ModelService,
        @inject(TreeLabelProvider) labelProvider: TreeLabelProvider
    ) {
        super(TreeEditorWidget.EDITOR_ID, modelService, labelProvider);
    }

    readonly id = TreeEditorWidget.WIDGET_ID;
    readonly label = MasterTreeWidget.WIDGET_LABEL;

    canHandle(uri: URI): number {
        if (uri.path.ext === ".ecore") {
            return 1000;
        }
        return 0;
    }

    protected createWidgetOptions(
        uri: URI,
        options?: WidgetOpenerOptions
    ): NavigatableWidgetOptions {
        return {
            kind: "navigatable",
            uri: this.serializeUri(uri)
        };
    }

    protected serializeUri(uri: URI): string {
        return uri.withoutFragment().toString();
    }
}
