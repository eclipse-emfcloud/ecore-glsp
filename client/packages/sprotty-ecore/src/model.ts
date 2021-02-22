/********************************************************************************
 * Copyright (c) 2019-2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
import { SChildElement } from "@eclipse-glsp/client";
import {
    boundsFeature,
    deletableFeature,
    EditableLabel,
    editLabelFeature,
    fadeFeature,
    hoverFeedbackFeature,
    isEditableLabel,
    layoutableChildFeature,
    layoutContainerFeature,
    Nameable,
    nameFeature,
    popupFeature,
    RectangularNode,
    SEdge,
    selectFeature,
    SLabel,
    SShapeElement,
    WithEditableLabel,
    withEditLabelFeature
} from "sprotty/lib";

export class LabeledNode extends RectangularNode implements WithEditableLabel, Nameable {

    get editableLabel(): (SChildElement & EditableLabel) | undefined {
        const headerComp = this.children.find(element => element.type === "comp:header");
        if (headerComp) {
            const label = headerComp.children.find(element => element.type === "label:heading");
            if (label && isEditableLabel(label)) {
                return label;
            }
        }
        return undefined;
    }

    get name(): string {
        if (this.editableLabel) {
            return this.editableLabel.text;
        }
        return this.id;
    }
    hasFeature(feature: symbol): boolean {
        return super.hasFeature(feature) || feature === nameFeature || feature === withEditLabelFeature;
    }
}

export class SEditableLabel extends SLabel implements EditableLabel {
    hasFeature(feature: symbol): boolean {
        return feature === editLabelFeature || super.hasFeature(feature);
    }
}

export class Icon extends SShapeElement {
    iconImageName: string;

    hasFeature(feature: symbol): boolean {
        return feature === boundsFeature || feature === layoutContainerFeature || feature === layoutableChildFeature || feature === fadeFeature;
    }
}

export class IconClass extends Icon {
    iconImageName = "EClass.svg";
}

export class IconAbstract extends Icon {
    iconImageName = "EClass_abstract.svg";
}

export class IconInterface extends Icon {
    iconImageName = "EClass_interface.svg";
}

export class IconEnum extends Icon {
    iconImageName = "EEnum.svg";
}

export class IconDataType extends Icon {
    iconImageName = "EDataType.svg";
}

export class SLabelNode extends SLabel implements EditableLabel {
    hoverFeedback = false;
    imageName: string;

    hasFeature(feature: symbol): boolean {
        return (feature === selectFeature || feature === editLabelFeature || feature === popupFeature || feature === deletableFeature ||
            feature === hoverFeedbackFeature || super.hasFeature(feature));
    }
}

export class SLabelNodeAttribute extends SLabelNode {
    imageName = "EAttribute.svg";
}

export class SLabelNodeLiteral extends SLabelNode {
    imageName = "EEnumLiteral.svg";
}

export class SLabelNodeOperation extends SLabelNode {
    imageName = "EOperation.svg";
}

export class ArrowEdge extends SEdge {
    public readonly targetAnchorCorrection = 3.3;
}

export class BidirectionalArrowEdge extends ArrowEdge {
    // eslint-disable-next-line no-invalid-this
    public readonly sourceAnchorCorrection = this.targetAnchorCorrection;
}

export class CompositionEdge extends SEdge {
    public readonly sourceAnchorCorrection = 3.0;
}

export class InheritanceEdge extends SEdge {
    public readonly targetAnchorCorrection = 2.3;
}
