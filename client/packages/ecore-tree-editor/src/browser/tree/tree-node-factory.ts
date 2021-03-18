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
import { TreeEditor } from "@eclipse-emfcloud/theia-tree-editor";
import { ILogger } from "@theia/core";
import { inject, injectable } from "inversify";
import { v4 } from "uuid";

import { TreeEditorWidget } from "./tree-editor-widget";
import { TreeLabelProvider } from "./tree-label-provider";
import { EcoreModel } from "./tree-model";

@injectable()
export class TreeNodeFactory implements TreeEditor.NodeFactory {
    constructor(
        @inject(TreeLabelProvider)
        private readonly labelProvider: TreeLabelProvider,
        @inject(ILogger) private readonly logger: ILogger
    ) { }

    mapDataToNodes(treeData: TreeEditor.TreeData): TreeEditor.Node[] {
        const node = this.mapData(treeData.data);
        if (node) {
            return [node];
        }
        return [];
    }

    mapData(
        data: any,
        parent?: TreeEditor.Node,
        property?: string,
        indexOrKey?: number | string
    ): TreeEditor.Node {
        if (!data) {
            this.logger.warn("mapData called without data");
        }

        const node: TreeEditor.Node = {
            ...this.defaultNode(),
            editorId: TreeEditorWidget.EDITOR_ID,
            name: this.labelProvider.getName(data)!,
            parent: parent,
            jsonforms: {
                type: this.getTypeId(data),
                data: data,
                property: property!,
                index:
                    typeof indexOrKey === "number"
                        ? indexOrKey.toFixed(0)
                        : indexOrKey
            }
        };

        // containments
        if (parent) {
            parent.children.push(node);
            if (parent.jsonforms.data.eClass === EcoreModel.Type.EPackage) {
                parent.expanded = true;
            } else {
                parent.expanded = false;
            }
        }
        if (data.eClassifiers) {
            // determine eClassifiers
            data.eClassifiers.forEach((element: any, idx: string | number | undefined) => {
                this.mapData(element, node, "eClassifiers", idx);
            });
        }
        if (data.eSuperTypes) {
            // determine eSuperTypes
            data.eSuperTypes.forEach((element: any, idx: string | number | undefined) => {
                element["type"] = EcoreModel.Type.EGenericSuperType;
                this.mapData(element, node, "eSuperTypes", idx);
            });
        }
        if (data.eExceptions) {
            // determine eExceptions
            data.eExceptions.forEach((element: any, idx: string | number | undefined) => {
                element["type"] = EcoreModel.Type.EException;
                this.mapData(element, node, "eExceptions", idx);
            });
        }
        if (data.eStructuralFeatures) {
            // determine eStructuralFeatures
            data.eStructuralFeatures.forEach((element: any, idx: string | number | undefined) => {
                this.mapData(element, node, "eStructuralFeatures", idx);
            });
        }
        if (data.eOperations) {
            // determine eOperations
            data.eOperations.forEach((element: any, idx: string | number | undefined) => {
                element["eClass"] = EcoreModel.Type.EOperation;
                this.mapData(element, node, "eOperations", idx);
            });
        }
        if (data.eLiterals) {
            // determine eLiterals
            data.eLiterals.forEach((element: any, idx: string | number | undefined) => {
                element["type"] = EcoreModel.Type.EEnumLiteral;
                this.mapData(element, node, "eLiterals", idx);
            });
        }
        if (data.eType) {
            this.mapData(data.eType, node, "eType", 0);
        }

        return node;
    }

    hasCreatableChildren(node: TreeEditor.Node): boolean {
        // FIXME
        return node
            ? EcoreModel.childrenMapping.get(node.jsonforms.type) !== undefined
            : false;
    }

    protected defaultNode(): Omit<TreeEditor.Node, "editorId"> {
        return {
            id: v4(),
            expanded: false,
            selected: false,
            parent: undefined,
            children: [],
            decorationData: {},
            name: "",
            jsonforms: {
                type: "",
                property: "",
                data: undefined
            }
        };
    }

    /** Derives the type id from the given data. */
    protected getTypeId(data: any): string {
        return (data && data.typeId) || "";
    }
}
