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
import URI from "@theia/core/lib/common/uri";
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
    [EcoreModel.Type.EGenericSuperType, "ecoreimg egenericsupertype"],
    [EcoreModel.Type.EGenericElementType, "ecoreimg egenericelementtype"],
    [EcoreModel.Type.EClassAbstract, "ecoreimg eclassabstract"],
    [EcoreModel.Type.EClassInterface, "ecoreimg eclassinterface"]
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
        if (TreeEditor.Node.is(element)) {
            const elementData = element.jsonforms.data;
            if (elementData.type) {
                switch (elementData.type) {
                    case EcoreModel.Type.EEnumLiteral:
                        iconClass = ICON_CLASSES.get(EcoreModel.Type.EEnumLiteral); break;
                    case EcoreModel.Type.EGenericSuperType:
                        iconClass = ICON_CLASSES.get(EcoreModel.Type.EGenericSuperType); break;
                }
            } else if (EcoreEType.is(elementData)) {
                iconClass = ICON_CLASSES.get(EcoreModel.Type.EGenericElementType);
            } else {
                if (elementData.abstract) {
                    iconClass = ICON_CLASSES.get(EcoreModel.Type.EClassAbstract);
                } else if (elementData.interface) {
                    iconClass = ICON_CLASSES.get(EcoreModel.Type.EClassInterface);
                } else {
                    iconClass = ICON_CLASSES.get(elementData.eClass);
                }
            }
        }

        return iconClass || UNKNOWN_ICON;
    }

    public getName(element: object): string | undefined {
        const elementData = TreeEditor.Node.is(element)
            ? element.jsonforms.data
            : element;
        if (elementData.eSuperTypes) {
            let name = `${elementData.name} \u2192 `; // \u2192 is an arrow to the right
            elementData.eSuperTypes.forEach((eSuperType: EcoreEType) => {
                name = name.concat(eSuperType.$ref.substring(2) + ", ");
            });
            return name.slice(0, -2);
        } else if (elementData.type) {
            switch (elementData.type) {
                case EcoreModel.Type.EEnumLiteral: {
                    return elementData.name.concat(` = ${elementData.value || "0"}`);
                }
                case EcoreModel.Type.EGenericSuperType: {
                    return this.toName(elementData.$ref);
                }
            }
        } else if (elementData.eType) {
            const name = `${elementData.name} : `;
            if (elementData.eType.$ref.startsWith("//")) {
                return name.concat(this.toName(elementData.eType.$ref));
            }
            return name.concat(this.toName(new URI(elementData.eType.$ref).fragment));
        } else if (EcoreEType.is(elementData)) {
            if (elementData.$ref.startsWith("//")) {
                return this.toName(elementData.$ref);
            }
            return this.toName(new URI(elementData.$ref).fragment);
        } else if (elementData.eClass === EcoreModel.Type.EDataType) {
            return elementData.name.concat(` [${elementData.instanceClassName}]`);
        } else if (elementData.name) {
            return elementData.name;
        }

        return undefined;
    }

    protected toName(semanticUriFragment: string): string {
        // we expect an uri fragment of pattern '//SemanticUri'
        return semanticUriFragment.substring(2);
    }

}
