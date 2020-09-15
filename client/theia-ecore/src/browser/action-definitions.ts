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
import { Action, generateRequestId, RequestAction, ResponseAction } from "@eclipse-glsp/client";

import { EcoreProperties } from "./ecore-api";

/**
 * Request Ecore Properties
 */

export class RequestEcorePropertiesAction implements RequestAction<SetEcorePropertiesAction> {
    static readonly KIND = "requestEcoreProperties";
    kind = RequestEcorePropertiesAction.KIND;
    constructor(
        public readonly graphicElementId: string,
        public readonly requestId: string = generateRequestId()) { }
}

export class SetEcorePropertiesAction implements ResponseAction {
    static readonly KIND = "setEcoreProperties";
    kind = SetEcorePropertiesAction.KIND;
    constructor(
        public readonly ecoreProperties: EcoreProperties,
        public readonly responseId: string = "") { }
}

export function isSetEcorePropertiesAction(action: Action): action is SetEcorePropertiesAction {
    return action !== undefined && (action.kind === SetEcorePropertiesAction.KIND)
        && (action as SetEcorePropertiesAction).ecoreProperties !== undefined;
}

/**
 * Edit Ecore Properties
 */

export class EditEcorePropertiesOperation implements Action {
    static readonly KIND = "editEcoreProperties";
    kind = EditEcorePropertiesOperation.KIND;
    constructor(
        public readonly properties: EcoreProperties,
        public readonly requestId: string = generateRequestId()) { }
}
