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
import { TreeEditor } from "@eclipse-emfcloud/theia-tree-editor";
import { JsonSchema, UISchemaElement } from "@jsonforms/core";
import { ILogger } from "@theia/core";
import { inject, injectable } from "inversify";

import { EcoreEType, EcoreModel } from "./tree-model";
import {
    eAttributeUiSchema,
    eClassUiSchema,
    eDataTypeUiSchema,
    eEnumLiteralUiSchema,
    eEnumUiSchema,
    ePackageUiSchema,
    eReferenceUiSchema,
    eTypeUiSchema
} from "./tree-schema";

@injectable()
export class TreeModelService implements TreeEditor.ModelService {

    constructor(@inject(ILogger) private readonly logger: ILogger) {
    }

    getDataForNode(node: TreeEditor.Node): any {
        if (EcoreEType.is(node.jsonforms.data)) {
            const eClassifier = node.jsonforms.data.$ref.split("//")[1];
            /* @ts-ignore */
            node.jsonforms.data["eClassifier"] = eClassifier;
            /* @ts-ignore */
            node.jsonforms.data["eTypeParameter"] = "";
            return node.jsonforms.data;
        }
        return node.jsonforms.data;
    }

    getSchemaForNode(node: TreeEditor.Node): JsonSchema | undefined {
        let eClass = "";
        if (EcoreEType.is(node.jsonforms.data)) {
            eClass = "etype";
        } else if (node.jsonforms.data.eClass) {
            eClass = node.jsonforms.data.eClass.split("#//")[1].toLowerCase();
            /* @ts-ignore */
        } else if (node.parent.jsonforms.data.eClass === EcoreModel.Type.EEnum || node.jsonforms.data.value) {
            eClass = EcoreModel.Type.EEnumLiteral.split("#//")[1].toLowerCase();
        }
        /* @ts-ignore */
        const elementSchema = ecoreTypeSchema.definitions[eClass];
        return {
            definitions: ecoreTypeSchema.definitions,
            ...elementSchema
        };
    }

    getUiSchemaForNode(node: TreeEditor.Node): UISchemaElement | undefined {
        // #FIXME as soon as the theia-tree-editor supports async schema fetching, we can replace the hard coded ui schemas and fetch them directly here
        const type = node.jsonforms.data.eClass || EcoreModel.Type.EEnumLiteral;
        if (EcoreEType.is(node.jsonforms.data)) {
            return eTypeUiSchema;
        }
        switch (type) {
            case EcoreModel.Type.EPackage:
                return ePackageUiSchema;
            case EcoreModel.Type.EAttribute:
                return eAttributeUiSchema;
            case EcoreModel.Type.EClass:
                return eClassUiSchema;
            case EcoreModel.Type.EDataType:
                return eDataTypeUiSchema;
            case EcoreModel.Type.EEnum:
                return eEnumUiSchema;
            case EcoreModel.Type.EEnumLiteral:
                return eEnumLiteralUiSchema;
            case EcoreModel.Type.EReference:
                return eReferenceUiSchema;
            default:
                this.logger.warn(
                    "Can't find registered ui schema for type " + type
                );
                return undefined;
        }
    }

    getChildrenMapping(): Map<string, TreeEditor.ChildrenDescriptor[]> {
        return EcoreModel.childrenMapping;
    }

    getNameForType(type: string): string {
        return EcoreModel.Type.name(type);
    }

}
