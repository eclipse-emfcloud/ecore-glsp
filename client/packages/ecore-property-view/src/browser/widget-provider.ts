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
import { ModelserverAwareWidgetProvider } from "@eclipse-emfcloud/modelserver-jsonforms-property-view";
import {
    ModelServerCommand,
    ModelServerCommandUtil,
    ModelServerReferenceDescription
} from "@eclipse-emfcloud/modelserver-theia/lib/common";
import { isGlspSelection } from "@eclipse-glsp/theia-integration/lib/browser/diagram";
import URI from "@theia/core/lib/common/uri";
import { injectable, postConstruct } from "inversify";
import { debounce, isEqual, isObject, transform } from "lodash";

@injectable()
export class EcoreGlspPropertyViewWidgetProvider extends ModelserverAwareWidgetProvider {

    @postConstruct()
    init(): void {
        this.propertyDataServices = this.propertyDataServices.concat(this.contributions.getContributions());
        this.currentPropertyData = {};
        this.currentModelUri = "";
        this.jsonFormsWidget.onChange(
            debounce((jsonFormsData: any) => {
                this.handleChanges(jsonFormsData);
            }, 200)
        );

        this.subscriptionService.onIncrementalUpdateListener(incrementalUpdate => {
            this.updateWidgetData(incrementalUpdate.data);
        });
    }

    canHandle(selection: Object | undefined): number {
        return isGlspSelection(selection) ? 100 : 0;
    }

    updateContentWidget(selection: any): void {
        this.getJsonFormsPropertyDataService(selection).then(service => {
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

    protected updateViaCommand(command: ModelServerCommand, semanticUri: string): void {
        const relativeRefURI = new URI(this.getRelativeModelUri(command.owner.$ref.replace("file:", "")));
        if (this.isCurrentModelUri(relativeRefURI)) {
            if (command.dataValues && relativeRefURI.fragment === semanticUri) {
                let newValue: any = command.dataValues[0];
                // Parse boolean and integer values
                if (newValue === "true" || newValue === "false") {
                    newValue = newValue === "true";
                } else if (!isNaN(parseInt(newValue, 10))) {
                    newValue = parseInt(newValue, 10);
                }
                this.currentPropertyData[command.feature] = newValue;
                this.jsonFormsWidget.updatePropertyViewData(this.currentPropertyData);
            } else if (command.type === "remove") {
                // clear global selection
                this.selectionService.selection = new Object();
            }
        }
    }

    protected isCurrentModelUri(uri: URI): boolean {
        return uri.path.toString() === "/" + this.currentModelUri;
    }

    protected handleChanges(jsonFormsData: any): void {
        if (jsonFormsData.semanticUri === this.currentPropertyData.semanticUri && !isEqual(jsonFormsData, this.currentPropertyData)) {
            const results = this.difference(jsonFormsData, this.currentPropertyData);
            const editCommand = this.createSetCommand(results as object, jsonFormsData);
            if (editCommand) {
                this.modelServerClient.edit(this.currentModelUri, editCommand);
            }
        }
        this.currentPropertyData = jsonFormsData;
    }

    private difference(object: any, base: any): unknown {
        function changes(o: any, b: any): unknown {
            return transform(o, function (result, value, key) {
                if (!isEqual(value, b[key])) {
                    /* @ts-ignore */
                    result[key] = (isObject(value) && isObject(b[key])) ? changes(value, b[key]) : value;
                }
            });
        }
        return changes(object, base);
    }

    protected getOwnerRef(semanticUri: string): string {
        return `${this.workspaceService.workspace!.resource}/${this.currentModelUri}#${semanticUri}`.replace("file:///", "file:/");
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
