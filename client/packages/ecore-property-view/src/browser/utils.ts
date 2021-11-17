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
import { ModelServerClient } from "@eclipse-emfcloud/modelserver-theia";

export async function getElementFromModelServer(modelServerClient: ModelServerClient, modelUri: string, elementId: string): Promise<Object> {
    return modelServerClient.getElementById(modelUri, elementId, "json")
        .then(response => {
            const returnObject = response.body as any;
            if (typeof returnObject === "object" && returnObject !== undefined) {
                // add elementId to jsonforms data structure
                returnObject["semanticUri"] = elementId;
                return returnObject;
            }
            // in case error message occurs the return object is a string, we return an empty object
            return {};
        });
}
