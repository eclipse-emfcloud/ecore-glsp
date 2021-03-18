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
import { ecoreTypeSchema } from "@eclipse-emfcloud/ecore-glsp-common/lib/browser/ecore-json-schema";
import { ModelServerClient } from "@eclipse-emfcloud/modelserver-theia/lib/common";
import { GlspSelection, isGlspSelection } from "@eclipse-emfcloud/theia-ecore/lib/browser/selection-forwarder";
import { JsonSchema, JsonSchema7, UISchemaElement } from "@jsonforms/core";
import { JsonFormsPropertiesService } from "@ndoschek/jsonforms-property-view";
import URI from "@theia/core/lib/common/uri";
import { inject, injectable } from "inversify";

@injectable()
export class EcoreGlspPropertiesService implements JsonFormsPropertiesService {

    @inject(ModelServerClient) protected readonly modelServerClient: ModelServerClient;

    protected ecoreUri = "ecore/model/ecore.ecore";
    protected classifierCounter = 0;

    canHandleSelection(selection: any): number {
        if (isGlspSelection(selection)) {
            return 15;
        }
        return 0;
    }

    protected getElementId(selection: GlspSelection): string {
        return selection.selectedElementsIDs[0] || "";
    }

    protected getSelectionData(selection: GlspSelection): any { // WorkflowElementSelectionData {
        if (selection.additionalSelectionData) {
            return selection.additionalSelectionData.selectionDataMap.get(this.getElementId(selection));
        }
    }

    async getProperties(selection: any): Promise<any> {
        if (selection.selectedElementsIDs) {
            const selectionData = this.getSelectionData(selection);
            return this.fetchElement(selectionData.modelUri, selectionData.semanticUri);
        }
        return undefined;
    }

    protected fetchElement(modelUri: string, elementId: string): Promise<string> {
        return this.modelServerClient.getElementById(modelUri, elementId)
            .then(response => {
                const returnObject = response.body as any;
                // add semanticUri to jsonforms data structure
                returnObject["semanticUri"] = elementId;
                return returnObject;
            });
    }

    async getSchema(selection: any, properties?: any): Promise<JsonSchema | undefined> {
        const eClassName = this.getSelectionData(selection).eClass || new URI(properties.eClass).fragment.substring(2);
        // FIXME atm we use a local version of the typeschema, as there exist performance issues with the fetched ecore type schema
        // const ecoreTypeSchema = await this.getTypeSchema();
        if (ecoreTypeSchema.definitions) {
            /* @ts-ignore */
            const elementSchema = ecoreTypeSchema.definitions[eClassName.toLowerCase()];
            return {
                definitions: ecoreTypeSchema.definitions,
                ...elementSchema
            };
        }
        return undefined;
    }

    protected async getTypeSchema(): Promise<JsonSchema7> {
        return this.modelServerClient.getTypeSchema(this.ecoreUri)
            .then(response => {
                const typeSchema = response.body as JsonSchema7;
                // add semanticuri to all definitions
                if (typeSchema.definitions) {
                    Object.entries(typeSchema.definitions).forEach(definition => {
                        if (definition[1].properties) {
                            definition[1].properties["semanticUri"] = { "type": "string" };
                        }
                    });
                }
                return typeSchema;
            });
    }

    async getUiSchema(selection: any, properties?: any): Promise<UISchemaElement | undefined> {
        if (properties && properties.eClass) {
            const eClassName = this.getSelectionData(selection).eClass || new URI(properties.eClass).fragment.substring(2);
            return this.modelServerClient.getUiSchema(eClassName.toLowerCase()).then((response: any) => response.body as UISchemaElement);
        }
        return undefined;
    }

}
