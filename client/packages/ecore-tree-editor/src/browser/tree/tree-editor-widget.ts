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
import {
    ModelServerClient,
    ModelServerCommand,
    ModelServerMessage,
    ModelServerReferenceDescription,
    ModelServerSubscriptionService,
    SetCommand
} from "@eclipse-emfcloud/modelserver-theia/lib/common";
import {
    AddCommandProperty,
    DetailFormWidget,
    MasterTreeWidget,
    NavigatableTreeEditorOptions,
    NavigatableTreeEditorWidget,
    TreeEditor
} from "@eclipse-emfcloud/theia-tree-editor";
import { Title, Widget } from "@theia/core/lib/browser";
import { ILogger } from "@theia/core/lib/common";
import { EditorPreferences } from "@theia/editor/lib/browser";
import { WorkspaceService } from "@theia/workspace/lib/browser/workspace-service";
import { inject, injectable } from "inversify";
import { isEqual, isObject, transform } from "lodash";

import { EcoreModel } from "./tree-model";

@injectable()
export class TreeEditorWidget extends NavigatableTreeEditorWidget {

    private delayedRefresh = false;
    protected instanceData: any;

    constructor(
        @inject(MasterTreeWidget) readonly treeWidget: MasterTreeWidget,
        @inject(DetailFormWidget) readonly formWidget: DetailFormWidget,
        @inject(WorkspaceService) readonly workspaceService: WorkspaceService,
        @inject(ILogger) readonly logger: ILogger,
        @inject(NavigatableTreeEditorOptions) protected readonly options: NavigatableTreeEditorOptions,
        @inject(ModelServerClient) protected modelServerClient: ModelServerClient,
        @inject(TreeEditor.NodeFactory) protected readonly nodeFactory: TreeEditor.NodeFactory,
        @inject(EditorPreferences) protected readonly editorPreferences: EditorPreferences,
        @inject(ModelServerSubscriptionService) private readonly subscriptionService: ModelServerSubscriptionService
    ) {
        super(
            treeWidget,
            formWidget,
            workspaceService,
            logger,
            TreeEditorWidget.WIDGET_ID,
            options
        );

        this.subscriptionService.onDirtyStateListener((message: ModelServerMessage) => {
            this.dirty = message.data as boolean;
            this.onDirtyChangedEmitter.fire();
        });

        this.subscriptionService.onIncrementalUpdateListener((message: ModelServerMessage) => {
            const command = message.data; // ModelServerCommand || ModelServerCompoundCommand
            // #FIXME: remove this if condition, as soon as command type for compound command is correctly set
            this.applyCommand(command);
        });

        this.subscriptionService.onFullUpdateListener(fullUpdate => {
            this.instanceData = fullUpdate;

            this.treeWidget
                .setData({ error: false, data: this.instanceData })
                .then(() => this.treeWidget.select(this.getOldSelectedPath()));

            if (!this.isVisible) {
                this.delayedRefresh = true;
            }
        });

        this.loadModel();

        this.modelServerClient.subscribe(this.modelIDToRequest);
        // see https://developer.mozilla.org/en-US/docs/Web/API/WindowEventHandlers/onbeforeunload
        window.onbeforeunload = () => this.dispose();
    }

    protected loadModel(oldSelectedPath: string[] = []): void {
        this.modelServerClient.get(this.modelIDToRequest).then(response => {
            if (response.statusCode === 200) {
                if (isEqual(this.instanceData, response.body)) {
                    return;
                }
                this.instanceData = response.body;
                this.treeWidget
                    .setData({ error: false, data: this.instanceData })
                    .then(() => {
                        if (oldSelectedPath) {
                            this.treeWidget.select(oldSelectedPath);
                        } else {
                            this.treeWidget.selectFirst();
                        }
                    });
                return;
            }
            this.treeWidget.setData({ error: !!response.statusMessage });
            this.renderError(
                "An error occurred when requesting '" +
                this.modelIDToRequest +
                "' - Status " +
                response.statusCode +
                " " +
                response.statusMessage
            );
            this.instanceData = undefined;
            return;
        });
    }

    protected getOldSelectedPath(): string[] {
        const paths: string[] = [];
        if (!this.selectedNode) {
            return paths;
        }
        paths.push(this.selectedNode.name);
        let parent = this.selectedNode.parent;
        while (parent) {
            /* @ts-ignore */
            paths.push(parent.name);
            parent = parent.parent;
        }
        paths.splice(paths.length - 1, 1);
        return paths;
    }

    protected applyCommand(command: ModelServerCommand): void {
        switch (command.type) {
            case "add":     // this.addNodeViaCommand(command);
            case "remove":  // this.removeNodeViaCommand(command);
            case "set":     // this.setNodeDataViaCommand(command);
            default:
                // #FIXME this is just workaround atm
                this.loadModel(this.getOldSelectedPath());
                break;
        }
    }

    protected get modelIDToRequest(): string {
        const workspaceUri = this.workspaceService.getWorkspaceRootUri(this.options.uri);
        if (workspaceUri) {
            const rootUriLength = workspaceUri.toString().length;
            return this.options.uri.toString().substring(rootUriLength + 1);
        }
        return "";
    }

    protected configureTitle(title: Title<Widget>): void {
        super.configureTitle(title);
        title.iconClass = "ecoremodelfileTabIcon";
    }

    show(): void {
        super.show();
        if (this.delayedRefresh) {
            this.delayedRefresh = false;
            this.treeWidget.model.refresh();
        }
    }

    dispose(): void {
        this.modelServerClient.unsubscribe(this.modelIDToRequest);
        super.dispose();
    }

    protected getOwnerRef(semanticUri: string): string {
        return `${this.workspaceService.workspace!.resource.toString()}/${this.modelIDToRequest}#${semanticUri}`.replace("file:///", "file:/");
    }

    protected getOwner(node: Readonly<TreeEditor.Node>): ModelServerReferenceDescription {
        const parentNode = node.parent;
        return {
            /* @ts-ignore */
            $ref: this.getOwnerRef(parentNode.jsonforms.data.eClass === EcoreModel.Type.EPackage ? "/" : "//" + parentNode.name),
            /* @ts-ignore */
            eClass: parentNode.jsonforms.data.eClass
        };
    }

    protected async deleteNode(node: Readonly<TreeEditor.Node>): Promise<void> {
        // TODO
        // const removeCommand = ModelServerCommandUtil.createRemoveCommand(
        //     this.getOwner(node),
        //     node.jsonforms.property,
        //     node.jsonforms.index ? [Number(node.jsonforms.index)] : []
        // );
        // this.modelServerClient.edit(this.modelIDToRequest, removeCommand);
    }

    protected async addNode({ node, type, property }: AddCommandProperty): Promise<void> {
        // TODO
        // const addCommand = ModelServerCommandUtil.createAddCommand(...);
        // this.modelServerClient.edit(this.modelIDToRequest, addCommand);
    }

    /* eslint-disable */
    /* @ts-ignore */
    private difference(object, base) {
        /* @ts-ignore */
        function changes(object, base) {
            return transform(object, function (result, value, key) {
                if (!isEqual(value, base[key])) {
                    /* @ts-ignore */
                    result[key] = (isObject(value) && isObject(base[key])) ? changes(value, base[key]) : value;
                }
            });
        }
        return changes(object, base);
    }


    protected async handleFormUpdate(data: any, node: TreeEditor.Node): Promise<void> {
        const results = this.difference(data, node.jsonforms.data);
        const editCommand = this.createSetCommand(results as object, data);
        if (editCommand) {
            this.modelServerClient.edit(this.modelIDToRequest, editCommand);
        }
    }

    protected createSetCommand(feature: object, jsonFormsData: any): ModelServerCommand | undefined {
        const changableFeatures = Object.keys(jsonFormsData).filter(key => key !== "eClass" && key !== "semanticUri" && key !== "source" && key !== "target");
        const featureName = Object.keys(feature)[0];
        if (featureName && changableFeatures.indexOf(featureName) > -1) {
            return new SetCommand(this.getOwnerFromData(jsonFormsData), featureName, [jsonFormsData[featureName]]);
        }
        return undefined;
    }

    protected getOwnerFromData(jsonFormsData: any): ModelServerReferenceDescription {
        return {
            $ref: this.getOwnerRef("//" + jsonFormsData.name),
            eClass: jsonFormsData.eClass
        };
    }

}

// eslint-disable-next-line no-redeclare
export namespace TreeEditorWidget {
    export const WIDGET_ID = "ecore-tree-editor";
    export const EDITOR_ID = "ecore.tree.editor";
}
