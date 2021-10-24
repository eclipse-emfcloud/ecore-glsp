/********************************************************************************
 * Copyright (c) 2021 EclipseSource and others.
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
