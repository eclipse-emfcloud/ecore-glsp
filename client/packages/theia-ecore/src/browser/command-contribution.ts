/********************************************************************************
 * Copyright (c) 2019-2021 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
import { MenuContribution, MenuModelRegistry } from "@theia/core";
import {
    CommonMenus,
    FrontendApplication,
    open,
    OpenerService,
    QuickOpenItem,
    QuickOpenMode,
    QuickOpenOptions,
    QuickOpenService
} from "@theia/core/lib/browser";
import { Command, CommandContribution, CommandRegistry, CommandService } from "@theia/core/lib/common/command";
import { MessageService } from "@theia/core/lib/common/message-service";
import { ProgressService } from "@theia/core/lib/common/progress-service";
import { QuickOpenModel } from "@theia/core/lib/common/quick-open-model";
import { SelectionService } from "@theia/core/lib/common/selection-service";
import URI from "@theia/core/lib/common/uri";
import { UriAwareCommandHandler, UriCommandHandler } from "@theia/core/lib/common/uri-command-handler";
import { EDITOR_CONTEXT_MENU } from "@theia/editor/lib/browser";
import { FileDialogService } from "@theia/filesystem/lib/browser";
import { FileService } from "@theia/filesystem/lib/browser/file-service";
import { FileStat } from "@theia/filesystem/lib/common/files";
import {
    FileNavigatorCommands,
    NAVIGATOR_CONTEXT_MENU,
    NavigatorContextMenu
} from "@theia/navigator/lib/browser/navigator-contribution";
import { WorkspaceService } from "@theia/workspace/lib/browser";
import { inject, injectable } from "inversify";

import { EcoreModelServerClient } from "../common/ecore-model-server-client";
import { FileGenServer } from "../common/generate-protocol";

export const EXAMPLE_NAVIGATOR = [...NAVIGATOR_CONTEXT_MENU, "example"];
export const EXAMPLE_EDITOR = [...EDITOR_CONTEXT_MENU, "example"];

export const NEW_ECORE_FILE_COMMAND: Command = {
    id: "file.newEcoreFile",
    category: "File",
    label: "New Ecore Model Diagram",
    iconClass: "ecoremodelfile"
};

export const GENERATE_GENMODEL_DEFAULT: Command = {
    id: "file.generateGenModelDefault",
    category: "File",
    label: "Generate GenModel (with default Values)"
};

export const GENERATE_GENMODEL: Command = {
    id: "file.generateGenModel",
    category: "File",
    label: "Generate GenModel"
};

export const GENERATE_CODE: Command = {
    id: "file.generateCode",
    category: "File",
    label: "Generate Code"
};

@injectable()
export class EcoreCommandContribution implements CommandContribution, MenuContribution {

    @inject(FileService) protected readonly fileService: FileService;
    @inject(SelectionService) protected readonly selectionService: SelectionService;
    @inject(OpenerService) protected readonly openerService: OpenerService;
    @inject(FrontendApplication) protected readonly app: FrontendApplication;
    @inject(MessageService) protected readonly messageService: MessageService;
    @inject(FileDialogService) protected readonly fileDialogService: FileDialogService;
    @inject(WorkspaceService) protected readonly workspaceService: WorkspaceService;
    @inject(ProgressService) protected readonly progressService: ProgressService;
    @inject(QuickOpenService) protected readonly quickOpenService: QuickOpenService;
    @inject(FileGenServer) private readonly fileGenServer: FileGenServer;
    @inject(EcoreModelServerClient) protected readonly modelServerClient: EcoreModelServerClient;
    @inject(CommandService) protected readonly commandService: CommandService;

    registerCommands(registry: CommandRegistry): void {
        registry.registerCommand(NEW_ECORE_FILE_COMMAND, this.newWorkspaceRootUriAwareCommandHandler({
            execute: () => {
                let workspaceUri: URI = new URI();
                if (this.workspaceService.tryGetRoots().length) {
                    workspaceUri = this.workspaceService.tryGetRoots()[0].resource;

                    this.showInput("Name", "Enter name of Ecore Model", nameOfEcore => {
                        this.showInput("NS Prefix", "Enter NS Prefix", nsPrefix => {
                            this.showInput("NS URI", "Enter NS URI", nsURI => {
                                this.createEcoreModelDiagram(nameOfEcore, workspaceUri, nsPrefix, nsURI);
                            });
                        });
                    });
                }
            }
        }));
        registry.registerCommand(GENERATE_GENMODEL_DEFAULT, this.newWorkspaceRootUriAwareCommandHandler({
            execute: uri => this.getDirectory(uri).then(parent => {
                if (parent) {
                    const parentUri = parent.resource;

                    this.fileGenServer.generateGenModel(parentUri.path.toString(), uri.path.toString(), "", "").then(() => {
                        const extensionStart = uri.displayName.lastIndexOf(".");
                        const genmodelPath = parentUri.toString() + "/" + uri.displayName.substring(0, extensionStart) + ".genmodel";
                        const fileUri = new URI(genmodelPath);
                        open(this.openerService, fileUri);
                    });
                }
            })
        }));
        registry.registerCommand(GENERATE_GENMODEL, this.newWorkspaceRootUriAwareCommandHandler({
            execute: uri => this.getDirectory(uri).then(parent => {
                if (parent) {
                    const parentUri = parent.resource;

                    this.showInput("Name", "Custom RootPackage Name", customPackageName => {
                        this.showInput("Output folder name (relative from project root)", "folder name", folderName => {
                            this.fileGenServer.generateGenModel(parentUri.path.toString(), uri.path.toString(), customPackageName, folderName).then(() => {
                                const extensionStart = uri.displayName.lastIndexOf(".");
                                const genmodelPath = parentUri.toString() + "/" + uri.displayName.substring(0, extensionStart) + ".genmodel";
                                const fileUri = new URI(genmodelPath);
                                open(this.openerService, fileUri);
                            });
                        });
                    });
                }
            })
        }));
        registry.registerCommand(GENERATE_CODE, this.newWorkspaceRootUriAwareCommandHandler({
            execute: uri => this.getDirectory(uri).then(parent => {
                if (parent) {
                    const parentUri = parent.resource;
                    if (parentUri.parent) {
                        this.fileGenServer.generateCode(uri.path.toString(), parentUri.parent.path.toString()).then(() => {
                            open(this.openerService, uri);
                        });
                    }
                }
            })
        }));
    }

    registerMenus(menus: MenuModelRegistry): void {
        menus.registerMenuAction(CommonMenus.FILE_NEW, {
            commandId: NEW_ECORE_FILE_COMMAND.id,
            label: NEW_ECORE_FILE_COMMAND.label,
            icon: NEW_ECORE_FILE_COMMAND.iconClass,
            order: "0"
        });

        menus.registerMenuAction(NavigatorContextMenu.NAVIGATION, {
            commandId: NEW_ECORE_FILE_COMMAND.id,
            label: NEW_ECORE_FILE_COMMAND.label,
            icon: NEW_ECORE_FILE_COMMAND.iconClass,
            order: "0"
        });

    }

    protected showInput(prefix: string, hint: string, onEnter: (result: string) => void): void {
        const quickOpenModel: QuickOpenModel = {
            onType(lookFor: string, acceptor: (items: QuickOpenItem[]) => void): void {
                const dynamicItems: QuickOpenItem[] = [];
                const suffix = "Press 'Enter' to confirm or 'Escape' to cancel.";

                dynamicItems.push(new SingleStringInputOpenItem(
                    `${prefix}: '${lookFor}'  > ${suffix}`,
                    () => onEnter(lookFor),
                    (mode: QuickOpenMode) => mode === QuickOpenMode.OPEN,
                    () => false
                ));

                acceptor(dynamicItems);
            }
        };
        this.quickOpenService.open(quickOpenModel, this.getOptions(hint, false));
    }

    protected createEcoreModelDiagram(modelName: string, workspaceUri: URI, nsUri: string, nsPrefix: string): void {
        if (modelName) {
            this.modelServerClient.createEcoreResources(modelName, nsUri, nsPrefix).then(() => {
                this.quickOpenService.hide();
                const modelUri = new URI(workspaceUri.path.toString() + `/${modelName}/model/${modelName}.uml`);
                this.commandService.executeCommand(FileNavigatorCommands.REFRESH_NAVIGATOR.id);
                this.openerService.getOpener(modelUri).then(openHandler => {
                    openHandler.open(modelUri);
                    this.commandService.executeCommand(FileNavigatorCommands.REVEAL_IN_NAVIGATOR.id);
                });
            });
        }
    }

    protected withProgress<T>(task: () => Promise<T>): Promise<T> {
        return this.progressService.withProgress("", "scm", task);
    }

    protected newUriAwareCommandHandler(handler: UriCommandHandler<URI>): UriAwareCommandHandler<URI> {
        return new UriAwareCommandHandler(this.selectionService, handler);
    }

    protected newMultiUriAwareCommandHandler(handler: UriCommandHandler<URI[]>): UriAwareCommandHandler<URI[]> {
        return new UriAwareCommandHandler(this.selectionService, handler, { multi: true });
    }

    protected newWorkspaceRootUriAwareCommandHandler(handler: UriCommandHandler<URI>): WorkspaceRootUriAwareCommandHandler {
        return new WorkspaceRootUriAwareCommandHandler(this.workspaceService, this.selectionService, handler);
    }

    protected async getDirectory(candidate: URI): Promise<FileStat | undefined> {
        const stat = await this.fileService.resolve(candidate);
        if (stat && stat.isDirectory) {
            return stat;
        }
        return this.getParent(candidate);
    }

    protected getParent(candidate: URI): Promise<FileStat | undefined> {
        return this.fileService.resolve(candidate.parent);
    }

    // eslint-disable-next-line @typescript-eslint/no-empty-function
    private getOptions(placeholder: string, fuzzyMatchLabel = true, onClose: (canceled: boolean) => void = () => { }): QuickOpenOptions {
        return QuickOpenOptions.resolve({
            placeholder,
            fuzzyMatchLabel,
            fuzzySort: false,
            onClose
        });
    }
}

export class WorkspaceRootUriAwareCommandHandler extends UriAwareCommandHandler<URI> {

    constructor(
        protected readonly workspaceService: WorkspaceService,
        protected readonly selectionService: SelectionService,
        protected readonly handler: UriCommandHandler<URI>
    ) {
        super(selectionService, handler);
    }

    public isEnabled(...args: any[]): boolean {
        return super.isEnabled(...args) && !!this.workspaceService.tryGetRoots().length;
    }

    public isVisible(...args: any[]): boolean {
        return super.isVisible(...args) && !!this.workspaceService.tryGetRoots().length;
    }

    protected getUri(...args: any[]): URI | undefined {
        const uri = super.getUri(...args);
        // If the URI is available, return it immediately.
        if (uri) {
            return uri;
        }
        // Return the first root if available.
        if (this.workspaceService.tryGetRoots().length) {
            return this.workspaceService.tryGetRoots()[0].resource;
        }
        return undefined;
    }
}

class SingleStringInputOpenItem extends QuickOpenItem {

    constructor(
        private readonly label: string,
        // eslint-disable-next-line @typescript-eslint/no-empty-function
        private readonly execute: (item: QuickOpenItem) => void = () => { },
        private readonly canRun: (mode: QuickOpenMode) => boolean = mode => mode === QuickOpenMode.OPEN,
        private readonly canClose: (mode: QuickOpenMode) => boolean = mode => true) {

        super();
    }

    getLabel(): string {
        return this.label;
    }

    run(mode: QuickOpenMode): boolean {
        if (!this.canRun(mode)) {
            return false;
        }
        this.execute(this);
        return this.canClose(mode);
    }

}
