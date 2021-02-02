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
import { RuleEffect } from "@jsonforms/core";

export const ePackageUiSchema = {

    "type": "VerticalLayout",
    "elements": [
        {
            "type": "Label",
            "text": "EPackage"
        },
        {
            "type": "Control",
            "label": "Name",
            "scope": "#/properties/name",
            "rule": {
                "effect": "DISABLE",
                "condition": {}
            }
        },
        {
            "type": "Control",
            "label": "Ns URI",
            "scope": "#/properties/nsURI"
        },
        {
            "type": "Control",
            "label": "Ns Prefix",
            "scope": "#/properties/nsPrefix"
        }
    ]
};

export const eEnumLiteralUiSchema =
{
    "type": "VerticalLayout",
    "elements": [
        {
            "type": "Label",
            "text": "EEnumLiteral"
        },
        {
            "type": "Control",
            "label": "Name",
            "scope": "#/properties/name",
            "rule": {
                "effect": "DISABLE",
                "condition": {}
            }
        },
        {
            "type": "Control",
            "label": "Value",
            "scope": "#/properties/value"
        },
        {
            "type": "Control",
            "label": "Literal",
            "scope": "#/properties/literal"
        }
    ]
};

export const eEnumUiSchema =
{
    "type": "VerticalLayout",
    "elements": [
        {
            "type": "Label",
            "text": "Classifier - EEnum"
        },
        {
            "type": "Control",
            "label": "Name",
            "scope": "#/properties/name",
            "rule": {
                "effect": "DISABLE",
                "condition": {}
            }
        },
        {
            "type": "Control",
            "label": "Instance Class Name",
            "scope": "#/properties/instanceClassName",
            "rule": {
                "effect": RuleEffect.DISABLE,
                "condition": {}
            }
        },
        {
            "type": "Control",
            "label": "Instance Type Name",
            "scope": "#/properties/instanceClass",
            "rule": {
                "effect": RuleEffect.DISABLE,
                "condition": {}
            }
        },
        {
            "type": "Control",
            "label": "Serializable",
            "scope": "#/properties/serializable"
        }
    ]
};

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export const eClassUiSchema =
{
    "type": "VerticalLayout",
    "elements": [
        {
            "type": "Label",
            "text": "Classifier - EClass"
        },
        {
            "type": "Control",
            "label": "Name",
            "scope": "#/properties/name",
            "rule": {
                "effect": "DISABLE",
                "condition": {}
            }
        },
        {
            "type": "Control",
            "label": "Instance Class Name",
            "scope": "#/properties/instanceClassName",
            "rule": {
                "effect": RuleEffect.DISABLE,
                "condition": {}
            }
        },
        {
            "type": "Control",
            "label": "Instance Type Name",
            "scope": "#/properties/instanceClass",
            "rule": {
                "effect": RuleEffect.DISABLE,
                "condition": {}
            }
        },
        {
            "type": "Control",
            "label": "Abstract",
            "scope": "#/properties/abstract"
        },
        {
            "type": "Control",
            "label": "Interface",
            "scope": "#/properties/interface"
        }
        // {
        //     "type": "VerticalLayout",
        //     "elements": [
        //         {
        //             "type": "Control",
        //             "label": "ESuperTypes",
        //             "scope": "#/properties/eSuperTypes",
        //             "options": {
        //                 "detail": {
        //                     "type": "VerticalLayout",
        //                     "elements": [
        //                         {
        //                             "type": "Control",
        //                             "scope": "#/properties/name",
        //                             "rule": {
        //                                 "effect": RuleEffect.DISABLE,
        //                                 "condition": {}
        //                             }
        //                         }
        //                     ]

        //                 }
        //             }
        //         }
        //     ]
        // }
    ]
};

export const eReferenceUiSchema =
{
    "type": "VerticalLayout",
    "elements": [
        {
            "type": "Label",
            "text": "EReference"
        },
        {
            "type": "Control",
            "label": "Name",
            "scope": "#/properties/name",
            "rule": {
                "effect": "DISABLE",
                "condition": {}
            }
        },
        // {
        //     "type": "Control",
        //     "label": "EType",
        //     "scope": "#/properties/eType",
        //     "rule": {
        //         "effect": RuleEffect.DISABLE,
        //         "condition": {}
        //     }
        // },
        {
            "type": "Control",
            "label": "Lower Bound",
            "scope": "#/properties/lowerBound"
        },
        {
            "type": "Control",
            "label": "Upper Bound",
            "scope": "#/properties/upperBound"
        },
        // {
        //     "type": "Control",
        //     "label": "EOpposite",
        //     "scope": "#/properties/eOpposite/properties/eClass",
        //     "rule": {
        //         "effect": RuleEffect.DISABLE,
        //         "condition": {}
        //     }
        // },
        {
            "type": "Control",
            "label": "Containment",
            "scope": "#/properties/containment"
        },
        {
            "type": "Control",
            "label": "Transient",
            "scope": "#/properties/transient"
        },
        {
            "type": "Control",
            "label": "Derived",
            "scope": "#/properties/derived"
        },
        {
            "type": "Control",
            "label": "Ordered",
            "scope": "#/properties/ordered"
        },
        {
            "type": "Control",
            "label": "Unique",
            "scope": "#/properties/unique"
        },
        {
            "type": "Control",
            "label": "Changeable",
            "scope": "#/properties/changeable"
        },
        {
            "type": "Control",
            "label": "Volatile",
            "scope": "#/properties/volatile"
        },
        {
            "type": "Control",
            "label": "Unsettable",
            "scope": "#/properties/unsettable"
        },
        {
            "type": "Control",
            "label": "Resolve Proxies",
            "scope": "#/properties/resolveProxies"
        }
        // {
        //     "type": "Control",
        //     "label": "EKeys",
        //     "scope": "#/properties/eKeys",
        //     "rule": {
        //         "effect": RuleEffect.DISABLE,
        //         "condition": {}
        //     }
        // }
    ]
};

export const eAttributeUiSchema =
{
    "type": "VerticalLayout",
    "elements": [
        {
            "type": "Label",
            "text": "EAttribute"
        },
        {
            "type": "Control",
            "label": "Name",
            "scope": "#/properties/name",
            "rule": {
                "effect": "DISABLE",
                "condition": {}
            }
        },
        // {
        //     "type": "Control",
        //     "label": "EType",
        //     "scope": "#/properties/eType",
        //     "rule": {
        //         "effect": RuleEffect.DISABLE,
        //         "condition": {}
        //     }
        // },
        {
            "type": "Control",
            "label": "Lower Bound",
            "scope": "#/properties/lowerBound"
        },
        {
            "type": "Control",
            "label": "Upper Bound",
            "scope": "#/properties/upperBound"
        },
        {
            "type": "Control",
            "label": "Default Value Literal",
            "scope": "#/properties/defaultValueLiteral"
        },
        {
            "type": "Control",
            "label": "Transient",
            "scope": "#/properties/transient"
        },
        {
            "type": "Control",
            "label": "Derived",
            "scope": "#/properties/derived"
        },
        {
            "type": "Control",
            "label": "Ordered",
            "scope": "#/properties/ordered"
        },
        {
            "type": "Control",
            "label": "Unique",
            "scope": "#/properties/unique"
        },
        {
            "type": "Control",
            "label": "Changeable",
            "scope": "#/properties/changeable"
        },
        {
            "type": "Control",
            "label": "Volatile",
            "scope": "#/properties/volatile"
        },
        {
            "type": "Control",
            "label": "Unsettable",
            "scope": "#/properties/unsettable"
        },
        {
            "type": "Control",
            "label": "ID",
            "scope": "#/properties/iD"
        }
    ]
};

export const eDataTypeUiSchema =
{
    "type": "VerticalLayout",
    "elements": [
        {
            "type": "Label",
            "text": "Classifier - EDataType"
        },
        {
            "type": "Control",
            "label": "Name",
            "scope": "#/properties/name",
            "rule": {
                "effect": "DISABLE",
                "condition": {}
            }
        },
        {
            "type": "Control",
            "label": "Instance Class Name",
            "scope": "#/properties/instanceClassName"
        },
        {
            "type": "Control",
            "label": "Instance Type Name",
            "scope": "#/properties/instanceClass"
        },
        {
            "type": "Control",
            "label": "Default Value",
            "scope": "#/properties/defaultValue"
        },
        {
            "type": "Control",
            "label": "Serializable",
            "scope": "#/properties/serializable"
        }
    ]
};

export const eTypeUiSchema =
{
    "type": "VerticalLayout",
    "elements": [
        {
            "type": "Label",
            "text": "EType"
        },
        {
            "type": "Control",
            "label": "EClassifier",
            "scope": "#/properties/eClassifier",
            "rule": {
                "effect": RuleEffect.DISABLE,
                "condition": {}
            }
        },
        {
            "type": "Control",
            "label": "EType Parameter",
            "scope": "#/properties/eTypeParameter",
            "rule": {
                "effect": RuleEffect.DISABLE,
                "condition": {}
            }
        }
    ]
};
