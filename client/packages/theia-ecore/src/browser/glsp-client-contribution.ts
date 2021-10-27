/********************************************************************************
 * Copyright (c) 2019-2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
import { Args } from "@eclipse-glsp/client";
import { BaseGLSPClientContribution } from "@eclipse-glsp/theia-integration/lib/browser";
import { MaybePromise } from "@theia/core";
import { injectable } from "inversify";

import { EcoreLanguage } from "../common/ecore-language";

@injectable()
export class EcoreGLSPClientContribution extends BaseGLSPClientContribution {

    readonly id = EcoreLanguage.contributionId;
    readonly fileExtensions = EcoreLanguage.fileExtensions;

    protected createInitializeOptions(): MaybePromise<Args | undefined> {
        return {
            ["timestamp"]: new Date().toString(),
            ["modelServerURL"]: "http://localhost:8081/api/v1/"
        };
    }

}
