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

export const ecoreTypeSchema = {
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
