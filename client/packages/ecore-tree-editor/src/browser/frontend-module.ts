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
import "../../src/browser/style/ecore-tree-editor.css";
import "@eclipse-emfcloud/theia-tree-editor/style/forms.css";
import "@eclipse-emfcloud/theia-tree-editor/style/index.css";

import { createBasicTreeContainer, NavigatableTreeEditorOptions } from "@eclipse-emfcloud/theia-tree-editor";
import { CommandContribution, MenuContribution } from "@theia/core";
import { LabelProviderContribution, NavigatableWidgetOptions, OpenHandler, WidgetFactory } from "@theia/core/lib/browser";
import URI from "@theia/core/lib/common/uri";
import { ContainerModule } from "inversify";

import { TreeContribution } from "./editor-contribution";
import { TreeEditorWidget } from "./tree/tree-editor-widget";
import { TreeLabelProvider } from "./tree/tree-label-provider";
import { TreeModelService } from "./tree/tree-model-service";
import { TreeNodeFactory } from "./tree/tree-node-factory";

export default new ContainerModule(bind => {
    // Bind Theia IDE contributions for the tree editor
    bind(OpenHandler).to(TreeContribution);
    bind(MenuContribution).to(TreeContribution);
    bind(CommandContribution).to(TreeContribution);
    bind(LabelProviderContribution).to(TreeLabelProvider);
    // bind services to themselves because we use them outside of the editor widget, too.
    bind(TreeModelService).toSelf().inSingletonScope();
    bind(TreeLabelProvider).toSelf().inSingletonScope();
    bind<WidgetFactory>(WidgetFactory).toDynamicValue(context => ({
        id: TreeEditorWidget.WIDGET_ID,
        createWidget: (options: NavigatableWidgetOptions) => {
            const treeContainer = createBasicTreeContainer(
                context.container,
                TreeEditorWidget,
                TreeModelService,
                TreeNodeFactory
            );
            // Bind options
            const uri = new URI(options.uri);
            treeContainer.bind(NavigatableTreeEditorOptions).toConstantValue({ uri });
            return treeContainer.get(TreeEditorWidget);
        }
    }));
});
