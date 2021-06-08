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
import { ApplicationShell, NavigatableWidget, SaveableWidget, Widget } from "@theia/core/lib/browser";
import URI from "@theia/core/lib/common/uri";
import { FileDeleteOptions } from "@theia/filesystem/lib/common/files";
import { WorkspaceDeleteHandler } from "@theia/workspace/lib/browser/workspace-delete-handler";
import { inject, injectable } from "inversify";

import { EcoreLanguage } from "../common/ecore-language";
import { EcoreModelServerClient } from "../common/ecore-model-server-client";

@injectable()
export class EcoreWorkspaceDeleteHandler extends WorkspaceDeleteHandler {

    @inject(EcoreModelServerClient) protected readonly modelServerClient: EcoreModelServerClient;

    protected readonly ECORE_EXTENSION = ".ecore";
    protected readonly ENOTATION_EXTENSION = ".enotation";

    /**
     * Get which URI are presently dirty.
     *
     * @param uris URIs of selected resources.
     * @returns An array of dirty URI.
     */
    protected getDirty(uris: URI[]): URI[] {
        const dirty = new Array<URI>();
        const widgets = SaveableWidget.getDirty(this.shell.widgets);
        let next = widgets.next();
        while (next.value) {
            if (NavigatableWidget.is(next.value)) {
                const uri = (next.value as NavigatableWidget).getResourceUri();
                if (uri) {
                    dirty.push(uri);
                }
            }
            next = widgets.next();
        }
        return dirty;
    }

    /**
     * Get the dialog confirmation message for deletion.
     *
     * @param uris URIs of selected resources.
     */
    protected getConfirmMessage(uris: URI[]): string | HTMLElement {
        const dirty = this.getDirty(uris);
        if (dirty.length) {
            const unsavedBaseText = uris.length === 1
                ? "Do you really want to delete the following file with unsaved changes?"
                : `Do you really want to delete the following ${dirty.length} files  with unsaved changes?`;
            return this.createMsgContainer(unsavedBaseText, dirty);
        }
        const baseText = uris.length === 1
            ? "Do you really want to delete the following file?"
            : `Do you really want to delete the following ${uris.length} files?`;
        return this.createMsgContainer(baseText, uris);
    }

    protected createMsgContainer(baseText: string, uris: URI[]): HTMLElement {
        const messageContainer = document.createElement("div");
        messageContainer.textContent = baseText;
        const list = document.createElement("ul");
        list.style.listStyleType = "none";
        for (const uri of uris) {
            const listItem = document.createElement("li");
            let content = uri.path.base;
            content = content.concat(this.additionalEcoreMsg(uri));
            listItem.textContent = content;
            list.appendChild(listItem);
        }
        messageContainer.appendChild(list);
        return messageContainer;
    }

    protected additionalEcoreMsg(resourceUri: URI): string {
        if (this.isEcoreFile(resourceUri)) {
            return " (This will also delete the Ecore resource *.enotation in the same directory)";
        } else if (resourceUri.path.ext === "") {
            return " (This will also delete any contained Ecore resources *.ecore/*.enotation)";
        }
        return "";
    }

    /**
     * Perform deletion of a given URI.
     *
     * @param resourceUri URI of selected resource.
     */
    protected async delete(resourceUri: URI, options: FileDeleteOptions): Promise<void> {
        try {
            if (this.isEcoreFile(resourceUri)) {
                // delete enotation file from file system as well
                const enotationUri = this.getEnotationFileUri(resourceUri);
                await Promise.all([
                    this.closeWithoutSaving(resourceUri),
                    this.closeWithoutSaving(enotationUri),
                    this.fileService.delete(resourceUri, options),
                    this.fileService.delete(enotationUri, options)
                ]);
                await this.modelServerClient.deleteEcoreResources(resourceUri.toString());
            } else if (this.isEnotationFile(resourceUri)) {
                const ecoreResourceUri = this.getEcoreFileUri(resourceUri);
                await Promise.all([
                    this.closeEcoreDiagramWidget(this.shell, ecoreResourceUri),
                    this.closeWithoutSaving(resourceUri),
                    this.fileService.delete(resourceUri, options)
                ]);
                await this.modelServerClient.deleteEnotationResource(resourceUri.toString());
            } else {
                if (await this.isEcoreDirectory(resourceUri)) {
                    const ecoreResourceUri = await this.getContainedEcoreFileUri(resourceUri);
                    const enotationUri = this.getEnotationFileUri(ecoreResourceUri!);
                    await Promise.all([
                        this.closeEcoreDiagramWidget(this.shell, ecoreResourceUri!),
                        this.closeWithoutSaving(enotationUri),
                        this.fileService.delete(resourceUri, options)
                    ]);
                    await this.modelServerClient.deleteEcoreResources(ecoreResourceUri!.toString());
                } else {
                    await Promise.all([
                        this.closeWithoutSaving(resourceUri),
                        this.fileService.delete(resourceUri, options)
                    ]);
                }
            }
        } catch (e) {
            console.error(e);
        }
    }

    protected closeEcoreDiagramWidget(applicationShell: ApplicationShell, resourceUri: URI): void {
        const diagramWidget = applicationShell.widgets.find((widget: Widget) => widget.id === `${EcoreLanguage.DiagramType}:${resourceUri}`);
        if (diagramWidget) {
            diagramWidget.close();
        }
    }

    protected isEcoreFile(resourceUri: URI): boolean {
        return resourceUri.path.ext === this.ECORE_EXTENSION;
    }

    protected getEcoreFileUri(enotationResourceUri: URI): URI {
        return new URI(enotationResourceUri.toString().replace(this.ENOTATION_EXTENSION, this.ECORE_EXTENSION));
    }

    protected isEnotationFile(resourceUri: URI): boolean {
        return resourceUri.path.ext === this.ENOTATION_EXTENSION;
    }

    protected getEnotationFileUri(ecoreResourceUri: URI): URI {
        return new URI(ecoreResourceUri.toString().replace(this.ECORE_EXTENSION, this.ENOTATION_EXTENSION));
    }

    protected async isEcoreDirectory(resourceUri: URI): Promise<boolean> {
        const fileStat = await this.fileService.resolve(resourceUri);
        const ecoreResourceUri = await this.getContainedEcoreFileUri(resourceUri);
        return fileStat.isDirectory && ecoreResourceUri !== undefined;
    }

    protected async getContainedEcoreFileUri(resourceUri: URI): Promise<URI | undefined> {
        const fileStat = await this.fileService.resolve(resourceUri);
        if (fileStat.isFile && this.isEcoreFile(fileStat.resource)) {
            return fileStat.resource;
        } else if (fileStat.isDirectory && fileStat.children) {
            for (const child of fileStat.children) {
                if (child.isFile && this.isEcoreFile(child.resource)) {
                    return child.resource;
                } else if (child.isDirectory) {
                    const childResourceUri = this.getContainedEcoreFileUri(child.resource);
                    if (childResourceUri) {
                        return childResourceUri;
                    }
                }
            }
        }
        return undefined;
    }

}

