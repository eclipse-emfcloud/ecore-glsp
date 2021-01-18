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
import { ModelServerSubscriptionService } from "@eclipse-emfcloud/modelserver-theia/lib/browser";
import {
    ModelServerClient,
    ModelServerCommand,
    ModelServerCommandUtil,
    ModelServerObject,
    ModelServerReferenceDescription
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
import { clone, isEqual, isObject, transform } from "lodash";

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

        this.subscriptionService.onDirtyStateListener(dirtyState => {
            this.dirty = dirtyState;
            this.onDirtyChangedEmitter.fire();
        });

        this.subscriptionService.onIncrementalUpdateListener(incrementalUpdate => {
            const command = incrementalUpdate as ModelServerCommand;
            if (command.commands && command.commands.length > 0) {
                command.commands.forEach(c => {
                    this.applyCommand(c);
                });
            } else {
                this.applyCommand(command);
            }
        });

        this.modelServerClient.get(this.modelIDToRequest).then(response => {
            if (response.statusCode === 200) {
                if (isEqual(this.instanceData, response.body)) {
                    return;
                }
                this.instanceData = response.body;
                this.treeWidget
                    .setData({ error: false, data: this.instanceData })
                    .then(() => this.treeWidget.selectFirst());
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
        this.modelServerClient.subscribe(this.modelIDToRequest);
        // see https://developer.mozilla.org/en-US/docs/Web/API/WindowEventHandlers/onbeforeunload
        window.onbeforeunload = () => this.dispose();
    }

    protected applyCommand(command: ModelServerCommand): void {
        switch (command.type) {
            case "add": {
                this.addNodeViaCommand(command);
                break;
            }
            case "remove": {
                this.removeNodeViaCommand(command);
                break;
            }
            case "set": {
                this.setNodeDataViaCommand(command);
                break;
            }
            default:
                break;
        }
    }

    protected getOwnerPropIndexPath(command: ModelServerCommand): { property: string; index?: string }[] {
        // the #/ marks the beginning of the actual path, but we also want the first slash removed so +3
        return command.owner.$ref
            .substring(command.owner.$ref.indexOf("#/") + 3)
            .split("/")
            .filter(v => v.length !== 0)
            .map(path => ({
                property: path,
                index: path.substr(path.length - 1)
            }));
    }

    protected findNode(ownerPropIndexPath: { property: string; index?: string }[]): TreeEditor.Node {
        const rootNode = this.treeWidget.model.root as TreeEditor.RootNode;
        if (ownerPropIndexPath.length !== 0) {
            const semanticUri = ownerPropIndexPath[0].property;
            /* @ts-ignore */
            let node = rootNode.children[0].children[0];
            /* @ts-ignore */
            rootNode.children[0].children.forEach(child => {
                // FIXME this only prototype solution, e.g. nested children are not found etc.
                // tree nodes need to get an id and then the whole tree needs to be searched
                const childName = child.name.indexOf("\u2192") > -1 ? child.name.split(" \u2192")[0] : child.name;
                if (childName === semanticUri || semanticUri.indexOf(childName) > -1 || childName.indexOf(semanticUri) > -1) {
                    node = child;
                }
            });
            /* @ts-ignore */
            return node;
        } else {
            return rootNode.children[0] as TreeEditor.Node;
        }
    }

    protected findNodeData(ownerPropIndexPath: { property: string; index?: string }[]): any {
        return ownerPropIndexPath.length === 0
            ? this.instanceData
            : ownerPropIndexPath.reduce(
                (data, path) =>
                    path.index === undefined
                        ? data[path.property]
                        : data[path.property][path.index],
                this.instanceData
            );
    }

    protected get modelIDToRequest(): string {
        const workspaceUri = this.workspaceService.getWorkspaceRootUri(this.options.uri);
        if (workspaceUri) {
            const rootUriLength = workspaceUri.toString().length;
            return this.options.uri.toString().substring(rootUriLength + 1);
        }
        return "";
    }

    protected getTypeProperty(): string {
        return "typeId";
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
        return `${this.workspaceService.workspace!.uri}/${this.modelIDToRequest}#${semanticUri}`.replace("file:///", "file:/");
    }

    protected getOwner(node: Readonly<TreeEditor.Node>): ModelServerReferenceDescription {
        const parentNode = node.parent;
        return {
            /* @ts-ignore */
            $ref: this.getOwnerRef(parentNode.jsonforms.data.eClass === EcoreModel.Type.EPackage ? "/" : "//" + parentNode.name),
            /* @ts-ignore */
            eClass: parentNode.jsonforms.data.eClass

            // $ref:'file:/home/nina/Clients/OpenSource/emfcloud/ecore-glsp-fork/client/workspace/empty/model/empty.ecore#/'
            // eClass:'http://www.eclipse.org/emf/2002/Ecore#//EPackage'
        };
    }

    protected deleteNode(node: Readonly<TreeEditor.Node>): void {
        const removeCommand = ModelServerCommandUtil.createRemoveCommand(
            this.getOwner(node),
            node.jsonforms.property,
            node.jsonforms.index ? [Number(node.jsonforms.index)] : []
        );
        this.modelServerClient.edit(this.modelIDToRequest, removeCommand);
    }

    protected addNode({ node, type, property }: AddCommandProperty): void {
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


    protected handleFormUpdate(data: any, node: TreeEditor.Node): void {
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
            return ModelServerCommandUtil.createSetCommand(this.getOwnerFromData(jsonFormsData), featureName, [jsonFormsData[featureName]]);
        }
        return undefined;
    }

    protected getOwnerFromData(jsonFormsData: any): ModelServerReferenceDescription {
        return {
            $ref: this.getOwnerRef("//" + jsonFormsData.name),
            eClass: jsonFormsData.eClass
        };
    }

    protected addNodeViaCommand(command: ModelServerCommand): void {
        const ownerPropIndexPath = this.getOwnerPropIndexPath(command);
        // FIXME once node IDs are unique: ownerNode = this.treeWidget.findNode(ownerPropIndexPath);
        const ownerNode = this.findNode(ownerPropIndexPath);
        const objectToModify = this.findNodeData(ownerPropIndexPath);

        if (!objectToModify[command.feature]) {
            objectToModify[command.feature] = [];
        }
        if (command.objectValues !== undefined) {
            objectToModify[command.feature].push(...command.objectsToAdd as ModelServerObject[]);
            this.treeWidget.addChildren(
                ownerNode,
                command.objectsToAdd as ModelServerObject[],
                command.feature
            );
            if (!this.isVisible) {
                this.delayedRefresh = true;
            }
        }
    }

    protected removeNodeViaCommand(command: ModelServerCommand): void {
        const ownerPropIndexPath = this.getOwnerPropIndexPath(command);
        // FIXME once node IDs are unique: ownerNode = this.treeWidget.findNode(ownerPropIndexPath);
        const ownerNode = this.findNode(ownerPropIndexPath);
        const objectToModify = this.findNodeData(ownerPropIndexPath);

        if (command.indices) {
            command.indices.forEach(i =>
                objectToModify[command.feature].splice(i, 1)
            );
            this.treeWidget.removeChildren(
                ownerNode,
                command.indices,
                command.feature
            );
            if (!this.isVisible) {
                this.delayedRefresh = true;
            }
        }
    }

    protected setNodeDataViaCommand(command: ModelServerCommand): void {
        const ownerPropIndexPath = this.getOwnerPropIndexPath(command);
        // FIXME once node IDs are unique: ownerNode = this.treeWidget.findNode(ownerPropIndexPath);
        const ownerNode = this.findNode(ownerPropIndexPath);

        // maybe we can directly manipulate the data?
        /* @ts-ignore */
        const data = clone(ownerNode.jsonforms.data);
        // FIXME handle array changes
        if (command.dataValues) {
            data[command.feature] = command.dataValues[0];
        } else {
            /* @ts-ignore */
            data[command.feature] = command.objectsToAdd[0];
        }
        if (data.interface) {
            data.interface = data.interface === "true";
        } else if (data.abstract) {
            data.abstract = data.abstract === "true";
        }
        /* @ts-ignore */
        this.treeWidget.updateDataForNode(ownerNode, data);
        if (!this.isVisible) {
            this.delayedRefresh = true;
        }
        if (this.selectedNode === ownerNode) {
            this.formWidget.setSelection(this.selectedNode);
        }
    }

}

// eslint-disable-next-line no-redeclare
export namespace TreeEditorWidget {
    export const WIDGET_ID = "ecore-tree-editor";
    export const EDITOR_ID = "ecore.tree.editor";
}
