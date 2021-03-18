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
import URI from "@theia/core/lib/common/uri";

export interface EcoreEType {
    eClass: string;
    $ref: string;
}

export namespace EcoreEType {
    export function is(element: any): element is EcoreEType {
        return !!element && "eClass" in element && "$ref" in element;
    }
}

export namespace EcoreModel {
    export namespace Type {
        export const EPackage = "http://www.eclipse.org/emf/2002/Ecore#//EPackage";
        export const EAttribute = "http://www.eclipse.org/emf/2002/Ecore#//EAttribute";
        export const EClass = "http://www.eclipse.org/emf/2002/Ecore#//EClass";
        export const EDataType = "http://www.eclipse.org/emf/2002/Ecore#//EDataType";
        export const EEnum = "http://www.eclipse.org/emf/2002/Ecore#//EEnum";
        export const EReference = "http://www.eclipse.org/emf/2002/Ecore#//EReference";
        export const EOperation = "http://www.eclipse.org/emf/2002/Ecore#//EOperation";

        export const EClassAbstract = "EClassAbstract";
        export const EClassInterface = "EClassInterface";
        export const EException = "EException";
        export const EEnumLiteral = "EEnumLiteral";
        export const EGenericElementType = "EGenericElementType";
        export const EGenericSuperType = "EGenericSuperType";
        export const EType = "EType";

        export function name(type: string): string {
            return new URI(type).fragment.substring(2);
        }
    }

    const eClassifiers = [
        Type.EClass,
        Type.EDataType,
        Type.EEnum
    ];

    /** Maps types to their creatable children */
    export const childrenMapping: Map<
        string,
        TreeEditor.ChildrenDescriptor[]
    > = new Map([
        [
            Type.EPackage,
            [
                {
                    property: "children",
                    children: eClassifiers
                }
            ]
        ],
        [
            Type.EClass,
            [
                {
                    property: "children",
                    children: [Type.EAttribute, Type.EReference, Type.EOperation]
                }
            ]
        ],
        [
            Type.EEnum,
            [
                {
                    property: "children",
                    children: [Type.EEnumLiteral]
                }
            ]
        ]
    ]);
}
