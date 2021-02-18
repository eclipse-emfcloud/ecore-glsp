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
import { ecoreTypeSchema } from "@eclipse-emfcloud/ecore-glsp-common/lib/browser/ecore-json-schema";
import { ModelServerClient } from "@eclipse-emfcloud/modelserver-theia";
import { TreeEditor } from "@eclipse-emfcloud/theia-tree-editor";
import { JsonSchema, UISchemaElement } from "@jsonforms/core";
import { MaybePromise } from "@theia/core";
import URI from "@theia/core/lib/common/uri";
import { inject, injectable } from "inversify";

import { EcoreEType, EcoreModel } from "./tree-model";

@injectable()
export class TreeModelService implements TreeEditor.ModelService {

    @inject(ModelServerClient) protected readonly modelServerClient: ModelServerClient;

    getDataForNode(node: TreeEditor.Node): MaybePromise<any> {
        const nodeData = node.jsonforms.data;
        if (EcoreEType.is(nodeData)) {
            const eClassifier = new URI(nodeData.$ref).fragment.substring(2);
            /* @ts-ignore */
            nodeData["eClassifier"] = eClassifier;
            /* @ts-ignore */
            nodeData["eTypeParameter"] = "";
            return nodeData;
        }
    }

    getSchemaForNode(node: TreeEditor.Node): JsonSchema | undefined {
        let eClass = "";
        const nodeData = node.jsonforms.data;
        if (EcoreEType.is(nodeData)) {
            eClass = EcoreModel.Type.EType;
        } else if (nodeData.eClass) {
            eClass = new URI(nodeData.eClass).fragment.substring(2);
            /* @ts-ignore */
        } else if (node.parent.jsonforms.data.eClass === EcoreModel.Type.EEnum || nodeData.value) {
            eClass = EcoreModel.Type.EEnumLiteral;
        }

        // FIXME atm we use a local version of the typeschema, as there exist performance issues with the fetched ecore type schema
        /* @ts-ignore */
        const elementSchema = ecoreTypeSchema.definitions[eClass.toLowerCase()];
        return {
            definitions: ecoreTypeSchema.definitions,
            ...elementSchema
        };
    }

    getUiSchemaForNode(node: TreeEditor.Node): MaybePromise<UISchemaElement | undefined> {
        const nodeData = node.jsonforms.data;
        const relativeRefURI = new URI(nodeData.eClass);
        let eClassName = relativeRefURI.fragment.substring(2) || EcoreModel.Type.EEnumLiteral;
        if (EcoreEType.is(nodeData)) {
            eClassName = EcoreModel.Type.EType;
        }
        if (eClassName) {
            return this.modelServerClient.getUiSchema(eClassName.toLowerCase()).then((response: any) => response.body as UISchemaElement);
        }
        return undefined;
    }

    getChildrenMapping(): Map<string, TreeEditor.ChildrenDescriptor[]> {
        return EcoreModel.childrenMapping;
    }

    getNameForType(type: string): string {
        return EcoreModel.Type.name(type);
    }

}
