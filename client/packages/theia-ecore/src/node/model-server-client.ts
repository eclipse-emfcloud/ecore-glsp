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
import { ModelServerMessage, Response } from "@eclipse-emfcloud/modelserver-theia";
import { DefaultModelServerClient } from "@eclipse-emfcloud/modelserver-theia/lib/node";
import { injectable } from "inversify";

import { EcoreModelServerClient } from "../common/ecore-model-server-client";

export namespace EcoreModelServerPaths {
    export const CREATE_ECORE_RESOURCES = "ecore/create";
    export const DELETE_ECORE_RESOURCES = "ecore/delete";
    export const DELETE_ENOTATION_RESOURCE = "enotation/delete";
}

@injectable()
export class EcoreModelServerClientImpl extends DefaultModelServerClient implements EcoreModelServerClient {

    async createEcoreResources(modelName: string, nsUri: string, nsPrefix: string): Promise<Response<ModelServerMessage>> {
        const newModelUri = `${modelName}/model/${modelName}.ecore`;
        return this.restClient.get(`${EcoreModelServerPaths.CREATE_ECORE_RESOURCES}?modeluri=${newModelUri}&nsUri=${nsUri}&nsPrefix=${nsPrefix}`);
    }

    async deleteEcoreResources(modelUri: string): Promise<Response<ModelServerMessage>> {
        return this.restClient.remove(`${EcoreModelServerPaths.DELETE_ECORE_RESOURCES}?modeluri=${modelUri}`);
    }

    async deleteEnotationResource(modelUri: string): Promise<Response<ModelServerMessage>> {
        return this.restClient.remove(`${EcoreModelServerPaths.DELETE_ENOTATION_RESOURCE}?modeluri=${modelUri}`);
    }
}

