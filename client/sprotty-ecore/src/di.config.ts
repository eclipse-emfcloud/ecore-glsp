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
import "@eclipse-glsp/client/css/glsp-sprotty.css";
import "sprotty/css/edit-label.css";

import {
    boundsModule,
    buttonModule,
    configureModelElement,
    configureViewerOptions,
    ConsoleLogger,
    copyPasteContextMenuModule,
    decorationModule,
    defaultGLSPModule,
    defaultModule,
    edgeLayoutModule,
    expandModule,
    exportModule,
    fadeModule,
    glspCommandPaletteModule,
    glspContextMenuModule,
    glspEditLabelModule,
    GLSPGraph,
    glspHoverModule,
    glspMouseToolModule,
    glspSelectModule,
    glspServerCopyPasteModule,
    HtmlRoot,
    HtmlRootView,
    labelEditModule,
    labelEditUiModule,
    layoutCommandsModule,
    LogLevel,
    modelHintsModule,
    modelSourceModule,
    openModule,
    paletteModule,
    PolylineEdgeView,
    RectangularNodeView,
    routingModule,
    saveModule,
    SCompartment,
    SCompartmentView,
    SEdge,
    SGraphView,
    SLabel,
    SLabelView,
    SNode,
    SRoutingHandle,
    SRoutingHandleView,
    toolFeedbackModule,
    toolsModule,
    TYPES,
    validationModule,
    viewportModule,
    zorderModule
} from "@eclipse-glsp/client/lib";
import executeCommandModule from "@eclipse-glsp/client/lib/features/execute/di.config";
import { Container, ContainerModule } from "inversify";
import { EditLabelUI } from "sprotty/lib";

import { EditLabelUIAutocomplete } from "./features/edit-label-autocomplete";
import { LabelSelectionFeedback } from "./feedback";
import {
    ArrowEdge,
    BidirectionalArrowEdge,
    CompositionEdge,
    IconAbstract,
    IconClass,
    IconDataType,
    IconEnum,
    IconInterface,
    InheritanceEdge,
    LabeledNode,
    SEditableLabel,
    SLabelNode,
    SLabelNodeAttribute,
    SLabelNodeLiteral
} from "./model";
import {
    ArrowEdgeView,
    BidirectionalEdgeView,
    ClassNodeView,
    CompositionEdgeView,
    IconView,
    InheritanceEdgeView,
    LabelNodeView
} from "./views";

export default (containerId: string): Container => {
    const classDiagramModule = new ContainerModule((bind, unbind, isBound, rebind) => {
        rebind(TYPES.ILogger).to(ConsoleLogger).inSingletonScope();
        rebind(TYPES.LogLevel).toConstantValue(LogLevel.info);
        rebind(EditLabelUI).to(EditLabelUIAutocomplete);

        const context = { bind, unbind, isBound, rebind };
        bind(TYPES.IVNodePostprocessor).to(LabelSelectionFeedback);
        configureModelElement(context, "graph", GLSPGraph, SGraphView);
        configureModelElement(context, "node:class", LabeledNode, ClassNodeView);
        configureModelElement(context, "node:enum", LabeledNode, ClassNodeView);
        configureModelElement(context, "node:datatype", LabeledNode, ClassNodeView);
        configureModelElement(context, "label:name", SEditableLabel, SLabelView);
        configureModelElement(context, "label:edge-name", SEditableLabel, SLabelView);
        configureModelElement(context, "label:edge-multiplicity", SEditableLabel, SLabelView);
        configureModelElement(context, "label:instancename", SLabelNode, LabelNodeView);
        configureModelElement(context, "node:attribute", SLabelNodeAttribute, LabelNodeView);
        configureModelElement(context, "node:enumliteral", SLabelNodeLiteral, LabelNodeView);
        configureModelElement(context, "node:operation", SNode, RectangularNodeView);
        configureModelElement(context, "label:text", SLabel, SLabelView);
        configureModelElement(context, "comp:comp", SCompartment, SCompartmentView);
        configureModelElement(context, "comp:header", SCompartment, SCompartmentView);
        configureModelElement(context, "icon:class", IconClass, IconView);
        configureModelElement(context, "icon:abstract", IconAbstract, IconView);
        configureModelElement(context, "icon:interface", IconInterface, IconView);
        configureModelElement(context, "icon:enum", IconEnum, IconView);
        configureModelElement(context, "icon:datatype", IconDataType, IconView);
        configureModelElement(context, "label:icon", SLabel, SLabelView);
        configureModelElement(context, "html", HtmlRoot, HtmlRootView);
        configureModelElement(context, "routing-point", SRoutingHandle, SRoutingHandleView);
        configureModelElement(context, "volatile-routing-point", SRoutingHandle, SRoutingHandleView);
        configureModelElement(context, "edge:reference", ArrowEdge, ArrowEdgeView);
        configureModelElement(context, "edge:bidirectional_reference", BidirectionalArrowEdge, BidirectionalEdgeView);
        configureModelElement(context, "edge:bidirectional_composition", CompositionEdge, CompositionEdgeView);
        configureModelElement(context, "edge:inheritance", InheritanceEdge, InheritanceEdgeView);
        configureModelElement(context, "edge:composition", CompositionEdge, CompositionEdgeView);
        configureModelElement(context, "edge", SEdge, PolylineEdgeView);
        configureViewerOptions(context, {
            needsClientLayout: true,
            baseDiv: containerId
        });
    });

    const container = new Container();
    container.load(decorationModule, validationModule, defaultModule, glspMouseToolModule, defaultGLSPModule, glspSelectModule, boundsModule, viewportModule, toolsModule,
        glspHoverModule, fadeModule, exportModule, expandModule, openModule, buttonModule, modelSourceModule, labelEditModule, labelEditUiModule, glspEditLabelModule,
        classDiagramModule, saveModule, executeCommandModule, toolFeedbackModule, modelHintsModule, glspContextMenuModule, glspServerCopyPasteModule,
        copyPasteContextMenuModule, glspCommandPaletteModule, paletteModule, routingModule, edgeLayoutModule, zorderModule,
        layoutCommandsModule);

    return container;

};
