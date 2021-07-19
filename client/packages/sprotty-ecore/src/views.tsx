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
/** @jsx svg */
/* eslint-disable react/jsx-key */
import { injectable } from "inversify";
import { svg } from "snabbdom-jsx";
import { VNode } from "snabbdom/vnode";
import {
    getSubType,
    IView,
    Point,
    PolylineEdgeView,
    RectangularNodeView,
    RenderingContext,
    SEdge,
    setAttr,
    SLabelView,
    toDegrees
} from "sprotty/lib";

import { Icon, LabeledNode, SLabelNode } from "./model";

@injectable()
export class ClassNodeView extends RectangularNodeView {
    render(node: LabeledNode, context: RenderingContext): VNode {

        const rhombStr = "M 0,38  L " + node.bounds.width + ",38";

        return <g class-node={true}>
            <defs>
                <filter id="dropShadow">
                    <feDropShadow dx="0.5" dy="0.5" stdDeviation="0.4" />
                </filter>
            </defs>

            <rect class-sprotty-node={true} class-selected={node.selected} class-mouseover={node.hoverFeedback}
                x={0} y={0} rx={6} ry={6}
                width={Math.max(0, node.bounds.width)} height={Math.max(0, node.bounds.height)} />
            {context.renderChildren(node)}
            {(node.children[1] && node.children[1].children.length > 0) ?
                <path class-sprotty-edge={true} d={rhombStr}></path> : ""}
        </g>;
    }
}

@injectable()
export class IconView implements IView {
    render(element: Icon, context: RenderingContext): VNode {
        // eslint-disable-next-line @typescript-eslint/no-var-requires
        const image = require("../images/" + element.iconImageName);

        return <g>
            <image class-sprotty-icon={true} href={image} x={-2} y={-1} width={20} height={20}></image>
            {context.renderChildren(element)}
        </g>;
    }
}

@injectable()
export class ArrowEdgeView extends PolylineEdgeView {
    protected renderAdditionals(edge: SEdge, segments: Point[], context: RenderingContext): VNode[] {
        const p1 = segments[segments.length - 2];
        const p2 = segments[segments.length - 1];
        return [
            <path class-sprotty-edge={true} d="M 10,-4 L 0,0 L 10,4"
                transform={`rotate(${angle(p2, p1)} ${p2.x} ${p2.y}) translate(${p2.x} ${p2.y})`} />
        ];
    }
}

@injectable()
export class BidirectionalEdgeView extends ArrowEdgeView {
    protected renderAdditionals(edge: SEdge, segments: Point[], context: RenderingContext): VNode[] {
        const source1 = segments[0];
        const source2 = segments[1];
        const target1 = segments[segments.length - 2];
        const target2 = segments[segments.length - 1];
        return [
            <path class-sprotty-edge={true} d="M 10,-4 L 0,0 L 10,4"
                transform={`rotate(${angle(target2, target1)} ${target2.x} ${target2.y}) translate(${target2.x} ${target2.y})`} />,
            <path class-sprotty-edge={true} d="M 10,-4 L 0,0 L 10,4"
                transform={`rotate(${angle(source1, source2)} ${source1.x} ${source1.y}) translate(${source1.x} ${source1.y})`} />
        ];
    }
}

@injectable()
export class InheritanceEdgeView extends ArrowEdgeView {
    protected renderAdditionals(edge: SEdge, segments: Point[], context: RenderingContext): VNode[] {
        const p1 = segments[segments.length - 2];
        const p2 = segments[segments.length - 1];
        return [
            <path class-sprotty-edge={true} class-triangle={true} d="M 10,-8 L 0,0 L 10,8 Z" class-inheritance={true}
                transform={`rotate(${angle(p2, p1)} ${p2.x} ${p2.y}) translate(${p2.x} ${p2.y})`} />
        ];
    }
}

@injectable()
abstract class DiamondEdgeView extends PolylineEdgeView {
    protected renderAdditionals(edge: SEdge, segments: Point[], context: RenderingContext): VNode[] {
        const p1 = segments[0];
        const p2 = segments[1];
        const r = 6;
        const rhombStr = "M 0,0 l" + r + "," + (r / 2) + " l" + r + ",-" + (r / 2) + " l-" + r + ",-" + (r / 2) + " l-" + r + "," + (r / 2) + " Z";
        const firstEdgeAngle = angle(p1, p2);
        return [
            <path class-sprotty-edge={true} class-diamond={true} class-composition={this.isComposition()} d={rhombStr}
                transform={`rotate(${firstEdgeAngle} ${p1.x} ${p1.y}) translate(${p1.x} ${p1.y})`} />
        ];
    }
    protected isComposition(): boolean {
        return false;
    }
    protected isAggregation(): boolean {
        return false;
    }
}

@injectable()
export class CompositionEdgeView extends DiamondEdgeView {
    protected isComposition(): boolean {
        return true;
    }
}

@injectable()
export class AggregationEdgeView extends DiamondEdgeView {
    protected isAggregation(): boolean {
        return true;
    }
}

@injectable()
export class LabelNodeView extends SLabelView {
    render(labelNode: Readonly<SLabelNode>, context: RenderingContext): VNode {
        let image;
        if (labelNode.imageName) {
            image = require("../images/" + labelNode.imageName);
        }

        const vnode = (
            <g
                class-selected={labelNode.selected}
                class-mouseover={labelNode.hoverFeedback}
                class-sprotty-label-node={true}
            >
                {!!image && <image class-sprotty-icon={true} href={image} y={-5} width={13} height={8}></image>}
                <text class-sprotty-label={true} x={image ? 20 : 0}>{labelNode.text}</text>
            </g>
        );

        const subType = getSubType(labelNode);
        if (subType) {
            setAttr(vnode, "class", subType);
        }
        return vnode;
    }
}

@injectable()
export class LabelNodeWithOccurrenceView extends SLabelView {
    render(labelNode: Readonly<SLabelNode>, context: RenderingContext): VNode {
        let image;
        if (labelNode.imageName) {
            image = require("../images/" + labelNode.imageName);
        }

        const isOperation = labelNode.imageName === "EOperation.svg";

        let occurrenceString = undefined;
        let exceptionsString = undefined;
        let singleOccurrence = false;
        if (labelNode.cssClasses) {

            const occurrenceClass = labelNode.cssClasses[0];
            switch (occurrenceClass) {
                case "eoccurrencezero": occurrenceString = "0"; singleOccurrence = true; break;
                case "eoccurrencezerotoone": occurrenceString = "0..1"; break;
                case "eoccurrencezeroton": occurrenceString = "0..n"; break;
                case "eoccurrencezerotounbounded": occurrenceString = "0..*"; break;
                case "eoccurrencezerotounspecified": occurrenceString = "0..?"; break;
                case "eoccurrenceone": occurrenceString = "1"; singleOccurrence = true; break;
                case "eoccurrenceoneton": occurrenceString = "1..n"; break;
                case "eoccurrenceonetounbounded": occurrenceString = "1..*"; break;
                case "eoccurrenceoneunspecified": occurrenceString = "1..?"; break;
                case "eoccurrencen": occurrenceString = "n"; singleOccurrence = true; break;
                case "eoccurrencentom": occurrenceString = "n..m"; break;
                case "eoccurrencentounbounded": occurrenceString = "n..*"; break;
                case "eoccurrencentounspecified": occurrenceString = "n..?"; break;
            }

            const exceptionsClass = labelNode.cssClasses[1];
            if (isOperation && exceptionsClass && exceptionsClass !== "none") {
                exceptionsString = ` throws ${exceptionsClass.split("-").join(", ")}`;
            } else {
                exceptionsString = undefined;
            }
        }

        const vnode = (
            <g
                class-selected={labelNode.selected}
                class-mouseover={labelNode.hoverFeedback}
                class-sprotty-label-node={true}
            >
                {!!image && <image class-sprotty-icon={true} href={image} y={isOperation ? -8 : -6} width={13} height={isOperation ? 10 : 8}></image>}
                {!!occurrenceString &&
                    <text class-sprotty-label={true} class-occurrence={true} x={singleOccurrence ? 4 : 0} y={8}>{occurrenceString}</text>}
                <text class-sprotty-label={true} x={image ? 25 : 0}>{`${labelNode.text}${isOperation && exceptionsString ? " *" : ""}`}</text>
                {isOperation && exceptionsString && <title>{exceptionsString}</title>}
            </g >
        );

        const subType = getSubType(labelNode);
        if (subType) {
            setAttr(vnode, "class", subType);
        }
        return vnode;
    }
}

export function angle(x0: Point, x1: Point): number {
    return toDegrees(Math.atan2(x1.y - x0.y, x1.x - x0.x));
}
