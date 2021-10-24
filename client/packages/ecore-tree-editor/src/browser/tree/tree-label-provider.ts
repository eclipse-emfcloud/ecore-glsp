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
import { codicon, LabelProviderContribution } from "@theia/core/lib/browser";
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
    [EcoreModel.Type.EOperation, "ecoreimg eoperation"],
    [EcoreModel.Type.EGenericSuperType, "ecoreimg egenericsupertype"],
    [EcoreModel.Type.EGenericElementType, "ecoreimg egenericelementtype"],
    [EcoreModel.Type.EClassAbstract, "ecoreimg eclassabstract"],
    [EcoreModel.Type.EClassInterface, "ecoreimg eclassinterface"],
    [EcoreModel.Type.EException, "ecoreimg egenericexception"]
]);

/* Icon for unknown types */
const UNKNOWN_ICON = `${codicon("question")}`;

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
                    case EcoreModel.Type.EException:
                        iconClass = ICON_CLASSES.get(EcoreModel.Type.EException); break;
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

            if (iconClass &&
                (elementData.eClass === EcoreModel.Type.EAttribute || elementData.eClass === EcoreModel.Type.EReference || elementData.eClass === EcoreModel.Type.EOperation)) {
                iconClass += this.appendOccurrenceIcon(elementData);
            }
        }

        return iconClass || UNKNOWN_ICON;
    }

    protected appendOccurrenceIcon(elementData: any): string {
        let occurrenceIconString = " ";
        const lowerBound = elementData.lowerBound;
        const upperBound = elementData.upperBound;
        if (!lowerBound || lowerBound === 0) {
            if (upperBound === 0) {
                occurrenceIconString += "eoccurrencezero";
            } else if (upperBound > 1) {
                occurrenceIconString += "eoccurrencezeroton";
            } else if (upperBound < 0) {
                occurrenceIconString += "eoccurrencezerotounbounded";
            } else {
                occurrenceIconString += "eoccurrencezerotoone";
                // FIXME if default values are fixed, do check for upperBound === 1 and otherwise return unspecified
                // occurrenceIconString += "eoccurrencezerotounspecified";
            }
        } else if (lowerBound === 1) {
            if (upperBound > 1) {
                occurrenceIconString += "eoccurrenceoneton";
            } else if (upperBound < 0) {
                occurrenceIconString += "eoccurrenceonetounbounded";
            } else {
                occurrenceIconString += "eoccurrenceone";
                // FIXME if default values are fixed, do check for upperBound === 1 and otherwise return unspecified
                // occurrenceIconString += "eoccurrenceoneunspecified";
            }
        } else if (lowerBound > 1) {
            if (lowerBound === upperBound) {
                occurrenceIconString += "eoccurrencen";
            } else if (upperBound > 1) {
                occurrenceIconString += "eoccurrencentom";
            } else if (upperBound < 0) {
                occurrenceIconString += "eoccurrencentounbounded";
            } else {
                occurrenceIconString += "eoccurrencentounspecified";
            }
        }
        return occurrenceIconString;
    }

    public getName(element: object): string | undefined {
        const elementData = TreeEditor.Node.is(element)
            ? element.jsonforms.data
            : element;
        if (elementData.eSuperTypes) {
            return `${elementData.name} \u2192 ${elementData.eSuperTypes.map((eSuperType: any) => this.toName(eSuperType.$ref)).join(", ")}`; // \u2192 is an arrow to the right
        } else if (elementData.eClass === EcoreModel.Type.EOperation) {
            let name = `${elementData.name}()`;
            if (elementData.eType) {
                name = name.concat(` : ${this.toName(new URI(elementData.eType.$ref).fragment)}`);
            }
            if (elementData.eExceptions) {
                name = name.concat(` throws ${elementData.eExceptions.map((eException: any) => this.toName(eException.$ref)).join(", ")}`);
            }
            return name;
        } else if (elementData.type) {
            switch (elementData.type) {
                case EcoreModel.Type.EEnumLiteral: {
                    return elementData.name.concat(` = ${elementData.value || "0"}`);
                }
                case EcoreModel.Type.EGenericSuperType: {
                    return this.toName(elementData.$ref);
                }
                case EcoreModel.Type.EException:
                    return this.toName(elementData.$ref);
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
