/********************************************************************************
 * Copyright (c) 2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
import { Action, generateRequestId, RequestAction, ResponseAction } from "@eclipse-glsp/client";

/**
 * Request Ecore Properties
 */

export class RequestSemanticUriAction implements RequestAction<SetSemanticUriAction> {
    static readonly KIND = "requestSemanticUri";
    kind = RequestSemanticUriAction.KIND;
    constructor(
        public readonly graphicElementId: string,
        public readonly requestId: string = generateRequestId()) { }
}

export class SetSemanticUriAction implements ResponseAction {
    static readonly KIND = "setSemanticUri";
    kind = SetSemanticUriAction.KIND;
    constructor(
        public readonly modelUri: string,
        public readonly semanticUri: string,
        public readonly elementEClass: string,
        public readonly responseId: string = "") { }
}

export function isSetSemanticUriAction(action: Action): action is SetSemanticUriAction {
    return action !== undefined && (action.kind === SetSemanticUriAction.KIND)
        && (action as SetSemanticUriAction).modelUri !== undefined
        && (action as SetSemanticUriAction).semanticUri !== undefined
        && (action as SetSemanticUriAction).elementEClass !== undefined;
}
