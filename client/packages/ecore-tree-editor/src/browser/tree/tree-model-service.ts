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
    constructor(@inject(ILogger) private readonly logger: ILogger) { }

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
        const elementSchema = this.ecoreTypeSchema.definitions[eClass];
        return {
            definitions: this.ecoreTypeSchema.definitions,
            ...elementSchema
        };
    }

    getUiSchemaForNode(node: TreeEditor.Node): UISchemaElement | undefined {
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

    public ecoreTypeSchema = {
        "$schema": "http://json-schema.org/draft-07/schema#",
        "$id": "http://www.eclipse.org/emf/2002/Ecore",
        "title": "JSON type schema for 'ecore'",
        "type": "object",
        "definitions": {
            "eclass": {
                "$id": "#eclass",
                "title": "EClass",
                "type": "object",
                "properties": {
                    "eClass": {
                        "const": "http://www.eclipse.org/emf/2002/Ecore#//EClass"
                    },
                    "name": {
                        "type": "string"
                    },
                    "instanceClassName": {
                        "type": "string"
                    },
                    "instanceClass": {
                        "type": "string"
                    },
                    "defaultValue": {
                        "type": "string"
                    },
                    "instanceTypeName": {
                        "type": "string"
                    },
                    "abstract": {
                        "type": "boolean"
                    },
                    "interface": {
                        "type": "boolean"
                    }
                },
                "additionalProperties": false
            },
            "edatatype": {
                "$id": "#edatatype",
                "title": "EDataType",
                "type": "object",
                "properties": {
                    "eClass": {
                        "const": "http://www.eclipse.org/emf/2002/Ecore#//EDataType"
                    },
                    "name": {
                        "type": "string"
                    },
                    "instanceClassName": {
                        "type": "string"
                    },
                    "instanceClass": {
                        "type": "string"
                    },
                    "defaultValue": {
                        "type": "string"
                    },
                    "instanceTypeName": {
                        "type": "string"
                    },
                    "serializable": {
                        "type": "boolean"
                    }
                },
                "additionalProperties": false
            },
            "eenum": {
                "$id": "#eenum",
                "title": "EEnum",
                "type": "object",
                "properties": {
                    "eClass": {
                        "const": "http://www.eclipse.org/emf/2002/Ecore#//EEnum"
                    },
                    "name": {
                        "type": "string"
                    },
                    "instanceClassName": {
                        "type": "string"
                    },
                    "instanceClass": {
                        "type": "string"
                    },
                    "defaultValue": {
                        "type": "string"
                    },
                    "instanceTypeName": {
                        "type": "string"
                    },
                    "serializable": {
                        "type": "boolean"
                    }
                },
                "additionalProperties": false
            },
            "epackage": {
                "$id": "#epackage",
                "title": "EPackage",
                "type": "object",
                "properties": {
                    "eClass": {
                        "const": "http://www.eclipse.org/emf/2002/Ecore#//EPackage"
                    },
                    "name": {
                        "type": "string"
                    },
                    "nsURI": {
                        "type": "string"
                    },
                    "nsPrefix": {
                        "type": "string"
                    }
                },
                "additionalProperties": false
            },
            "ereference": {
                "$id": "#ereference",
                "title": "EReference",
                "type": "object",
                "properties": {
                    "eClass": {
                        "const": "http://www.eclipse.org/emf/2002/Ecore#//EReference"
                    },
                    "name": {
                        "type": "string"
                    },
                    "ordered": {
                        "type": "boolean"
                    },
                    "unique": {
                        "type": "boolean"
                    },
                    "lowerBound": {
                        "type": "integer",
                        "default": 0
                    },
                    "upperBound": {
                        "type": "integer",
                        "default": 1
                    },
                    "many": {
                        "type": "boolean"
                    },
                    "required": {
                        "type": "boolean"
                    },
                    "changeable": {
                        "type": "boolean"
                    },
                    "volatile": {
                        "type": "boolean"
                    },
                    "transient": {
                        "type": "boolean"
                    },
                    "defaultValueLiteral": {
                        "type": "string"
                    },
                    "defaultValue": {
                        "type": "string"
                    },
                    "unsettable": {
                        "type": "boolean"
                    },
                    "derived": {
                        "type": "boolean"
                    },
                    "containment": {
                        "type": "boolean"
                    },
                    "container": {
                        "type": "boolean"
                    },
                    "resolveProxies": {
                        "type": "boolean"
                    }
                },
                "additionalProperties": false
            },
            "eenumliteral": {
                "$id": "#eenumliteral",
                "title": "EEnumLiteral",
                "type": "object",
                "properties": {
                    "eClass": {
                        "const": "http://www.eclipse.org/emf/2002/Ecore#//EEnumLiteral"
                    },
                    "name": {
                        "type": "string"
                    },
                    "value": {
                        "type": "integer"
                    },
                    "instance": {
                        "type": "string"
                    },
                    "literal": {
                        "type": "string"
                    },
                    "eEnum": {
                        "$ref": "#/definitions/eenum"
                    }
                },
                "additionalProperties": false
            },
            "eattribute": {
                "$id": "#eattribute",
                "title": "EAttribute",
                "type": "object",
                "properties": {
                    "eClass": {
                        "const": "http://www.eclipse.org/emf/2002/Ecore#//EAttribute"
                    },
                    "name": {
                        "type": "string"
                    },
                    "ordered": {
                        "type": "boolean"
                    },
                    "unique": {
                        "type": "boolean"
                    },
                    "lowerBound": {
                        "type": "integer",
                        "default": 0
                    },
                    "upperBound": {
                        "type": "integer",
                        "default": 1
                    },
                    "many": {
                        "type": "boolean"
                    },
                    "required": {
                        "type": "boolean"
                    },
                    "changeable": {
                        "type": "boolean"
                    },
                    "volatile": {
                        "type": "boolean"
                    },
                    "transient": {
                        "type": "boolean"
                    },
                    "defaultValueLiteral": {
                        "type": "string"
                    },
                    "defaultValue": {
                        "type": "string"
                    },
                    "unsettable": {
                        "type": "boolean"
                    },
                    "derived": {
                        "type": "boolean"
                    },
                    "iD": {
                        "type": "boolean"
                    }
                },
                "additionalProperties": false
            },
            "etype": {
                "$id": "#etype",
                "title": "EType",
                "type": "object",
                "properties": {
                    "eClassifier": {
                        "type": "string"
                    },
                    "eTypeParameter": {
                        "type": "string"
                    }
                },
                "additionalProperties": false
            }
        }
    };
}
