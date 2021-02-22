/********************************************************************************
 * Copyright (c) 2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ********************************************************************************/
import {
    ModelServerClient,
    ModelServerCommand,
    ModelServerCommandUtil,
    ModelServerCompoundCommand,
    ModelServerReferenceDescription,
    ModelServerSubscriptionService
} from "@eclipse-emfcloud/modelserver-theia/lib/common";
import { isGlspSelection } from "@eclipse-emfcloud/theia-ecore/lib/browser/selection-forwarder";
import { JsonFormsCore } from "@jsonforms/core";
import { JsonFormsPropertyViewWidgetProvider } from "@ndoschek/jsonforms-property-view";
import URI from "@theia/core/lib/common/uri";
import { WorkspaceService } from "@theia/workspace/lib/browser";
import { inject, injectable, postConstruct } from "inversify";
import { debounce, isEqual, isObject, transform } from "lodash";

import { ModelServerJsonFormsPropertyViewWidget } from "./widget";

@injectable()
export class EcoreGlspPropertyViewWidgetProvider extends JsonFormsPropertyViewWidgetProvider {

    @inject(WorkspaceService) readonly workspaceService: WorkspaceService;
    @inject(ModelServerClient) protected readonly modelServerClient: ModelServerClient;
    @inject(ModelServerSubscriptionService) protected readonly subscriptionService: ModelServerSubscriptionService;

    protected currentPropertiesCore: JsonFormsCore;
    private _currentModelUri: string;

    @postConstruct()
    init(): void {
        this.currentPropertiesCore = this.jsonFormsWidget.currentJsonFormsCore;
        this.jsonFormsWidget.onChange(
            debounce(jsonFormsData => {
                this.handleChanges(jsonFormsData);
            }, 200)
        );

        this.subscriptionService.onIncrementalUpdateListener(incrementalUpdate => {
            if (this.jsonFormsWidget instanceof ModelServerJsonFormsPropertyViewWidget) {
                this.updateWidgetData(incrementalUpdate.data);
            }
        });
    }

    protected get currentModelUri(): string {
        return this._currentModelUri;
    }

    protected set currentModelUri(modelUri: string) {
        this._currentModelUri = modelUri;
    }

    protected getRelativeModelUri(sourceUri: string): string {
        const workspaceUri = this.workspaceService.getWorkspaceRootUri(new URI(sourceUri));
        if (workspaceUri) {
            const workspaceString = workspaceUri.toString().replace("file://", "");
            const rootUriLength = workspaceString.length;
            return sourceUri.substring(rootUriLength + 1);
        }
        return "";
    }

    updateWidget(selection: any): void {
        this.getPropertiesService(selection).then(service => {
            if (this.currentModelUri !== this.getRelativeModelUri(selection.sourceUri)) {
                if (this.currentModelUri) {
                    this.modelServerClient.unsubscribe(this.currentModelUri);
                }
                this.currentModelUri = this.getRelativeModelUri(selection.sourceUri);
                this.modelServerClient.subscribe(this.currentModelUri);
            }
            this.jsonFormsWidget.updatePropertyViewContent(service, selection);
        });
    }

    protected updateWidgetData(command: ModelServerCommand | ModelServerCompoundCommand): void {
        const semanticUri = this.jsonFormsWidget.currentJsonFormsCore.data.semanticUri;
        if (command.type) {
            this.updateViaCommand(command as ModelServerCommand, semanticUri);
        } else { // #FIXME: command.type='compound' type not set right now!
            (command as ModelServerCompoundCommand).commands.forEach((cmd: ModelServerCommand | ModelServerCompoundCommand) => {
                this.updateWidgetData(cmd);
            });
        }
    }

    protected updateViaCommand(command: ModelServerCommand, semanticUri: string): void {
        const relativeRefURI = new URI(this.getRelativeModelUri(command.owner.$ref.replace("file:", "")));
        if (this.isCurrentModelUri(relativeRefURI)) {
            if (command.dataValues && relativeRefURI.fragment === semanticUri) {
                console.log("incrementalUpdate of '" + semanticUri + "' received: " + command.feature + " " + command.dataValues[0]);

                if (this.jsonFormsWidget instanceof ModelServerJsonFormsPropertyViewWidget) {
                    let newValue: any = command.dataValues[0];
                    // Parse boolean and integer values
                    if (newValue === "true" || newValue === "false") {
                        newValue = newValue === "true";
                    } else if (!isNaN(parseInt(newValue, 10))) {
                        newValue = parseInt(newValue, 10);
                    }
                    this.jsonFormsWidget.updateModelServerWidgetData(command.feature, newValue);
                    this.currentPropertiesCore = this.jsonFormsWidget.currentJsonFormsCore;
                }
            } else if (command.type === "remove") {
                // clear global selection
                this.selectionService.selection = new Object();
            }
        }
    }

    protected isCurrentModelUri(uri: URI): boolean {
        return uri.path.toString() === "/" + this.currentModelUri;
    }

    canHandle(selection: any): number {
        if (isGlspSelection(selection)) {
            return 15;
        }
        return super.canHandle(selection);
    }

    protected handleChanges(jsonFormsData: any): void {
        if (this.jsonFormsWidget instanceof ModelServerJsonFormsPropertyViewWidget) {
            if (jsonFormsData.semanticUri === this.currentPropertiesCore.data.semanticUri && !isEqual(jsonFormsData, this.currentPropertiesCore.data)) {
                const results = this.difference(jsonFormsData, this.currentPropertiesCore.data);
                const editCommand = this.createSetCommand(results as object, jsonFormsData);
                if (editCommand) {
                    this.modelServerClient.edit(this.currentModelUri, editCommand);
                }
            }
            this.currentPropertiesCore = this.jsonFormsWidget.currentJsonFormsCore;
        }
    }

    /* eslint-disable */
    /* @ts-ignore */
    private difference(object, base) {
        /* @ts-ignore */
        function changes(object, base) {
            return transform(object, function (result, value, key) {
                if (!isEqual(value, base[key])) {
                    /* @ts-ignore */
                    result[key] = (isObject(value) && isObject(base[key])) ? changes(value, base[key]) : value;
                }
            });
        }
        return changes(object, base);
    }

    protected getOwnerRef(semanticUri: string): string {
        return `${this.workspaceService.workspace!.uri}/${this.currentModelUri}#${semanticUri}`.replace("file:///", "file:/");
    }

    protected getOwner(jsonFormsData: any): ModelServerReferenceDescription {
        return {
            $ref: this.getOwnerRef(jsonFormsData.semanticUri),
            eClass: jsonFormsData.eClass
        };
    }

    protected createSetCommand(feature: object, jsonFormsData: any): ModelServerCommand | undefined {
        const changableFeatures = Object.keys(jsonFormsData).filter(key => key !== "eClass" && key !== "semanticUri" && key !== "source" && key !== "target");
        const featureName = Object.keys(feature)[0];
        if (featureName && changableFeatures.indexOf(featureName) > -1) {
            return ModelServerCommandUtil.createSetCommand(this.getOwner(jsonFormsData), featureName, [jsonFormsData[featureName]]);
        }
        return undefined;
    }

}
