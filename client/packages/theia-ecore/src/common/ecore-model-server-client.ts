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
import { ModelServerClient, ModelServerMessage, Response } from "@eclipse-emfcloud/modelserver-theia";

export const EcoreModelServerClient = Symbol("EcoreModelServerClient");
export interface EcoreModelServerClient extends ModelServerClient {
    createEcoreResources(modelName: string, nsUri: string, nsPrefix: string): Promise<Response<ModelServerMessage>>;
    deleteEcoreResources(modelUri: string): Promise<Response<ModelServerMessage>>;
    deleteEnotationResource(modelUri: string): Promise<Response<ModelServerMessage>>;
}

