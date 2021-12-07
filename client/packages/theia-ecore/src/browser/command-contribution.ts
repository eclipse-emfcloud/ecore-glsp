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
    LabelProvider,
    open,
    OpenerService,
    QuickInputService
} from "@theia/core/lib/browser";
import { Command, CommandContribution, CommandRegistry, CommandService } from "@theia/core/lib/common/command";
import { MessageService } from "@theia/core/lib/common/message-service";
import { ProgressService } from "@theia/core/lib/common/progress-service";
import { SelectionService } from "@theia/core/lib/common/selection-service";
import URI from "@theia/core/lib/common/uri";
import { UriAwareCommandHandler, UriCommandHandler } from "@theia/core/lib/common/uri-command-handler";
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

export const ECORE_FILE_EXTENSION = ".ecore";
export const ENOTATION_EXTENSION = ".enotation";
export const GENMODEL_EXTENSION = ".genmodel";

export const GENMODEL_NAVIGATOR_CONTEXT_MENU = [...NAVIGATOR_CONTEXT_MENU, "2_genmodel"];

export const NEW_ECORE_FILE_COMMAND: Command = {
    id: "file.newEcoreFile",
    category: "File",
    label: "New Ecore Model Diagram",
    iconClass: "newecorefile ecore-glsp-icon"
};

export const GENERATE_GENMODEL: Command = {
    id: "file.generateGenModel",
    category: "File",
    label: "Generate EMF Generator Model",
    iconClass: "genmodelfile ecore-glsp-icon"
};

export const GENERATE_GENMODEL_WIZARD: Command = {
    id: "file.generateGenModelViaWizard",
    category: "File",
    label: "Generate EMF Generator Model...",
    iconClass: "genmodelfile ecore-glsp-icon"
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
    @inject(QuickInputService) protected readonly quickInputService: QuickInputService;
    @inject(FileGenServer) private readonly fileGenServer: FileGenServer;
    @inject(EcoreModelServerClient) protected readonly modelServerClient: EcoreModelServerClient;
    @inject(CommandService) protected readonly commandService: CommandService;
    @inject(LabelProvider) private readonly labelProvider: LabelProvider;

    registerCommands(registry: CommandRegistry): void {
        registry.registerCommand(NEW_ECORE_FILE_COMMAND, this.newWorkspaceRootUriAwareCommandHandler({
            execute: async () => {
                let workspaceUri: URI = new URI();
                if (this.workspaceService.tryGetRoots().length) {
                    workspaceUri = this.workspaceService.tryGetRoots()[0].resource;

                    this.showInput("Name of Ecore Model", "Enter name of Ecore Model").then(nameOfEcore => {
                        if (nameOfEcore) {
                            this.showInput("NS Prefix", "Enter NS Prefix").then(nsPrefix => {
                                if (nsPrefix) {
                                    this.showInput("NS URI", "Enter NS URI").then(nsURI => {
                                        if (nsURI) {
                                            this.createEcoreModelDiagram(nameOfEcore, workspaceUri, nsPrefix, nsURI);
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        }));
        registry.registerCommand(GENERATE_GENMODEL, this.newWorkspaceRootUriAwareCommandHandler({
            isVisible: (uri: URI) => uri.path.ext === ECORE_FILE_EXTENSION,
            isEnabled: (uri: URI) => uri.path.ext === ECORE_FILE_EXTENSION,
            execute: (uri: URI) => this.getDirectory(uri).then(parent => {
                if (parent) {
                    const parentUri = parent.resource;

                    this.fileGenServer.generateGenModel(parentUri.path.toString(), uri.path.toString(), "", "").then(() => {
                        const extensionStart = this.labelProvider.getName(uri).lastIndexOf(".");
                        const genmodelPath = parentUri.toString() + "/" + this.labelProvider.getName(uri).substring(0, extensionStart) + GENMODEL_EXTENSION;
                        const fileUri = new URI(genmodelPath);
                        open(this.openerService, fileUri);
                    });
                }
            })
        }));
        registry.registerCommand(GENERATE_GENMODEL_WIZARD, this.newWorkspaceRootUriAwareCommandHandler({
            isVisible: (uri: URI) => uri.path.ext === ECORE_FILE_EXTENSION,
            isEnabled: (uri: URI) => uri.path.ext === ECORE_FILE_EXTENSION,
            execute: (uri: URI) => this.getDirectory(uri).then(async parent => {
                if (parent) {
                    const parentUri = parent.resource;

                    this.showInput("Root package name: the java package in which sources will be generated (e.g. my.company.project)", "Root package name")
                        .then(customPackageName => {
                            if (customPackageName) {
                                this.showInput("Output folder name (relative to the workspace root)", "Output folder name",
                                    async (input: string) => !input ? "Please enter a valid path (e.g. 'src' or 'myjavaproject/src'" : undefined
                                ).then(folderName => {
                                    if (folderName) {
                                        this.fileGenServer.generateGenModel(parentUri.path.toString(), uri.path.toString(), customPackageName, folderName || "").then(() => {
                                            const extensionStart = this.labelProvider.getName(uri).lastIndexOf(".");
                                            const genmodelPath = parentUri.toString() + "/" + this.labelProvider.getName(uri).substring(0, extensionStart) + GENMODEL_EXTENSION;
                                            const fileUri = new URI(genmodelPath);
                                            open(this.openerService, fileUri);
                                        });
                                    }
                                });
                            }
                        });
                }
            })
        }));
        registry.registerCommand(GENERATE_CODE, this.newWorkspaceRootUriAwareCommandHandler({
            isVisible: (uri: URI) => uri.path.ext === GENMODEL_EXTENSION,
            isEnabled: (uri: URI) => uri.path.ext === GENMODEL_EXTENSION,
            execute: (uri: URI) => {
                const wsPath = this.workspaceService.tryGetRoots()[0].resource;
                this.fileGenServer.generateCode(uri.path.toString(), wsPath.toString()).then(result => console.log("Codegen result: " + result));
            }
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

        menus.registerMenuAction(GENMODEL_NAVIGATOR_CONTEXT_MENU, {
            commandId: GENERATE_GENMODEL.id,
            label: GENERATE_GENMODEL.label,
            icon: GENERATE_GENMODEL.iconClass,
            order: "a0"
        });

        menus.registerMenuAction(GENMODEL_NAVIGATOR_CONTEXT_MENU, {
            commandId: GENERATE_GENMODEL_WIZARD.id,
            label: GENERATE_GENMODEL_WIZARD.label,
            icon: GENERATE_GENMODEL_WIZARD.iconClass,
            order: "a1"
        });

        menus.registerMenuAction(GENMODEL_NAVIGATOR_CONTEXT_MENU, {
            commandId: GENERATE_CODE.id,
            label: GENERATE_CODE.label,
            icon: GENERATE_CODE.iconClass,
            order: "a2"
        });

    }

    protected async showInput(prefix: string, hint: string, inputCheck?: (input: string) => Promise<string | undefined>): Promise<string | undefined> {
        return this.quickInputService.input({
            prompt: prefix,
            placeHolder: hint,
            ignoreFocusLost: true,
            validateInput: async input => {
                if (inputCheck) {
                    return inputCheck(input);
                }
                return !input ? `Please enter a valid string for '${prefix}'` : undefined;
            }
        });
    }

    protected createEcoreModelDiagram(modelName: string, workspaceUri: URI, nsUri: string, nsPrefix: string): void {
        if (modelName) {
            this.modelServerClient.createEcoreResources(modelName, nsUri, nsPrefix).then(() => {
                this.quickInputService.hide();
                const modelUri = new URI(workspaceUri.path.toString() + `/${modelName}/model/${modelName}.ecore`);
                this.commandService.executeCommand(FileNavigatorCommands.REFRESH_NAVIGATOR.id);
                this.openerService.getOpener(modelUri).then(openHandler => {
                    openHandler.open(modelUri);
                    this.commandService.executeCommand(FileNavigatorCommands.REVEAL_IN_NAVIGATOR.id);
                });
            });
        }
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
