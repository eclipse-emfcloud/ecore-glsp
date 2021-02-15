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
import { LabelProviderContribution } from "@theia/core/lib/browser";
import { injectable } from "inversify";

import { TreeEditorWidget } from "./tree-editor-widget";
import { EcoreEType, EcoreModel } from "./tree-model";

const ICON_CLASSES: Map<string, string> = new Map([
    [EcoreModel.Type.EPackage, "ecoreimg epackage"],
    [EcoreModel.Type.EClass, "ecoreimg eclass"],
    [EcoreModel.Type.EEnum, "ecoreimg eenum"],
    [EcoreModel.Type.EDataType, "ecoreimg edatatype"],
    [EcoreModel.Type.EReference, "ecoreimg ereference"],
    [EcoreModel.Type.EAttribute, "ecoreimg eattribute"],
    [EcoreModel.Type.EEnumLiteral, "ecoreimg eenumliteral"],
    ["EGenericSuperType", "ecoreimg egenericsupertype"],
    ["EGenericElementType", "ecoreimg egenericelementtype"],
    ["EClassAbstract", "ecoreimg eclassabstract"],
    ["EClassInterface", "ecoreimg eclassinterface"]
]);

/* Icon for unknown types */
const UNKNOWN_ICON = "fa fa-question-circle";

@injectable()
export class TreeLabelProvider implements LabelProviderContribution {
    public canHandle(element: object): number {
        if ((TreeEditor.Node.is(element) || TreeEditor.CommandIconInfo.is(element))
            && element.editorId === TreeEditorWidget.EDITOR_ID
        ) {
            return 1000;
        }
        return 0;
    }

    public getIcon(element: object): string | undefined {
        let iconClass: string | undefined;
        if (TreeEditor.CommandIconInfo.is(element)) {
            iconClass = ICON_CLASSES.get(element.type);
        } else if (TreeEditor.Node.is(element)) {
            /* @ts-ignore */
            if ((element.parent && element.parent.jsonforms && element.parent.jsonforms.data.eClass === EcoreModel.Type.EEnum) || element.jsonforms.data.value) {
                iconClass = ICON_CLASSES.get(EcoreModel.Type.EEnumLiteral);
            } else if (EcoreEType.is(element.jsonforms.data)) {
                /* @ts-ignore */
                if (element.parent.jsonforms.data.eClass === EcoreModel.Type.EClass) {
                    iconClass = ICON_CLASSES.get("EGenericSuperType");
                } else {
                    iconClass = ICON_CLASSES.get("EGenericElementType");
                }
            } else {
                if (element.jsonforms.data.abstract) {
                    iconClass = ICON_CLASSES.get("EClassAbstract");
                } else if (element.jsonforms.data.interface) {
                    iconClass = ICON_CLASSES.get("EClassInterface");
                } else {
                    iconClass = ICON_CLASSES.get(element.jsonforms.data.eClass);
                }
            }
        }

        return iconClass ? iconClass : UNKNOWN_ICON;
    }

    public getName(element: object): string | undefined {
        const data = TreeEditor.Node.is(element)
            ? element.jsonforms.data
            : element;
        if (data.eSuperTypes) {
            let name = data.name + " \u2192 ";
            data.eSuperTypes.forEach((eSuperType: EcoreEType) => {
                name = name.concat(eSuperType.$ref.split("//")[1] + ", ");
            });
            return name.slice(0, -2);
        } else if (data.eType) {
            const name = data.name + " : ";
            if (data.eType.eClass.indexOf("EClass") > -1 || data.eType.eClass.indexOf("EEnum") > -1) {
                return name.concat(data.eType.$ref.split("//")[1]);
            } else if (data.eType.eClass.indexOf("EDataType") && data.eType.$ref.startsWith("//")) {
                return name.concat(data.eType.$ref.split("//")[1]);
            }
            return name.concat(data.eType.$ref.split("//")[2]);
        } else if (data.value) {
            return data.name + " = " + data.value;
            /* @ts-ignore */
        } else if (element.parent && element.parent.jsonforms && element.parent.jsonforms.data.eClass === EcoreModel.Type.EEnum) {
            return data.name + " = 0";
        } else if (data.instanceClassName) {
            return data.name + " [" + data.instanceClassName + "]";
        } else if (data.name) {
            return data.name;
        } else if (EcoreEType.is(data)) {
            if (data.eClass.indexOf("EClass") > -1 || data.eClass.indexOf("EEnum") > -1) {
                return data.$ref.split("//")[1];
            } else if (data.eClass.indexOf("EDataType") && data.$ref.startsWith("//")) {
                return data.$ref.split("//")[1];
            }
            return data.$ref.split("//")[2];
        }

        return undefined;
    }

}
