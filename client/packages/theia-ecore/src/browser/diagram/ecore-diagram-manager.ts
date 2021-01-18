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
import {
    EnableToolPaletteAction,
    GLSPActionDispatcher,
    InitializeClientSessionAction,
    RequestTypeHintsAction,
    SelectAction,
    SelectionResult,
    SetEditModeAction
} from "@eclipse-glsp/client";
import {
    GLSPDiagramManager,
    GLSPDiagramWidget,
    GLSPNotificationManager,
    GLSPTheiaDiagramServer,
    GLSPTheiaSprottyConnector,
    GLSPWidgetOpenerOptions,
    GLSPWidgetOptions
} from "@eclipse-glsp/theia-integration/lib/browser";
import { Message } from "@phosphor/messaging/lib";
import { MessageService, SelectionService } from "@theia/core";
import { WidgetManager } from "@theia/core/lib/browser";
import { Emitter, Event } from "@theia/core/lib/common";
import URI from "@theia/core/lib/common/uri";
import { EditorManager } from "@theia/editor/lib/browser";
import { WorkspaceService } from "@theia/workspace/lib/browser";
import { inject, injectable, postConstruct } from "inversify";
import { DiagramServer, GetSelectionAction, ModelSource, RequestModelAction, TYPES } from "sprotty";
import { DiagramWidget, DiagramWidgetOptions, TheiaFileSaver } from "sprotty-theia";

import { EcoreLanguage } from "../../common/ecore-language";
import { EcoreGLSPDiagramClient } from "./ecore-glsp-diagram-client";

export interface EcoreDiagramWidgetOptions extends DiagramWidgetOptions, GLSPWidgetOptions {
    workspaceRoot: string;
}

@injectable()
export class EcoreDiagramManager extends GLSPDiagramManager {
    readonly diagramType = EcoreLanguage.DiagramType;
    readonly iconClass = "fa fa-project-diagram";
    readonly label = EcoreLanguage.Label + " Editor";

    private _diagramConnector: GLSPTheiaSprottyConnector;
    private workspaceRoot: string;

    private _currentDiagramWidget: EcoreDiagramWidget | undefined;

    @inject(SelectionService) protected readonly selectionService: SelectionService;

    constructor(
        @inject(EcoreGLSPDiagramClient) diagramClient: EcoreGLSPDiagramClient,
        @inject(TheiaFileSaver) fileSaver: TheiaFileSaver,
        @inject(WidgetManager) widgetManager: WidgetManager,
        @inject(EditorManager) editorManager: EditorManager,
        @inject(MessageService) messageService: MessageService,
        @inject(GLSPNotificationManager) notificationManager: GLSPNotificationManager,
        @inject(WorkspaceService) workspaceService: WorkspaceService) {
        super();
        this._diagramConnector = new GLSPTheiaSprottyConnector({
            diagramClient,
            fileSaver, editorManager, widgetManager, diagramManager: this, messageService, notificationManager
        });
        workspaceService.roots.then(roots => this.workspaceRoot = roots[0].uri);
    }

    get fileExtensions(): string[] {
        return [EcoreLanguage.FileExtension];
    }

    get diagramConnector(): GLSPTheiaSprottyConnector {
        return this._diagramConnector;
    }

    async createWidget(options?: any): Promise<DiagramWidget> {
        if (DiagramWidgetOptions.is(options)) {
            const clientId = this.createClientId();
            const widgetId = this.createWidgetId(options);
            const config = this.getDiagramConfiguration(options);
            const diContainer = config.createContainer(clientId);
            return new EcoreDiagramWidget(options, widgetId, diContainer, this.editorPreferences, this.diagramConnector);
        }
        throw Error("DiagramWidgetFactory needs DiagramWidgetOptions but got " + JSON.stringify(options));
    }

    @postConstruct()
    protected init(): void {
        super.init();
        this.shell.onDidChangeActiveWidget(() => this.updateCurrentDiagramWidget());
        this.onCreated(widget => widget.disposed.connect(() => this.updateCurrentDiagramWidget()));
    }

    protected createWidgetOptions(uri: URI, options?: GLSPWidgetOpenerOptions): EcoreDiagramWidgetOptions {
        return {
            ...super.createWidgetOptions(uri, options),
            workspaceRoot: this.workspaceRoot
        } as EcoreDiagramWidgetOptions;
    }

    get currentDiagramWidget(): EcoreDiagramWidget | undefined {
        return this._currentDiagramWidget;
    }

    protected setCurrentDiagramWidget(current: EcoreDiagramWidget | undefined): void {
        if (this.currentDiagramWidget !== current) {
            this._currentDiagramWidget = current;
            if (this._currentDiagramWidget !== undefined) {
                this._currentDiagramWidget.updateGlobalSelection();
                this._currentDiagramWidget.onWidgetClosed(() => {
                    this.clearGlobalSelection();
                });
            }
        }
    }
    protected updateCurrentDiagramWidget(): void {
        const currentWidget = this.shell.currentWidget;
        if (currentWidget instanceof EcoreDiagramWidget) {
            this.setCurrentDiagramWidget(currentWidget);
        } else if (!this.currentDiagramWidget || !this.currentDiagramWidget.isVisible) {
            this.setCurrentDiagramWidget(undefined);
        }
    }

    protected clearGlobalSelection(): void {
        console.log("clearGlobalSelection");
        this.selectionService.selection = new Object();
    }
}

export class EcoreDiagramWidget extends GLSPDiagramWidget {

    protected readonly onWidgetClosedEmitter = new Emitter<undefined>();
    get onWidgetClosed(): Event<undefined> {
        return this.onWidgetClosedEmitter.event;
    }

    options: EcoreDiagramWidgetOptions;

    protected initializeSprotty(): void {
        const modelSource = this.diContainer.get<ModelSource>(TYPES.ModelSource);
        if (modelSource instanceof DiagramServer) {
            modelSource.clientId = this.id;
        }
        if (modelSource instanceof GLSPTheiaDiagramServer && this.connector) {
            this.connector.connect(modelSource);
        }

        this.disposed.connect(() => {
            if (modelSource instanceof GLSPTheiaDiagramServer && this.connector) {
                this.connector.disconnect(modelSource);
            }
        });

        this.actionDispatcher.dispatch(new InitializeClientSessionAction(this.widgetId));
        this.actionDispatcher.dispatch(new RequestModelAction({
            sourceUri: this.uri.path.toString(),
            needsClientLayout: `${this.viewerOptions.needsClientLayout}`,
            ... this.options
        }));

        this.actionDispatcher.dispatch(new RequestTypeHintsAction(this.options.diagramType));
        this.actionDispatcher.dispatch(new EnableToolPaletteAction());
        this.actionDispatcher.dispatch(new SetEditModeAction(this.options.editMode));
    }

    getGlspActionDispatcher(): GLSPActionDispatcher {
        return this.diContainer.get<GLSPActionDispatcher>(TYPES.IActionDispatcher);
    }

    protected onCloseRequest(msg: Message): void {
        super.onCloseRequest(msg);
        console.log("diagramwidget onclose");
        this.onWidgetClosedEmitter.fire(undefined);
        this.clearGlobalSelection();
    }

    private async getSelectedElementIds(): Promise<string[]> {
        const selectedElementIds = await this.actionDispatcher.request(GetSelectionAction.create()).then((selection: SelectionResult) => {
            if (selection.selectedElementsIDs) {
                return selection.selectedElementsIDs;
            }
            return [];
        });
        return selectedElementIds;
    }

    async updateGlobalSelection(): Promise<void> {
        this.getSelectedElementIds().then((prevSelection: string[]) => this.actionDispatcher.dispatch(new SelectAction(prevSelection)));
    }

    protected async clearGlobalSelection(): Promise<void> {
        this.actionDispatcher.dispatch(new SelectAction());
    }
}
